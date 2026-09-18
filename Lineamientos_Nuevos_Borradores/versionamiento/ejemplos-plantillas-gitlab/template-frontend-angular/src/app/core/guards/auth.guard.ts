import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/**
 * Guard de autenticación: bloquea el acceso al `ShellComponent` y sus rutas
 * hijas sin sesión activa, redirigiendo a `/login`. Ver `LIN-FE-ANG-001 §6.1`.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  return router.createUrlTree(['/login']);
};
