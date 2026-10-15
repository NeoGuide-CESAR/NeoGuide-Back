## Why

Para permitir que a equipe acadêmica execute localmente todo o ambiente do NeoGuide (banco de dados PostgreSQL e API Spring Boot) de forma padronizada e reprodutível, sem necessidade de instalações ou configurações manuais complexas no sistema operacional hospedeiro, é essencial orquestrar os containers via Docker Compose com volume persistente e rede isolada.

## What Changes

- Adição do `Dockerfile` multi-estágio (Maven 3.9 + Java 21 Temurin e JRE 21 Alpine) para build e empacotamento otimizado da API Spring Boot.
- Adição do `docker-compose.yml` na raiz contendo os serviços `neoguide-db` (PostgreSQL 16) e `neoguide-api` (API Spring Boot).
- Configuração de volume persistente `postgres_data` para assegurar retenção dos dados entre reinicializações dos containers.
- Configuração de healthcheck com `pg_isready` no banco de dados e dependência condicional `service_healthy` na API.
- Configuração da rede interna em modo bridge `neoguide-net`.

## Capabilities

### New Capabilities
- `infra`: Especificação de orquestração de containers, isolamento de rede, persistência de volume e empacotamento multi-estágio da aplicação NeoGuide.

### Modified Capabilities

## Impact

- Novos arquivos na raiz: `Dockerfile` e `docker-compose.yml`.
- Requer Docker e Docker Compose instalados no host de desenvolvimento.
- Exposição das portas 5432 (Postgres) e 8080 (API).
