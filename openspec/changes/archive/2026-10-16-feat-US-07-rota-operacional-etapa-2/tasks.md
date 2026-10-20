# Tarefas de Implementação: Rota Operacional da Etapa 2 (US-07)

- [x] 1. Modelagem de Dados e DTOs (Zero Lombok) <!-- id: 1 -->
  - [x] 1.1 Criar entidade JPA `ResidentialTypology` em `neoguide.project.model`
  - [x] 1.2 Criar repositório `ResidentialTypologyRepository` em `neoguide.project.repository`
  - [x] 1.3 Criar DTOs `ResidentialTypologyItemRequest`, `ResidentialStageRequest` e `ResidentialStageResponse` em `neoguide.project.dto`
- [x] 2. Lógica de Serviço e Validação <!-- id: 2 -->
  - [x] 2.1 Adicionar validação de payload com `Validator.java`
  - [x] 2.2 Implementar método `saveResidentialStage` em `ProjectService` com autorização, idempotência e avanço de etapa
- [x] 3. Controlador REST <!-- id: 3 -->
  - [x] 3.1 Adicionar rota `PUT /{id}/etapa-residencial` em `ProjectController`
- [x] 4. Testes Automatizados <!-- id: 4 -->
  - [x] 4.1 Adicionar testes unitários em `ProjectServiceTest`
  - [x] 4.2 Adicionar testes de integração MockMvc em `ProjectControllerTest`
  - [x] 4.3 Executar `./mvnw.cmd test` e garantir 100% de aprovação
- [x] 5. Sincronização e Arquivamento OpenSpec <!-- id: 5 -->
  - [x] 5.1 Marcar todas as tarefas como concluídas
  - [x] 5.2 Sincronizar delta spec para `openspec/specs/residential-stage/spec.md`
  - [x] 5.3 Mover change para pasta de arquivamento `openspec/changes/archive/2026-10-16-feat-US-07-rota-operacional-etapa-2/`
