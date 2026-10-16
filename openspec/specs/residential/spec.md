# residential

Mecanismo matemático e normativo de apuração da Demanda Residencial Final ($D_{rf}$) baseado na norma DIS-NOR-053 (Rev. 06) da Neoenergia Pernambuco.

## Requirements

### Requirement: Cálculo Determinístico da Demanda Residencial Final
O sistema DEVE fornecer um motor de cálculo em memória (`ResidentialCalculationEngine`) que processe uma lista de tipologias residenciais e apure a demanda residencial final ($D_{rf}$) e as memórias intermediárias ($D_r$, $F_c$, $D_{tr}$, $F_r$).

#### Scenario: Memorial canônico de 100 apartamentos
- **GIVEN** uma lista contendo 50 apartamentos de 63 m² ($D_i = 1,47$ kVA) e 50 apartamentos de 68 m² ($D_i = 1,57$ kVA)
- **AND** `isSmartStudio` é falso
- **WHEN** o motor executa o cálculo de demanda residencial
- **THEN** a demanda bruta acumulada $D_r$ é $152.00$ kVA
- **AND** o fator de coincidência $F_c$ é $0.6823$
- **AND** a demanda coincidente provisória $D_{tr}$ é $103.71$ kVA
- **AND** o fator de segurança $F_r$ é $1.10$
- **AND** a demanda residencial final $D_{rf}$ é $114.08$ kVA

### Requirement: Regra Especial de Smart Studios (Item 6.25 DIS-NOR-053)
Para empreendimentos do tipo Smart Studio, o fator de coincidência deve ser protegido contra subdimensionamento quando o total de unidades for superior a 15.

#### Scenario: Smart Studios com mais de 15 unidades
- **GIVEN** um empreendimento com 20 apartamentos de 35 m²
- **AND** `isSmartStudio` é verdadeiro
- **WHEN** o motor executa o cálculo de demanda residencial
- **THEN** o fator de coincidência $F_c$ é fixado em $0.9000$ (90%)

#### Scenario: Smart Studios com até 15 unidades
- **GIVEN** um empreendimento com 10 apartamentos de 35 m²
- **AND** `isSmartStudio` é verdadeiro
- **WHEN** o motor executa o cálculo de demanda residencial
- **THEN** o fator de coincidência $F_c$ segue a curva padrão ($0.9640$)

### Requirement: Escalonamento do Fator de Segurança Fr (Quadro 37)
O motor DEVE aplicar o fator de segurança $F_r$ conforme a faixa estrita de $D_r$.

#### Scenario: Patamares limítrofes de Dr
- **WHEN** $D_r \le 25.00$ kVA **THEN** $F_r$ é $1.50$
- **WHEN** $25.00 < D_r \le 50.00$ kVA **THEN** $F_r$ é $1.30$
- **WHEN** $50.00 < D_r \le 100.00$ kVA **THEN** $F_r$ é $1.20$
- **WHEN** $D_r > 100.00$ kVA **THEN** $F_r$ é $1.10$

### Requirement: Resolução Automática de Demanda Unitária (Quadro 35)
Quando o valor de $D_i$ não for explicitamente fornecido em uma tipologia, o motor DEVE determiná-lo a partir da área útil privativa segundo o Quadro 35.

#### Scenario: Inferência por área útil
- **WHEN** área privativa for 45 m² **THEN** $D_i$ inferido é $1.28$ kVA
- **WHEN** área privativa for 63 m² **THEN** $D_i$ inferido é $1.47$ kVA
- **WHEN** área privativa for 110 m² **THEN** $D_i$ inferido é $2.45$ kVA
- **WHEN** área privativa for 350 m² **THEN** $D_i$ inferido é $7.20$ kVA

### Requirement: Validação de Entrada Defensiva
O motor DEVE rejeitar dados inconsistentes com mensagens informativas.

#### Scenario: Entrada inválida
- **WHEN** a lista de tipologias for nula ou vazia, ou contiver área $\le 0$ ou quantidade $\le 0$
- **THEN** o motor lança `IllegalArgumentException`
