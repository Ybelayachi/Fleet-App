# Fleet KM - Application de Gestion des Kilométrages

Application web de gestion des kilométrages des véhicules électriques pour NSI.

## 🚀 Démarrage Rapide

### Backend (Spring Boot + IBM DB2)
```bash
cd fleet-backend
# Profil db2 (base de données IBM DB2 locale)
$env:SPRING_PROFILES_ACTIVE="db2"
mvn spring-boot:run
```
➡️ API disponible sur http://localhost:8080

> **Profils disponibles** : `db2` (production locale), `e2e` (H2 en mémoire pour les tests)

### Frontend (Angular)
```bash
cd fleet-frontend
npm install
npm start
```
➡️ Application disponible sur http://localhost:4200

### Docker (alternative)
```bash
# Depuis la racine du workspace
docker compose up --build -d
```
Services : backend sur `:8080`, frontend SSR sur `:4200`

## 📦 Technologies

- **Backend** : Java 21, Spring Boot 3.5.10, Spring Security, JWT, IBM DB2, Flyway
- **Frontend** : Angular 20, TypeScript, PrimeNG 20, RxJS 7.8, SSR activé
- **Tests E2E** : Selenium WebDriver 4.35

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
│   ├── db/              # Script SQL schéma DB2
│   └── pom.xml
├── fleet-frontend/      # Application Angular SSR
│   ├── src/app/
│   ├── public/
│   └── package.json
├── selenium-e2e/        # Tests E2E UI (Selenium)
├── k8s/                 # Manifestes Kubernetes
├── docker-compose.yml   # Orchestration Docker
└── DOCUMENTATION.md     # Documentation technique complète
```

## 🧪 Tests

```bash
# Backend — tests unitaires (51 tests)
cd fleet-backend
mvn test

# Backend — avec profil H2 en mémoire (sans DB2)
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