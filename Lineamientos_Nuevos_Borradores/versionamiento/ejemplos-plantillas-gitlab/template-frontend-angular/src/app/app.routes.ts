import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';

/**
 * Rutas raíz de la aplicación. Ver `LIN-FE-ANG-001 §6.1`.
 *
 * La ruta `''` monta `ShellComponent` (layout de cuatro zonas, §6) protegida
 * por `authGuard`; `/login` y `/error/:code` quedan fuera del shell.
 */
export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./layout/shell/shell.component').then((m) => m.ShellComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'home',
        loadComponent: () =>
          import('./features/home/home.component').then((m) => m.HomeComponent),
        data: { breadcrumb: 'Inicio' },
      },
      {
        path: 'usuarios',
        loadChildren: () =>
          import('./features/usuarios/usuario.routes').then((m) => m.USUARIO_ROUTES),
      },
      { path: '', pathMatch: 'full', redirectTo: 'home' },
    ],
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'error/:code',
    loadComponent: () =>
      import('./shared/components/error-page/error-page.component').then(
        (m) => m.ErrorPageComponent,
      ),
  },
  {
    path: '**',
    loadComponent: () =>
      import('./shared/components/not-found/not-found.component').then(
        (m) => m.NotFoundComponent,
      ),
  },
];
