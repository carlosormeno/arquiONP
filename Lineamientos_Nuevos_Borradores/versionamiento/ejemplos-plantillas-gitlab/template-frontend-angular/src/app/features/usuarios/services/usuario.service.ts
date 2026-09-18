import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ApiBaseService } from '../../../core/services/api-base.service';
import { ApiResponse } from '../../../shared/models/api-response.model';
import { toHttpParams } from '../../../shared/models/http-params.util';
import {
  CreateUsuarioRequest,
  FiltroUsuarioRequest,
  UpdateUsuarioRequest,
  Usuario,
} from '../models/usuario.model';

/**
 * Servicio HTTP de dominio del feature "usuarios". Ver `LIN-FE-ANG-001 §9.2`.
 */
@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private api = inject(ApiBaseService);
  private base = `${environment.apiUrl}/usuarios`;

  listar(filtros: FiltroUsuarioRequest): Observable<ApiResponse<Usuario[]>> {
    return this.api.get<Usuario[]>(this.base, toHttpParams(filtros));
  }

  obtener(id: number): Observable<ApiResponse<Usuario>> {
    return this.api.get<Usuario>(`${this.base}/${id}`);
  }

  crear(request: CreateUsuarioRequest): Observable<ApiResponse<Usuario>> {
    return this.api.post<Usuario>(this.base, request);
  }

  actualizar(id: number, request: UpdateUsuarioRequest): Observable<ApiResponse<Usuario>> {
    return this.api.put<Usuario>(`${this.base}/${id}`, request);
  }

  eliminar(id: number): Observable<ApiResponse<void>> {
    return this.api.delete<void>(`${this.base}/${id}`);
  }
}
