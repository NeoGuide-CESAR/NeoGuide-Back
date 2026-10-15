# Especificação de Autenticação e Segurança Stateless (JWT)

## 1. Visão Geral
Define as regras de negócio, contratos de API e mecanismos de proteção stateless via JSON Web Token (JWT) e Spring Security para a aplicação NeoGuide.

## 2. Endpoints de Autenticação

### POST `/api/v1/auth/register` (e `/auth/register`)
- **Objetivo**: Cadastro de novos projetistas.
- **Payload**:
  - `name`: string obrigatória (3 a 100 caracteres)
  - `email`: string obrigatória em formato de e-mail válido (5 a 150 caracteres)
  - `password`: string obrigatória com requisitos de senha forte (mínimo 8 caracteres, maiúscula, minúscula, dígito e símbolo)
  - `confirmPassword`: deve coincidir com `password`
- **Comportamento**:
  - Valida payload; falhas retornam HTTP 400 com mapa de erros.
  - Verifica duplicidade de e-mail (case-insensitive); duplicatas retornam HTTP 400 com mensagem "E-mail já cadastrado".
  - Criptografa a senha com algoritmo BCrypt.
  - Retorna HTTP 201 Created com DTO `UserResponse` (`id`, `name`, `email`, `createdAt`).

### POST `/api/v1/auth/login` (e `/auth/login`)
- **Objetivo**: Autenticação de projetistas e emissão de token JWT.
- **Payload**:
  - `email`: string obrigatória em formato de e-mail válido
  - `password`: string obrigatória
- **Comportamento**:
  - Valida credenciais (email e hash BCrypt). Falha retorna HTTP 401 Unauthorized com formato padronizado `ErrorResponse`.
  - Emite token JWT assinado HMAC-SHA256 com validade de 24 horas.
  - Retorna HTTP 200 OK com DTO `LoginResponse` contendo:
    - `token`: string JWT assinada
    - `tipo`: "Bearer"
    - `usuario`: dados do projetista em `UserResponse`

## 3. Especificação do Token JWT
- **Algoritmo**: HMAC-SHA256 (`HS256`).
- **Chave de Assinatura**: Configurada via `security.jwt.secret` (mínimo 256 bits / 32 bytes).
- **Tempo de Expiração**: 24 horas (`86400000` ms) configurado em `security.jwt.expiration`.
- **Claims**:
  - `sub`: Identificador / e-mail do usuário
  - `userId`: ID numérico (`Long`) do usuário
  - `email`: E-mail normalizado do usuário
  - `name`: Nome completo do projetista
  - `iat`: Timestamp de emissão
  - `exp`: Timestamp de expiração

## 4. Filtro de Autenticação e Autorização (Spring Security)
- **Filtro `JwtAuthenticationFilter`**:
  - Executado uma vez por requisição (`OncePerRequestFilter`) antes de `UsernamePasswordAuthenticationFilter`.
  - Extrai o header `Authorization: Bearer <token>`.
  - Valida integridade e expiração do token através de `JwtService`.
  - Injeta autenticação no `SecurityContextHolder` com `UsernamePasswordAuthenticationToken`.
- **Controle de Acesso**:
  - Rotas públicas (acesso liberado sem token):
    - `/api/v1/auth/**` e `/auth/**`
    - `/h2-console/**`
  - Rotas protegidas:
    - Qualquer outra rota (`.anyRequest().authenticated()`).
    - Requisições sem token ou com token inválido/expirado recebem resposta HTTP 401 Unauthorized tratada por `JwtAuthenticationEntryPoint` no formato `ErrorResponse`:
      ```json
      {
        "timestamp": "...",
        "status": 401,
        "message": "Acesso não autorizado. Token ausente ou inválido.",
        "errors": {}
      }
      ```
  - Suporte ao console H2 com `frameOptions.sameOrigin()`.
