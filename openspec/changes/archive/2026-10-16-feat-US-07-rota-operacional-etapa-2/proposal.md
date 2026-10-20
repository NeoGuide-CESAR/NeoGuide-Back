# Proposta: Rota Operacional da Etapa 2 — Tipologias Residenciais (US-07)

## Por que
Na esteira do Wizard de Dimensionamento Elétrico do NeoGuide, a **Etapa 2 (Unidades Residenciais)** é o primeiro estágio de dimensionamento de cargas da edificação multifamiliar. Nela, o projetista cadastra os tipos de apartamentos agrupados por área útil privativa ($m^2$), quantidade de unidades e a condição de *Smart Studios* (microapartamentos compactos).
Faz-se necessária a criação da rota operacional `PUT /api/v1/projects/{id}/etapa-residencial` com persistência idempotente, validação RFC 7807, autorização por projetista, integração com o motor normativo de cálculo residencial (`ResidentialCalculationEngine` — US-06) e atualização do progresso do projeto (`etapaAtual = Math.max(etapaAtual, 2)`).

## O que muda
- Criação da entidade JPA `ResidentialTypology` em `neoguide.project.model` mapeando a tabela `residential_typologies` (padrão ZERO Lombok).
- Criação do repositório `ResidentialTypologyRepository` em `neoguide.project.repository` com consultas e deleção transacional por projeto.
- Criação dos DTOs em `neoguide.project.dto`: `ResidentialTypologyItemRequest`, `ResidentialStageRequest` e `ResidentialStageResponse`.
- Implementação da lógica de negócio no `ProjectService`:
  - Validação rigorosa dos campos via `Validator.java`;
  - Verificação de existência do projeto (404 Not Found via `ResourceNotFoundException`);
  - Verificação de propriedade do projeto (403 Forbidden via `ForbiddenException`);
  - Invocação do `ResidentialCalculationEngine`;
  - Exclusão idempotente prévia (`deleteByProjectId` + `flush`) e gravação atômica das tipologias com demandas unitárias e totais calculadas;
  - Atualização do estágio do projeto `etapaAtual = Math.max(project.getEtapaAtual(), 2)` e `updatedAt = LocalDateTime.now()`.
- Exposição do endpoint no `ProjectController`: `PUT /api/v1/projects/{id}/etapa-residencial` (e `/projects/{id}/etapa-residencial`).
- Implementação de testes automatizados com cobertura completa em `ProjectServiceTest` e `ProjectControllerTest` (200 OK, idempotência, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found).

## Capacidades Adicionadas
- Persistência idempotente e atômica da Etapa 2 do Wizard.
- Integração ponta-a-ponta entre o controlador REST e o motor de cálculo residencial normativo (DIS-NOR-053).
- Avanço consistente do progresso da esteira do projeto elétrico.
