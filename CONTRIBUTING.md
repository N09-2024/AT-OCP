# Guide de Contribution

Merci de contribuer à AT-OCP! Ce guide vous aider à démarrer.

## 🚀 Démarrage

### Configuration de l'Environnement

1. **Prérequis**:
   - JDK 21+
   - Maven 3.9+
   - PostgreSQL 16+
   - Docker (optionnel)

2. **Clone et Setup**:
   ```bash
   git clone https://github.com/OCP-N09-2024/AT-OCP.git
   cd AT-OCP/AT-OCP
   
   # Installer les dépendances
   cd backend
   mvn dependency:go-offline
   ```

3. **Base de données**:
   ```bash
   # Démarrer PostgreSQL avec Docker
   docker run -d \
     --name at-ocp-db \
     -e POSTGRES_DB=at_ocp_db \
     -e POSTGRES_USER=at_ocp_user \
     -e POSTGRES_PASSWORD=password \
     -p 5432:5432 \
     postgres:16-alpine
   ```

4. **Variables d'environnement**:
   ```bash
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_NAME=at_ocp_db
   export DB_USERNAME=at_ocp_user
   export DB_PASSWORD=password
   export JWT_SECRET=dev-secret-key-change-in-production
   ```

5. **Démarrer l'application**:
   ```bash
   mvn spring-boot:run
   ```

## 🔄 Processus de Développement

### 1. Créer une Branche

```bash
# Format: type/description-courte
git checkout -b feature/gestion-visa
git checkout -b fix/correction-workflow
git checkout -b docs/update-readme
```

Types de branches:
- `feature/` - Nouvelles fonctionnalités
- `fix/` - Corrections de bugs
- `hotfix/` - Corrections urgentes en production
- `docs/` - Documentation
- `refactor/` - Refactoring
- `test/` - Ajout/modification de tests

### 2. Conventions de Commits

Nous utilisons les [commits sémantiques](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

**Types**:
- `feat` - Nouvelle fonctionnalité
- `fix` - Correction de bug
- `docs` - Documentation
- `style` - Formatage, style (pas de changement de code)
- `refactor` - Refactoring
- `perf` - Amélioration performance
- `test` - Tests
- `build` - Build, dépendances
- `ci` - CI/CD
- `chore` - Tâches diverses

**Exemples**:
```bash
git commit -m "feat(workflow): ajout validation multi-niveaux"
git commit -m "fix(visa): correction état par défaut"
git commit -m "docs(api): mise à jour swagger"
git commit -m "test(service): ajout tests unitaires WorkflowAT"
```

### 3. Écrire du Code

**Standards**:
- Respecter les conventions Java (Checkstyle)
- Ajouter des logs appropriés (SLF4J)
- Utiliser les annotations Lombok
- Documenter les méthodes publiques avec Javadoc

**Ordre des imports**:
1. Java / Jakarta
2. Spring
3. Bibliothèques tierces
4. Imports du projet

**Nommage**:
- Classes: PascalCase (ex: `AutorisationTravailController`)
- Méthodes: camelCase (ex: `findById`)
- Constantes: UPPER_SNAKE_CASE (ex: `MAX_RETRY_COUNT`)
- Packages: lowercase (ex: `com.ocp.at.service`)

### 4. Tests

**Couverture minimale**:
- 50% lignes de code
- 30% branches

**Types de tests**:
- Unitaires: `@ExtendWith(MockitoExtension.class)`
- Integration: `@SpringBootTest`
- Controller: `@WebMvcTest`

**Exécuter les tests**:
```bash
# Tous les tests
mvn test

# Tests spécifiques
mvn test -Dtest=WorkflowATServiceImplTest

# Couverture
mvn jacoco:report
# Rapport dans: target/site/jacoco/index.html
```

### 5. Pull Request

**Template PR**: Le template est automatiquement appliqué.

**Checklist avant soumission**:
- [ ] Le code compile sans erreurs
- [ ] Tous les tests passent
- [ ] La couverture de code est suffisante
- [ ] La documentation est mise à jour
- [ ] Pas de secrets exposés
- [ ] Le titre du PR est conforme

**Processus de revue**:
1. Soumettre le PR
2. CI/CD s'exécute automatiquement
3. Un reviewer est assigné (CODEOWNERS)
4. Corrections demandées si nécessaire
5. Approval requis avant merge
6. Squash and merge

## 📋 Règles de Code

### Entités JPA

```java
@Entity
@Table(name = "autorisations_travail")
@Getter
@Setter
@NoArgsConstructor
public class AutorisationTravail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    // Relations toujours en LAZY
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demandeur_id")
    private Utilisateur demandeur;
    
    // Énumérations simples
    @Enumerated(EnumType.STRING)
    private StatutAT statut;
}
```

### Services

```java
@Service
@RequiredArgsConstructor
@Transactional
public class AutorisationTravailServiceImpl {
    
    private final AutorisationTravailRepository repository;
    private final WorkflowATService workflowService;
    
    public AutorisationTravailDTO create(AutorisationTravailDTO dto) {
        // Validation, logique métier, retourne DTO
    }
}
```

### DTOs

```java
// Request DTO
@Data
public class CreateAutorisationTravailRequest {
    @NotBlank(message = "L'objet est obligatoire")
    private String objet;
    
    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;
}

// Response DTO
@Data
@Builder
public class AutorisationTravailResponse {
    private String id;
    private String numero;
    private StatutAT statut;
}
```

## 🐛 Signaler un Bug

Utiliser le [template de bug](.github/ISSUE_TEMPLATE/bug_report.md):

1. Décrire clairement le bug
2. Fournir les étapes de reproduction
3. Inclure les logs d'erreur
4. Mentionner l'environnement (OS, version Java, etc.)

## 💡 Demander une Feature

Utiliser le [template de feature](.github/ISSUE_TEMPLATE/feature_request.md):

1. Décrire la fonctionnalité souhaitée
2. Expliquer le problème résolu
3. Proposer une solution
4. Lister les alternatives considérées

## 📖 Ressources

- [Style Guide Java](https://google.github.io/styleguide/javaguide.html)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Test-Driven Development](https://martinfowler.com/articles/is-tdd-dead/)

## ❓ Questions

- Ouvrir une issue avec le tag `question`
- Consulter la documentation existante
- Contacter l'équipe: dev@ocp.ma
