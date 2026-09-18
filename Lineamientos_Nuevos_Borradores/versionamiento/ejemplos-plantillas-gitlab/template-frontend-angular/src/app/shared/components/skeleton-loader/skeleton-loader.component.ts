import { Component, input } from '@angular/core';

/**
 * Skeleton loader con dimensiones fijas, para usar como `@placeholder` de
 * bloques `@defer` sin introducir layout shift (CLS). Ver
 * `LIN-FE-ANG-001 §15.2` — "Estabilidad del CLS con bloques deferibles".
 *
 * El `@placeholder` que envuelve este componente debe reservar exactamente
 * el mismo alto que el contenido real; este componente no lo calcula por sí
 * mismo, solo dibuja el shimmer dentro del espacio que se le da.
 */
@Component({
  selector: 'onp-skeleton-loader',
  standalone: true,
  templateUrl: './skeleton-loader.component.html',
  styleUrl: './skeleton-loader.component.scss',
})
export class SkeletonLoaderComponent {
  /** Alto del skeleton en CSS (debe igualar el alto del contenido real). */
  height = input('120px');
  /** Texto accesible mientras el contenido real no ha cargado. */
  ariaLabel = input('Cargando contenido');
}
