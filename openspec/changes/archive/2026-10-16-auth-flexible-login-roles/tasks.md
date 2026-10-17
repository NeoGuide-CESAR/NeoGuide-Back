# Tarefas: Login Flexível e Perfis de Acesso

- [x] Criar enum `UserRole` (`ROLE_PROJETISTA`, `ROLE_ANALISTA`) <!-- id: 1 -->
- [x] Atualizar modelo `User` com `cpfCnpj` e `role` mantendo Java 21 puro <!-- id: 2 -->
- [x] Criar `ConflictException` e atualizar `GlobalExceptionHandler` com HTTP 409 <!-- id: 3 -->
- [x] Atualizar `UserRepository` com `findByCpfCnpj`, `findByEmailOrCpfCnpj` e `existsByCpfCnpj` <!-- id: 4 -->
- [x] Atualizar DTOs `LoginRequest`, `RegisterRequest`, `UserResponse` e `LoginResponse` <!-- id: 5 -->
- [x] Atualizar `JwtService` com claim `"role"` e método `extractRole(String token)` <!-- id: 6 -->
- [x] Atualizar `JwtAuthenticationFilter` para associar authorities do perfil <!-- id: 7 -->
- [x] Atualizar `AuthService` com sanitização, validação de duplicidade e busca flexível <!-- id: 8 -->
- [x] Atualizar `AuthServiceTest` cobrindo login flexível por e-mail e CPF, e CPF duplicado <!-- id: 9 -->
- [x] Atualizar `JwtServiceTest` cobrindo a claim `role` e extrator <!-- id: 10 -->
- [x] Atualizar `AuthControllerTest` cobrindo login flexível, cadastro com CPF/CNPJ e conflito 409 <!-- id: 11 -->
- [x] Executar `mvn clean test` e garantir 100% de sucesso <!-- id: 12 -->
- [x] Sincronizar delta spec para `openspec/specs/auth/spec.md` <!-- id: 13 -->
- [x] Arquivar change em `openspec/changes/archive/2026-10-16-auth-flexible-login-roles/` <!-- id: 14 -->
