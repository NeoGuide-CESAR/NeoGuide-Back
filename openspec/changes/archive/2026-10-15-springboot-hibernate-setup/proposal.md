# Proposal: Setup Spring Boot 3, Dependências e Hibernate DDL

## Why
A aplicação backend NeoGuide necessita de uma camada de persistência robusta integrada ao PostgreSQL 16 provisionado via container Docker. O setup do Spring Boot 3 deve assegurar a injeção flexível de credenciais de banco via variáveis de ambiente (`SPRING_DATASOURCE_*`), sincronização de esquemas relacionais via Hibernate DDL automático (`ddl-auto: update`), e conformidade estrita com as diretrizes do projeto: proibição expressa de bibliotecas de geração de boilerplate (como Lombok), priorizando recursos modernos do Java 21 (records e POJOs explícitos).

## What Changes
- Atualização do `src/main/resources/application.yaml` para configurar o datasource PostgreSQL com suporte a variáveis de ambiente e fallback local (`localhost:5432/neoguide_db`).
- Configuração do Hibernate com dialeto `org.hibernate.dialect.PostgreSQLDialect` e estratégia `ddl-auto: update`.
- Higienização e validação do `pom.xml`, assegurando driver `postgresql`, Spring Data JPA, `spring-boot-starter-validation` e ausência total de Lombok.
- Configuração de perfil/ambiente de testes garantindo execução automatizada limpa e independente sem falhas de conexão de banco.
- Testes automatizados verificando o carregamento das propriedades de configuração e do contexto da aplicação.

## Capabilities

### New Capabilities
- `config`: Parametrização dinâmica e segura do datasource PostgreSQL e Hibernate via variáveis de ambiente com fallbacks locais.
- `validation`: Disponibilidade do starter de validação do Spring Boot no classpath sem geradores de código.

### Modified Capabilities
- `persistence`: Migração da configuração padrão de banco em memória temporário para PostgreSQL oficial com DDL gerenciado.

## Impact
- Aplicação pronta para rodar conectada ao container `neoguide-db` (US-01).
- Total aderência às boas práticas de código Java 21 sem dependências mágicas ou processadores de anotação de boilerplate.
