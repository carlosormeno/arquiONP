import { CommonModule, Location } from '@angular/common';
import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, Router } from '@angular/router';
import { map } from 'rxjs';

interface ErrorInfo {
  titulo: string;
  mensaje: string;
}

/**
 * Vista de Error estándar ONP (tipo de vista "Error", `LIN-FE-ANG-001 §7.3`).
 * Ruta: `/error/:code`. Se monta fuera del `ShellComponent` (sin menú lateral
 * ni cabecera completa).
 */
const ERRORES: Record<string, ErrorInfo> = {
  '400': {
    titulo: 'Solicitud incorrecta',
    mensaje: 'Los datos enviados no son válidos.',
  },
  '401': {
    titulo: 'No autenticado',
    mensaje: 'Su sesión ha expirado. Inicie sesión nuevamente.',
  },
  '403': {
    titulo: 'Acceso denegado',
    mensaje: 'No tiene permiso para acceder a este recurso.',
  },
  '404': {
    titulo: 'No encontrado',
    mensaje: 'El recurso solicitado no existe.',
  },
  '500': {
    titulo: 'Error del servidor',
    mensaje: 'Se produjo un error interno. Contacte a soporte.',
  },
  '503': {
    titulo: 'Servicio no disponible',
    mensaje: 'El servicio está temporalmente fuera de línea.',
  },
};

const ERROR_GENERICO: ErrorInfo = {
  titulo: 'Ocurrió un error',
  mensaje: 'No fue posible completar la operación solicitada.',
};

@Component({
  selector: 'onp-error-page',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  templateUrl: './error-page.component.html',
  styleUrl: './error-page.component.scss',
})
export class ErrorPageComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private location = inject(Location);

  // Sin parámetro :code (p.ej. cuando se usa embebido desde la ruta comodín
  // '**' vía NotFoundComponent) se asume 404.
  readonly codigo = toSignal(
    this.route.paramMap.pipe(map((params) => params.get('code') ?? '404')),
    { initialValue: '404' },
  );

  readonly info = () => ERRORES[this.codigo()] ?? ERROR_GENERICO;

  regresar(): void {
    if (window.history.length > 1) {
      this.location.back();
    } else {
      this.router.navigate(['/home']);
    }
  }
}
