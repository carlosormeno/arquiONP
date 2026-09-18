import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';

import { UsuarioListComponent } from './usuario-list.component';
import { UsuarioService } from '../../services/usuario.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { ApiResponse } from '../../../../shared/models/api-response.model';
import { Usuario } from '../../models/usuario.model';

describe('UsuarioListComponent', () => {
  let usuarioServiceSpy: jasmine.SpyObj<UsuarioService>;

  const usuariosMock: Usuario[] = [
    { id: 1, nombre: 'Juan Pérez', dni: '12345678', correo: 'juan@onp.gob.pe', estado: 'ACTIVO' },
    { id: 2, nombre: 'Ana Torres', dni: '87654321', correo: 'ana@onp.gob.pe', estado: 'INACTIVO' },
  ];

  function respuesta(data: Usuario[]): ApiResponse<Usuario[]> {
    return {
      codHttp: 200,
      codDetRespuesta: '000',
      menDetRespuesta: 'Éxito',
      data,
      errors: null,
      meta: { timestamp: '', requestId: '', version: '', totalElementos: data.length },
    };
  }

  beforeEach(() => {
    usuarioServiceSpy = jasmine.createSpyObj('UsuarioService', ['listar', 'eliminar']);
    usuarioServiceSpy.listar.and.returnValue(of(respuesta(usuariosMock)));

    TestBed.configureTestingModule({
      imports: [UsuarioListComponent, NoopAnimationsModule],
      providers: [
        provideRouter([]),
        { provide: UsuarioService, useValue: usuarioServiceSpy },
        { provide: NotificationService, useValue: jasmine.createSpyObj('NotificationService', ['showSuccess', 'showError']) },
      ],
    });
  });

  it('renderiza filas de datos al cargar', () => {
    const fixture = TestBed.createComponent(UsuarioListComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance.usuarios().length).toBe(2);
    const filas = (fixture.nativeElement as HTMLElement).querySelectorAll('tr[mat-row]');
    expect(filas.length).toBe(2);
  });

  it('los filtros activan una nueva búsqueda al enviar el formulario', () => {
    const fixture = TestBed.createComponent(UsuarioListComponent);
    fixture.detectChanges();
    usuarioServiceSpy.listar.calls.reset();

    fixture.componentInstance.frmFiltros.patchValue({ nombre: 'Juan' });
    fixture.componentInstance.buscar();

    expect(usuarioServiceSpy.listar).toHaveBeenCalledWith(
      jasmine.objectContaining({ nombre: 'Juan', pagina: 0 }),
    );
  });
});
