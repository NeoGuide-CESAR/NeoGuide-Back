# Tarefas: Autenticação JWT e Spring Security

- [x] Adicionar dependências JJWT 0.12.6 no `pom.xml` (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) <!-- id: 1 -->
- [x] Configurar propriedades JWT em `application.yaml` <!-- id: 2 -->
- [x] Criar DTO `LoginResponse` <!-- id: 3 -->
- [x] Implementar `JwtService` com HMAC-SHA256 e extração/validação de claims <!-- id: 4 -->
- [x] Implementar `JwtAuthenticationEntryPoint` com HTTP 401 JSON <!-- id: 5 -->
- [x] Implementar `JwtAuthenticationFilter` estendendo `OncePerRequestFilter` <!-- id: 6 -->
- [x] Atualizar `SecurityConfig` com filtro JWT, entrypoint, rotas públicas e frameOptions H2 <!-- id: 7 -->
- [x] Atualizar `AuthService` para retornar `LoginResponse` com token JWT no login <!-- id: 8 -->
- [x] Atualizar `AuthController` com mapeamento `/api/v1/auth` e `/auth` retornando `LoginResponse` <!-- id: 9 -->
- [x] Implementar testes unitários `JwtServiceTest` <!-- id: 10 -->
- [x] Implementar testes unitários `AuthServiceTest` <!-- id: 11 -->
- [x] Implementar testes de integração MockMvc `AuthControllerTest` <!-- id: 12 -->
- [x] Executar `mvn test` e validar 100% de aprovação <!-- id: 13 -->
- [x] Sincronizar delta spec para `openspec/specs/auth/spec.md` <!-- id: 14 -->
- [x] Arquivar change em `openspec/changes/archive/2026-10-15-auth-jwt-security/` <!-- id: 15 -->
