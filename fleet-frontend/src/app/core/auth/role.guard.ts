import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { map } from 'rxjs/operators';
import { AuthService } from './auth.service';

export const roleGuard = (...roles: string[]): CanActivateFn => {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    return auth.ensureProfile().pipe(
      map((role) => {
        if (role && roles.includes(role)) {
          return true;
        }
        return router.parseUrl('/login');
      })
    );
  };
};
