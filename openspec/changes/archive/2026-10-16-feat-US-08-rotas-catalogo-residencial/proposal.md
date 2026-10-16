# Proposal: feat-US-08-rotas-catalogo-residencial

## Motivação e Contexto
A plataforma NeoGuide necessita expor endpoints REST públicos sob `/api/v1/catalogo` para os Quadros 35, 36 e 37 da norma regulatória DIS-NOR-053 da Neoenergia Pernambuco. Esses endpoints fornecem dados normativos de faixas de área útil ($D_i$), curva estatística de fatores de coincidência ($F_c$) e fatores de segurança ($F_r$) para alimentar seletores dinâmicos, chips e tabelas do frontend sem que constantes técnicas fiquem hardcoded no cliente web.

Para atender aos requisitos de alta performance (tempo de resposta sub-15ms), as consultas normativas devem ser cacheadas em memória RAM via Spring Cache (`@EnableCaching` e `@Cacheable`).

## Escopo
- Habilitação de cache em memória no Spring Boot via `neoguide.project.config.CacheConfig` (`catalogoFaixasArea`, `catalogoFatoresCoincidencia`, `catalogoFatoresSeguranca`).
- Liberação das rotas `/api/v1/catalogo/**` no `SecurityConfig.java` como `permitAll()` para permitir acesso tanto anônimo quanto autenticado.
- Extensão dos repositórios JPA em `neoguide.project.repository.normativo` com métodos de ordenação crescente (`findAllByOrderByMinAreaM2Asc`, `findAllByOrderByTotalApartamentosAsc`, `findAllByOrderByMinDrKvaAsc`).
- Criação dos DTOs de catálogo em `neoguide.project.dto.catalogo` estritamente em Java 21 sem Lombok (`AreaRangeResponse`, `CoincidenceFactorItemResponse`, `CoincidenceFactorWrapperResponse`, `SafetyFactorResponse`).
- Criação do serviço `CatalogService` com anotações `@Cacheable`.
- Criação do controlador REST `CatalogController` sob `/api/v1/catalogo`.
- Testes automatizados MockMvc (`CatalogControllerTest`) e testes de caching (`CatalogServiceTest`).

## Critérios de Sucesso
- `GET /api/v1/catalogo/faixas-area` retorna HTTP 200 com lista completa das 13 faixas do Quadro 35 ordenadas por metragem.
- `GET /api/v1/catalogo/fatores-coincidencia-residencial` retorna HTTP 200 com wrapper contendo norma, regra de studios e tabela do Quadro 36.
- `GET /api/v1/catalogo/fatores-seguranca-residencial` retorna HTTP 200 com patamares e multiplicadores $F_r$ do Quadro 37.
- Acesso liberado publicamente sem exigir token JWT (sem erros 401/403).
- Resolução via cache em memória a partir da 2ª consulta (`times(1)` no repository).
- 100% de testes unitários e de integração passando via `./mvnw.cmd test`.
