import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';

import { UsuarioDetailComponent } from './usuario-detail.component';
import { UsuarioService } from '../../services/usuario.service';

describe('UsuarioDetailComponent', () => {
  it('carga y muestra el usuario correspondiente al id de la ruta', () => {
    const usuarioServiceSpy = jasmine.createSpyObj('UsuarioService', ['obtener']);
    usuarioServiceSpy.obtener.and.returnValue(
      of({
        codHttp: 200,
        codDetRespuesta: '000',
        menDetRespuesta: '',
        data: { id: 5, nombre: 'Ana Torres', dni: '87654321', correo: 'ana@onp.gob.pe', estado: 'ACTIVO' },
        errors: null,
        meta: null,
      }),
    );

    TestBed.configureTestingModule({
      imports: [UsuarioDetailComponent],
      providers: [
        provideRouter([]),
        { provide: UsuarioService, useValue: usuarioServiceSpy },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '5' }) } } },
      ],
    });

    const fixture = TestBed.createComponent(UsuarioDetailComponent);
    fixture.detectChanges();

    expect(usuarioServiceSpy.obtener).toHaveBeenCalledWith(5);
    expect(fixture.componentInstance.usuario()?.nombre).toBe('Ana Torres');
  });
});
