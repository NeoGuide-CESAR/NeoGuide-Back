# Design: feat-US-08-rotas-catalogo-residencial

## Arquitetura e Decisões Técnicas

### 1. Caching em Memória com Spring Cache
Configuração centralizada em `CacheConfig.java` anotada com `@Configuration` e `@EnableCaching`.
Utiliza-se `ConcurrentMapCacheManager` registrando explicitamente os três nomes de cache:
- `catalogoFaixasArea`
- `catalogoFatoresCoincidencia`
- `catalogoFatoresSeguranca`

Essas tabelas normativas possuem conteúdo estático derivado da norma técnica DIS-NOR-053 semeado no boot pelo `DataSeeder`. O cache em memória garante latência sub-milissegundo para os endpoints após o warm-up inicial.

### 2. Liberação de Segurança
A classe `SecurityConfig.java` intercepta todas as requisições HTTP. Para permitir acesso por visitantes sem autenticação ou usuários logados montando projetos sem overhead de token nas tabelas de referência, adiciona-se `/api/v1/catalogo/**` aos `requestMatchers` do `permitAll()`.

### 3. Repositórios Normativos Ordenados
As consultas utilizam convenções de métodos de consulta do Spring Data JPA:
- `AreaRangeReferenceRepository.findAllByOrderByMinAreaM2Asc()`: garante que a faixa inicial (Até 40 m²) venha primeiro até Acima de 300 m².
- `CoincidenceFactorReferenceRepository.findAllByOrderByTotalApartamentosAsc()`: garante que os fatores de coincidência comecem em 2 até 60+ apartamentos.
- `SafetyFactorReferenceRepository.findAllByOrderByMinDrKvaAsc()`: garante que os fatores de segurança venham ordenados por patamar de demanda (0.00 a 25.00, etc.).

### 4. DTOs de Catálogo (ZERO Lombok)
Classes Java 21 imutáveis/POJO com construtores explícitos e métodos de fábrica `from(entity)`:
- `AreaRangeResponse`: campos `id`, `faixa`, `rotulo`, `minAreaM2`, `maxAreaM2`, `demandaUnitariakVa` (anotado com `@JsonProperty("demandaUnitariakVa")`), `normaOrigem`.
- `CoincidenceFactorItemResponse`: campos `totalApartamentos`, `fatorCoincidencia`.
- `CoincidenceFactorWrapperResponse`: campos `norma`, `regraEspecialStudios`, `tabela`.
- `SafetyFactorResponse`: campos `minDrKva`, `maxDrKva`, `fatorSegurancaFr`.

### 5. Camada de Serviço e Controlador REST
- `CatalogService`: componentes anotados com `@Service` e métodos de leitura decorados com `@Cacheable(...)`.
- `CatalogController`: endpoint REST sob `/api/v1/catalogo` expondo:
  - `GET /faixas-area`
  - `GET /fatores-coincidencia-residencial`
  - `GET /fatores-seguranca-residencial`
