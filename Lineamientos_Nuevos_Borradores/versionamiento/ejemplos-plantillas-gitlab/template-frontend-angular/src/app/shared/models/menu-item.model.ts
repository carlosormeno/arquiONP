/**
 * Ítem del menú lateral general (`LIN-FE-ANG-001 §6.3`): jerarquía de dos
 * niveles máximo, filtrado por roles del usuario autenticado.
 */
export interface MenuItem {
  label: string;
  icon: string;
  route?: string;
  /** Roles habilitados para ver el ítem. Vacío/omitido = visible para todos. */
  roles?: string[];
  children?: MenuItem[];
}
