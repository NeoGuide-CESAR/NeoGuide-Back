# Technical Design: Setup Spring Boot 3, Dependências e Hibernate DDL

## Architecture & Context
A aplicação backend NeoGuide utiliza Spring Boot 3 no Java 21 LTS. A persistência baseia-se em Spring Data JPA com Hibernate ORM, conectado a um banco relacional PostgreSQL 16.

Conforme a arquitetura consolidada do projeto:
- Não são utilizadas ferramentas de migração manual de schema (como Flyway ou Liquibase); a sincronização DDL é delegada ao Hibernate via `spring.jpa.hibernate.ddl-auto: update`.
- A parametrização de conexão utiliza a convenção de variáveis de ambiente do Spring Boot (`SPRING_DATASOURCE_*` e `SPRING_JPA_HIBERNATE_DDL_AUTO`), permitindo injeção transparente via Docker Compose, Kubernetes ou CI/CD, com valores padrão convenientes para desenvolvimento na máquina local (`localhost:5432`).
- As classes do domínio e transferência são escritas em Java 21 idiomático: records nativos e construtores/métodos explícitos. Não é admitido Lombok.

## Technical Decisions
1. **Datasource Parametrizado com Fallbacks**:
   - `spring.datasource.url`: `${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/neoguide_db}`
   - `spring.datasource.username`: `${SPRING_DATASOURCE_USERNAME:neoguide_user}`
   - `spring.datasource.password`: `${SPRING_DATASOURCE_PASSWORD:neoguide_password}`
   - `spring.datasource.driver-class-name`: `org.postgresql.Driver`
2. **Hibernate e Dialeto**:
   - `spring.jpa.hibernate.ddl-auto`: `${SPRING_JPA_HIBERNATE_DDL_AUTO:update}`
   - `spring.jpa.properties.hibernate.dialect`: `org.hibernate.dialect.PostgreSQLDialect`
3. **Dependências no `pom.xml`**:
   - Adicionado `org.springframework.boot:spring-boot-starter-validation`.
   - Garantido `org.postgresql:postgresql` no escopo runtime.
   - Nenhuma referência ou annotation processor do Lombok configurado.
4. **Estratégia de Testes Automatizados**:
   - Teste unitário/integrado de propriedades (`ApplicationConfigurationPropertiesTest`) validando o binding das configurações do `application.yaml` e os valores padrão de fallback PostgreSQL.
   - Perfil de teste (`test`) com banco em memória H2 (`src/test/resources/application-test.yaml`) para testes de integração rápidos e sem dependência obrigatória do container PostgreSQL estar em execução durante o build isolado do Maven (`mvn test`).

## Risks & Mitigations
- **Risco**: Falha na compilação por anotações ausentes de Lombok.
  - **Mitigação**: O código existente já se encontra limpo e sem dependência de anotações Lombok; novos componentes seguirão estritamente records e POJOs explícitos.
- **Risco**: Falha em testes de integração caso a porta 5432 local não esteja acessível.
  - **Mitigação**: Separação clara entre a configuração padrão de produção/desenvolvimento e o profile de testes automatizados com H2.
