# Especificação de Autenticação e Perfis (Delta)

## 1. Perfis de Usuário
- Enum `UserRole`:
  - `ROLE_PROJETISTA`: perfil padrão do usuário que elabora e submete projetos elétricos.
  - `ROLE_ANALISTA`: perfil de analista da distribuidora responsável pela análise e homologação.
- Mapeado como string (`EnumType.STRING`) na entidade `User` (coluna `role`, nullable = false, length = 30, default `ROLE_PROJETISTA`).

## 2. Documento de Identificação (CPF/CNPJ)
- Coluna `cpf_cnpj` única e opcional na entidade `User` (length = 20, unique = true).
- Sanitização de CPF/CNPJ: caracteres não numéricos (`[^0-9]`) são removidos antes de persistência ou busca por documento.
- Validação de unicidade no cadastro: duplicidade de CPF/CNPJ resulta em HTTP 409 Conflict (`ConflictException`).

## 3. Endpoints de Autenticação

### POST `/api/v1/auth/register` (e `/auth/register`)
- **Payload expandido**:
  - `name`: string obrigatória (3 a 100 caracteres)
  - `email`: string obrigatória em formato de e-mail válido (5 a 150 caracteres)
  - `cpfCnpj`: string opcional (CPF ou CNPJ formatado ou apenas dígitos)
  - `role`: string ou enum opcional (`ROLE_PROJETISTA` por padrão)
  - `password`: string obrigatória com requisitos de senha forte
  - `confirmPassword`: deve coincidir com `password`
- **Comportamento**:
  - Valida duplicidade de CPF/CNPJ: se já existente, retorna HTTP 409 Conflict.
  - Retorna HTTP 201 Created com `UserResponse` contendo `cpfCnpj` e `role`.

### POST `/api/v1/auth/login` (e `/auth/login`)
- **Payload expandido**:
  - `login`: identificador flexível do usuário, podendo ser e-mail (ex: `projetista@neoguide.com`) ou CPF/CNPJ (com máscara `123.456.789-01` ou sem máscara `12345678901`).
  - `email`: campo mantido para retrocompatibilidade com clientes legados.
  - `password`: senha do usuário.
- **Comportamento**:
  - Valida se o campo de login (ou email) e a senha foram fornecidos.
  - Consulta o repositório por e-mail ou documento sanitizado/original.
  - Valida hash de senha com BCrypt.
  - Retorna HTTP 200 OK com `LoginResponse` contendo `token`, `tipo: "Bearer"`, `role`, `roles` e `usuario` com dados completos (`cpfCnpj`, `role`).

## 4. Token JWT
- Contém a claim `"role"` com o nome do perfil (ex: `"ROLE_PROJETISTA"`).
- Fornece métodos extratores `extractRole(String token)` e `extractRoles(String token)`.
- O filtro `JwtAuthenticationFilter` extrai a role e atribui a devida autoridade (`SimpleGrantedAuthority`) no `SecurityContextHolder`.
