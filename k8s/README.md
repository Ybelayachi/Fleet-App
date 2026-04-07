# Kubernetes — Fleet KM (Cloud / Production)

> **Usage local** : préférer `docker-compose up -d` à la racine du projet.  
> Les manifestes Kubernetes sont destinés au déploiement cloud (Azure AKS, AWS EKS, GCP GKE).

## Stack déployée

| Composant | Image | Description |
|-----------|-------|-------------|
| `fleet-db` | `postgres:16` | Base de données PostgreSQL (PVC 1Gi) |
| `fleet-backend` | `projetangular-fleet-backend` | API Spring Boot + Flyway |
| `fleet-frontend` | `projetangular-fleet-frontend` | Angular SSR |

## Fichiers

| Fichier | Rôle |
|---------|------|
| `namespace.yaml` | Namespace `fleet` |
| `backend-secret.yaml` | Credentials DB + JWT secret |
| `backend-configmap.yaml` | Config datasource PostgreSQL |
| `database.yaml` | Deployment + Service + PVC PostgreSQL |
| `backend.yaml` | Deployment + Service backend |
| `frontend.yaml` | Deployment + Service frontend |
| `ingress.yaml` | Ingress nginx (`fleet.local`) |
| `hpa.yaml` | HorizontalPodAutoscaler (1-3 réplicas) |

## Prérequis

- Docker Desktop avec Kubernetes activé, ou Minikube/Kind
- `kubectl` installé

## Déployer

Depuis la racine du projet :

```powershell
docker compose build
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/backend-secret.yaml
kubectl apply -f k8s/backend-configmap.yaml
kubectl apply -f k8s/database.yaml
kubectl apply -f k8s/backend.yaml
kubectl apply -f k8s/frontend.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/hpa.yaml
```

Vérification :

```powershell
kubectl get pods -n fleet
kubectl get services -n fleet
```

## Mettre à jour après modification du code

```powershell
# 1) Rebuild des images
docker compose build fleet-backend fleet-frontend

# 2) Réappliquer les manifests
kubectl apply -f k8s/backend.yaml
kubectl apply -f k8s/frontend.yaml

# 3) Forcer le redéploiement
kubectl rollout restart deployment/fleet-backend -n fleet
kubectl rollout restart deployment/fleet-frontend -n fleet

# 4) Attendre la fin
kubectl rollout status deployment/fleet-backend -n fleet
kubectl rollout status deployment/fleet-frontend -n fleet
```

## Logs

```powershell
kubectl logs deployment/fleet-backend -n fleet --tail=100
kubectl logs deployment/fleet-frontend -n fleet --tail=100
kubectl logs deployment/fleet-db -n fleet --tail=50
```

Accès frontend local (NodePort) : `http://localhost:30200`

## Tests E2E Selenium sur Kubernetes

```powershell
cd selenium-e2e
mvn test
# Mode visible :
mvn test "-De2e.headless=false" "-De2e.pauseMs=5000"
```

## Mettre à jour après modification du code

Quand tu modifies le frontend (ex: migration Angular Material -> PrimeNG) ou le backend,
il faut **reconstruire les images** puis **forcer le redéploiement** des pods.

```powershell
# 1) Rebuild des images locales utilisées par Kubernetes
docker compose build fleet-backend fleet-frontend

# 2) Réappliquer les manifests (idempotent)
kubectl apply -f k8s/backend.yaml
kubectl apply -f k8s/frontend.yaml

# 3) Forcer un rollout pour prendre la nouvelle image :latest
kubectl rollout restart deployment/fleet-backend -n fleet
kubectl rollout restart deployment/fleet-frontend -n fleet

# 4) Attendre la fin du déploiement
kubectl rollout status deployment/fleet-backend -n fleet
kubectl rollout status deployment/fleet-frontend -n fleet
```

Vérification rapide :

```powershell
kubectl get pods -n fleet
kubectl logs deployment/fleet-frontend -n fleet --tail=100
kubectl logs deployment/fleet-backend -n fleet --tail=100
```

Accès frontend local (NodePort) : `http://localhost:30200`

### Exécuter Selenium sur la version Kubernetes

Les tests Selenium pointent déjà par défaut sur `http://localhost:30200`.

```powershell
cd selenium-e2e
mvn test
```

Mode visible (debug) :

```powershell
mvn test "-De2e.headless=false" "-De2e.pauseMs=5000"
```

## Vérifier

```powershell
kubectl get all -n fleet
kubectl logs deployment/fleet-backend -n fleet --tail=100
kubectl logs deployment/fleet-frontend -n fleet --tail=100
kubectl get ingress -n fleet
kubectl get hpa -n fleet
```

## Autoscaling (HPA minimal)

Le fichier `k8s/hpa.yaml` configure:

- backend: min 1, max 3, cible CPU 70%
- frontend: min 1, max 3, cible CPU 70%

Commandes utiles:

```powershell
kubectl get hpa -n fleet -w
kubectl top pods -n fleet
```

Le frontend est exposé en `NodePort` sur `30200`.

- Docker Desktop Kubernetes: http://localhost:30200
- Minikube: `minikube service fleet-frontend -n fleet --url`

## Option URL propre via Ingress

Le fichier `k8s/ingress.yaml` route:

- `http://fleet.local/` -> frontend
- `http://fleet.local/api` -> backend

1. Vérifie qu'un Ingress Controller NGINX est installé.
2. Ajoute dans ton fichier hosts local:

```txt
127.0.0.1 fleet.local
```

3. Applique l'Ingress:

```powershell
kubectl apply -f k8s/ingress.yaml
```

## Nettoyer

```powershell
kubectl delete namespace fleet
```
