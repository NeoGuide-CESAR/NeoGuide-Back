# Design: normative-tables-dataseeder

## Arquitetura e Decisões de Design

### 1. Entidades JPA Normativas (`neoguide.project.model.normativo`)
Todas as entidades representam fielmente os esquemas descritos em `02-tabelas-normativas-mapeadas.md` e `03-modelagem-dados-unificada.md`:
1. `AreaRangeReference` (`catalogo_faixas_area` - Quadro 35)
2. `CoincidenceFactorReference` (`catalogo_fatores_coincidencia` - Quadro 36)
3. `SafetyFactorReference` (`catalogo_fatores_seguranca` - Quadro 37)
4. `VeFactorReference` (`catalogo_fatores_ve` - Quadro 33)
5. `ServiceEntranceReference` (`catalogo_dimensionamento_entrada` - Tabelas 1 e 2)
6. `BusbarReference` (`catalogo_barramentos_qdg` - Tabela 3)
7. `MinimumOutletsReference` (`catalogo_tomadas_minimas` - Tabela 5)
8. `LightingResidentialDemandFactor` (`catalogo_fatores_iluminacao_res` - Tabela 6)
9. `WaterHeaterInstantFactor` (`catalogo_fatores_aquecimento_passagem` - Tabela 7)
10. `BoilerDemandFactor` (`catalogo_fatores_boilers` - Tabela 8)
11. `AppliancesDemandFactor` (`catalogo_fatores_eletrodomesticos` - Tabela 9)
12. `CooktopDemandFactor` (`catalogo_fatores_cooktops` - Tabela 10)
13. `AirConditionerCatalog` (`catalogo_aparelhos_ar_condicionado` - Tabela 11)
14. `AcDemandFactor` (`catalogo_fatores_ar_condicionado` - Tabela 12)
15. `MotorDemandFactor` (`catalogo_fatores_motores` - Tabela 14)
16. `SpecialEquipmentDemandFactor` (`catalogo_fatores_especiais` - Tabela 15)
17. `PoolPumpDemandFactor` (`catalogo_fatores_piscina` - Tabela 16)
18. `MotorStarterReference` (`catalogo_dispositivos_partida` - Tabela 17)
19. `SinglePhaseMotorReference` (`catalogo_motores_monofasicos` - Tabela 18)
20. `MotorReference` (`catalogo_motores` - Tabela 19)
21. `OccupancyLoadReference` (`catalogo_cargas_ocupacao` - Tabela 22)

### 2. Diretriz Estrita de Código (Java 21 Puro)
Nenhum uso de Lombok (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, etc.) é permitido. Todas as entidades conterão construtores explícitos (padrão sem argumentos para Hibernate e parametrizado completo) e métodos getters/setters explícitos.

### 3. Estratégia de Povoamento (DataSeeder Modular)
Para mitigar o risco de classes excessivamente extensas e garantir organização limpa:
- Um coordenador de seed normativo `NormativeDataSeeder` organiza a execução por temas:
  - `ResidencialNormativoSeeder` (Quadro 35, 36, 37, Tabela 5, Tabela 6)
  - `EntradaBarramentoNormativoSeeder` (Tabelas 1, 2, 3)
  - `EquipamentosClimaCargasNormativoSeeder` (Tabelas 7, 8, 9, 10, 11, 12, 16, 22)
  - `MotoresPartidasNormativoSeeder` (Tabelas 14, 15, 17, 18, 19)
  - `VeNormativoSeeder` (Quadro 33)
- Cada bloco valida `repository.count() == 0` antes de executar `saveAll()`, garantindo idempotência total.
- `DataSeeder` orquestra a chamada de `normativeDataSeeder.seedAll()`.

### 4. Validação e Testes
- Teste de integração `@SpringBootTest` verificando contagens exatas esperadas de registros em cada tabela normativa.
- Teste de idempotência chamando explicitamente a rotina de carga duas vezes consecutivas para certificar a invariabilidade da contagem.
