# Selenium E2E tests

Tests d'intégration UI pour le frontend Fleet avec Selenium.

## Prérequis

- Application accessible (Docker/Kubernetes/local) sur une URL web
- Chrome installé
- Java 21+
- Maven

## Lancer les tests

Depuis le dossier `selenium-e2e`:

```powershell
mvn test
```

Suite complète actuelle: 15 tests, stable en headless et non-headless.

Par défaut:

- `baseUrl=http://localhost:30200`
- `e2e.apiRootUrl=http://localhost:8080`
- `e2e.email=demo.hpa@fleet.local`
- `e2e.password=Demo123!`
- `e2e.headless=true`

## Exécuter sans écrire dans DB2 (profil H2 e2e)

1) Démarrer le backend avec le profil `e2e`:

```powershell
cd ..\fleet-backend
mvn spring-boot:run "-Dspring-boot.run.profiles=e2e"
```

2) Lancer le frontend (si nécessaire), puis les tests Selenium:

```powershell
cd ..\selenium-e2e
mvn test "-DbaseUrl=http://localhost:4200" "-De2e.apiRootUrl=http://localhost:8080"
```

Dans ce mode, toutes les écritures E2E vont vers H2 en mémoire (pas vers DB2).
Le `baseUrl` doit pointer vers un frontend relié au backend local (`localhost:8080`).

## Script one-shot (backend H2 + Selenium)

Depuis la racine du projet:

```powershell
./run-selenium-h2.ps1
```

Exemple avec filtre de test:

```powershell
./run-selenium-h2.ps1 -TestFilter FleetExportE2ETest -Headless:$false
```

## Surcharger les variables

```powershell
mvn test -DbaseUrl=http://fleet.local -De2e.email=ton.user@mail.com -De2e.password=TonMotDePasse -De2e.headless=false
```

Sous PowerShell, si un `-D...` est mal interprété, utilise des guillemets:

```powershell
mvn test "-De2e.headless=false" "-De2e.pauseMs=5000"
```

`e2e.pauseMs` garde le navigateur ouvert quelques secondes avant fermeture.

## Scénarios inclus

- Login avec identifiants invalides -> reste sur `/login`, aucun token
- Login avec identifiants valides -> token JWT stocké
- Validation formulaire login (champs requis)
- Guard de route -> redirection vers `/login` sans authentification
- Parcours admin -> `Utilisateurs`, `Véhicules`, `Affectations`, `Tableau de bord flotte`
- Parcours driver -> `Mes véhicules`, `Déclarer le kilométrage`
- Logout -> suppression token/rôle et retour `/login`
- Vérification visibilité menu selon rôle (admin vs driver)
- Route protégée inaccessible après logout
- Workflow métier bout-en-bout (setup admin + usage driver)
- Règle métier mileage (2e déclaration mensuelle refusée si non croissante)
- Règle métier mileage (2e déclaration mensuelle acceptée si croissante)
- Pagination admin utilisateurs
- Pagination admin + recherche + statuts actifs/inactifs
- Export fleet CSV (contenu et entête)

## Lancer une classe précise

```powershell
mvn test "-Dtest=AdminPaginationE2ETest"
mvn test "-Dtest=FleetExportE2ETest"
mvn test "-Dtest=BusinessWorkflowE2ETest"
mvn test "-Dtest=MileageSecondDeclarationValidationE2ETest"
```

Exécuter plusieurs classes:

```powershell
mvn test "-Dtest=AdminPaginationE2ETest,FleetExportE2ETest"
```

## Classes de test

- `AdminPagesE2ETest`
- `AdminPaginationE2ETest`
- `BusinessWorkflowE2ETest`
- `DriverPagesE2ETest`
- `FleetExportE2ETest`
- `LoginFormValidationE2ETest`
- `LoginInvalidCredentialsE2ETest`
- `LoginSuccessE2ETest`
- `LogoutE2ETest`
- `MileageSecondDeclarationValidationE2ETest`
- `ProtectedRouteAfterLogoutE2ETest`
- `RoleBasedMenuVisibilityE2ETest`
