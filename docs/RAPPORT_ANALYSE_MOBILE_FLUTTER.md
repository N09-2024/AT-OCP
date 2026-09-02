# ANALYSE DU PROJET POUR FLUTTER - Phase 1

> Rapport d'analyse du système existant (backend Spring Boot + frontend React) en vue du
> développement de l'application mobile Flutter de gestion des Autorisations de Travail (AT) OCP.
> **Aucun fichier du projet n'a été modifié.** Cette analyse est basée exclusivement sur le code actuel.

---

## A. Architecture actuelle

```
frontend/  React 19 + Vite + MUI (port 5173, proxy /api → 8080)
    ↘
backend/   Spring Boot 3 (port 8080, préfixe /api, stateless JWT)
    ↕
ai-service/ FastAPI Python (port 8000, provider MOCK | LANG_CHAIN | CREW_AI, Gemini)
    ↓
PostgreSQL at_ocp_db (Flyway, migrations V1→V21+)
Stockage fichiers : système de fichiers local (dossier uploads/), AUCUN MinIO/S3
```

- Backend : packages `controller`, `service/impl`, `repository`, `entity`, `dto/request|response`,
  `security`, `workflow`, `pdf`, `storage`, `notification`, `audit`, `ocr`.
- Sécurité stateless (aucune session serveur) : chaque requête porte `Authorization: Bearer <JWT>`.
- Upload multipart limité à **20 Mo / fichier, 25 Mo / requête**.
- `docker-compose` : dev / staging / prod + nginx reverse proxy.

## B. Entités disponibles (entity/)

**Cœur métier :**
| Entité | Rôle |
|---|---|
| `AutorisationTravail` | L'AT (table `autorisations_travail`) - objet central |
| `DemandeIntervention` (DI) | Intervention ponctuelle, source possible d'une AT |
| `OrdreTravail` (OT) | Intervention répétitive, source possible d'une AT |
| `BonTravail` (BT) | Contrat OCP ↔ entreprise externe, source possible d'une AT |
| `Visa` | Visa/signature formelle d'une AT (statut, ordre, PNG, hash, IP) |
| `Permis` / `PermisDocument` / `TypePermis` | Permis HSE rattachés à une AT / au BT |
| `ReceptionTravaux` (+ `PhotoReception`, `RemiseEtat`, `Essai`) | Réception conjointe de fin de travaux |
| `Reconduction` | Prolongation d'une AT (début de poste, >24 h) |
| `VisitePrealable` (+ `Photo`, `AnalyseRisque`, `AnalyseIA`) | Visite conjointe du chantier §8.2 |
| `HistoriqueAT` | Journal des actions d'une AT |
| `ClassificationIntervention` | Référentiel classification Niveau 1/2 |
| `ArchiveAT`, `AuditLog`, `WorkflowAT` | Archivage, audit, machine à états |

**Utilisateurs & sécurité :** `Utilisateur`, `Role`, `Permission`, `RefreshToken`, `Habilitation`.

**Référentiels :** `Zone`, `Service` (rattaché à une Zone), `Equipement`, `EntrepriseExterne`,
`Risque`, `MesurePreparation`, `EPI`, `MoyenAcces`.

