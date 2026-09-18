import { HttpInterceptorFn } from '@angular/common/http';
import { tap } from 'rxjs/operators';

/**
 * Interceptor de correlación X-Request-ID.
 *
 * Cada request HTTP saliente incluye el header `X-Request-ID` para que los
 * traces del backend sean correlacionables con la acción del usuario en el
 * navegador (pilar "Trazas distribuidas" de `ARQ-R-005`, `LIN-ARQ-001 §5.3`).
 *
 * Debe registrarse PRIMERO en `provideHttpClient(withInterceptors([...]))`
 * para que el header esté presente incluso en requests rechazados por el
 * interceptor de autenticación/sesión o de errores.
 *
 * Ver `LIN-FE-ANG-001 §15.1`.
 */
export const correlationInterceptor: HttpInterceptorFn = (req, next) => {
  const requestId = crypto.randomUUID();

  const cloned = req.clone({
    setHeaders: { 'X-Request-ID': requestId },
  });

  return next(cloned).pipe(
    tap({
      error: () => {
        // El requestId ya viaja en el header — el backend lo devuelve en
        // meta.requestId para facilitar búsquedas en Kibana/Jaeger si el
        // usuario reporta un error.
      },
    }),
  );
};
