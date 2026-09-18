import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { AuthService } from '../services/auth.service';

/**
 * Interceptor de autenticación — escenario **SIN BFF (canal único)**.
 *
 * Ver `LIN-FE-ANG-001 §9.3.1`. Adjunta el token SAA a cada request. Aplica
 * solo cuando el proyecto real NO tiene BFF Token Handler delante del core
 * (criterios de adopción de `DIS-R-005`, `LIN-DIS-001 §5.1`). Si el sistema
 * real atiende más de un canal y requiere BFF, este interceptor se reemplaza
 * por `session.interceptor.ts` (§9.3.2) — nunca se registran ambos.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken(); // token opaco de SAA — no es JWT, no decodificar en cliente

  if (token) {
    req = req.clone({
      // Esquema Bearer requerido por LIN-API-REST-001 sección 7.1.
      // El backend valida el token contra el endpoint SAA (SaaTokenValidationFilter).
      setHeaders: { Authorization: `Bearer ${token}` },
    });
  }
  return next(req);
};
