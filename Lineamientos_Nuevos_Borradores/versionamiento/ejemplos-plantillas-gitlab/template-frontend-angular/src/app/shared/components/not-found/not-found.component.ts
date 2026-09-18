import { Component } from '@angular/core';

import { ErrorPageComponent } from '../error-page/error-page.component';

/**
 * Componente de destino de la ruta comodín `**` (`LIN-FE-ANG-001 §6.1`).
 * Reutiliza `ErrorPageComponent` sin parámetro `:code`, que por defecto
 * resuelve al mensaje de 404 "No encontrado" (§7.3).
 */
@Component({
  selector: 'onp-not-found',
  standalone: true,
  imports: [ErrorPageComponent],
  template: `<onp-error-page></onp-error-page>`,
})
export class NotFoundComponent {}
