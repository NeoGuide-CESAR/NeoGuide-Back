# Implementation Tasks: Setup Spring Boot 3, Dependências e Hibernate DDL

- [x] Atualizar `pom.xml` assegurando presença de `spring-boot-starter-validation`, driver `postgresql` e ausência de Lombok
- [x] Atualizar `src/main/resources/application.yaml` com as propriedades de datasource PostgreSQL e dialeto Hibernate parametrizados por variáveis de ambiente
- [x] Criar perfil de testes e recursos em `src/test/resources/application-test.yaml` para permitir execução de testes isolados
- [x] Criar testes automatizados para validar que as propriedades de configuração do PostgreSQL e Hibernate são carregadas corretamente
- [x] Executar `./mvnw clean test-compile` e `./mvnw test` garantindo 100% de sucesso
- [x] Sincronizar delta spec em `openspec/specs/config/spec.md`
- [x] Arquivar change em `openspec/changes/archive/`
