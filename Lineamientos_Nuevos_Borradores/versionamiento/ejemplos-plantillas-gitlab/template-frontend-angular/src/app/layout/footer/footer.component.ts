import { Component } from '@angular/core';

import { environment } from '../../../environments/environment';

/**
 * Pie de página del layout ONP (`LIN-FE-ANG-001 §6.4`).
 * Contenido fijo: "Oficina de Normalización Previsional — OTI | v{version} | {año}".
 */
@Component({
  selector: 'onp-footer',
  standalone: true,
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss',
})
export class FooterComponent {
  readonly version = environment.version;
  readonly anio = new Date().getFullYear();
}
