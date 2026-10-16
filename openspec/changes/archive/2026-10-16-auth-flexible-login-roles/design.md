# Design: Login Flexível e Perfis de Acesso

## Decisões Arquiteturais

1. **Enum `UserRole`**:
   - `neoguide.project.model.UserRole` com os perfis `ROLE_PROJETISTA` e `ROLE_ANALISTA`.
   - Adicionado à entidade `User` com `@Enumerated(EnumType.STRING)` e valor default `ROLE_PROJETISTA`.

2. **Campo `cpfCnpj` no `User`**:
   - `@Column(name = "cpf_cnpj", unique = true, length = 20) private String cpfCnpj;`
   - Sanitizado no cadastro para armazenar preferencialmente os dígitos (`replaceAll("[^0-9]", "")`), garantindo integridade contra variações de máscaras.

3. **Consulta Flexível no Repositório**:
   - `UserRepository`:
     - `Optional<User> findByEmail(String email);`
     - `Optional<User> findByCpfCnpj(String cpfCnpj);`
     - `@Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:login) OR u.cpfCnpj = :login OR u.cpfCnpj = :sanitized") Optional<User> findByEmailOrCpfCnpj(@Param("login") String login, @Param("sanitized") String sanitized);`
     - `boolean existsByCpfCnpj(String cpfCnpj);`

4. **DTOs com Retrocompatibilidade**:
   - `LoginRequest`: campos `login` e `email` mapeiam internamente para o identificador fornecido. Se um cliente legado enviar apenas `email`, `getLogin()` devolve o valor de `email`.
   - `RegisterRequest`: campos opcionais `cpfCnpj` e `role` (default `ROLE_PROJETISTA`).
   - `UserResponse`: expõe `cpfCnpj` e `role`.
   - `LoginResponse`: expõe `role`, `roles` e `usuario` atualizado.

5. **Emissão de JWT com Claims de Perfil**:
   - `JwtService.generateToken()` inclui `claim("role", roleName)` e `claim("roles", List.of(roleName))`.
   - Método `extractRole(String token)` para obter o perfil diretamente.
   - `JwtAuthenticationFilter` injeta autoridade `SimpleGrantedAuthority(role)` no `SecurityContextHolder`.

6. **Tratamento de Exceções**:
   - `ConflictException` (estendendo `BusinessException`) mapeado para `HTTP 409 CONFLICT` no `GlobalExceptionHandler`.
   - Lançado quando CPF/CNPJ já existir na base de dados.
