# Architecture AT OCP + IA (S-HSE-SEC-31 / F-HSE-SEC-31-04)

## Processus métier (source de vérité)

1. **Formulaire unique** F-HSE-SEC-31-04 (sections A–G + réception)
2. **Workflow** = logigramme standard §7–§8
3. **Visas humains** CEEP puis CEEE sur la section G (même document)
4. **P/E** calculés selon service/zone (`ATContextService`)

## IA (assistance uniquement)

```
React formulaire
  → Spring Boot /api/ia/*
      → IAProvider (@Primary = MockAIProvider)
          → optionnel FastAPI + LangChain + CrewAI (ocp.ai.fastapi-url)
```

| Endpoint | Rôle |
|----------|------|
| `POST /api/ia/analyser-intervention` | Suggestions A, B, D, E |
| `POST /api/ia/controler-dossier` | Alertes avant soumission CEEP |

**L’IA ne signe jamais et ne change jamais le statut de l’AT.**

## Parcours utilisateurs

| Rôle | Action |
|------|--------|
| CEEP | Remplit formulaire → Analyse IA (optionnel) → Visa CEEP → Soumettre |
| CEEE | Menu « AT à viser » → même formulaire `?mode=viser` → Visa CEEE |
| HCEE | Garantie / validation selon permissions |

## Providers

- `MockAIProvider` : mots-clés (hauteur, acide, feu…) — PFE offline
- `LangChainProvider` / `CrewAIProvider` : HTTP vers FastAPI si `ocp.ai.fastapi-url` défini

## Migrations utiles

- V26 : transitions workflow standard
- V27 : suppression CHECK historiques_at trop stricts
