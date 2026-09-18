import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';

import { HighlightDirective } from './highlight.directive';

@Component({
  standalone: true,
  imports: [HighlightDirective],
  // Nota: `onpHighlight` es a la vez el selector de la directiva y el nombre
  // de su input. Un atributo estático sin corchetes (`onpHighlight` a secas)
  // se interpreta como el input con valor `''`, pisando el default del
  // signal — por eso aquí se enlaza explícitamente con `[onpHighlight]`.
  template: `<div [onpHighlight]="color">contenido</div>`,
})
class HostTestComponent {
  color = 'var(--onp-primary-light)';
}

describe('HighlightDirective', () => {
  it('aplica el color de fondo al activarse y lo retira al desactivarse', () => {
    const fixture = TestBed.createComponent(HostTestComponent);
    fixture.detectChanges();

    const debugElement = fixture.debugElement.query(By.directive(HighlightDirective));
    const nativeElement = debugElement.nativeElement as HTMLElement;
    const directiva = debugElement.injector.get(HighlightDirective);

    expect(nativeElement.style.backgroundColor).toBe('');

    directiva.activar();
    fixture.detectChanges();
    expect(nativeElement.style.backgroundColor).toContain('var(--onp-primary-light)');

    directiva.desactivar();
    fixture.detectChanges();
    expect(nativeElement.style.backgroundColor).toBe('');
  });
});
