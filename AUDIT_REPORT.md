# AUDIT TECHNIQUE COMPLET - AT-OCP

**Date**: 2026-07-11  
**Auditeur**: OpenHands AI Agent  
**Version du projet**: 1.0.0  
**Technologies**: Java 17, Spring Boot 3, PostgreSQL, Flyway, MapStruct

---

## 1. RÉSUMÉ EXÉCUTIF

Le projet AT-OCP (Système Intelligent de Gestion des Autorisations de Travail) présente plusieurs problèmes critiques qui doivent être résolus avant la mise en production. Le code compile et les tests passent, mais des incohérences architecturales et des problèmes de sécurité ont été identifiés.

### Scores Globaux

| Critère | Score | /100 |
|---------|-------|------|
| **Architecture** | 65 | 100 |
| **Sécurité** | 70 | 100 |
| **Performance** | 75 | 100 |
| **Maintenabilité** | 60 | 100 |
| **Production Ready** | 55 | 100 |

---

## 2. PROBLÈMES CRITIQUES (Critical)

### 2.1 CRITICAL: Migration Flyway V1 corrompue

**Fichier**: `backend/src/main/resources/db/migration/V1__init_schema.sql`

**Problème**: Le fichier V1 contient 519 instructions `CREATE TABLE` pour seulement ~30 tables. Chaque table est recréée 15-20 fois avec des définitions différentes.

**Explication**: Le fichier a été modifié plusieurs fois pendant le développement, violant le principe fondamental de Flyway "versionner et ne jamais modifier". En production, seule la première exécution est appliquée, mais le contenu est un cauchemar de maintenance.

**Impact**: 
- Impossible de reproduire l'état de la base en développement
- Incohérence entre le schéma défini et le schéma appliqué
- Risque de conflits de migration en environnement collaboratif

**Correction proposée**: 
1. Conserver uniquement la DERNIÈRE définition de chaque table
2. Extraire les modifications successives dans des fichiers V1.1, V1.2, etc.
3. Utiliser `ALTER TABLE` pour les changements de schéma

---

### 2.2 CRITICAL: Enum StatutAT incohérent avec les contraintes Flyway

**Fichier**: `backend/src/main/java/com/ocp/at/entity/enums/StatutAT.java`

**Problème**: L'enum contient 9 statuts:
```java
BROUILLON, SOUMISE, VALIDEE, REJETEE, RENOUVELEE, CLOTUREE, ARCHIVEE, ANNULEE
```

