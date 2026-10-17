# Proposta: Login Flexível (CPF/CNPJ ou E-mail) e Perfis de Acesso (User Roles)

## Por que
Evoluir o subsistema de autenticação do NeoGuide para atender às diretrizes do Moodboard oficial de UI. Os usuários devem conseguir realizar login informando indistintamente seu e-mail corporativo ou seu CPF/CNPJ (com ou sem máscara/pontuação). Além disso, a plataforma passa a suportar perfis de acesso (`ROLE_PROJETISTA` e `ROLE_ANALISTA`), embutindo a role no token JWT para governança e controle de permissões no frontend e backend.

## O que muda
- Criação do enum `UserRole` (`ROLE_PROJETISTA`, `ROLE_ANALISTA`) em `neoguide.project.model`.
- Evolução da entidade `User` com atributos `cpfCnpj` (único, até 20 caracteres) e `role` (`UserRole`, default `ROLE_PROJETISTA`), mantendo Java 21 puro (sem Lombok) e compatibilidade com os construtores legados.
- Evolução de `UserRepository` com métodos `findByCpfCnpj`, `existsByCpfCnpj` e `@Query findByEmailOrCpfCnpj(@Param("login") String login, @Param("sanitized") String sanitized)`.
- Atualização do `LoginRequest` para aceitar `login` flexível (mantendo compatibilidade com `email`), e `RegisterRequest` para suportar `cpfCnpj` e `role`.
- Atualização de `UserResponse` e `LoginResponse` para expor `cpfCnpj` e perfil (`role` / `roles`).
- Atualização de `JwtService` para embutir claim `"role"` no token assinado HMAC-SHA256 e fornecer extratores `extractRole` e `extractRoles`.
- Atualização de `AuthService` para sanitizar documentos (remover pontuações), validar duplicidade de CPF/CNPJ lançando conflito (HTTP 409), e autenticar tanto por e-mail quanto por documento normalizado.
- Criação de `ConflictException` e tratamento em `GlobalExceptionHandler` retornando HTTP 409 Conflict.
- Cobertura completa de testes automatizados unitários e de integração (`AuthServiceTest`, `JwtServiceTest`, `AuthControllerTest`).

## Capacidades Adicionadas
- Login flexível por e-mail ou CPF/CNPJ (com e sem formatação).
- Controle de perfis de usuário (`ROLE_PROJETISTA`, `ROLE_ANALISTA`) no token JWT.
- Proteção contra cadastro com CPF/CNPJ duplicado com resposta HTTP 409 padronizada.
- Retrocompatibilidade total com clientes e testes existentes.
