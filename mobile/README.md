# OCP AT Mobile — Application Flutter

Application mobile de gestion des Autorisations de Travail (AT) et permis HSE OCP.
Elle consomme **exclusivement** les API REST du backend Spring Boot existant
(`../backend`) — aucune logique métier dupliquée côté mobile.

> ⚠️ La notion « Installation » a été supprimée du projet : elle ne doit jamais
> être réintroduite dans aucun modèle, écran ou filtre mobile.

## Architecture

```
mobile/lib/
├── main.dart
├── core/
│   ├── config/app_config.dart      # baseUrl par environnement (--dart-define)
│   ├── errors/                     # Failure + mapping Dio → messages utilisateur
│   ├── network/api_client.dart     # Dio + JWT + refresh 401 + rejeu anti-boucle
│   ├── storage/                    # flutter_secure_storage (tokens + session)
│   ├── theme/                      # Palette OCP + Material 3
│   ├── utils/app_date.dart         # Formats de dates
│   └── widgets/                    # AppShell, AtCard, StatutChip, états UI
├── features/
│   ├── auth/       # login, session, rôles/permissions
│   ├── at/         # liste, détail, (formulaire, workflow à venir)
│   ├── dashboard/  # KPIs réels /dashboard/stats
│   ├── notifications/
│   ├── referentiels/
│   ├── visas/
│   ├── photos/
│   ├── pdf/
│   └── profile/
└── routing/app_router.dart         # GoRouter + redirect auth
```

## Démarrage

Le SDK Flutter n'est pas encore installé sur ce poste. Après installation :

```bash
cd mobile

# 1. Générer les dossiers de plateforme manquants (android/, ios/...) :
flutter create --org com.ocp.at --project-name ocp_at_mobile .

# 2. Récupérer les dépendances :
flutter pub get

# 3. Placer les polices (Inter, Space Grotesk) dans assets/fonts/
#    (sinon commenter la section fonts de pubspec.yaml)

# 4. Lancer (émulateur Android → 10.0.2.2 = localhost de l'hôte) :
flutter run --dart-define=ENV=dev
```

## Intégration Docker & Émulateur Android Studio

### 1. Développement local avec l'Émulateur Android Studio
Lorsque le backend tourne dans Docker (`docker-compose up -d backend postgres`), vous pouvez exécuter l'application mobile depuis Android Studio ou le terminal :
- L'émulateur Android résout automatiquement le backend Docker via **`http://10.0.2.2:8080`** (configuré par défaut avec `--dart-define=ENV=dev`).
- Aucune configuration réseau complexe n'est nécessaire.

### 2. Build de l'APK Release dans Docker (sans installer Flutter/Android SDK localement)
Pour générer l'APK release dans un conteneur et l'exporter directement dans `./mobile/output/` :
```bash
# Via le Makefile à la racine :
make build-apk

# Ou via Docker Compose directement :
docker-compose -f docker-compose.mobile-build.yml run --rm apk-builder
```
Le fichier `app-release.apk` sera généré dans `mobile/output/`.

### 3. Exécution Flutter Web dans Docker
Pour compiler et servir la version Web du mobile via Nginx :
```bash
make mobile-web
# Accessible sur http://localhost:3000
```


### Environnements

| ENV | baseUrl par défaut | Utilisation |
|---|---|---|
| `dev` | `http://10.0.2.2:8080` | backend local, émulateur Android |
| `staging` | `https://staging-at-ocp.ocp.ma` | recette |
| `prod` | `https://at-ocp.ocp.ma` | production |

Override ponctuel : `--dart-define=API_BASE_URL=http://192.168.x.x:8080`.

### Comptes de test (backend local, DataInitializer)

| Email | Mot de passe | Rôle |
|---|---|---|
| admin@ocp.ma | Admin@123 | ADMIN |
| ceep@ocp.ma | Password123! | CEEP |
| ceee@ocp.ma | Password123! | CEEE |
| hcep@ocp.ma | Password123! | HCEP |
| hcee@ocp.ma | Password123! | HCEE |
| hmep@ocp.ma | Password123! | HMEP |
| hmee@ocp.ma | Password123! | HMEE |

## Endpoints consommés (source de vérité : backend)

- `POST /api/auth/login` — `{email, motDePasse}` → tokens + utilisateur + rôles + permissions
- `POST /api/auth/refresh-token`, `POST /api/auth/logout`, `GET /api/auth/me`
- `GET /api/autorisations-travail?statut=&search=&page=&size=` (paginé)
- `GET /api/autorisations-travail/{id}` (détail, `exportPdfAutorise`, verrou)
- `GET /api/dashboard/stats`, `GET /api/notifications/count-unread`, etc.

Rapport complet : `docs/RAPPORT_ANALYSE_MOBILE_FLUTTER.md`.

## Phases

- [x] Phase 1 — Analyse du projet (rapport A→Q)
- [x] Phase 2 — Initialisation Flutter (structure, thème, réseau, navigation, écrans de base)
- [x] Phase 3 — Authentification (login/refresh/logout/restauration de session)
- [x] Phase 4 — Dashboard (KPIs réels + accès rapides par permission)
- [x] Phase 5 — Liste / recherche / filtres / détail AT (+ historique, visas, PDF)
- [x] Phase 6 — Formulaire AT mobile en 9 étapes (Stepper)
- [x] Phase 7 — Référentiels réels (zones P/E, risques, mesures, EPI, moyens d'accès, permis)
- [x] Phase 8 — Auto-save (diff + debounce + anti-requêtes simultanées) et verrou d'édition
- [x] Phase 9 — Photos (visite & réception : caméra/galerie, légende, suppression)
      et permis complémentaires (création, upload multipart PDF/image, analyse IA)
- [x] Phase 10 — Signature manuscrite des visas (PNG → multipart /visa/{id}/sign),
      accusé de réception CEEE
- [x] Phase 11 — Workflow complet selon statut réel : visite, rédaction, soumission,
      validation/rejet avec motif, démarrage intervention, fin travaux, reconduction
      (>24 h), incident, réception conjointe, clôture
- [x] Phase 12 — Bandeau hors-ligne (connectivity_plus, saisies conservées),
      navigation depuis les notifications (champ `lien`), tests logique/modèles
      (payload AutoSaveRequest, mapping erreurs, dates, routes)
- [x] Suite — interface TokenStorage testable, tests AuthController & widgets,
      icône placeholder + config flutter_launcher_icons/flutter_native_splash
- [ ] À faire — remplacer l'icône placeholder par la version définitive OCP puis
      `dart run flutter_launcher_icons && dart run flutter_native_splash:create`,
      build release (keystore signé), tests providers supplémentaires

### Icône & splash

`assets/images/app_icon.png` est un **placeholder** (bouclier OCP simplifié généré
par script). Pour la version définitive :

```bash
# 1. Remplacer le PNG (1024×1024, sans transparence pour iOS)
# 2. Générer toutes les tailles Android/iOS :
dart run flutter_launcher_icons
# 3. Splash natif Android 12+ / iOS :
dart run flutter_native_splash:create
```
