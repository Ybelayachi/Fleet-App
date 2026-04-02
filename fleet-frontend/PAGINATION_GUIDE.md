# Guide d'utilisation de la pagination - Frontend Angular

## Vue d'ensemble

La pagination a été implémentée dans le frontend Angular pour gérer les réponses paginées du backend Spring Boot.

## 1. Interface `Page<T>`

L'interface générique `Page<T>` est définie dans `src/app/core/models/page.model.ts` et représente une réponse paginée du backend.

**Propriétés** :
- `content: T[]` - Tableau des éléments de la page actuelle
- `totalElements: number` - Nombre total d'éléments
- `totalPages: number` - Nombre total de pages
- `size: number` - Nombre d'éléments par page
- `number: number` - Numéro de la page actuelle (0-indexé)
- `numberOfElements: number` - Nombre d'éléments dans la page actuelle
- `first: boolean` - Indique si c'est la première page
- `last: boolean` - Indique si c'est la dernière page
- `empty: boolean` - Indique si la page est vide

## 2. Services mis à jour

### UserService
```typescript
// Récupérer les utilisateurs avec pagination
this.userService.getUsers({
  page: 0,
  size: 10,
  sort: 'email,asc'
}).subscribe(page => {
  console.log(page.content); // Les utilisateurs
  console.log(page.totalPages); // Total de pages
});
```

### VehicleService
```typescript
// Récupérer les véhicules avec pagination
this.vehicleService.getFleetVehicles({
  page: 0,
  size: 20,
  sort: 'brand,asc'
}).subscribe(page => {
  console.log(page.content); // Les véhicules
});

// Récupérer les véhicules manquants avec pagination
this.vehicleService.getMissingVehicles(2026, 2, {
  page: 0,
  size: 10
}).subscribe(page => {
  console.log(page.content); // Les véhicules manquants
});
```

### MileageService
```typescript
// Récupérer les véhicules du conducteur avec pagination
this.mileageService.getDriverVehicles({
  page: 0,
  size: 5
}).subscribe(page => {
  console.log(page.content); // Les véhicules du conducteur
});

// Récupérer les kilométrages avec pagination
this.mileageService.getFleetMileage(2026, 2, {
  page: 0,
  size: 20,
  sort: 'vehicle,asc'
}).subscribe(page => {
  console.log(page.content); // Les kilométrages
});
```

## 3. Utilisation dans les composants

### Exemple simple :

```typescript
import { Component, OnInit } from '@angular/core';
import { UserService } from '../../core/services/user.service';
import { Page } from '../../core/models/page.model';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-user-list',
  template: `
    <table>
      <tr *ngFor="let user of users">
        <td>{{ user.email }}</td>
      </tr>
    </table>
    
    <div>
      <button (click)="previousPage()" [disabled]="currentPage === 0">
        Précédent
      </button>
      <span>Page {{ currentPage + 1 }} / {{ totalPages }}</span>
      <button (click)="nextPage()" [disabled]="currentPage >= totalPages - 1">
        Suivant
      </button>
    </div>
  `
})
export class UserListComponent implements OnInit {
  users: User[] = [];
  currentPage: number = 0;
  totalPages: number = 0;

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.userService.getUsers({
      page: this.currentPage,
      size: 10
    }).subscribe(page => {
      this.users = page.content;
      this.totalPages = page.totalPages;
    });
  }

  nextPage() {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadUsers();
    }
  }

  previousPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadUsers();
    }
  }
}
```

## 4. Paramètres de pagination

L'interface `PaginationParams` accepte les paramètres suivants :

```typescript
interface PaginationParams {
  page?: number;        // Numéro de page (0-indexé)
  size?: number;        // Nombre d'éléments par page
  sort?: string;        // Format: "field,asc" ou "field,desc"
}
```

### Exemples de tri :
```typescript
// Trier par email A-Z
{ page: 0, size: 10, sort: 'email,asc' }

// Trier par marque Z-A
{ page: 0, size: 20, sort: 'brand,desc' }

// Trier par kilométrage (ascendant)
{ page: 0, size: 15, sort: 'mileage,asc' }
```

## 5. Composant exemple complet

Un composant d'exemple complet avec table, pagination, tri et sélection de taille de page est fourni dans :
`src/app/features/admin/components/paginated-list-example.component.ts`

Ce composant montre :
- ✅ Affichage des données paginées
- ✅ Navigation page par page
- ✅ Sélection de la taille de page
- ✅ Tri des données
- ✅ États de chargement
- ✅ État vide

## 6. Intégration avec Angular Material (optionnel)

Pour une meilleure UX, vous pouvez utiliser `MatPaginator` d'Angular Material :

```typescript
import { MatPaginatorModule } from '@angular/material/paginator';

@Component({
  template: `
    <div>
      <!-- Votre table ici -->
    </div>
    
    <mat-paginator 
      [length]="totalElements"
      [pageSize]="pageSize"
      [pageSizeOptions]="[5, 10, 20, 50]"
      (page)="onPageChange($event)">
    </mat-paginator>
  `
})
export class MyComponent {
  totalElements = 0;
  pageSize = 10;
  
  onPageChange(event: PageEvent) {
    this.loadData(event.pageIndex, event.pageSize);
  }
}
```

## 7. Bonnes pratiques

- ✅ Toujours vérifier `isFirstPage()` et `isLastPage()` avant naviguer
- ✅ Réinitialiser à la page 0 avant de changer le tri ou le taille
- ✅ Afficher le message "Aucun résultat" si `numberOfElements === 0`
- ✅ Désactiver les boutons "Précédent" et "Suivant" quand approprié
- ✅ Afficher un indicateur de chargement pendant la requête

## 8. Erreurs communes

❌ Ne pas réinitialiser la page lors du changement de filtre
❌ Oublier de désactiver les boutons aux limites des pages
❌ Accéder directement à `content[index]` sans vérifier que la page n'est pas vide

---

**Besoin d'aide ?** Consultez l'exemple complet dans `paginated-list-example.component.ts`
