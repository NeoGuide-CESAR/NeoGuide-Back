# config Specification

## Purpose
TBD - created by archiving change springboot-hibernate-setup. Update Purpose after archive.
## Requirements
### Requirement: PostgreSQL Datasource Configuration
The application MUST configure connectivity with PostgreSQL 16 database using Spring Boot datasource properties, supporting environment variable injection with functional local development fallbacks.

#### Scenario: Fallback para desenvolvimento local
- GIVEN nenhuma variável de ambiente de datasource informada
- WHEN o contexto da aplicação for carregado
- THEN a URL deve ser `jdbc:postgresql://localhost:5432/neoguide_db`
- AND o username deve ser `neoguide_user`
- AND a senha deve ser `neoguide_password`
- AND o driver class deve ser `org.postgresql.Driver`

#### Scenario: Sobrescrita via variáveis de ambiente
- GIVEN a variável de ambiente `SPRING_DATASOURCE_URL` definida como `jdbc:postgresql://postgres-host:5432/custom_db`
- WHEN a propriedade `spring.datasource.url` for avaliada
- THEN o valor retornado deve ser `jdbc:postgresql://postgres-host:5432/custom_db`

### Requirement: Hibernate DDL Auto and Dialect
The application MUST use Hibernate ORM configured with PostgreSQL official dialect and `ddl-auto: update` strategy by default, overridable by environment variables.

#### Scenario: DDL Auto e Dialeto Padrão
- GIVEN a inicialização do contexto JPA
- WHEN as propriedades do Hibernate forem lidas
- THEN a estratégia `spring.jpa.hibernate.ddl-auto` deve ser `update`
- AND o dialeto deve ser `org.hibernate.dialect.PostgreSQLDialect`

### Requirement: Boilerplate Prohibition
The codebase MUST NOT use boilerplate generator libraries such as Lombok, relying exclusively on standard Java 21 features.

#### Scenario: Validação de ausência de Lombok
- GIVEN o arquivo `pom.xml` e os arquivos de código-fonte em `src/`
- WHEN inspecionados por dependências e anotações
- THEN nenhuma referência a `org.projectlombok` ou anotações Lombok deve ser encontrada
- AND todo DTO deve utilizar records nativos do Java 21 ou POJOs com construtores e acessores explícitos.

