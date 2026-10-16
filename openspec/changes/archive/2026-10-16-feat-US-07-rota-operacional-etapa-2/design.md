# Design Técnico: Rota Operacional da Etapa 2 — Tipologias Residenciais (US-07)

## Arquitetura de Componentes

```mermaid
flowchart TD
    Client[Frontend Client / Wizard Step 2] -->|PUT /api/v1/projects/{id}/etapa-residencial| Controller[ProjectController]
    Controller -->|saveResidentialStage(id, req, userId)| Service[ProjectService]
    Service -->|validateResidentialStageRequest| Validator[Validator.java]
    Service -->|findById(id)| ProjRepo[ProjectRepository]
    Service -->|calculate(tipologias)| Engine[ResidentialCalculationEngine]
    Service -->|deleteByProjectId(id) + flush()| TypoRepo[ResidentialTypologyRepository]
    Service -->|saveAll(entities)| TypoRepo
    Service -->|save(project)| ProjRepo
    Service -->|ResidentialStageResponse| Controller
    Controller -->|200 OK + JSON| Client
```

## Entidade JPA (ZERO Lombok)

- `ResidentialTypology` mapeia a tabela `residential_typologies`:
  - `id`: `Long`, chave primária auto-incremento.
  - `project`: `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "project_id", nullable = false) Project`.
  - `catalogoFaixaId`: `Long`, nullable.
  - `nome`: `String`, não nulo, tamanho 100.
  - `areaUtilM2`: `BigDecimal`, precisão 8, escala 2, não nulo.
  - `quantidadeUnidades`: `Integer`, não nulo.
  - `isSmartStudio`: `boolean`, default false.
  - `demandaUnitariaKva`: `BigDecimal`, precisão 6, escala 2.
  - `demandaTotalTipologiaKva`: `BigDecimal`, precisão 8, escala 2.

## Repositório Spring Data JPA

- `ResidentialTypologyRepository`:
  - `List<ResidentialTypology> findByProjectIdOrderByIdAsc(Long projectId)`
  - `@Modifying @Query("DELETE FROM ResidentialTypology r WHERE r.project.id = :projectId") void deleteByProjectId(@Param("projectId") Long projectId)`

## Integração com Motor de Cálculo

- Os itens recebidos via `ResidentialStageRequest` são mapeados para `ResidentialTypologyItem` e passados ao `ResidentialCalculationEngine`.
- O motor resolve a demanda unitária $D_i$ por faixa de área útil (Quadro 35) e apura o total por tipologia ($D_i \times N$), além de $D_r$, $F_c$, $F_r$ e $D_{rf}$.
- As tipologias são salvas com `demandaUnitariaKva` e `demandaTotalTipologiaKva` preenchidas.
