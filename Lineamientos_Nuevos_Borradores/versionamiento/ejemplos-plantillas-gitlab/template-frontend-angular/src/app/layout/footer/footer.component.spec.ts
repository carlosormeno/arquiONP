import { TestBed } from '@angular/core/testing';

import { FooterComponent } from './footer.component';

describe('FooterComponent', () => {
  it('muestra el texto institucional fijo y la versión del environment', () => {
    TestBed.configureTestingModule({ imports: [FooterComponent] });
    const fixture = TestBed.createComponent(FooterComponent);
    fixture.detectChanges();

    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('Oficina de Normalización Previsional — OTI');
    expect(texto).toContain('v0.1.0');
  });
});
