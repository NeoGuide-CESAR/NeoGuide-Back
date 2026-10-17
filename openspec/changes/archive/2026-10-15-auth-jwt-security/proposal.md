# Proposta: Autenticação JWT e Spring Security para Projetistas

## Por que
Garantir o controle de acesso seguro e identificação de autoria dos projetos elétricos no NeoGuide através de autenticação stateless com tokens JWT. Permite que os projetistas se cadastrem com senha criptografada (BCrypt), autentiquem-se recebendo um token Bearer assinado e acessem rotas protegidas da API REST.

## O que muda
- Inclusão das dependências do JJWT 0.12.6 (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) no `pom.xml`.
- Configuração das propriedades `security.jwt.secret` e `security.jwt.expiration` em `application.yaml`.
- Implementação de `JwtService` para geração HMAC-SHA256, extração de claims (`email`, `name`, subject) e validação de tokens.
- Implementação de `JwtAuthenticationFilter` (`OncePerRequestFilter`) e `JwtAuthenticationEntryPoint` (HTTP 401 padronizado).
- Atualização do `SecurityConfig` para proteger endpoints não públicos por padrão, mantendo abertos `/api/v1/auth/**`, `/auth/**` e `/h2-console/**`.
- Implementação do DTO `LoginResponse` e atualização do `AuthService.login()` e `AuthController` (`/api/v1/auth/login`).
- Construção de suíte de testes unitários e de integração (`JwtServiceTest`, `AuthServiceTest`, `AuthControllerTest`).

## Capacidades Adicionadas
- Autenticação stateless via Bearer Token.
- Proteção de endpoints de API contra acessos não autenticados.
- Resposta padronizada de erro HTTP 401 com `ErrorResponse`.
