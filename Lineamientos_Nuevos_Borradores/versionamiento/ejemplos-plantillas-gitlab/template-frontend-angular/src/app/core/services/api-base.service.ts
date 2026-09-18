import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiResponse } from '../../shared/models/api-response.model';

/**
 * Servicio base HTTP. Todos los servicios de dominio delegan en este servicio
 * en lugar de inyectar `HttpClient` directamente, para estandarizar el tipo
 * de retorno (`ApiResponse<T>`) en toda la aplicación.
 *
 * Ver `LIN-FE-ANG-001 §9.2`.
 */
@Injectable({ providedIn: 'root' })
export class ApiBaseService {
  private http = inject(HttpClient);

  get<T>(path: string, params?: HttpParams): Observable<ApiResponse<T>> {
    return this.http.get<ApiResponse<T>>(path, { params });
  }

  post<T>(path: string, body: unknown): Observable<ApiResponse<T>> {
    return this.http.post<ApiResponse<T>>(path, body);
  }

  put<T>(path: string, body: unknown): Observable<ApiResponse<T>> {
    return this.http.put<ApiResponse<T>>(path, body);
  }

  delete<T>(path: string): Observable<ApiResponse<T>> {
    return this.http.delete<ApiResponse<T>>(path);
  }
}