**Champs clés de `AutorisationTravail` :** `id` (UUID), `numero` (unique, AT-2026-000001),
`objet`, `descriptionTravaux`, `dateDebut/dateFin/heureDebut/heureFin`, `statut` (legacy) +
`statutWorkflow` (9 étapes standard S-HSE-SEC-31), `etatVerrou` + `proprietaireBrouillon`
(verrou d'édition), `typeDocumentSource` (DI|OT|BT) + lien vers DI/OT/BT (un seul),
`zoneProprietaire` (P) / `zoneExecutante` (E), `ceee`, listes ManyToMany `risques`,
`mesures`, `epis`, `moyensAcces`, `visas`, `permis`, `historiques`, `reconductions`,
`receptionTravaux` (OneToOne), champs texte `servicesIntervenants`,
`entreprisesIntervenantes`, `mesuresSecuriteExecutant` (section F), champs dupliqués JSON
`formRisquesIds`, `formMesuresIds`, `formEpisIds`, `formMoyensIds`, `formPermisIds`.

> ⚠️ **"Installation" est bien SUPPRIMÉE.** Aucune entité, table, relation ni endpoint Installation
> n'existe. Résidus purement cosmétiques détectés (à signaler, à ne PAS reproduire côté mobile) :
> - `DashboardDataResponse.AtSummaryDto.installation` et `ArchiveResponse.installation` : simples
>   champs texte remplis avec `DI.equipement.nomEquipement` (DashboardServiceImpl:117, ArchiveServiceImpl:390) ;
> - booléen `installationRemiseEnEtat` de `ReceptionTravaux` (« remise en état de l'installation »,
>   notion réglementaire ≠ entité) ;
> - mentions « propriétaire de l'installation » dans les textes légaux du PDF.

## C. DTO principaux

- **Auth** : `LoginRequest{email, motDePasse}` → `JwtResponse{accessToken, refreshToken, type:"Bearer", utilisateur:UtilisateurResponse, roles:[String], permissions:[String]}` ;
  `TokenRefreshRequest{refreshToken}` → `TokenRefreshResponse{accessToken, refreshToken, type}`.
- **UtilisateurResponse** : id, matricule, nom, prenom, email, telephone, photo, actif,
  compteVerrouille, motDePasseExpire, enAttenteValidation, dateCreation, derniereConnexion,
  `service:ServiceResponse`, `roles:[RoleResponse]` (sans permissions).
- **AutorisationTravailResponse** : id, numero, version, objet, descriptionTravaux,
  dates/heures, statut, etatVerrou, dates verrou, proprietaireBrouillon(Id|NomComplet),
  zoneProprietaire(Id|Nom), zoneExecutante(Id|Nom), typeDocumentSource, documentSourceId,
  documentSourceNumero, servicesIntervenants, entreprisesIntervenantes,
  mesuresSecuriteExecutant, g1NomCeep, g1NomCeee, dateReceptionCeee, latitude, longitude,
  visiteCommentaire, visiteEffectuee, photoPath, risquesIds, mesuresIds, episIds,
  moyensAccesIds, permisIds, **exportPdfAutorise + exportPdfMotifsRefus** ( parfait pour
  conditionner le bouton PDF mobile).
- **AutoSaveRequest** (payload complet du brouillon) : objet, descriptionTravaux, dates/heures,
  servicesIntervenants, serviceIntervenantId, zoneProprietaireId|Nom, zoneExecutanteId|Nom,
  entreprisesIntervenantes, mesuresSecuriteExecutant, risquesIds, mesuresIds, episIds,
  moyensAccesIds, permisIds, typeDocumentSource, documentSourceId, documentSourceNumero,
  latitude, longitude, visiteCommentaire, visiteEffectuee, photoPath.
- **VisaResponse** : id, dateVisa, dateSignature, statut, commentaire, ordre,
  signaturePresente (bool - le hash n'est jamais exposé), adresseIP, utilisateurId,
  utilisateurNomComplet, autorisationTravailId.
- **NotificationResponse** : id, titre, message, dateCreation, dateLecture, lu, type, lien,
  utilisateurId.
- **DashboardDataResponse** : kpis{autorisationsEnCours, visasEnAttente, permisActifs,
  receptionsEnAttente, totalArchives}, monthlyStats, statusDistribution, recentAutorisations.
- **WorkflowResponse** : atId, atNumero, statutActuel, transitionsDisponibles[{action,
  etatArrivee, roleAutorise, obligatoire}] - exploitable tel quel pour l'UI mobile.
- **ReadinessCheckResponse / ClosureReadinessResponse / ArchiveReadinessResponse** : listes de
  pré-conditions avant démarrage/clôture/archivage.

## D. Endpoints REST (tous préfixés /api, authentifiés sauf mention contraire)

**Auth (`/api/auth`, public)** : `POST /login`, `POST /refresh-token`, `POST /logout`,
`POST /register`, `GET /me`.

**AT (`/api`)** :
- `POST /documents/{type}/{id}/creer-at` - créer l'AT depuis un DI/OT/BT ;
- `POST /documents/{type}/{id}/classifier?niveau=2` - Étape 0, permission CLASSIFY_INTERVENTION (HCEP) ;
- `POST /autorisations-travail` - création directe (brouillon vierge) ;
- `PUT /autorisations-travail/{id}/accuser-reception-ceee` ;
- `GET /autorisations-travail?statut=&search=&page=&size=&sort=` - liste paginée (Page<AutorisationTravailResponse>) ;
- `GET /autorisations-travail/{id}` - détail ;
- `PUT /autorisations-travail/{id}/autosave` - sauvegarde du brouillon ;
- `PUT .../{id}/prendre-verrou` | `liberer-verrou` | `transferer-verrou` - verrou d'édition ;
- Workflow : `POST .../{id}/submit` / `validate` / `reject {motif}` / `renew` / `close` /
  `demarrer-intervention` / `declarer-fin` / `visite` / `rediger` / `reconduire?depasse24h=` /
  `incident?motif=` / `reception-standard` ;
- `GET .../{id}/historique` - List<HistoriqueATResponse> ;
- `GET .../{id}/visas` - List<VisaResponse> ;
- `GET .../{id}/export-pdf` - **byte[] PDF** (permission EXPORT_PDF, conditions métier vérifiées).

**Cycle de vie (`/api/autorisations-travail/{id}`)** : `GET /intervention/readiness`,
`POST /intervention/start`, `POST /intervention/end`, `GET /archive/readiness`.

**Visas (`/api/visa`)** : `POST /` (créer, VisaRequest), `POST /{id}/sign`
(multipart `signature`=PNG + `commentaire`), `GET /at/{atId}`, `GET /{id}/signature` (PNG).

**Notifications (`/api/notifications`)** : `GET ?page=&size=` (Page), `GET /count-unread` →
`{count}`, `PUT /{id}/read`, `PUT /read-all`.

**Dashboard** : `GET /api/dashboard/stats`.

**Workflow** : `GET /api/workflow/{atId}` - transitions disponibles.

**Documents sources** : `/api/demandes-intervention`, `/api/ordres-travail`, `/api/bons-travail`
(CRUD complet chacune).

**Référentiels** : `/api/zones` (+ `/{id}/services`), `/api/services` (+ `/{id}/chefs-equipe`,
`/search`), `/api/equipements`, `/api/entreprises-externes`, `/api/risques`, `/api/mesures-preparation`,
`/api/epis`, `/api/moyens-acces` (tous : liste, détail, `/search`, CRUD), `/api/types-permis`,
`/api/classifications` (+ `/niveau/{niveau}`).

**Visites (`/api/visites-prealables`)** : CRUD, `PUT /{id}/finaliser`,
`POST /{id}/photos` (multipart `file`+`legende`), `DELETE /{id}/photos/{photoId}`.

**Permis (`/api/permis`)** : CRUD, `POST /{id}/upload` (multipart), `GET /{id}/download`,
`PUT /{id}/reanalyser`, `GET /{id}/analyse` ; `/api/permis-documents` : `POST /at/{atId}/initialiser`,
`POST /at/{atId}/upload`, `GET /at/{atId}`, `POST /{id}/relancer-analyse` (IA OCR).

**Réceptions (`/api/receptions`)** : CRUD, `PUT /{id}/signer`, `POST /{id}/evaluer-ceep`
(multipart), `GET /verification-cloture/{atId}`, `PUT /{id}/cloturer`,
`GET|POST /{id}/photos`, `DELETE /{id}/photos/{photoId}`.

**Reconductions** : `POST|GET /api/autorisations-travail/{atId}/reconductions`,
`GET /api/reconductions/pending`, `POST /api/reconductions/{id}/decider`.

**Archives (`/api/archives`)** : `POST /export/{atId}`, `POST /archive/{atId}`, `GET ""`,
`GET /{id}`, `GET /at/{atId}`, `GET /{atId}/versions`, `GET /{id}/download`,
`GET /{id}/verify`, `GET /search`.

**Divers** : `/api/users` (admin + `PUT /me/password`), `/api/roles`, `/api/permissions`,
`/api/settings`, `/api/audit-logs`, `/api/habilitations`, `/api/analyses-risques`,
`/api/verification/{numero}` (**public**, vérification QR), `/api/ia/*` (assistance IA),
`/actuator/health` (public), Swagger `/v3/api-docs` + `/swagger-ui`.

> Controllers vides (stubs `/api/s`) : `ATController`, `FileController`, `AuditController`,
> `HistoriqueATController` - la vraie logique vit dans les controllers listés ci-dessus.

## E. Authentification

- **Login** : `POST /api/auth/login` avec `{ "email": "...", "motDePasse": "..." }` (champs FR).
- **Réponse** : `{ accessToken, refreshToken, type: "Bearer", utilisateur: {...}, roles: ["CEEP", ...], permissions: ["READ_AT", ...] }`.
- **Refresh** : `POST /api/auth/refresh-token` `{ refreshToken }` → nouveaux access+refresh.
- **Logout** : `POST /api/auth/logout` `{ refreshToken }` (révocation côté serveur).
- **Profil** : `GET /api/auth/me`.
- JWT : header `Authorization: Bearer`, expiration **24 h**, refresh **7 j**
  (`app.jwt.expiration-ms=86400000`, `refresh-expiration-ms=604800000`).
- Compte verrouillé après 5 tentatives (`max-login-attempts: 5`).
- Réponses 401/403 : `AuthEntryPointJwt` / `RestAccessDeniedHandler` → JSON d'erreur exploitable.
- CORS ouvert (`*`) - compatible app mobile.
- **Comptes de test (DataInitializer)** : `admin@ocp.ma` / `Admin@123` (ADMIN) ; `ceep@`,
  `ceee@`, `hcep@`, `hcee@`, `hmep@`, `hmee@ocp.ma` / `Password123!`.
- ⚠️ Le client web React se déconnecte sur 401 sans tenter le refresh ; l'app mobile
  **doit implémenter le refresh + rejeu de requête** (l'endpoint existe).

## F. Rôles (11 en base, `roles.nom`)

`ADMIN`, `CEEP`, `CEEE`, `HCEP`, `HCEE`, `HMEP`, `HMEE` (7 rôles standard S-HSE-SEC-31) +
génériques `CE`, `HM`, `HC` + `RESPONSABLE_EXTERIEUR` (entreprise extérieure, BT/permis uniquement).

Principe **P/E contextuel** : la position Propriétaire/Exécutant dépend du territoire de l'AT
(comparaison `utilisateur.service.zone` ↔ `zoneProprietaire`/`zoneExecutante` de l'AT) -
résolue par `RoleUtils.userHasRolePattern` et `ATContextService`, jamais côté client.
Le DTO AT expose `zoneProprietaireNom` / `zoneExecutanteNom` pour l'affichage.

## G. Permissions (39, renvoyées dans JwtResponse.permissions)

READ_AT, CREATE_AT, EDIT_AT, SUBMIT_AT, VALIDATE_AT, REJECT_AT, CLOSE_AT,
CLASSIFY_INTERVENTION, CREATE_VISITE, VALIDATE_VISITE, SIGN_AT, START_INTERVENTION,
DECLARE_FIN_TRAVAUX, RECEIVE_AT, RENEW_AT, ARCHIVE_AT, VIEW_ARCHIVE, MANAGE_HABILITATIONS,
TRANSFER_AT, EXPORT_PDF, UPLOAD_FILES, VIEW_PERMIS, CREATE_PERMIS, EDIT_PERMIS, DELETE_PERMIS,
UPLOAD_PERMIS, ANALYSE_PERMIS, RECEIVE_NOTIFICATION, VIEW_RECEPTION, CREATE_RECEPTION,
EDIT_RECEPTION, SIGN_RECEPTION, DELETE_RECEPTION, MANAGE_USERS, MANAGE_ROLES,
MANAGE_REFERENTIALS, VIEW_AUDIT, MANAGE_DOCUMENTS.

Exemples de attribution : CEEP = création/soumission AT, visites, réception, permis ;
HCEE/HCEP = validation/rejet, classification, archivage ; CEEE = démarrage/fin intervention ;
HMEE intentionnellement fail-closed (lecture + signatures).

## H. Workflow AT

**Double statut** : `statut` (legacy) + `statutWorkflow` (9 étapes standard §7).
`StatutAT` (22 valeurs) : BROUILLON, DEMANDE_CREEE, CLASSIFICATION_EFFECTUEE, EN_VISITE_REDACTION,
VISITE_REALISEE, AT_REDIGEE, SOUMISE, VALIDEE, AT_VALIDEE, EN_COURS, INTERVENTION_EN_COURS,
EN_RECONDUCTION, AT_RECONDUITE, RENOUVELEE, DECLAREE_TERMINEE, FIN_TRAVAUX_DECLAREE, RECEPTIONEES,
TRAVAUX_RECEPTIONES, ARCHIVEE, REJETEE, ANNULEE, VALIDATION/SIGNATURE (via StatutVisa).

**Machine à états réelle** (`WorkflowATServiceImpl`) :
```
CLASSIFICATION_EFFECTUEE →CREATION_DEMANDE→ DEMANDE_CREEE
DEMANDE_CREEE →VISITE_CHANTIER→ EN_VISITE_REDACTION →VISITE_CHANTIER→ VISITE_REALISEE
              →REDACTION_AT/SOUMISSION→ AT_REDIGEE →VALIDATION→ AT_VALIDEE | →REFUS→ REJETEE
AT_VALIDEE →DEBUT_INTERVENTION→ EN_COURS →DECLARATION_FIN→ DECLAREE_TERMINEE
           →RECONDUCTION→ EN_RECONDUCTION →DEBUT_INTERVENTION→ EN_COURS
INTERVENTION_EN_COURS →RECONDUCTION→ AT_RECONDUITE ; →DECLARATION_FIN→ FIN_TRAVAUX_DECLAREE
DECLAREE_TERMINEE →RECEPTION_CONJOINTE/CLÔTURE→ RECEPTIONEES
FIN_TRAVAUX_DECLAREE →RECEPTION_CONJOINTE→ TRAVAUX_RECEPTIONES
RECEPTIONEES / TRAVAUX_RECEPTIONES →ARCHIVAGE_OFFICIEL→ ARCHIVEE
ANNULATION possible depuis la plupart des états ; incident → retour visite
```
Endpoint d'aide : `GET /api/workflow/{atId}` renvoie les transitions disponibles avec rôle autorisé.

**Niveaux** : `NiveauIntervention` = NIVEAU_1, NIVEAU_2 - fixés à l'Étape 0 par HCEP
(`classifier?niveau=`), via le référentiel `ClassificationIntervention`.

**Auto-save / verrou** : `PUT /{id}/autosave` (AutoSaveRequest complet) ; verrou
`etatVerrou` LIBRE/EN_COURS_EDITION avec `proprietaireBrouillon` - prendre/libérer/transférer
(TRANSFER_AT). Une AT verrouillée par un autre utilisateur ne doit pas être modifiable côté mobile.

## I. Référentiels (tous exposés en API)

Zone (code, nom, description) ← Service (code, nom, zone) ; Equipement ; EntrepriseExterne ;
Risque (nom, description, niveau) ; MesurePreparation ; EPI ; MoyenAcces ; TypePermis ;
ClassificationIntervention (par niveau). Chacun dispose de `GET /api/<ressource>` et
`GET /api/<ressource>/{id}` (+ `/search` pour la plupart). **Aucune donnée ne doit être codée
en dur dans Flutter.**

## J. Fichiers

- Stockage : **système de fichiers local** (`app.storage.location=uploads`), service
  `StorageServiceImpl` (path-traversal protégé). Pas de MinIO/S3. PostgreSQL ne stocke que
  les métadonnées (Photo, PermisDocument, Visa.signaturePath…).
- Photos de visite : `POST /api/visites-prealables/{id}/photos` (multipart `file`, `legende`).
- Photos de réception : `GET|POST|DELETE /api/receptions/{id}/photos`.
- Signature de visa : multipart `signature` (PNG) ; consultation `GET /api/visa/{id}/signature`.
- Permis : `POST /api/permis/{id}/upload` + `GET /{id}/download`.
- Limites : 20 Mo/fichier.

## K. Signatures & visas

- `Visa` : statut (EN_ATTENTE, VALIDE, REFUSE, VALIDATION, SIGNATURE), ordre, commentaire,
  dateVisa/dateSignature, `signaturePath` + `signatureHash` (jamais exposés en réponse -
  seul `signaturePresente` est renvoyé), adresseIP, navigateur, utilisateur, AT.
- Parcours mobile : lister `GET /api/visa/at/{atId}` → créer si besoin `POST /api/visa`
  (VisaRequest{autorisationTravailId, commentaire, ordre}) → signer
  `POST /api/visa/{id}/sign` (multipart PNG + commentaire) - permissions SIGN_AT/VALIDATE_AT.
- L'accusé de réception CEEE (`accuser-reception-ceee`) est un préalable à sa signature.
- Le hash + IP + user-agent enregistrés côté serveur servent à la non-répudiation.

## L. Notifications

Pull uniquement (pas de WebSocket/SSE) : `GET /api/notifications` (paginé),
`GET /api/notifications/count-unread` → `{count}`, `PUT /{id}/read`, `PUT /read-all`.
Le champ `lien` permet la navigation vers l'objet concerné. L'app mobile devra **poller**
le compteur (ex. toutes les 30–60 s) en phase 1.

## M. PDF officiel

- Généré **uniquement par le backend** (`PdfGeneratorServiceImpl`, formulaire F-HSE officiel,
  QR code, hash d'intégrité, archivage).
- `GET /api/autorisations-travail/{id}/export-pdf` → `byte[]` PDF (attachment,
  `numero_v{version}.pdf`), permission EXPORT_PDF.
- Conditions métier (`calculerMotifsRefusExportPdf`) : statut ≥ soumise (refus si BROUILLON,
  REJETEE, ANNULEE, DEMANDE_CREEE, EN_VISITE_REDACTION) **et** tous les permis obligatoires
  CONFORMES. Le DTO AT expose déjà `exportPdfAutorise` + `exportPdfMotifsRefus` : le mobile
  affiche/masque le bouton sur cette base, sans dupliquer la règle.
- Archives : `POST /api/archives/export/{atId}`, `GET /api/archives/{id}/download`,
  `GET /api/archives/{id}/verify`.

## N. Fonctionnalités mobiles réalisables avec l'existant

| Fonctionnalité demandée | Faisabilité | API |
|---|---|---|
| Auth + refresh + logout sécurisé | ✅ | /api/auth/* |
| Dashboard (KPIs, rôle, notifications) | ✅ | /api/dashboard/stats, /auth/me, /notifications/count-unread |
| Liste AT + recherche + filtre statut + pagination | ✅ | GET /autorisations-travail?statut=&search=&page= |
| Détail AT complet | ✅ | GET /autorisations-travail/{id} |
| Création (depuis DI/OT/BT ou directe) | ✅ | /documents/{type}/{id}/creer-at, POST /autorisations-travail |
| Édition par étapes + auto-save + verrou | ✅ | /autosave, /prendre-verrou, /liberer-verrou |
| Risques / Mesures / EPI / Moyens d'accès | ✅ | référentiels + IDs dans AutoSaveRequest |
| Visite préalable (GPS + photos + finalisation) | ✅ | /visites-prealables/* |
| Validation / refus / workflow complet | ✅ | /submit, /validate, /reject, /demarrer-intervention… |
| Visa + signature manuscrite PNG | ✅ | /api/visa/* |
| Historique | ✅ | GET /{id}/historique |
| Notifications + compteur non lues | ✅ | /api/notifications/* |
| PDF officiel (affichage/téléchargement) | ✅ | GET /{id}/export-pdf (byte[]) |
| Documents (permis) upload/consultation | ✅ | /api/permis/*, /api/permis-documents/* |
| Vérification QR publique | ✅ | /api/verification/{numero} (bonus) |

## O. APIs manquantes ou limitées pour le mobile

1. **Pas de filtre « Mes AT » / « AT à valider par moi »** côté serveur : `findAll` ne filtre
   que par `statut` + `search` (texte). Contournement phase 1 : filtrer côté client sur la
   page courante + utiliser `dashboard/stats` (visasEnAttente) et `GET /visa/at/{atId}`.
   → *Suggestion backend (optionnelle, plus tard)* : paramètres `mine=true` / `aValider=true`.
2. **Pas de temps réel** : notifications en polling uniquement (acceptable en phase 1).
3. **Pas de galerie photos générique « AT »** : les photos vivent sur les visites et les
   réceptions ; l'AT n'expose qu'un `photoPath` de visite. Le mobile consultera donc les
   photos via l'API visites (`/visites-prealables` du document source) et réceptions.
4. **`FichierJointService` sans controller exposé** (FileController vide) : pas d'upload de
   documents génériques rattachés à l'AT ; seuls les permis disposent d'upload/download.
5. **`GET /api/services/search`, zones…** : vérifier le nom du paramètre de recherche
   (`q` ou `nom`) au moment du branchement (Swagger disponible).
6. **Aucun endpoint d'annulation exposé** dans le controller AT (la transition ANNULEE existe
   dans la machine à états mais aucune route publique ne la déclenche) - ne pas inventer de
   bouton « Annuler » mobile.

## P. Architecture Flutter proposée

```
mobile/
├── lib/
│   ├── main.dart
│   ├── core/
│   │   ├── config/app_config.dart          # baseUrl dev/prod (--dart-define)
│   │   ├── network/api_client.dart         # Dio + intercepteurs JWT/refresh/401
│   │   ├── errors/                         # Failure, mapDioError (400→500, timeout, offline)
│   │   ├── storage/secure_token_storage.dart # flutter_secure_storage
│   │   ├── theme/app_theme.dart            # palette OCP (cf. §Q)
│   │   ├── utils/                          # dates (intl), statut→couleur
│   │   └── widgets/                        # statut_chip, at_card, states (loading/empty/error/retry)
│   ├── features/
│   │   ├── auth/          (data/domain/presentation : login, session)
│   │   ├── dashboard/
│   │   ├── at/            (liste, détail, formulaire Stepper 9 étapes, autosave, verrou, workflow)
│   │   ├── referentiels/  (zones, services, risques, mesures, EPI, moyens, types permis)
│   │   ├── visas/         (liste, signature manuscrite)
│   │   ├── photos/        (caméra/galerie, preview, upload)
│   │   ├── notifications/
│   │   ├── pdf/           (visualisation byte[])
│   │   └── profile/
│   └── routing/app_router.dart             # GoRouter + redirect auth
├── assets/ (logo, icônes)
└── pubspec.yaml
```

Packages : flutter_riverpod, dio, go_router, flutter_secure_storage, image_picker,
file_picker, signature, intl, connectivity_plus (+ cached_network_image si affichage
d'images distantes). Rien d'autre.

## Q. Plan d'implémentation & identité visuelle

**Phases** conformes à la demande : 1 analyse (ce rapport) → 2 initialisation Flutter →
3 auth (login/refresh/logout/me) → 4 dashboard → 5 liste/recherche/filtres/détail AT →
6 formulaire Stepper (sections réelles du formulaire : Nature & affectation services,
Description & planning, Visite préalable, A. Risques, B. Mesures, C. Moyens d'accès,
D. EPI, E. Permis complémentaires, F. Mesures exécutant, G. Validation & signature) →
7 référentiels → 8 auto-save/verrou → 9 photos/permis → 10 visas/signatures →
11 workflow/readiness → 12 notifications (polling) → 13 PDF → 14 tests → 15 optimisation.

**Thème (tokens extraits du frontend React)** : primary `#1F4D3E` (forest) / dark `#163C30`,
secondary `#3C7A5C` (moss) / `#2E624A`, accent mint `#7FC8A9`, fonds `#F7FAF8`/`#FFFFFF`,
texte `#16241E` / `#5C6E67`, bordures `#D6E3DC`, warning `#A87532`, error `#9A3D2F`,
success mintSoft `#E2F0E8`. Rayons 8–12. Fonts : Space Grotesk (titres) / Inter (corps).
Statuts : vert=soumise/validée, ambre=en cours/visite, rouge=rejetée/annulée,
gris=brouillon/archivée - à caler sur `statusDistribution` du dashboard.

---
*Rapport généré le 2026-08-23 - Phase 1 (analyse) du développement mobile Flutter.*
