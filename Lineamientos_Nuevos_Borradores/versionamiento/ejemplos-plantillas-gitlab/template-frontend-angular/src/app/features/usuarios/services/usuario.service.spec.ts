import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { UsuarioService } from './usuario.service';
import { environment } from '../../../../environments/environment';

describe('UsuarioService', () => {
  let service: UsuarioService;
  let httpMock: HttpTestingController;
  const base = `${environment.apiUrl}/usuarios`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UsuarioService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('listar() construye la URL correcta con los filtros como query params', () => {
    service.listar({ nombre: 'Juan', pagina: 1 }).subscribe();

    const req = httpMock.expectOne(
      (r) => r.url === base && r.params.get('nombre') === 'Juan' && r.params.get('pagina') === '1',
    );
    expect(req.request.method).toBe('GET');
    req.flush({ codHttp: 200, codDetRespuesta: '000', menDetRespuesta: '', data: [], errors: null, meta: null });
  });

  it('crear() envía POST al endpoint base', () => {
    const request = { nombre: 'Juan Pérez', dni: '12345678', correo: 'juan@onp.gob.pe' };
    service.crear(request).subscribe();

    const req = httpMock.expectOne(base);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({ codHttp: 201, codDetRespuesta: '000', menDetRespuesta: '', data: null, errors: null, meta: null });
  });

  it('actualizar() envía PUT al endpoint con id', () => {
    service.actualizar(5, { nombre: 'Juan', correo: 'juan@onp.gob.pe', estado: 'ACTIVO' }).subscribe();

    const req = httpMock.expectOne(`${base}/5`);
    expect(req.request.method).toBe('PUT');
    req.flush({ codHttp: 200, codDetRespuesta: '000', menDetRespuesta: '', data: null, errors: null, meta: null });
  });

  it('eliminar() envía DELETE al endpoint con id', () => {
    service.eliminar(5).subscribe();

    const req = httpMock.expectOne(`${base}/5`);
    expect(req.request.method).toBe('DELETE');
    req.flush({ codHttp: 200, codDetRespuesta: '000', menDetRespuesta: '', data: null, errors: null, meta: null });
  });
});
