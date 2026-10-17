# Especificação: Gestão de Projetos Elétricos e Protocolo

## Finalidade
Permitir o gerenciamento do ciclo de vida de projetos elétricos no NeoGuide, armazenando os metadados fundamentais do empreendimento (DNA: localização, tensão de fornecimento, flags de etapas ativas do wizard) com geração automática do protocolo anual oficial (`PRJ-AAAA-XXXX`), navegação paginada com filtros combinados de busca, duplicação rápida com 1-clique e isolamento estrito de acesso entre projetistas.

## Modelo de Dados

### Entidade `Project`
Tabela: `projects`
- `id`: `BIGINT`, chave primária autoincremento.
- `user_id`: `BIGINT`, chave estrangeira referenciando `users(id)`, não nula (`@ManyToOne(fetch = FetchType.LAZY)`).
- `protocolo`: `VARCHAR(30)`, único, não nulo (formato `PRJ-AAAA-XXXX`).
- `titulo`: `VARCHAR(150)`, não nulo (tamanho de 3 a 150 caracteres).
- `descricao`: `VARCHAR(500)`.
- `tipo`: `VARCHAR(30)`, enum `ProjectType` (padrão `LIGACAO_NOVA`).
- `status`: `VARCHAR(30)`, enum `ProjectStatus` (padrão `RASCUNHO`).
- `norma_tecnica`: `VARCHAR(100)` (padrão `"NBR 5410"`).
- `responsavel`: `VARCHAR(100)` (nome do projetista responsável).
- `crea`: `VARCHAR(50)` (registro profissional do engenheiro/técnico).
- `municipio`: `VARCHAR(100)`, não nulo.
- `tensao_fornecimento`: `VARCHAR(20)`, não nulo (valores válidos: `"380_220V"`, `"220_127V"`).
- `possui_comercial`: `BOOLEAN`, não nulo, padrão `false`.
- `possui_ve`: `BOOLEAN`, não nulo, padrão `false`.
- `possui_incendio`: `BOOLEAN`, não nulo, padrão `false`.
- `etapa_atual`: `INTEGER`, não nulo, padrão `1`.
- `cep`: `VARCHAR(20)`.
- `endereco`: `VARCHAR(255)`.
- `bairro`: `VARCHAR(100)`.
- `created_at`: `TIMESTAMP`, não nulo.
- `updated_at`: `TIMESTAMP`.
- `data_submissao`: `TIMESTAMP`.

## Regras de Negócio

### 1. Geração Sequencial de Protocolo Anual
- Padrão: `PRJ-AAAA-XXXX`, onde `AAAA` é o ano civil corrente e `XXXX` é um sequencial de 4 dígitos iniciado em `0001`.
- Ao criar um novo projeto ou duplicar um existente, o sistema busca o maior protocolo existente para o prefixo `PRJ-AAAA-`.
- Se encontrado, incrementa 1 no valor numérico final (`PRJ-2026-0001` -> `PRJ-2026-0002`).
- Caso não haja registros no ano corrente, inicia em `PRJ-AAAA-0001`.

### 2. Validação dos Dados do Empreendimento
- `titulo`: obrigatório, tamanho entre 3 e 150 caracteres.
- `tensaoFornecimento`: obrigatória, restrita aos valores `"380_220V"` ou `"220_127V"`.
- `municipio`: obrigatório, não vazio.
- Violações disparam `ValidationException` mapeada para HTTP 400 Bad Request com mapa de erros por campo.

### 3. Isolamento e Controle de Acesso
- A criação associa automaticamente o projeto ao usuário autenticado identificado via JWT.
- A listagem retorna estritamente os projetos vinculados ao `user_id` do usuário autenticado.
- A consulta detalhada por ID verifica se o projeto pertence ao usuário solicitante. Caso pertença a outro usuário, o sistema bloqueia o acesso com `ForbiddenException` resultando em HTTP 403 Forbidden.
- A duplicação é estritamente permitida apenas ao projetista proprietário; requisições de outros usuários resultam em HTTP 403 Forbidden.

### 4. Duplicação de Projetos (Clonagem)
- Cria um novo registro preservando os dados elétricos e cadastrais (tensão, flags, responsável, município, endereço, etc.).
- Atribui novo título com o sufixo `" (cópia)"`.
- Reseta o status para `ProjectStatus.RASCUNHO` e a etapa para `1`.
- Gera um novo protocolo anual sequencial oficial (`PRJ-AAAA-XXXX`).
- Atualiza as datas `createdAt` e `dataSubmissao` para o instante atual.

## Endpoints REST

- `POST /api/v1/projects` (e alias `/projects`):
  - Autenticação: Bearer Token.
  - Retorno: HTTP 201 Created com DTO `CreateProjectResponse`.
- `GET /api/v1/projects` (e alias `/projects`):
  - Autenticação: Bearer Token.
  - Parâmetros opcionais:
    - `status`: filtro pelo enum `ProjectStatus`.
    - `search`: busca por termo em título, protocolo ou município.
    - `page`, `size`, `sort`: paginação Spring Data (default 10 itens por página, `createdAt DESC`).
  - Retorno: HTTP 200 OK com `Page<ProjectResponse>`.
- `POST /api/v1/projects/{id}/duplicate` (e alias `/projects/{id}/duplicate`):
  - Autenticação: Bearer Token.
  - Retorno: HTTP 201 Created com DTO `ProjectResponse` do projeto clonado.
- `GET /api/v1/projects/{id}` (e alias `/projects/{id}`):
  - Autenticação: Bearer Token.
  - Retorno: HTTP 200 OK com DTO `ProjectResponse`.
