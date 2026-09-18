import { Directive, input, signal } from '@angular/core';

/**
 * Resalta el fondo de un elemento al enfocarlo con foco de teclado o mouse.
 * Ejemplo de directiva compartida — ver `LIN-FE-ANG-001 §5.1`.
 *
 * Usa el objeto `host` del decorador `@Directive` (binding y listeners
 * declarativos de Angular) en lugar de manipular `classList`/`style`
 * directamente sobre el DOM, tal como exige `LIN-FE-ANG-001 §15.2`
 * ("Prohibición de manipulación directa del DOM").
 *
 * Uso: `<div [onpHighlight]="'var(--onp-accent)'">` — si se usa como
 * atributo estático sin corchetes (`<div onpHighlight>`), Angular pasa `''`
 * como valor del input (mismo nombre que el selector) y pisa el default del
 * signal; usar siempre binding de propiedad si se requiere el color por
 * defecto explícitamente.
 */
@Directive({
  selector: '[onpHighlight]',
  standalone: true,
  host: {
    '[style.backgroundColor]': 'backgroundColor()',
    '(mouseenter)': 'activar()',
    '(focus)': 'activar()',
    '(mouseleave)': 'desactivar()',
    '(blur)': 'desactivar()',
  },
})
export class HighlightDirective {
  onpHighlight = input<string>('var(--onp-primary-light)');

  private activo = signal(false);

  readonly backgroundColor = () => (this.activo() ? this.onpHighlight() : null);

  activar(): void {
    this.activo.set(true);
  }

  desactivar(): void {
    this.activo.set(false);
  }
}
