# Delta Spec: Autenticação JWT e Spring Security

## Requisitos do Sistema

### 1. Autenticação e Emissão de Tokens
- **POST `/api/v1/auth/register` (e `/auth/register`)**:
  - Recebe `RegisterRequest` (`name`, `email`, `password`, `confirmPassword`).
  - Cria usuário com senha criptografada via BCrypt.
  - Retorna HTTP 201 com `UserResponse`.
- **POST `/api/v1/auth/login` (e `/auth/login`)**:
  - Recebe `LoginRequest` (`email`, `password`).
  - Valida as credenciais.
  - Retorna HTTP 200 com `LoginResponse` contendo `token` JWT gerado, `tipo: "Bearer"` e `usuario: UserResponse`.
  - Se credenciais incorretas, retorna HTTP 401 com `ErrorResponse`.

### 2. Formato e Ciclo de Vida do Token JWT
- Assinado com algoritmo HMAC-SHA256 (`HS256`) usando chave simétrica configurada em `security.jwt.secret` (mínimo 32 bytes).
- Tempo de expiração de 24 horas (86.400.000 ms) configurado em `security.jwt.expiration`.
- Subject (`sub`) contendo o ID ou email do usuário.
- Claims customizadas contendo `email` e `name`.

### 3. Filtro de Autenticação e Proteção de Rotas
- Cabeçalho `Authorization: Bearer <token>` extraído em todas as requisições.
- Se o token for válido e não expirado, popula o `SecurityContextHolder`.
- Se o token for ausente ou inválido em rotas restritas, o `JwtAuthenticationEntryPoint` responde com HTTP 401 Unauthorized e JSON estruturado `ErrorResponse`.
- Rotas públicas:
  - `/api/v1/auth/**` e `/auth/**`
  - `/h2-console/**`
- Demais rotas:
  - Exigem autenticação (`.authenticated()`).
