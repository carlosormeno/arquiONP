import { MenuItem } from '../../shared/models/menu-item.model';

/**
 * Configuración estática del menú lateral general (`LIN-FE-ANG-001 §6.3`).
 * En un sistema real, reemplazar por los dominios funcionales reales.
 */
export const SIDEBAR_MENU: MenuItem[] = [
  { label: 'Inicio', icon: 'home', route: '/home' },
  {
    label: 'Usuarios',
    icon: 'people',
    children: [
      { label: 'Listado', icon: 'list', route: '/usuarios' },
      { label: 'Nuevo usuario', icon: 'person_add', route: '/usuarios/new' },
    ],
  },
];
