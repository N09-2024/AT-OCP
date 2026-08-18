# WORKFLOW_STANDARD.md
## Workflow Logiciel - Extrait du Logigramme Officiel S-HSE-SEC-31

> **Source :** Logigramme §7 + Règles de Gestion §8 du Standard OCP S-HSE-SEC-31 v1.0
> **Implémentation :** `WorkflowATServiceImpl` + migration `V26__standard_workflow_transitions.sql`

## Chaîne des statuts (`statutWorkflow`)

```
CLASSIFICATION_EFFECTUEE → DEMANDE_CREEE → VISITE_REALISEE → AT_REDIGEE
→ INTERVENTION_EN_COURS ⇄ AT_RECONDUITE → FIN_TRAVAUX_DECLAREE
→ TRAVAUX_RECEPTIONES → ARCHIVEE
```

### Guards

| Transition | Condition |
|-----------|-----------|
| Visite → Rédaction | Actions de prévention en place |
| Rédaction → Début | AT signée + permis validés |
| Intervention → Reconduction | Dépassement d'un poste |
| Reconduction → Visite | Dépassement 24 h |
| Incident | Retour `VISITE_REALISEE` |
| Fin → Réception | Essais concluants |
| Réception → Archive | Documents classés (≥ 1 an) |

### Endpoints API

| Étape | Méthode | Path |
|-------|---------|------|
| 2 Visite | POST | `/api/autorisations-travail/{id}/visite` |
| 3 Rédaction | POST | `/api/autorisations-travail/{id}/rediger` |
| 4 Début | POST | `/api/autorisations-travail/{id}/demarrer-intervention` |
| 5b Reconduction | POST | `/api/autorisations-travail/{id}/reconduire?depasse24h=` |
| Incident | POST | `/api/autorisations-travail/{id}/incident` |
| 6 Fin | POST | `/api/autorisations-travail/{id}/declarer-fin` |
| 7 Réception | POST | `/api/autorisations-travail/{id}/reception-standard` |
| 8 Archive | POST | `/api/archives/archive/{atId}` |

Voir le document complet fourni pour le détail acteurs E/P/I/G.