Mais les contraintes CHECK dans Flyway ne définissent que:
- `V1__init_schema.sql`: `(BROUILLON, SOUMISE, VALIDEE, REJETEE, RENOUVELEE, CLOTUREE)` + variations avec `ANNULEE`
- `workflows_at`: Même chose sans `ARCHIVEE`
- `historiques_at`: Inclut `ANNULATION` (non présent dans l'enum!)

**Impact**: L'application peut insérer `ARCHIVEE` ou `ANNULEE` mais PostgreSQL rejettera certaines valeurs selon les contraintes.

**Correction proposée**:
1. Synchroniser l'enum avec les contraintes CHECK
2. Ajouter `ANNULATION` comme valeur dans l'enum `TypeActionAT`
3. Documenter les statuts valides par table

---

## 3. PROBLÈMES HIGH

### 3.1 HIGH: @Data Lombok sur toutes les entités JPA

**Fichiers**: TOUTES les entités (34 fichiers dans `entity/`)

**Problème**: `@Data` génère `equals()` et `hashCode()` qui utilisent TOUS les champs, y compris les relations. Pour les entités JPA avec `@OneToMany` et `@ManyToOne`, cela peut causer:
- Comparaison récursive infinie
- Problèmes de performance avec Hibernate
- StackOverflowError en cas de sérialisation JSON bidirectionnelle

**Explication**: Les entités comme `AutorisationTravail` contiennent des relations bidirectionnelles (`visas`, `permis`, `historiques`). Avec `@Data`, `equals()` comparera ces collections, causant des problèmes.

**Impact**:
- Risque de StackOverflowError
- Comportement inattendu dans les Collections (HashSet, HashMap)
- Incompatibilité potentielle avec Jackson JSON

**Correction proposée**:
Remplacer `@Data` par:
```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"visas", "permis", "historiques", "receptionTravaux"})
public class AutorisationTravail {
    @Id
    @EqualsAndHashCode.Include
    private String id;
    // ... autres champs sans relations
}
```

---

### 3.2 HIGH: FetchType.EAGER sur les relations ManyToMany

**Fichiers**: 
- `Utilisateur.java` (ligne 65)
- `Role.java` (ligne 28)

**Problème**: 
```java
// Utilisateur.java
@ManyToMany(fetch = FetchType.EAGER)  // PROBLÈME!
private Set<Role> roles = new HashSet<>();

// Role.java
@ManyToMany(fetch = FetchType.EAGER)  // PROBLÈME!
private Set<Permission> permissions = new HashSet<>();
```

**Impact**: Chaque chargement d'un `Utilisateur` déclenche le chargement de tous ses `Role` et, par transitivité, toutes les `Permission`. Cela peut causer:
- N+1 queries involontaires
- Charge mémoire excessive
- Dégradation des performances

**Correction proposée**:
```java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "utilisateur_roles",
    joinColumns = @JoinColumn(name = "utilisateur_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
@Fetch(FetchMode.JOIN)  // Charge avec JOIN dans la même requête
private Set<Role> roles = new HashSet<>();
```

---

### 3.3 HIGH: Clé secrète JWT hardcodée

**Fichier**: `backend/src/main/resources/application.yml` (ligne 11)

**Problème**:
```yaml
app:
  jwt:
    secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

**Impact**: La clé secrète est exposée dans le code source. En production, elle devrait être dans une variable d'environnement ou un vault.

**Correction proposée**:
```yaml
app:
  jwt:
    secret: ${JWT_SECRET}  # Variable d'environnement obligatoire
```

---

## 4. PROBLÈMES MEDIUM

### 4.1 MEDIUM: Controller FileController vide

**Fichier**: `backend/src/main/java/com/ocp/at/controller/FileController.java`

**Problème**:
```java
@RestController
@RequestMapping("/api/s")
public class FileController {
}
```

**Impact**: Un contrôleur vide avec un chemin `/api/s` n'a aucun sens. Soit il manque des endpoints, soit le contrôleur devrait être supprimé.

**Correction**: À Investiguer - Vérifier si des endpoints de téléchargement de fichiers sont manquants.

---

### 4.2 MEDIUM: Champs @Autowired dans SecurityConfig

**Fichier**: `backend/src/main/java/com/ocp/at/config/SecurityConfig.java`

**Problème**:
```java
@Autowired
UserDetailsServiceImpl userDetailsService;

@Autowired
private AuthEntryPointJwt unauthorizedHandler;
```

**Impact**: L'injection par champ (`@Autowired`) est moins testable et moins explicite qu'une injection par constructeur.

**Correction**: Utiliser l'injection par constructeur:
```java
private final UserDetailsServiceImpl userDetailsService;
private final AuthEntryPointJwt unauthorizedHandler;

public SecurityConfig(UserDetailsServiceImpl userDetailsService, 
                      AuthEntryPointJwt unauthorizedHandler) {
    this.userDetailsService = userDetailsService;
    this.unauthorizedHandler = unauthorizedHandler;
}
```

---

### 4.3 MEDIUM: JwtUtils - Exception non catchée

**Fichier**: `backend/src/main/java/com/ocp/at/security/JwtUtils.java`

**Problème**: `validateJwtToken` ne capture pas `SecurityException`:
```java
public boolean validateJwtToken(String authToken) {
    try {
        Jwts.parser().verifyWith(key()).build().parse(authToken);
        return true;
    } catch (MalformedJwtException e) { ... }
    catch (ExpiredJwtException e) { ... }
    // Missing: SecurityException, UnsupportedJwtException, etc.
    return false;
}
```

**Impact**: Certaines exceptions JWT peuvent ne pas être capturées.

**Correction**:
```java
catch (JwtException | SecurityException | IllegalArgumentException e) {
    logger.error("JWT validation error: {}", e.getMessage());
}
```

---

### 4.4 MEDIUM: WorkflowATServiceImpl.obtenirEtatSuivant non implémenté

**Fichier**: `backend/src/main/java/com/ocp/at/service/impl/WorkflowATServiceImpl.java` (lignes 44-51)

**Problème**:
```java
@Override
public StatutAT obtenirEtatSuivant(String atId, StatutAT etatActuel) {
    // Implementation simplifiée : dans la vraie vie...
    return etatActuel;  // Retourne l'état actuel!
}
```

**Impact**: La logique de transition d'état est simplifiée et ne respecte pas le workflow configuré.

---

### 4.5 MEDIUM: Faible couverture de tests

**Statistiques**:
- Fichiers source: 306
- Fichiers test: 16
- Couverture estimée: ~5-10%

**Correction**: Ajouter des tests pour:
- Tous les contrôleurs
- Les services métier critiques
- Les cas d'erreur
- Les transitions de workflow

---

## 5. PROBLÈMES LOW

### 5.1 LOW: Double définition de AuditLog.entity

**Fichier**: `backend/src/main/java/com/ocp/at/entity/AuditLog.java`

**Note**: `entity` est un mot-clé SQL/JPA. Éviter d'utiliser `entity` comme nom de champ.

---

### 5.2 LOW: Nom de variable atypique dans Role.permissions

**Fichier**: `backend/src/main/java/com/ocp/at/entity/Role.java`

**Problème**: Le champ `permissions` est de type `Set<Permission>`, ce qui peut créer une confusion avec les annotations de sécurité Spring.

---

### 5.3 LOW: Import unused dans JwtUtils

**Fichier**: `backend/src/main/java/com/ocp/at/security/JwtUtils.java`

L'import `javax.crypto.SecretKey` n'est pas utilisé directement (la clé est créée via `Keys.hmacShaKeyFor`).

---

## 6. VÉRIFICATIONS PASSÉES ✓

### 6.1 Architecture
- ✓ Structure de packages cohérente
- ✓ Pas de dépendances circulaires détectées
- ✓ Séparation Controllers/Services/Repositories/Entities

### 6.2 Sécurité Stockage
- ✓ Protection contre path traversal dans StorageServiceImpl
- ✓ Validation des types MIME dans PermisController

### 6.3 API REST
- ✓ Documentation Swagger complète
- ✓ Codes HTTP appropriés (200, 201, 400, 401, 403, 404)
- ✓ Validation des entrées avec @Valid

### 6.4 Exception Handling
- ✓ GlobalExceptionHandler bien implémenté
- ✓ Messages d'erreur appropriés
- ✓ Pas de fuite d'informations sensibles

### 6.5 JPA Configuration
- ✓ `open-in-view: false` configuré (évite LazyInitializationException)

---

## 7. CHECKLIST FINALE

| Critère | Status |
|---------|--------|
| ✅ Projet compilable | **OUI** |
| ⚠️ Flyway cohérent | **NON** - V1 corrompu |
| ⚠️ PostgreSQL cohérent | **NON** - Contraintes Enum désynchronisées |
| ✅ API cohérentes | **OUI** |
| ✅ Swagger complet | **OUI** |
| ⚠️ Tests OK | **PARTIEL** - Couverture insuffisante |
| ⚠️ Sécurité OK | **PARTIEL** - JWT hardcodé |
| ⚠️ Production Ready | **NON** - Plusieurs corrections nécessaires |

---

## 8. RECOMMANDATIONS PRIORITAIRES

### Phase 1: Corrections Urgentes (avant mise en pré-production)
1. **Réparer V1__init_schema.sql** - Conserver uniquement les définitions finales
2. **Synchroniser les contraintes CHECK avec StatutAT enum**
3. **Externaliser la clé JWT** dans variable d'environnement

### Phase 2: Améliorations Importantes (avant mise en production)
4. Remplacer `@Data` par `@Getter/@Setter` avec `@EqualsAndHashCode` personnalisé
5. Changer `FetchType.EAGER` en `FetchType.LAZY` pour les rôles
6. Implémenter correctement `WorkflowATServiceImpl.obtenirEtatSuivant`

### Phase 3: Optimisations (après mise en production)
7. Augmenter la couverture de tests à 70%+
8. Ajouter des indexes pour les requêtes fréquentes
9. Configurer un cache Redis pour les données quasi-statiques

---

## 9. CONCLUSION

Le projet AT-OCP présente une architecture solide dans l'ensemble, mais souffre de problèmes critiques liés au processus de développement:

1. **Migration Flyway corrompue** - Nécessite une refactorisation complète
2. **Annotations Lombok incorrectes** - Risque de bugs en production
3. **Configuration de sécurité** - Clé JWT exposée

**Verdict**: Le projet nécessite des corrections importantes avant d'être prêt pour la production. Les problèmes identifiés sont corrigeables sans refonte majeure de l'architecture.

---

*Rapport généré par OpenHands AI Agent - 2026-07-11*
