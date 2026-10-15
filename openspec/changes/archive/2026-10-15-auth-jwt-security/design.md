# Design: Autenticação JWT e Spring Security

## Arquitetura de Segurança

A autenticação é stateless, utilizando JSON Web Token (JWT) assinado com HMAC-SHA256 (`Keys.hmacShaKeyFor`).

```
Requisição HTTP (com Authorization: Bearer <token>)
   │
   ▼
[JwtAuthenticationFilter] ──(Token Válido)──> Injeta Authentication no SecurityContextHolder
   │                                                    │
(Token Ausente ou Inválido)                             ▼
   │                                            [Filtros subsequentes]
   ▼                                                    │
[SecurityFilterChain] ───────────────────────────> [Controller]
   │
(Não autorizado nas rotas protegidas)
   ▼
[JwtAuthenticationEntryPoint] ──(401 Unauthorized)──> JSON ErrorResponse
```

## Componentes

1. **`JwtService`** (`neoguide.project.security.jwt.JwtService`):
   - Injeta `@Value("${security.jwt.secret}")` e `@Value("${security.jwt.expiration}")`.
   - Gera tokens assinados com HMAC-SHA256 (`Jwts.builder()`).
   - Subject: ID do usuário (ou email) convertido em String.
   - Claims: `email`, `name`.
   - Métodos: `generateToken(User user)`, `validateToken(String token)`, `extractUsername(String token)`, `extractUserId(String token)`.

2. **`JwtAuthenticationFilter`** (`neoguide.project.security.jwt.JwtAuthenticationFilter`):
   - Estende `OncePerRequestFilter`.
   - Intercepta `Authorization` header no formato `Bearer <token>`.
   - Valida o token via `JwtService`. Se válido, obtém o username (e/ou consulta o UserRepository), cria `UsernamePasswordAuthenticationToken` sem credenciais com authorities vazias, define os detalhes com `WebAuthenticationDetailsSource` e seta no `SecurityContextHolder`.

3. **`JwtAuthenticationEntryPoint`** (`neoguide.project.security.jwt.JwtAuthenticationEntryPoint`):
   - Implementa `AuthenticationEntryPoint`.
   - Intercepta falhas de autenticação em recursos protegidos e retorna `HTTP 401 Unauthorized` com JSON estruturado compatível com `ErrorResponse`:
     ```json
     {
       "timestamp": "2026-10-15T...",
       "status": 401,
       "message": "Acesso não autorizado. Token ausente ou inválido.",
       "errors": {}
     }
     ```

4. **`SecurityConfig`** (`neoguide.project.security.SecurityConfig`):
   - Adiciona `JwtAuthenticationFilter` antes de `UsernamePasswordAuthenticationFilter`.
   - Configura `exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint)`.
   - Configura `authorizeHttpRequests`:
     - Liberadas: `/api/v1/auth/**`, `/auth/**`, `/h2-console/**`.
     - Restritas: `.anyRequest().authenticated()`.
   - Desabilita CSRF e ativa CORS.
   - Headers: `frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)` para suportar `/h2-console/**`.

5. **DTOs e Controladores**:
   - `LoginResponse`: `{ "token": "...", "tipo": "Bearer", "usuario": { ... } }`.
   - `AuthController`: expõe `/api/v1/auth` e `/auth` para `/register` e `/login`.
   - `AuthService.login()`: valida credenciais e retorna `LoginResponse` com token gerado por `JwtService`.
