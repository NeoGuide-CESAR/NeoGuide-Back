## ADDED Requirements

### Requirement: Orquestração de Containers com Docker Compose
O sistema SHALL disponibilizar manifest `docker-compose.yml` orquestrando os serviços de banco de dados (`neoguide-db`) e aplicação (`neoguide-api`) em uma rede dedicada `neoguide-net`.

#### Scenario: Execução dos serviços
- **WHEN** o comando `docker compose up -d` for executado na raiz do repositório
- **THEN** o container do banco de dados `neoguide-db` e o container da API `neoguide-api` devem iniciar e comunicar-se via rede `neoguide-net`.

### Requirement: Persistência de Dados e Healthcheck do Banco
O serviço `neoguide-db` SHALL utilizar a imagem `postgres:16-alpine`, volume persistente `postgres_data`, credenciais padronizadas e verificação de saúde com `pg_isready`.

#### Scenario: Validação de saúde do banco de dados
- **WHEN** o container `neoguide-db` é iniciado
- **THEN** o comando `pg_isready -U neoguide_user -d neoguide_db` deve retornar status saudável e a API `neoguide-api` só deve inicializar após esta condição ser atendida.

#### Scenario: Persistência de dados
- **WHEN** os containers forem parados via `docker compose down` e reiniciados via `docker compose up -d`
- **THEN** todos os dados gravados no banco de dados devem permanecer intactos através do volume `postgres_data`.

### Requirement: Empacotamento Otimizado Multi-Estágio da API
O sistema SHALL prover um `Dockerfile` multi-estágio separando compilação (Maven 3.9 + Temurin 21) e execução (JRE 21 Alpine).

#### Scenario: Build do container da aplicação
- **WHEN** o processo de build do Docker for executado via `docker compose build` ou `docker build`
- **THEN** o artefato compilado deve ser empacotado em uma imagem JRE 21 alpine expondo a porta 8080.
