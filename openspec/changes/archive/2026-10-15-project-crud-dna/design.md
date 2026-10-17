# Design: CRUD e Gestão de Projetos Elétricos (DNA do Empreendimento)

## Arquitetura e Decisões Técnicas

### 1. Modelo de Domínio
A entidade `Project` passa a representar a raiz de agregação do projeto elétrico e seu DNA.
- **Vínculo com Usuário**: `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;`
- **DNA Elétrico**:
  - `tensaoFornecimento` (String não nula, ex.: "380_220V", "220_127V")
  - `possuiComercial` (boolean, default false)
  - `possuiVe` (boolean, default false)
  - `possuiIncendio` (boolean, default false)
  - `etapaAtual` (Integer, default 1)
- **Localização e Responsável**:
  - `cep` (String, length 20)
  - `endereco` (String, length 255)
  - `bairro` (String, length 100)
  - `crea` (String, length 50)
  - `municipio` (String, length 100)
- **Auditoria**:
  - `createdAt` (LocalDateTime, nullable = false)
  - `updatedAt` (LocalDateTime)

### 2. Geração de Protocolo Anual Sequencial
- Prefixo: `PRJ-AAAA-` (onde `AAAA` é obtido via `LocalDate.now().getYear()`).
- O método consulta o último protocolo salvo no banco com o prefixo.
- Se existir (ex: `PRJ-2026-0003`), extrai o sequencial `3`, incrementa para `4`, formatando como `PRJ-2026-0004`.
- Se não existir nenhum projeto para o ano corrente, inicia com `PRJ-2026-0001`.
- A coluna `protocolo` possui restrição de unicidade (`unique = true`).

### 3. Segurança e Controle de Acesso
- O usuário autenticado é recuperado no `ProjectController` via contexto de segurança (`SecurityContextHolder` / `Authentication`).
- `POST /api/v1/projects`: associa o projeto ao usuário logado.
- `GET /api/v1/projects`: filtra estritamente por `user_id`.
- `GET /api/v1/projects/{id}`: verifica se `project.getUser().getId().equals(currentUserId)`. Se for de outro usuário, lança `ForbiddenException`, que é interceptada pelo `GlobalExceptionHandler` e resulta em status HTTP 403 Forbidden.

### 4. Validação
- O componente `Validator` valida:
  - `titulo`: obrigatório, tamanho entre 3 e 150 caracteres.
  - `tensaoFornecimento`: obrigatório, valor restrito a `"380_220V"` ou `"220_127V"`.
  - `municipio`: obrigatório.
