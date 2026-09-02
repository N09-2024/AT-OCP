# OCP AT Mobile - Application Flutter

Application mobile de gestion des Autorisations de Travail (AT) et permis HSE OCP.
Elle consomme **exclusivement** les API REST du backend Spring Boot existant
(`../backend`) - aucune logique métier dupliquée côté mobile.

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

Prérequis : SDK Flutter **3.47+** installé (`flutter doctor`), Android Studio (SDK Android 36, licences acceptées).

```bash
cd mobile
flutter pub get
flutter analyze          # 0 erreur attendue
flutter test             # suites modèles / logique / auth / widgets
```

## A. Lancer sur l'émulateur Android Studio (développement)

1. Démarrer le backend (local ou Docker) : `docker-compose up -d backend postgres` (ou `mvn spring-boot:run` dans `backend/`).
2. Ouvrir **Android Studio → Device Manager → ▶** pour démarrer un émulateur (API 34+ recommandé).
3. Depuis `mobile/` :

```bash
flutter run --dart-define=ENV=dev
```

- L'émulateur joint le backend via **`http://10.0.2.2:8080`** (10.0.2.2 = localhost de l'hôte), URL par défaut de `ENV=dev`.
- HTTP en clair autorisé **en debug/profile uniquement** (manifest `debug/`), pas en release.
- Hot reload inclus ; comptes de test ci-dessous.

## B. Générer l'APK dans Docker (sans SDK Android local)

Le `Dockerfile` multi-stage compile l'APK release dans un conteneur (Flutter 3.47 + JDK 21 + SDK Android 36) :

```bash
# Depuis la racine du projet :
make build-apk
# équivalent à :
docker-compose -f docker-compose.mobile-build.yml run --rm apk-builder
```

L'APK est exporté dans **`mobile/output/app-release.apk`**.

Variables utiles (avant la commande) :
```bash
ENV=dev API_BASE_URL=http://10.0.2.2:8080 make build-apk   # APK pointant vers le backend local
ENV=prod make build-apk                                     # URL de production
```

Notes :
- Sans `key.properties`, l'APK release est signé avec le keystore debug (installable en test/CI). Pour la production : `mobile/android/app/key.properties` + keystore dédié.
- Le contexte Docker exclut build/, secrets (`.dockerignore`).

## C. Installer l'APK sur un appareil / émulateur

```bash
# Émulateur lancé ou appareil branché (débogage USB activé) :
adb install mobile/output/app-release.apk

# Backend joignable depuis un appareil PHYSIQUE : utiliser l'IP LAN de l'hôte
ENV=dev API_BASE_URL=http://192.168.x.x:8080 make build-apk
```

## D. Notifications ciblées par rôle de l'AT

Conformément au standard S-HSE-SEC-31 (logique P/E contextuelle), les notifications
métier sont désormais adressées **aux acteurs de l'AT concernée, pas à tous les
porteurs du rôle** :

| Rôle | Destinataires réels |
|---|---|
| CEEP, HCEP, HMEP (côté P) | Utilisateurs actifs de ce rôle dont le service est rattaché à la **zone propriétaire** de l'AT |
| CEEE, HCEE, HMEE (côté E) | Zone **exécutante** de l'AT |
| CEEP / CEEE désignés | Toujours inclus s'ils portent le rôle (même service sur autre zone) |
| ADMIN, RESPONSABLE_EXTERIEUR | Diffusion globale par rôle (hors logique P/E) |
| Aucun porteur du rôle sur la zone | **Aucune notification** (isolation garantie - pas de repli global) |

Implémentation backend : `NotificationService.sendNotificationToRoleForAt(role, at, ...)`
(`UtilisateurRepository.findActiveByRoleNomAndZoneId`) - 16 points d'émission migrés
(soumission, validation/rejet, démarrage, fin travaux, reconduction, incident,
réception, visas HCEP/HCEE/HMEP/HMEE). Un HCEP ne reçoit donc jamais les
notifications destinées au HCEE d'une même AT, ni celles d'autres AT.
L'app mobile n'a rien à changer : elle affiche les notifications de l'utilisateur connecté.

## Environnements

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

- `POST /api/auth/login` - `{email, motDePasse}` → tokens + utilisateur + rôles + permissions
- `POST /api/auth/refresh-token`, `POST /api/auth/logout`, `GET /api/auth/me`
- `GET /api/autorisations-travail?statut=&search=&page=&size=` (paginé)
- `GET /api/autorisations-travail/{id}` (détail, `exportPdfAutorise`, verrou)
- `GET /api/dashboard/stats`, `GET /api/notifications/count-unread`, etc.

Rapport complet : `docs/RAPPORT_ANALYSE_MOBILE_FLUTTER.md`.

## Phases

- [x] Phase 1 - Analyse du projet (rapport A→Q)
- [x] Phase 2 - Initialisation Flutter (structure, thème, réseau, navigation, écrans de base)
- [x] Phase 3 - Authentification (login/refresh/logout/restauration de session)
- [x] Phase 4 - Dashboard (KPIs réels + accès rapides par permission)
- [x] Phase 5 - Liste / recherche / filtres / détail AT (+ historique, visas, PDF)
- [x] Phase 6 - Formulaire AT mobile en 9 étapes (Stepper)
- [x] Phase 7 - Référentiels réels (zones P/E, risques, mesures, EPI, moyens d'accès, permis)
- [x] Phase 8 - Auto-save (diff + debounce + anti-requêtes simultanées) et verrou d'édition
- [x] Phase 9 - Photos (visite & réception : caméra/galerie, légende, suppression)
      et permis complémentaires (création, upload multipart PDF/image, analyse IA)
- [x] Phase 10 - Signature manuscrite des visas (PNG → multipart /visa/{id}/sign),
      accusé de réception CEEE
- [x] Phase 11 - Workflow complet selon statut réel : visite, rédaction, soumission,
      validation/rejet avec motif, démarrage intervention, fin travaux, reconduction
      (>24 h), incident, réception conjointe, clôture
- [x] Phase 12 - Bandeau hors-ligne (connectivity_plus, saisies conservées),
      navigation depuis les notifications (champ `lien`), tests logique/modèles
      (payload AutoSaveRequest, mapping erreurs, dates, routes)
- [x] Suite - interface TokenStorage testable, tests AuthController & widgets,
      icône placeholder + config flutter_launcher_icons/flutter_native_splash
- [ ] À faire - remplacer l'icône placeholder par la version définitive OCP puis
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
