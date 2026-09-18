import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';

import { ErrorPageComponent } from './error-page.component';

describe('ErrorPageComponent', () => {
  function crearComponente(codigo: string) {
    TestBed.configureTestingModule({
      imports: [ErrorPageComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { paramMap: of(convertToParamMap({ code: codigo })) },
        },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
      ],
    });
    const fixture = TestBed.createComponent(ErrorPageComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('muestra el título y mensaje correspondientes al código 404', () => {
    const fixture = crearComponente('404');
    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('No encontrado');
    expect(texto).toContain('El recurso solicitado no existe.');
  });

  it('muestra el título y mensaje correspondientes al código 403', () => {
    const fixture = crearComponente('403');
    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('Acceso denegado');
  });

  it('usa un mensaje genérico para códigos no catalogados', () => {
    const fixture = crearComponente('999');
    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('Ocurrió un error');
  });
});
