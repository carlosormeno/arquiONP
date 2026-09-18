import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Componente raíz de la aplicación. No contiene layout propio: el layout de
 * cuatro zonas (LIN-FE-ANG-001 §6) vive en `layout/shell/shell.component.ts`,
 * que se activa como componente de una de las rutas hijas de `app.routes.ts`
 * (la ruta `/login` y las de error, en cambio, no usan el shell).
 */
@Component({
  selector: 'onp-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {}
