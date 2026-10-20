# Proposta: Motor de Cálculo Residencial (Drf — DIS-NOR-053)

## Por que
Implementar o motor de cálculo normativo puro e determinístico da Demanda Residencial Final ($D_{rf}$) no backend NeoGuide, fundamentado no Anexo I da norma **DIS-NOR-053 (Rev. 06)** da Neoenergia Pernambuco.
Este motor é o núcleo matemático e regulatório da Etapa 2 do Wizard e do simulador em tempo real (Live Demand HUD), garantindo alta performance (< 1ms, zero I/O) e precisão decimal absoluta através de `BigDecimal`.

## O que muda
- Criação dos modelos imutáveis em Java 21 (`record`) sob padrão ZERO Lombok:
  - `ResidentialTypologyItem`: entrada de tipologia (nome, área em m², quantidade, $D_i$ opcional e flag de Smart Studio).
  - `ResidentialCalculationInput`: payload agrupador (lista de tipologias e flag geral `isSmartStudio`).
  - `ResidentialTypologyOutput`: item de cálculo individualizado com subtotal.
  - `ResidentialCalculationResult`: resultado analítico completo ($N$, $D_r$, $F_c$, $D_{tr}$, $F_r$, $D_{rf}$, lista discriminada de tipologias).
- Criação do serviço puro e stateless `ResidentialCalculationEngine` (`@Component` Spring):
  - Resolução automática de demanda unitária $D_i$ via Quadro 35 por faixa de área útil privativa ($m^2$);
  - Apuração da Demanda Bruta Acumulada $D_r = \sum (D_{i,j} \times N_j)$;
  - Resolução do Fator de Coincidência $F_c$ via Quadro 36 com interpolação linear determinística e aplicação do Item 6.25 para Smart Studios ($F_c = 0.9000$ se $N > 15$);
  - Cálculo da Demanda Provisória Coincidente $D_{tr} = D_r \times F_c$;
  - Resolução do Fator de Segurança $F_r$ via Quadro 37 conforme os patamares de $D_r$;
  - Apuração da Demanda Residencial Final $D_{rf} = D_{tr} \times F_r$;
  - Validações defensivas para dados inválidos (área <= 0, quantidade <= 0, lista de tipologias nula/vazia).
- Suíte exaustiva de testes unitários em JUnit 5 validando o memorial canônico de 100 unidades, casos de Smart Studio, interpolação de $F_c$, fronteiras de $F_r$ e inferência do Quadro 35.

## Capacidades Adicionadas
- Cálculo determinístico e normativo de demanda residencial segundo a DIS-NOR-053 (Rev. 06).
- Resolução e inferência automática de $D_i$ a partir da área privativa do imóvel.
- Interpolação matemática e trava regulatória de simultaneidade para Smart Studios.
