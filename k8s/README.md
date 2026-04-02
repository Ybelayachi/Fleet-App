# Kubernetes local (backend + frontend)

## À quoi sert Kubernetes ?

Kubernetes sert à **orchestrer** les conteneurs en production (ou en local) :

- redémarrage automatique des pods en cas de crash
- déploiements reproductibles via fichiers YAML
- scalabilité horizontale (plusieurs réplicas)
- exposition réseau contrôlée (Services)
- séparation config/secrets

## Prérequis

- Docker Desktop avec Kubernetes activé, ou Minikube/Kind
- `kubectl` installé

## Déployer

Depuis la racine du projet :

```powershell
docker compose build
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/backend-configmap.yaml
kubectl apply -f k8s/backend-secret.yaml
kubectl apply -f k8s/backend.yaml
kubectl apply -f k8s/frontend.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/hpa.yaml
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
