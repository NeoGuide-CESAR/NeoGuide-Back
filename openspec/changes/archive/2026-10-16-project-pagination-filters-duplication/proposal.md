# Proposta: Paginação, Filtros de Busca e Duplicação de Projetos

## Por quê
O NeoGuide necessita evoluir a listagem e gestão de projetos no painel do projetista para oferecer:
1. Navegação eficiente e escalável com paginação (`Pageable`) e ordenação padrão por data de criação (`createdAt,desc`).
2. Filtros combinados por `status` (enum `ProjectStatus`) e busca textual (`search`) cobrindo `titulo`, `protocolo` e `municipio`.
3. Clonagem ágil com 1-clique (`POST /api/v1/projects/{id}/duplicate`) que replica os parâmetros técnicos (tensão, endereço, flags de etapas) gerando um novo protocolo sequencial e resetando status para rascunho.

## O que muda
- `ProjectRepository`: adição da consulta JPQL paginada `findByUserWithFilters`.
- `ProjectService`: implementação de `listByUserWithFilters` e `duplicateProject`.
- `ProjectController`: evolução do endpoint `GET /api/v1/projects` para paginação com filtros e novo endpoint `POST /api/v1/projects/{id}/duplicate`.
- Suíte de testes unitários e de integração mockMvc cobrindo todos os cenários.
