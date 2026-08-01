# Rôles CE / HM / HC — Standard S-HSE-SEC-31

## Fonctions RH (fixes) vs position P/E (dynamique)

| Fonction RH | Côté Propriétaire (P) | Côté Exécutant (E) |
|-------------|----------------------|---------------------|
| **CE** Chef d'Équipe | **CEEP** — rédige AT, visite, visa terrain | **CEEE** — visa G, démarre, déclare fin |
| **HM** Haute Maîtrise | **HMEP** — garant terrain P | **HMEE** — garant terrain E |
| **HC** Hors Cadre | **HCEP** — classification N1/N2, garant archive, habilitations | **HCEE** — garant validation, archive |

La position **P ou E** se calcule : service de l'utilisateur vs `zoneProprietaire` / service intervenant de l'AT.

## Qui fait quoi sur le formulaire F-HSE-SEC-31-04

| Étape | Acteur principal | Garant (HM / HC) |
|-------|------------------|------------------|
| Classification N2 | **HCEP** | — |
| Demande / rédaction A–F | **CEEP** | HCEE / HMEP selon §8 |
| Visite chantier | **CEEP** (E), CEEE (P) | **HCEE + HMEP** (G) |
| Section G Visa CEEP | **CEEP** | — |
| Section G Visa CEEE | **CEEE** | — |
| Validation / garantie dossier | **HCEE** (souvent) | **HMEP / HMEE** co-garant |
| Début intervention | **CEEE** | HCEE / HMEP (G) |
| Fin travaux | **CEEE** | CEEP informé |
| Réception | **CEEP + CEEE** | — |
| Archive | **HCEE** (E) | **HCEP** (G) |

## Parcours UI

- **CEEP** : formulaire complet → Visa CEEP → Soumettre
- **CEEE** : AT soumises → même formulaire → Visa CEEE
- **HCEE / HMEP / HMEE** : AT soumises → page validation / garantie
- **HCEP** : documents → classification Niveau 1/2 ; archives ; habilitations
