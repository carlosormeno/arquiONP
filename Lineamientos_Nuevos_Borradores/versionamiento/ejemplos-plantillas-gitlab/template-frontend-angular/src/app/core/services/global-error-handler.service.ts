import { ErrorHandler, Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../../environments/environment';

/**
 * `ErrorHandler` global — captura errores JS no manejados por `errorInterceptor`
 * (errores de lógica, excepciones en componentes) y los reporta al backend en
 * producción. Ver `LIN-FE-ANG-001 §15.3`.
 *
 * Nota de implementación: el código de referencia de `§15.3` usa
 * `environment.apiBase`, campo que NO existe en el modelo de `environment`
 * definido en `§13.1` (que solo declara `apiUrl`). Se usa aquí `apiUrl` por
 * consistencia con el resto del scaffold; la discrepancia entre `§13.1` y
 * `§15.3` debe resolverse en el lineamiento (ver reporte de entrega).
 */
@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  private http = inject(HttpClient);

  handleError(error: unknown): void {
    console.error(error);

    if (environment.production) {
      this.http
        .post(`${environment.apiUrl}/logs/frontend-error`, {
          message: error instanceof Error ? error.message : String(error),
          stack: error instanceof Error ? error.stack : null,
          url: window.location.href,
          userAgent: navigator.userAgent,
        })
        // Callback vacío a propósito: si el propio envío del log falla, no queremos que el
        // error handler global vuelva a lanzar (bucle de errores). Excepción documentada a
        // no-empty-function.
        // eslint-disable-next-line @typescript-eslint/no-empty-function
        .subscribe({ error: () => {} });
    }
  }
}
