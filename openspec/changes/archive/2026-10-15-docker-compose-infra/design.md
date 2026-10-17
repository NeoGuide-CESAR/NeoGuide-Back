## Context

O backend do NeoGuide é desenvolvido em Spring Boot 3 com Java 21 e utiliza PostgreSQL 16 para persistência relacional. Para viabilizar testes integrados e execução local consistente por diferentes desenvolvedores da equipe sem depender de serviços instalados no host, a infraestrutura deve ser conteinerizada com Docker Compose.

## Goals / Non-Goals

**Goals:**
- Prover ambiente dockerizado com dois serviços: `neoguide-db` (Postgres 16) e `neoguide-api` (Spring Boot).
- Configurar healthcheck robusto com `pg_isready` no banco para garantir que a API só inicialize com o banco pronto para receber conexões.
- Garantir persistência de dados em volume nomeado `postgres_data`.
- Criar imagem da API via Dockerfile multi-estágio para otimizar tempo de build e tamanho final da imagem.

**Non-Goals:**
- Ferramentas de migração manual (Flyway ou Liquibase), mantendo Hibernate `ddl-auto: update`.
- Infraestrutura de produção ou orquestração em nuvem (K8s, ECS, etc.).
- Configuração de pipelines de CI/CD.

## Decisions

- **PostgreSQL 16 Alpine:** Imagem leve, segura e rápida para download e execução local.
- **Dockerfile Multi-Stage:** Estágio 1 com `maven:3.9.6-eclipse-temurin-21-alpine` para build e resolução de dependências, e Estágio 2 com `eclipse-temurin:21-jre-alpine` executando o JAR gerado, minimizando o footprint da imagem final sem ferramentas desnecessárias em tempo de execução.
- **Rede Bridge `neoguide-net`:** Comunicação interna direta por DNS de serviço (`neoguide-db:5432`), isolando o tráfego dos containers.
- **Healthcheck e `depends_on` condicional:** Uso de `service_healthy` para evitar falha de inicialização da API por tentativas prematuras de conexão ao Postgres antes do daemon estar pronto.

## Risks / Trade-offs

- **[Conflito de porta 5432]** → Desenvolvedores com Postgres local rodando no host podem encontrar erro de bind. Documentar mapeamento alternativo ou instrução para pausar serviço local (`Stop-Service postgresql*`).
- **[Tempo de build inicial Maven]** → O primeiro build no Dockerfile precisará baixar dependências. Mitigado usando cache de camada Docker com `COPY pom.xml .` e `RUN mvn dependency:go-offline -B` antes de copiar o código-fonte.
