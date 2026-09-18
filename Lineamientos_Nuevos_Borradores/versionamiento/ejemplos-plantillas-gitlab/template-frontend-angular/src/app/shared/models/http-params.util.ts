import { HttpParams } from '@angular/common/http';

/**
 * Convierte un objeto de filtros (p.ej. un `*Request` de listado) en
 * `HttpParams`, omitiendo valores `undefined`/`null`/cadena vacía.
 *
 * Utilidad referenciada implícitamente por el ejemplo de `UsuarioService` en
 * `LIN-FE-ANG-001 §9.2` (`toHttpParams(filtros)`), que no publica su código;
 * esta es la implementación de referencia del scaffold.
 */
export function toHttpParams<T extends object>(filtros: T): HttpParams {
  let params = new HttpParams();
  for (const [clave, valor] of Object.entries(filtros)) {
    if (valor !== undefined && valor !== null && valor !== '') {
      params = params.set(clave, String(valor));
    }
  }
  return params;
}
