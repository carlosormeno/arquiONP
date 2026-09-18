import { CommonModule } from '@angular/common';
import {
  Component,
  DestroyRef,
  EventEmitter,
  Output,
  inject,
  signal,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { ActivatedRoute, ActivatedRouteSnapshot, NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, map, startWith } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/services/auth.service';

interface BreadcrumbItem {
  label: string;
  url: string;
}

/**
 * Cabecera del layout de cuatro zonas ONP (`LIN-FE-ANG-001 §6.2`).
 *
 * Elementos obligatorios: logo, nombre del sistema (`environment.systemName`),
 * usuario y rol autenticado, fecha/hora actualizada cada minuto, enlace
 * "Cerrar sesión" y breadcrumb dinámico generado desde el router.
 */
@Component({
  selector: 'onp-header',
  standalone: true,
  imports: [CommonModule, RouterLink, MatToolbarModule, MatButtonModule, MatIconModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
})
export class HeaderComponent {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  @Output() toggleMenu = new EventEmitter<void>();

  readonly systemName = environment.systemName;
  readonly usuario = this.authService.usuarioActual;

  readonly fechaHora = signal(new Date());

  readonly breadcrumbs = signal<BreadcrumbItem[]>(this.construirBreadcrumb(this.route.snapshot));

  constructor() {
    // Reloj de cabecera — se actualiza cada minuto (§6.2). Basado en un
    // temporizador RxJS gestionado por Signals, no en manipulación del DOM
    // ni en setTimeout como hack de detección de cambios (§15.2): el binding
    // del template reacciona solo a la Signal `fechaHora`.
    const intervaloId = window.setInterval(() => this.fechaHora.set(new Date()), 60_000);
    this.destroyRef.onDestroy(() => window.clearInterval(intervaloId));

    this.router.events
      .pipe(
        filter((evento) => evento instanceof NavigationEnd),
        startWith(null),
        map(() => this.construirBreadcrumb(this.route.snapshot)),
      )
      .subscribe((migas) => this.breadcrumbs.set(migas));
  }

  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  private construirBreadcrumb(snapshot: ActivatedRouteSnapshot): BreadcrumbItem[] {
    const items: BreadcrumbItem[] = [];
    let actual: ActivatedRouteSnapshot | null = snapshot.root;
    let url = '';

    while (actual) {
      const segmento = actual.url.map((s) => s.path).join('/');
      if (segmento) {
        url += `/${segmento}`;
        const label =
          (actual.data?.['breadcrumb'] as string | undefined) ??
          this.capitalizar(segmento);
        items.push({ label, url });
      }
      actual = actual.firstChild;
    }

    return items;
  }

  private capitalizar(texto: string): string {
    return texto.charAt(0).toUpperCase() + texto.slice(1).replace(/-/g, ' ');
  }
}
