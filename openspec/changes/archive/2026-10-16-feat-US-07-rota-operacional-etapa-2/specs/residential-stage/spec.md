# Especificação: Etapa 2 — Tipologias Residenciais (US-07)

## Contrato RESTful

### Endpoint
- Método: `PUT`
- Rota: `/api/v1/projects/{id}/etapa-residencial` (e `/projects/{id}/etapa-residencial`)
- Autenticação: Obrigatória via `Bearer <JWT>`

### Payload de Entrada (JSON)
```json
{
  "tipologias": [
    {
      "areaUtilM2": 65.00,
      "quantidadeUnidades": 50,
      "isSmartStudio": false
    },
    {
      "areaUtilM2": 78.00,
      "quantidadeUnidades": 50,
      "isSmartStudio": false
    }
  ]
}
```

### Payload de Resposta de Sucesso (200 OK)
```json
{
  "sucesso": true,
  "projectId": 1,
  "demandaResidencialFinalDrfKva": 122.72
}
```

## Regras de Negócio e Validação

1. **Validação de Entrada (400 Bad Request — RFC 7807)**:
   - `tipologias`: não nula e não vazia.
   - Cada item de `tipologias`:
     - `areaUtilM2`: obrigatório e estritamente positivo ($> 0$).
     - `quantidadeUnidades`: obrigatório e inteiro positivo ($> 0$).
     - `isSmartStudio`: booleano (default `false`).
2. **Autorização e Existência**:
   - `401 Unauthorized`: token ausente ou inválido.
   - `403 Forbidden`: usuário autenticado não é o proprietário do projeto.
   - `404 Not Found`: projeto com o ID especificado não encontrado.
3. **Idempotência**:
   - Toda invocação substitui integralmente a lista anterior de tipologias do projeto na tabela `residential_typologies`.
   - O backend gera automaticamente a identificação descritiva (`Tipologia <área>m²`) para persistência.
4. **Atualização de Progresso**:
   - O campo `etapaAtual` do projeto é atualizado para `Math.max(project.getEtapaAtual(), 2)`.
   - O campo `updatedAt` é preenchido com a data/hora corrente.
5. **Cálculo Normativo**:
   - A demanda unitária e total de cada tipologia, assim como a Demanda Residencial Final ($D_{rf}$), são calculadas pelo `ResidentialCalculationEngine`.
