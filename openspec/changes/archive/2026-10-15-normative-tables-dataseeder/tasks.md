# Tasks: normative-tables-dataseeder

- [x] Criar estrutura inicial da change OpenSpec
- [x] Modelar entidades JPA normativas em `neoguide.project.model.normativo` (21 classes POJO sem Lombok)
- [x] Criar repositories Spring Data JPA em `neoguide.project.repository.normativo` (21 interfaces)
- [x] Implementar seeders temáticos e integrá-los a `DataSeeder.java` com garantia de idempotência
- [x] Implementar teste automatizado `NormativeDataSeederTest.java`
- [x] Validar compilação e 100% de testes passando via `.\mvnw.cmd test`
- [x] Sincronizar e arquivar change OpenSpec
- [x] Gerar commit semântico `feat(US-03): populate normative tables via dataseeder`
