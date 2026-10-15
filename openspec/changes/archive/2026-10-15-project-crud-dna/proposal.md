# Proposta: CRUD e Gestão de Projetos Elétricos (DNA do Empreendimento)

## Por que
Permitir que projetistas autenticados criem e gerenciem os seus projetos elétricos no NeoGuide, armazenando os metadados fundamentais do empreendimento (DNA: localização, tensão de fornecimento, flags de etapas ativas do wizard) com geração automática do protocolo anual oficial (`PRJ-AAAA-XXXX`) e isolamento estrito de acesso entre usuários.

## O que muda
- Evolução do modelo JPA `Project`:
  - Associação `@ManyToOne(fetch = FetchType.LAZY)` com `User` (`user_id`).
  - Adição dos campos do DNA do Empreendimento: `tensaoFornecimento`, `possuiComercial`, `possuiVe`, `possuiIncendio`, `etapaAtual`, `cep`, `endereco`, `bairro`, `crea`, `createdAt`, `updatedAt`.
  - Adição do status `RASCUNHO` ao enum `ProjectStatus`.
- Atualização do repositório `ProjectRepository`:
  - `findAllByUserIdOrderByCreatedAtDesc(Long userId)`
  - Query HQL `findLastProtocoloByPrefix` para resgatar o último protocolo anual cadastrado.
- Implementação de DTOs:
  - `CreateProjectRequest`: payload de entrada para criação de projetos.
  - `CreateProjectResponse`: payload de resposta resumida (201 Created).
  - Enriquecimento de `ProjectResponse`: inclusão dos novos campos do DNA.
- Evolução de `Validator`:
  - Regra de validação para tensão de fornecimento ("380_220V" ou "220_127V"), tamanho do título e campos obrigatórios.
- Evolução do serviço `ProjectService`:
  - Geração sequencial e anual de protocolo no padrão `PRJ-AAAA-XXXX`.
  - Criação de projeto vinculada ao usuário autenticado e etapa 1 inicial.
  - Listagem de projetos isolada por usuário logado.
  - Consulta por ID com proteção contra acesso cruzado (HTTP 403 Forbidden).
- Atualização do controller `ProjectController`:
  - Endpoints `/api/v1/projects` e `/projects`.
  - `POST` (201 Created), `GET` (200 OK com lista do usuário), `GET /{id}` (200 OK ou 403 Forbidden).
- Tratamento de exceções:
  - Criação de `ForbiddenException` e mapeamento no `GlobalExceptionHandler` para HTTP 403.
- Atualização do `DataSeeder` para vincular usuários e preencher dados do DNA nos projetos iniciais.
- Suíte completa de testes:
  - Testes unitários de `ProjectServiceTest`.
  - Testes de integração de `ProjectControllerTest` via MockMvc.

## Capacidades Adicionadas
- Geração sequencial de protocolo anual de projeto elétrico.
- Persistência e validação dos parâmetros de DNA do empreendimento.
- Isolamento multi-tenant por usuário projetista na gestão de projetos.
