import { Component, signal } from '@angular/core';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterOutlet } from '@angular/router';

import { FooterComponent } from '../footer/footer.component';
import { HeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';

/**
 * Shell del layout de cuatro zonas ONP: cabecera, menú lateral, contenido
 * (por ruta) y pie de página. Ver `LIN-FE-ANG-001 §6`.
 *
 * Se activa como componente de la ruta raíz protegida por `authGuard`
 * (`app.routes.ts`); la ruta `/login` y las de error no lo usan.
 */
@Component({
  selector: 'onp-shell',
  standalone: true,
  imports: [RouterOutlet, MatSidenavModule, HeaderComponent, SidebarComponent, FooterComponent],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent {
  readonly menuAbierto = signal(true);

  toggleMenu(): void {
    this.menuAbierto.update((valor) => !valor);
  }
}
