import { Component, computed, effect, inject, signal } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { MenuItem } from '../../shared/models/menu-item.model';
import { AuthService } from '../../core/services/auth.service';
import { SIDEBAR_MENU } from './sidebar-menu.config';

const STORAGE_KEY = 'onp.sidebar.collapsed';

/**
 * Menú lateral general del layout ONP (`LIN-FE-ANG-001 §6.3`): jerarquía de
 * dos niveles máximo, implementado con `MatNavList`, filtrado por roles y con
 * estado colapsado/expandido persistido en `localStorage`.
 */
@Component({
  selector: 'onp-sidebar',
  standalone: true,
  imports: [MatListModule, MatIconModule, MatExpansionModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class SidebarComponent {
  private authService = inject(AuthService);

  readonly collapsed = signal(this.leerEstadoAlmacenado());

  readonly items = computed<MenuItem[]>(() => {
    const rol = this.authService.usuarioActual()?.rol;
    return this.filtrarPorRol(SIDEBAR_MENU, rol);
  });

  constructor() {
    effect(() => {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(this.collapsed()));
      } catch {
        // Almacenamiento no disponible (modo privado, cuota excedida): el
        // estado colapsado simplemente no persiste entre sesiones.
      }
    });
  }

  toggleCollapsed(): void {
    this.collapsed.update((valor) => !valor);
  }

  private filtrarPorRol(items: MenuItem[], rol: string | undefined): MenuItem[] {
    return items
      .filter((item) => !item.roles || !rol || item.roles.includes(rol))
      .map((item) => ({
        ...item,
        children: item.children ? this.filtrarPorRol(item.children, rol) : undefined,
      }));
  }

  private leerEstadoAlmacenado(): boolean {
    try {
      return localStorage.getItem(STORAGE_KEY) === 'true';
    } catch {
      return false;
    }
  }
}
