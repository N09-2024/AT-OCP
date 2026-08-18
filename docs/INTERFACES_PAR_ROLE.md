# Interfaces par rôle applicatif - AT-OCP
## Standard S-HSE-SEC-31 v1.0

> Rôles applicatifs : **CE · HM · HC · ADMIN · RESPONSABLE_EXTERIEUR**  
> Positions contextuelles P/E résolues à l’exécution via `ATContextService`.

---

## 1. Routing frontend

```
/login
/dashboard/ce          → CEDashboardPage
/dashboard/hm          → HMDashboardPage
/dashboard/hc          → HCDashboardPage
/dashboard/admin       → AdminDashboardPage
/dashboard/externe     → ResponsableExterieurDashboardPage

/autorisations/...
/visites/...
/visas/...
/receptions/...
/archives/...
/administration/...
/bt/...                (RESPONSABLE_EXTERIEUR)
```

Après login :
1. Lire les rôles de l’utilisateur.
2. Priorité de redirection : `ADMIN` > `HC` > `HM` > `CE` > `RESPONSABLE_EXTERIEUR`.
3. Si plusieurs rôles métier (ex. CE + HC), afficher un sélecteur de contexte ou un dashboard combiné.

---

## 2. Dashboard CE (`/dashboard/ce`)

### Cartes principales
| Carte | Filtre | Action principale |
|-------|--------|-------------------|
| AT Propriétaire à rédiger | position P + statut ∈ {DEMANDE_CREEE, VISITE_REALISEE, BROUILLON} | Ouvrir formulaire F-HSE-SEC-31-04 |
| AT Propriétaire à réceptionner | position P + statut = FIN_TRAVAUX_DECLAREE | Lancer réception |
| AT Exécutant à viser | position E + statut ∈ {SOUMISE, AT_REDIGEE} | Signer visa CEEE |
| AT Exécutant à démarrer | position E + statut = AT_REDIGEE | Démarrer intervention |
| Interventions en cours (E) | position E + statut = INTERVENTION_EN_COURS | Déclarer fin / signaler incident |
| AT à reconduire | position P ou E + dépassement poste | Viser reconduction |

### Boutons globaux
- **Nouvelle demande d’intervention** (crée DI/OT/BT ou AT directe) - réservé position P possible
- Voir toutes mes AT (P + E)

### Actions autorisées (backend + UI)
| Action | Condition |
|--------|-----------|
| Créer demande / AT | rôle CE |
| Visite chantier | CE + (P exécute ou E participe) |
| Rédiger AT | CE + position P |
| Signer / viser | CE + (P ou E) |
| Démarrer intervention | CE + position E |
| Déclarer fin travaux | CE + position E |
| Réceptionner | CE + position P |
| Reconduire | CE + (P exécute ou E participe) |

---

## 3. Dashboard HM (`/dashboard/hm`)

### Cartes
| Carte | Filtre | Action |
|-------|--------|--------|
| Visites à garantir | position P + statut = VISITE_REALISEE (ou en attente garantie) | Garantir visite |
| Démarrages à cautionner | position P + statut = AT_REDIGEE | Garantir démarrage |
| Consultation périmètre | toutes AT de ma zone | Lecture seule |

### Règle fail-closed
- Position **E** (HMEE) : **aucune action d’écriture** - lecture seule uniquement.

---

## 4. Dashboard HC (`/dashboard/hc`)

### Cartes
| Carte | Filtre | Action |
|-------|--------|--------|
| Interventions à classifier | nouvelles demandes non classées | Classifier Niv 1 / Niv 2 |
| AT en attente de garantie | position E (HCEE) - visite / rédaction / démarrage / visa | Garantir / valider / rejeter |
| AT à archiver | statut = TRAVAUX_RECEPTIONES | Archiver (garant P / exécute E) |
| Agents habilités | - | Gérer liste F-HSE-SEC-31-02 |
| Registre Niveau 1 | - | Gérer F-HSE-SEC-31-01 |

### Actions
| Action | Condition |
|--------|-----------|
| Classifier | HC (position P / HCEP) |
| Garantir visite / rédaction / démarrage / visa | HC + position E (HCEE) |
| Archiver | HC (P garant, E exécute) |
| Gérer habilitations | HC |
| Liste interventions Niv 1 | HC |

---

## 5. Dashboard ADMIN (`/dashboard/admin`)

- KPI globaux (AT par statut, par zone, délais)
- Accès complet aux modules
- Administration : utilisateurs, rôles, services, zones, référentiels
- Audit / logs

---

## 6. Dashboard RESPONSABLE_EXTERIEUR (`/dashboard/externe`)

### Cartes
| Carte | Action |
|-------|--------|
| Mes Bons de Travaux | Créer / consulter BT |
| Permis à uploader | Upload PDF permis liés aux BT |
| AT liées à mes BT | Consultation seule |

### Interdit
- Création / rédaction / validation d’AT
- Visite, démarrage, réception, archivage

---

## 7. Composants React à créer / adapter

```
frontend/src/modules/dashboard/pages/
  CEDashboardPage.tsx
  HMDashboardPage.tsx
  HCDashboardPage.tsx
  AdminDashboardPage.tsx
  ResponsableExterieurDashboardPage.tsx

frontend/src/hooks/
  useATContext.ts          // appelle API position P/E pour une AT
  usePrimaryRole.ts        // rôle principal pour redirection

frontend/src/routes/index.tsx
  // guards : RequireRole(['CE']), RequireRole(['HC']), ...
```

### API utile
```
GET /api/autorisations-travail/{id}/context
→ { position: 'PROPRIETAIRE' | 'EXECUTANT' | 'HORS_PERIMETRE', roleApplicatif: 'CE'|... }
```

---

## 8. Priorité d’implémentation frontend

1. `usePrimaryRole` + redirection post-login
2. 5 pages dashboard (structure + cartes vides branchées API)
3. Guards de routes par rôle
4. Affichage conditionnel des boutons d’action selon `position` (P/E)
5. Remplacer les anciens dashboards Demandeur / ResponsableOcp / ResponsableEntreprise

---

*Document généré pour l’implémentation parallèle backend (V28) + frontend.*
