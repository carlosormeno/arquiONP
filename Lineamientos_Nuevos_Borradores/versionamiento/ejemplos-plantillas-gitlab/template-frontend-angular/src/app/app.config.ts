import {
  ApplicationConfig,
  ErrorHandler,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { correlationInterceptor } from './core/interceptors/correlation.interceptor';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { GlobalErrorHandler } from './core/services/global-error-handler.service';

/**
 * Configuración de bootstrap standalone. Ver `LIN-FE-ANG-001 §9.5`.
 *
 * Este scaffold implementa el escenario **SIN BFF** (§9.3.1): registra
 * `authInterceptor`, que adjunta el token SAA directamente. Si el proyecto
 * real requiere el patrón Token Handler (§9.3.2, dos o más canales), este
 * bloque se reemplaza reemplazando `authInterceptor` por `sessionInterceptor`
 * — nunca se registran ambos a la vez.
 *
 * El orden de los interceptores importa: `correlationInterceptor` va primero
 * para que `X-Request-ID` esté presente incluso en requests rechazados por
 * `authInterceptor` o `errorInterceptor` (§15.1).
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideAnimationsAsync(),
    provideHttpClient(
      withInterceptors([correlationInterceptor, authInterceptor, errorInterceptor]),
    ),
    // Captura de errores JS no manejados (§15.3)
    { provide: ErrorHandler, useClass: GlobalErrorHandler },
  ],
};
