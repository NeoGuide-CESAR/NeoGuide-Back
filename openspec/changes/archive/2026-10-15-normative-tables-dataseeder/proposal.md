# Proposal: normative-tables-dataseeder

## Motivação e Contexto
A plataforma NeoGuide necessita que as 21 tabelas normativas da concessionária Neoenergia Pernambuco (normas DIS-NOR-053 e DIS-NOR-030) estejam devidamente indexadas e persistidas no banco de dados. Estas tabelas constituem a base matemática de todos os cálculos de demanda e abastecem diretamente o catálogo normativo do frontend (seletores inteligentes, autocompletes, chips rápidos e validações reativas).

## Escopo
- Modelagem de 21 entidades JPA normativas sob `neoguide.project.model.normativo.*` sem o uso de Lombok (Java 21 POJOs puros).
- Criação dos 21 repositories Spring Data JPA sob `neoguide.project.repository.normativo.*`.
- Criação de seeders temáticos e modulares integrados ao `DataSeeder.java` com garantia rigorosa de idempotência (`count() == 0`).
- Implementação de testes automatizados com `@SpringBootTest` (`NormativeDataSeederTest.java`) certificando a carga correta e idempotente das tabelas normativas.

## Critérios de Sucesso
- As 21 tabelas oficiais DIS-NOR-053 e DIS-NOR-030 são criadas e povoadas no banco com 100% de exatidão de dados.
- O processo de inicialização e seed é completamente idempotente.
- A suíte de testes passa com 100% de sucesso sem quebrar classes ou testes existentes.
