# Delta Spec: Paginação, Filtros e Duplicação de Projetos

## Modificações em Endpoints REST

### 1. `GET /api/v1/projects` (e alias `/projects`)
- Suporte a paginação com parâmetros:
  - `page`: índice da página (default 0).
  - `size`: tamanho da página (default 10).
  - `sort`: campo e direção de ordenação (default `createdAt,DESC`).
- Parâmetros opcionais de filtro:
  - `status`: filtro por enum `ProjectStatus` (`RASCUNHO`, `EM_ANALISE`, `APROVADO`, `REPROVADO`).
  - `search`: termo de busca insensível a maiúsculas aplicado a `titulo`, `protocolo` e `municipio`.
- Retorno: HTTP 200 OK encapsulado no formato `Page<ProjectResponse>`.

### 2. `POST /api/v1/projects/{id}/duplicate` (e alias `/projects/{id}/duplicate`)
- Autenticação obrigatória com Bearer Token.
- Validação de propriedade: apenas o dono do projeto pode duplicá-lo (retorna HTTP 403 Forbidden para outros usuários).
- Clonação com novas propriedades:
  - Título com sufixo `" (cópia)"`.
  - Status resetado para `RASCUNHO`.
  - Etapa atual resetada para `1`.
  - Novo número de protocolo sequencial anual oficial (`PRJ-AAAA-XXXX`).
  - Novas datas de criação e atualização.
- Retorno: HTTP 201 Created com DTO `ProjectResponse` do projeto clonado.
