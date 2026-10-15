# Especificação: CRUD e Gestão de Projetos Elétricos

## Entidades e Estruturas de Dados

### Project
```
Project {
  id: Long (PK, auto-increment)
  user: User (FK not null)
  protocolo: String (unique, not null, max 30)
  titulo: String (not null, 3..150)
  descricao: String (max 500)
  tipo: ProjectType (enum, default LIGACAO_NOVA)
  status: ProjectStatus (enum, default RASCUNHO)
  normaTecnica: String (max 100)
  responsavel: String (max 100)
  municipio: String (not null, max 100)
  tensaoFornecimento: String (not null, 380_220V | 220_127V)
  possuiComercial: boolean (default false)
  possuiVe: boolean (default false)
  possuiIncendio: boolean (default false)
  etapaAtual: Integer (default 1)
  cep: String (max 20)
  endereco: String (max 255)
  bairro: String (max 100)
  crea: String (max 50)
  createdAt: LocalDateTime (not null)
  updatedAt: LocalDateTime
  dataSubmissao: LocalDateTime
}
```

## Endpoints

### 1. Criar Projeto
- **Método**: `POST`
- **Caminho**: `/api/v1/projects` ou `/projects`
- **Autenticação**: Bearer Token obrigatório
- **Request Body (`CreateProjectRequest`)**:
  - `titulo` (string, 3..150, obrigatório)
  - `descricao` (string, opcional)
  - `tipo` (string, opcional)
  - `tensaoFornecimento` ("380_220V" | "220_127V", obrigatório)
  - `possuiComercial` (boolean)
  - `possuiVe` (boolean)
  - `possuiIncendio` (boolean)
  - `responsavel` (string, opcional)
  - `crea` (string, opcional)
  - `municipio` (string, obrigatório)
  - `cep` (string, opcional)
  - `endereco` (string, opcional)
  - `bairro` (string, opcional)
- **Resposta**: `201 Created`
  - Body: `CreateProjectResponse` (`id`, `protocolo`, `titulo`, `status`, `etapaAtual`, `createdAt`)

### 2. Listar Projetos do Usuário
- **Método**: `GET`
- **Caminho**: `/api/v1/projects` ou `/projects`
- **Autenticação**: Bearer Token obrigatório
- **Resposta**: `200 OK`
  - Body: `List<ProjectResponse>` ordenado por `createdAt DESC`

### 3. Obter Detalhes do Projeto
- **Método**: `GET`
- **Caminho**: `/api/v1/projects/{id}` ou `/projects/{id}`
- **Autenticação**: Bearer Token obrigatório
- **Resposta**:
  - `200 OK` se o projeto pertencer ao usuário autenticado
  - `403 Forbidden` se o projeto pertencer a outro usuário
  - `400 Bad Request` ou `404 Not Found` se o projeto não existir
