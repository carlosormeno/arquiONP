import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';

import { UsuarioFormComponent } from './usuario-form.component';
import { UsuarioService } from '../../services/usuario.service';
import { NotificationService } from '../../../../core/services/notification.service';

describe('UsuarioFormComponent', () => {
  let usuarioServiceSpy: jasmine.SpyObj<UsuarioService>;
  let routerSpy: jasmine.SpyObj<Router>;

  function configurar(idRuta: string | null) {
    usuarioServiceSpy = jasmine.createSpyObj('UsuarioService', ['crear', 'actualizar', 'obtener']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [UsuarioFormComponent, NoopAnimationsModule],
      providers: [
        { provide: UsuarioService, useValue: usuarioServiceSpy },
        { provide: NotificationService, useValue: jasmine.createSpyObj('NotificationService', ['showSuccess', 'showError']) },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap(idRuta ? { id: idRuta } : {}) } },
        },
      ],
    });
  }

  it('no llama al servicio si el formulario es inválido (campos requeridos vacíos)', () => {
    configurar(null);
    const fixture = TestBed.createComponent(UsuarioFormComponent);
    fixture.detectChanges();

    fixture.componentInstance.guardar();

    expect(usuarioServiceSpy.crear).not.toHaveBeenCalled();
    expect(fixture.componentInstance.frmRegistro.invalid).toBeTrue();
  });

  it('guardar() llama a crear() con los datos del formulario cuando es válido (modo alta)', () => {
    configurar(null);
    usuarioServiceSpy.crear.and.returnValue(
      of({ codHttp: 201, codDetRespuesta: '000', menDetRespuesta: '', data: null, errors: null, meta: null }),
    );

    const fixture = TestBed.createComponent(UsuarioFormComponent);
    fixture.detectChanges();

    fixture.componentInstance.frmRegistro.patchValue({
      nombre: 'Juan Pérez',
      dni: '12345678',
      correo: 'juan@onp.gob.pe',
    });
    fixture.componentInstance.guardar();

    expect(usuarioServiceSpy.crear).toHaveBeenCalledWith({
      nombre: 'Juan Pérez',
      dni: '12345678',
      correo: 'juan@onp.gob.pe',
    });
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/usuarios']);
  });

  it('en modo edición carga el usuario y deshabilita el campo DNI', () => {
    configurar('5');
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

    const fixture = TestBed.createComponent(UsuarioFormComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance.esEdicion()).toBeTrue();
    expect(fixture.componentInstance.frmRegistro.get('dni')?.disabled).toBeTrue();
    expect(fixture.componentInstance.frmRegistro.get('nombre')?.value).toBe('Ana Torres');
  });
});
