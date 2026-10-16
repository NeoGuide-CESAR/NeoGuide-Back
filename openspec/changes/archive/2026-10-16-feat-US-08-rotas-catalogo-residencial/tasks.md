# Tasks: feat-US-08-rotas-catalogo-residencial

- [x] Criar CacheConfig com @EnableCaching e ConcurrentMapCacheManager
- [x] Atualizar SecurityConfig liberando `/api/v1/catalogo/**` em permitAll
- [x] Atualizar AreaRangeReferenceRepository, CoincidenceFactorReferenceRepository e SafetyFactorReferenceRepository com consultas ordenadas
- [x] Implementar DTOs de catálogo (AreaRangeResponse, CoincidenceFactorItemResponse, CoincidenceFactorWrapperResponse, SafetyFactorResponse) sem Lombok
- [x] Implementar CatalogService com anotações @Cacheable
- [x] Implementar CatalogController mapeando os 3 endpoints de catálogo
- [x] Implementar testes de integração CatalogControllerTest com MockMvc
- [x] Implementar testes unitários/integração CatalogServiceTest validando caching
- [x] Executar `./mvnw.cmd test` com 100% de sucesso
- [x] Sincronizar delta spec para openspec/specs/catalogo/spec.md
- [x] Arquivar change em openspec/changes/archive/2026-10-16-feat-US-08-rotas-catalogo-residencial/
- [x] Realizar commit git na branch da US-08
