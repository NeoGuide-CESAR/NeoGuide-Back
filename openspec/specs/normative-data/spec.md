# normative-data Specification

## Purpose
TBD - created by archiving change normative-tables-dataseeder. Update Purpose after archive.
## Requirements
### Requirement: Normative Tables Schema
The system MUST provide JPA relational entities representing the 21 official tables of DIS-NOR-053 and DIS-NOR-030 standards from Neoenergia Pernambuco, without using Lombok.

#### Scenario: Entidades mapeadas
- **WHEN** o schema JPA é gerado
- **THEN** as tabelas `catalogo_faixas_area`, `catalogo_fatores_coincidencia`, `catalogo_fatores_seguranca`, `catalogo_fatores_ve`, `catalogo_dimensionamento_entrada`, `catalogo_barramentos_qdg`, `catalogo_tomadas_minimas`, `catalogo_fatores_iluminacao_res`, `catalogo_fatores_aquecimento_passagem`, `catalogo_fatores_boilers`, `catalogo_fatores_eletrodomesticos`, `catalogo_fatores_cooktops`, `catalogo_aparelhos_ar_condicionado`, `catalogo_fatores_ar_condicionado`, `catalogo_fatores_motores`, `catalogo_fatores_especiais`, `catalogo_fatores_piscina`, `catalogo_dispositivos_partida`, `catalogo_motores_monofasicos`, `catalogo_motores` e `catalogo_cargas_ocupacao` devem ser criadas com tipos de dados compatíveis com os cálculos normativos.

### Requirement: Automatic Idempotent Data Seeder
The application MUST seed all official regulatory normative data during application startup via `DataSeeder.java` in an idempotent manner.

#### Scenario: Primeira inicialização
- **WHEN** a aplicação é iniciada pela primeira vez com base limpa
- **THEN** todas as tabelas normativas devem conter os registros regulatórios correspondentes.

#### Scenario: Reinicialização subsequente
- **WHEN** a aplicação é reiniciada ou o seeder é executado com registros já existentes
- **THEN** nenhum registro duplicado deve ser inserido e a contagem de registros deve permanecer idêntica.

