# Fleet KM - Application de Gestion des Kilométrages

Application web de gestion des kilométrages des véhicules électriques pour NSI.

## � Versions

| Version | Date | Description |
|---------|------|-------------|
| v2.0 | Avril 2026 | Migration IBM DB2 → PostgreSQL, stack Docker complète (DB conteneurisée) |
| v1.0 | — | Version initiale avec IBM DB2 local |

### Changements v2.0
- **Base de données** : IBM DB2 remplacé par **PostgreSQL 16** (conteneur Docker)
- **Stack Docker complète** : `docker-compose up -d` lance DB + Backend + Frontend + pgAdmin
- **Aucune installation** de base de données requise sur la machine hôte
- Données persistantes via volume Docker nommé (`db_data`)
- Interface d'administration DB via **pgAdmin** sur `:5050`

---

## 🚀 Démarrage Rapide

### ⭐ Recommandé — Docker (stack complète)
```bash
# Depuis la racine du workspace
docker compose up -d
```
Lance automatiquement dans l'ordre : **PostgreSQL** → **Backend** → **Frontend** + **pgAdmin**

| Service | URL |
|---------|-----|
| Application | http://localhost:4200 |
| API Backend | http://localhost:8080 |
| pgAdmin (DB) | http://localhost:5050 |

> pgAdmin : email `admin@fleet.com` / mot de passe `admin`  
> Connexion DB dans pgAdmin : host `fleet-db`, port `5432`, user `fleetuser`, password `fleetpassword`

### Développement local (sans Docker)
```bash
# Démarrer PostgreSQL via Docker uniquement
docker compose up -d fleet-db

# Puis lancer le backend
cd fleet-backend
mvn spring-boot:run
```
➡️ API disponible sur http://localhost:8080

```bash
# Frontend
cd fleet-frontend
npm install
npm start
```
➡️ Application disponible sur http://localhost:4200

> **Profils disponibles** : `docker` (PostgreSQL), `e2e` (H2 en mémoire pour les tests)

## 📦 Technologies

- **Backend** : Java 21, Spring Boot 3.5.10, Spring Security, JWT, PostgreSQL 16, Flyway
- **Frontend** : Angular 20, TypeScript, PrimeNG 20, RxJS 7.8, SSR activé
- **Tests E2E** : Selenium WebDriver 4.35
- **Infrastructure** : Docker Compose, Kubernetes (manifestes disponibles dans `k8s/`)

## 👥 Compte de test par défaut

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| Admin | admin@example.com | Admin123! |

> D'autres comptes peuvent être créés via l'interface Admin après connexion.

## 📚 Documentation

Voir [DOCUMENTATION.md](./DOCUMENTATION.md) pour la documentation technique complète.

## 🔑 Fonctionnalités

- ✅ Authentification JWT
- ✅ Gestion des utilisateurs (Admin)
- ✅ Gestion des véhicules (Admin)
- ✅ Affectation véhicules-conducteurs
- ✅ Déclaration mensuelle de kilométrage (Conducteur)
- ✅ Tableau de bord flotte (Gestionnaire)
- ✅ Export CSV des données
- ✅ Migrations de base de données versionnées (Flyway)

## 📁 Structure

```
.
├── fleet-backend/       # API REST Spring Boot
│   ├── src/main/java/
│   ├── src/test/        # 51 tests unitaires
│   ├── db/              # Script SQL schéma (référence)
│   └── pom.xml
├── fleet-frontend/      # Application Angular SSR
│   ├── src/app/
│   ├── public/
│   └── package.json
├── selenium-e2e/        # Tests E2E UI (Selenium)
├── k8s/                 # Manifestes Kubernetes (cloud/prod)
├── docker-compose.yml   # Orchestration Docker (DB + Backend + Frontend + pgAdmin)
└── .env.docker.example  # Variables d'environnement (copier vers .env)
```

## 🧪 Tests

```bash
# Backend — tests unitaires (51 tests)
cd fleet-backend
mvn test

# Backend — avec profil H2 en mémoire (sans PostgreSQL)
$env:SPRING_PROFILES_ACTIVE="e2e"
mvn spring-boot:run

# Frontend
cd fleet-frontend
npm test

# Tests E2E Selenium (backend e2e requis)
cd selenium-e2e
mvn test
# Mode visible :
mvn test "-De2e.headless=false" "-De2e.pauseMs=3000"
```

## 🐳 Gestion Docker

```bash
# Démarrer toute la stack
docker compose up -d

# Voir l'état des conteneurs
docker compose ps

# Voir les logs du backend (Flyway, erreurs)
docker compose logs -f fleet-backend

# Arrêter sans supprimer les données
docker compose stop

# Arrêter et supprimer les données (reset complet)
docker compose down -v

# Rebuilder le backend après modification du code
docker compose build --no-cache fleet-backend
docker compose up -d fleet-backend
```