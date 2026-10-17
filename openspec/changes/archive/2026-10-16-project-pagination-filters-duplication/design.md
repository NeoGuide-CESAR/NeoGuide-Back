# Decisões de Design: Paginação, Filtros e Duplicação de Projetos

## Arquitetura e Estrutura de Componentes

### 1. Repositório Paginado com Filtros Opcionais
Utiliza consulta JPQL com parâmetros opcionais no `ProjectRepository`:
```java
@Query("SELECT p FROM Project p WHERE p.user.id = :userId " +
       "AND (:status IS NULL OR p.status = :status) " +
       "AND (:termo IS NULL OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', :termo, '%')) " +
       "     OR LOWER(p.protocolo) LIKE LOWER(CONCAT('%', :termo, '%')) " +
       "     OR LOWER(p.municipio) LIKE LOWER(CONCAT('%', :termo, '%')))")
Page<Project> findByUserWithFilters(@Param("userId") Long userId,
                                    @Param("status") ProjectStatus status,
                                    @Param("termo") String termo,
                                    Pageable pageable);
```
O termo de busca é normalizado no serviço (`search == null || search.isBlank() ? null : search.trim()`) para evitar filtros desnecessários quando vazio.

### 2. Serviço de Duplicação Transacional
- O método `duplicateProject(Long projectId, Long currentUserId)` garante integridade com `@Transactional`.
- Se o projeto não for encontrado, lança `ResourceNotFoundException`.
- Se o projeto pertencer a outro usuário, lança `ForbiddenException`.
- A nova entidade é clonada com:
  - Título original acrescido de `" (cópia)"`.
  - Status resetado para `ProjectStatus.RASCUNHO`.
  - Etapa atual resetada para `1`.
  - Novo protocolo gerado via `generateNextProtocolo()`.
  - Datas `createdAt` e `dataSubmissao` atualizadas para `LocalDateTime.now()`.
  - Herança de todos os dados do DNA (tensão, flags comerciais, VE e incêndio, CREA, responsável, município, endereço, CEP, etc.).

### 3. Controlador REST e Pageable
- Atualização do método `list` em `ProjectController` para aceitar `status`, `search` e `Pageable` com `@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)`.
- Novo endpoint `POST /{id}/duplicate` respondendo com status HTTP 201 Created.
