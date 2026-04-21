import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const loginGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const role = authService.getUserRole();
  if (role === 'ADMIN') {
    return router.createUrlTree(['/admin/dashboard']);
  } else if (role === 'CLIENT') {
    return router.createUrlTree(['/home']);
  }
  
  return true;
};
