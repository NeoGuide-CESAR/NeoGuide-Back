# Spec: Catálogo Normativo Residencial (Quadros 35, 36 e 37)

## ADDED Requirements

### Requirement: Endpoints de Catálogo Residencial
A aplicação DEVE disponibilizar sob o prefixo `/api/v1/catalogo` endpoints públicos para consulta dos dados normativos dos Quadros 35, 36 e 37 da DIS-NOR-053.

#### Scenario: Consulta de Faixas de Área Útil (Quadro 35)
- **WHEN** uma requisição GET é feita para `/api/v1/catalogo/faixas-area`
- **THEN** o sistema responde com status 200 OK contendo a lista das faixas de área ordenadas por `minAreaM2` crescente com os atributos `id`, `faixa`, `rotulo`, `minAreaM2`, `maxAreaM2`, `demandaUnitariakVa` e `normaOrigem`.

#### Scenario: Consulta de Fatores de Coincidência (Quadro 36)
- **WHEN** uma requisição GET é feita para `/api/v1/catalogo/fatores-coincidencia-residencial`
- **THEN** o sistema responde com status 200 OK contendo um wrapper com os atributos `norma` ("DIS-NOR-053 Quadro 36"), `regraEspecialStudios` e a lista `tabela` com `totalApartamentos` e `fatorCoincidencia`.

#### Scenario: Consulta de Fatores de Segurança (Quadro 37)
- **WHEN** uma requisição GET é feita para `/api/v1/catalogo/fatores-seguranca-residencial`
- **THEN** o sistema responde com status 200 OK contendo a lista dos fatores de segurança com `minDrKva`, `maxDrKva` e `fatorSegurancaFr`.

### Requirement: Acesso Público Irrestrito
Os endpoints sob `/api/v1/catalogo/**` DEVEM permitir acesso tanto anônimo quanto autenticado sem bloqueios 401 ou 403 pelo Spring Security.

#### Scenario: Requisição anônima
- **WHEN** um cliente não autenticado solicita `/api/v1/catalogo/faixas-area`
- **THEN** a resposta é 200 OK.

### Requirement: Cache em Memória RAM
As consultas normativas do catálogo residencial DEVEM ser cacheadas em memória RAM de modo que requisições repetidas não disparem novas consultas ao banco de dados.

#### Scenario: Resolução via cache
- **WHEN** múltiplos acessos são efetuados para o mesmo endpoint de catálogo
- **THEN** a consulta ao repositório JPA subjacente é executada apenas na primeira chamada.
