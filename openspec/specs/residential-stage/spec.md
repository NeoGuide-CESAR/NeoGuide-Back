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
      "nome": "Tipo A (2Q)",
      "catalogoFaixaId": 6,
      "areaUtilM2": 65.00,
      "quantidadeUnidades": 50,
      "isSmartStudio": false
    },
    {
      "nome": "Tipo B (3Q)",
      "catalogoFaixaId": 8,
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
  "etapaAtual": 2,
  "totalApartamentos": 100,
  "demandaResidencialBrutaDrKva": 163.50,
  "fatorCoincidenciaFc": 0.6823,
  "fatorSegurancaFr": 1.10,
  "demandaResidencialFinalDrfKva": 122.72
}
```

## Regras de Negócio e Validação

1. **Validação de Entrada (400 Bad Request — RFC 7807)**:
   - `tipologias`: não nula e não vazia.
   - Cada item de `tipologias`:
     - `nome`: obrigatório (não nulo e não em branco).
     - `areaUtilM2`: obrigatório e estritamente positivo ($> 0$).
     - `quantidadeUnidades`: obrigatório e inteiro positivo ($> 0$).
2. **Autorização e Existência**:
   - `401 Unauthorized`: token ausente ou inválido.
   - `403 Forbidden`: usuário autenticado não é o proprietário do projeto.
   - `404 Not Found`: projeto com o ID especificado não encontrado.
3. **Idempotência**:
   - Toda invocação substitui integralmente a lista anterior de tipologias do projeto na tabela `residential_typologies`.
4. **Atualização de Progresso**:
   - O campo `etapaAtual` do projeto é atualizado para `Math.max(project.getEtapaAtual(), 2)`.
   - O campo `updatedAt` é preenchido com a data/hora corrente.
5. **Cálculo Normativo**:
   - A demanda unitária e total de cada tipologia, assim como os totais e fatores ($D_r$, $F_c$, $F_r$, $D_{rf}$), são calculados pelo `ResidentialCalculationEngine`.
