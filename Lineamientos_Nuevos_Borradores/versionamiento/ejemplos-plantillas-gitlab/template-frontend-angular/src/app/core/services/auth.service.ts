import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { ApiBaseService } from './api-base.service';

/**
 * Credenciales del formulario de login (tipo de vista "Login", `LIN-FE-ANG-001 §7`).
 */
export interface LoginRequest {
  usuario: string;
  clave: string;
}

/** Datos mínimos de sesión devueltos por el endpoint de autenticación SAA. */
export interface SesionUsuario {
  token: string;
  nombre: string;
  rol: string;
}

/**
 * Servicio de sesión y token SAA.
 *
 * Implementa el escenario **9.3.1 — sin BFF (canal único)** de `LIN-FE-ANG-001`:
 * Angular es responsable de adjuntar el token en cada request mediante
 * `core/interceptors/auth.interceptor.ts`. Si el sistema real requiere el
 * patrón Token Handler (`LIN-FE-ANG-001 §9.3.2` / `LIN-DIS-001 §5.1.1`) por
 * atender más de un canal, este servicio y `auth.interceptor.ts` NO se usan —
 * se reemplazan por un `sessionInterceptor` con cookie `HttpOnly`.
 *
 * El token SAA es opaco (no es JWT): este servicio nunca lo decodifica, solo
 * lo transporta. El almacenamiento en `sessionStorage` (se limpia al cerrar la
 * pestaña) es una elección conservadora del template — el mecanismo de
 * almacenamiento cliente autorizado es normado por `LIN-SEC-APP-001 §10.1`,
 * documento fuera del alcance de este scaffold; validar contra esa norma antes
 * de usar este servicio en un sistema real.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = inject(ApiBaseService);
  private readonly storageKey = 'onp.saa.token';

  private sesion = signal<SesionUsuario | null>(this.leerSesionAlmacenada());

  readonly usuarioActual = computed(() => this.sesion());
  readonly estaAutenticado = computed(() => this.sesion() !== null);

  login(request: LoginRequest): Observable<ApiResponse<SesionUsuario>> {
    return this.api
      .post<SesionUsuario>(`${environment.apiUrl}/auth/login`, request)
      .pipe(
        tap((respuesta) => {
          if (respuesta.data) {
            this.guardarSesion(respuesta.data);
          }
        }),
      );
  }

  logout(): void {
    this.sesion.set(null);
    sessionStorage.removeItem(this.storageKey);
  }

  getToken(): string | null {
    return this.sesion()?.token ?? null;
  }

  isAuthenticated(): boolean {
    return this.estaAutenticado();
  }

  private guardarSesion(sesion: SesionUsuario): void {
    this.sesion.set(sesion);
    sessionStorage.setItem(this.storageKey, JSON.stringify(sesion));
  }

  private leerSesionAlmacenada(): SesionUsuario | null {
    try {
      const crudo = sessionStorage.getItem(this.storageKey);
      return crudo ? (JSON.parse(crudo) as SesionUsuario) : null;
    } catch {
      return null;
    }
  }
}
