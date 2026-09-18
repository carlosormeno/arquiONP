import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { NotificationService } from '../services/notification.service';

/**
 * Interceptor de errores HTTP. Ver `LIN-FE-ANG-001 §9.4`.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const notifier = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      switch (error.status) {
        case 401:
          router.navigate(['/login']);
          break;
        case 403:
          router.navigate(['/error/403']);
          break;
        case 429:
          // Límite de peticiones excedido (codDetRespuesta 302, LIN-API-REST-001 sección 8.4).
          // No reintentar automáticamente: agravaría la saturación que disparó el límite.
          notifier.showError(
            'Demasiadas solicitudes. Espere unos segundos e intente nuevamente.',
          );
          break;
        case 500:
        case 503:
          notifier.showError('Error del servidor. Intente nuevamente más tarde.');
          break;
      }
      return throwError(() => error);
    }),
  );
};
