# Design: Motor de Cálculo Residencial (Drf — DIS-NOR-053)

## Arquitetura e Decisões Técnicas

### 1. Arquitetura Stateless e Zero I/O
O motor `ResidentialCalculationEngine` é projetado como um componente Spring (`@Component`) totalmente stateless e desacoplado de banco de dados, interfaces de rede ou transações JPA. Isso assegura latência sub-milissegundo para alimentar simulações em tempo real no frontend (Live Demand HUD).

### 2. Modelos de Domínio Imutáveis (Zero Lombok / Java 21 Records)
Em conformidade com a convenção do projeto NeoGuide de **ZERO Lombok**:
- `ResidentialTypologyItem`: Representa uma linha de tipologia informada. Fornece construtores flexíveis (com ou sem `demandaUnitariaDi`, com ou sem `isSmartStudio`).
- `ResidentialCalculationInput`: Payload de entrada que agrega a lista de tipologias e uma flag global `isSmartStudio`.
- `ResidentialTypologyOutput`: Representa a tipologia calculada contendo o $D_i$ efetivamente aplicado e a demanda subtotal ($D_{i,j} \times N_j$).
- `ResidentialCalculationResult`: Resultado consolidado com $N_{\text{total}}$, $D_r$, $F_c$, $D_{tr}$, $F_r$, $D_{rf}$, a flag `isSmartStudio` e a listagem analítica `List<ResidentialTypologyOutput>`. Disponibiliza tanto os accessors canônicos de record quanto getters de conveniência.

### 3. Aritmética e Escala Decimal
- Todas as operações utilizam `BigDecimal` para afastar qualquer imprecisão de ponto flutuante binário (`double`/`float`).
- Escala de potências e demandas acumuladas ($D_r, D_{tr}, D_{rf}$, subtotais): 2 casas decimais com `RoundingMode.HALF_UP`.
- Escala de fatores de simultaneidade ($F_c$): 4 casas decimais com `RoundingMode.HALF_UP`.
- Fator de segurança ($F_r$): 2 casas decimais.

### 4. Mapeamento Normativo — DIS-NOR-053 (Rev. 06)

#### Quadro 35 — Demanda Unitária por Área Útil ($D_i$)
Faixas estáticas de área privativa $A$ ($m^2$):
- $A \le 40.00 \implies 1.20\text{ kVA}$
- $40.00 < A \le 45.00 \implies 1.28\text{ kVA}$
- $45.00 < A \le 50.00 \implies 1.35\text{ kVA}$
- $50.00 < A \le 55.00 \implies 1.40\text{ kVA}$
- $55.00 < A \le 60.00 \implies 1.43\text{ kVA}$
- $60.00 < A \le 65.00 \implies 1.47\text{ kVA}$ *(memorial: 63 m²)*
- $65.00 < A \le 70.00 \implies 1.57\text{ kVA}$ *(memorial: 68 m²)*
- $70.00 < A \le 80.00 \implies 1.80\text{ kVA}$
- $80.00 < A \le 90.00 \implies 1.95\text{ kVA}$
- $90.00 < A \le 100.00 \implies 2.15\text{ kVA}$
- $100.00 < A \le 120.00 \implies 2.45\text{ kVA}$
- $120.00 < A \le 140.00 \implies 2.80\text{ kVA}$
- $140.00 < A \le 170.00 \implies 3.30\text{ kVA}$
- $170.00 < A \le 200.00 \implies 3.90\text{ kVA}$
- $200.00 < A \le 300.00 \implies 5.40\text{ kVA}$
- $A > 300.00 \implies 7.20\text{ kVA}$

Caso a tipologia traga um $D_i$ previamente informado (ex.: selecionado no catálogo), este valor é mantido; caso contrário, é inferido pela tabela de faixas.

#### Quadro 36 — Fator de Coincidência ($F_c$)
- Padrão tabelado:
  - $N = 10 \implies 0.9640$
  - $N = 15 \implies 0.9027$
  - $N = 20 \implies 0.8560$
  - $N = 25 \implies 0.8184$
  - $N = 30 \implies 0.7827$
  - $N = 35 \implies 0.7580$
  - $N = 40 \implies 0.7380$
  - $N = 45 \implies 0.7231$
  - $N = 50 \implies 0.7080$
  - $N = 55 \implies 0.6935$
  - $N \ge 60 \implies 0.6823$
- Interpolação Linear:
  - Para $1 \le N < 10$: interpolado linearmente entre $(1, 1.0000)$ e $(10, 0.9640)$.
  - Para valores intermediários entre os limites tabelados: interpolação linear entre os nós adjacentes com arredondamento `HALF_UP` a 4 casas decimais.
- **Regra do Item 6.25 (Smart Studios):**
  Se a flag `isSmartStudio == true` e $N > 15$, fixa-se $F_c = 0.9000$. Se $N \le 15$, segue a curva normal.

#### Quadro 37 — Fator de Segurança ($F_r$)
Patamares em função da demanda bruta $D_r$:
- $D_r \le 25.00\text{ kVA} \implies F_r = 1.50$
- $25.00 < D_r \le 50.00\text{ kVA} \implies F_r = 1.30$
- $50.00 < D_r \le 100.00\text{ kVA} \implies F_r = 1.20$
- $D_r > 100.00\text{ kVA} \implies F_r = 1.10$

### 5. Fórmulas Normativas de Consolidação
1. Subtotal Tipologia $j$: $\text{subtotal}_j = \text{round}(D_{i,j} \times N_j, 2)$
2. Demanda Bruta $D_r$: $D_r = \sum_{j=1}^M \text{subtotal}_j$
3. Demanda Provisória Coincidente $D_{tr}$: $D_{tr} = \text{round}(D_r \times F_c, 2)$
4. Demanda Residencial Final $D_{rf}$: $D_{rf} = \text{round}(D_{tr} \times F_r, 2)$
