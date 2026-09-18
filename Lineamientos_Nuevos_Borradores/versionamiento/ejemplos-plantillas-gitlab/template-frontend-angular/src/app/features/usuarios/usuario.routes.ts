import { Routes } from '@angular/router';

/**
 * Rutas lazy del feature "usuarios". Ver `LIN-FE-ANG-001 §4.2`.
 */
export const USUARIO_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/list/usuario-list.component').then((m) => m.UsuarioListComponent),
    data: { breadcrumb: 'Usuarios' },
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./pages/form/usuario-form.component').then((m) => m.UsuarioFormComponent),
    data: { breadcrumb: 'Nuevo' },
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/detail/usuario-detail.component').then((m) => m.UsuarioDetailComponent),
    data: { breadcrumb: 'Detalle' },
  },
  {
    path: ':id/edit',
    loadComponent: () =>
      import('./pages/form/usuario-form.component').then((m) => m.UsuarioFormComponent),
    data: { breadcrumb: 'Editar' },
  },
];
