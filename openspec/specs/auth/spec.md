# Especificação de Autenticação e Segurança Stateless (JWT)

## 1. Visão Geral
Define as regras de negócio, contratos de API e mecanismos de proteção stateless via JSON Web Token (JWT) e Spring Security para a aplicação NeoGuide, incluindo suporte a login flexível (e-mail ou CPF/CNPJ) e múltiplos perfis de acesso (`ROLE_PROJETISTA`, `ROLE_ANALISTA`).

## 2. Perfis de Acesso (User Roles)
- **Enum `UserRole`**:
  - `ROLE_PROJETISTA`: perfil padrão dos usuários que elaboram e submetem projetos elétricos.
  - `ROLE_ANALISTA`: perfil de analista técnico responsável pela avaliação e homologação dos projetos.
- Mapeado no modelo `User` como `@Enumerated(EnumType.STRING)` (coluna `role`, nullable = false, length = 30, padrão `ROLE_PROJETISTA`).

## 3. Identificação por CPF/CNPJ
- Coluna `cpf_cnpj` única e opcional na entidade `User` (length = 20, unique = true).
- **Sanitização**: Caracteres não numéricos (`[^0-9]`) são removidos antes da persistência ou busca por documento.
- **Unicidade**: Tentativa de cadastro com CPF/CNPJ já registrado retorna `HTTP 409 Conflict` padronizado.

## 4. Endpoints de Autenticação

### POST `/api/v1/auth/register` (e `/auth/register`)
- **Objetivo**: Cadastro de novos usuários.
- **Payload**:
  - `name`: string obrigatória (3 a 100 caracteres)
  - `email`: string obrigatória em formato de e-mail válido (5 a 150 caracteres)
  - `cpfCnpj`: string opcional (CPF ou CNPJ com ou sem pontuação)
  - `role`: string ou enum opcional (`ROLE_PROJETISTA` por padrão)
  - `password`: string obrigatória com requisitos de senha forte (mínimo 8 caracteres, letra e número)
  - `confirmPassword`: deve coincidir com `password`
- **Comportamento**:
  - Valida payload; falhas de validação retornam HTTP 400 com mapa de erros.
  - Verifica duplicidade de e-mail (case-insensitive); duplicatas retornam HTTP 400 com mensagem "E-mail já cadastrado".
  - Verifica duplicidade de CPF/CNPJ (sanitizado); duplicatas retornam HTTP 409 Conflict com mensagem "CPF/CNPJ já cadastrado".
  - Criptografa a senha com algoritmo BCrypt.
  - Retorna HTTP 201 Created com DTO `UserResponse` (`id`, `name`, `email`, `cpfCnpj`, `role`, `createdAt`).

### POST `/api/v1/auth/login` (e `/auth/login`)
- **Objetivo**: Autenticação de usuários e emissão de token JWT.
- **Payload**:
  - `login`: string flexível aceitando e-mail (ex: `projetista@neoguide.com`) ou CPF/CNPJ (ex: `12345678901` ou `123.456.789-01`).
  - `email`: string mantida para total retrocompatibilidade com clientes legados.
  - `password`: string obrigatória
- **Comportamento**:
  - Localiza o usuário via consulta flexível por e-mail ou documento (sanitizado e original).
  - Valida credenciais via hash BCrypt. Falha retorna HTTP 401 Unauthorized com formato padronizado `ErrorResponse`.
  - Emite token JWT assinado HMAC-SHA256 com validade de 24 horas.
  - Retorna HTTP 200 OK com DTO `LoginResponse` contendo:
    - `token`: string JWT assinada
    - `tipo`: "Bearer"
    - `role`: string da role ativa (ex: `"ROLE_PROJETISTA"`)
    - `roles`: lista de strings de roles
    - `usuario`: dados do usuário em `UserResponse` (`id`, `name`, `email`, `cpfCnpj`, `role`, `createdAt`)

## 5. Especificação do Token JWT
- **Algoritmo**: HMAC-SHA256 (`HS256`).
- **Chave de Assinatura**: Configurada via `security.jwt.secret` (mínimo 256 bits / 32 bytes).
- **Tempo de Expiração**: 24 horas (`86400000` ms) configurado em `security.jwt.expiration`.
- **Claims**:
  - `sub`: Identificador / e-mail do usuário
  - `userId`: ID numérico (`Long`) do usuário
  - `email`: E-mail normalizado do usuário
  - `name`: Nome completo do usuário
  - `role`: Nome do perfil de acesso (`ROLE_PROJETISTA` ou `ROLE_ANALISTA`)
  - `roles`: Lista de roles atribuídas
  - `iat`: Timestamp de emissão
  - `exp`: Timestamp de expiração

## 6. Filtro de Autenticação e Autorização (Spring Security)
- **Filtro `JwtAuthenticationFilter`**:
  - Executado uma vez por requisição (`OncePerRequestFilter`) antes de `UsernamePasswordAuthenticationFilter`.
  - Extrai o header `Authorization: Bearer <token>`.
  - Valida integridade e expiração do token através de `JwtService`.
  - Extrai as authorities (`SimpleGrantedAuthority`) a partir da claim `role` do token ou do usuário.
  - Injeta autenticação no `SecurityContextHolder` com `UsernamePasswordAuthenticationToken`.
- **Controle de Acesso**:
  - Rotas públicas (acesso liberado sem token):
    - `/api/v1/auth/**` e `/auth/**`
    - `/h2-console/**`
  - Rotas protegidas:
    - Qualquer outra rota (`.anyRequest().authenticated()`).
    - Requisições sem token ou com token inválido/expirado recebem resposta HTTP 401 Unauthorized tratada por `JwtAuthenticationEntryPoint` no formato `ErrorResponse`.
  - Suporte ao console H2 com `frameOptions.sameOrigin()`.
