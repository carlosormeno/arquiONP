import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { ApiBaseService } from './api-base.service';
import { ApiResponse } from '../../shared/models/api-response.model';

describe('ApiBaseService', () => {
  let service: ApiBaseService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ApiBaseService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('se crea correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('get() construye la URL correcta y retorna el ApiResponse tal cual', () => {
    const respuestaEsperada: ApiResponse<{ id: number }> = {
      codHttp: 200,
      codDetRespuesta: '000',
      menDetRespuesta: 'Éxito',
      data: { id: 1 },
      errors: null,
      meta: null,
    };

    service.get('/api/v1/usuarios').subscribe((respuesta) => {
      expect(respuesta).toEqual(respuestaEsperada);
    });

    const req = httpMock.expectOne('/api/v1/usuarios');
    expect(req.request.method).toBe('GET');
    req.flush(respuestaEsperada);
  });

  it('post() envía el body correcto', () => {
    const body = { nombre: 'Prueba' };
    service.post('/api/v1/usuarios', body).subscribe();

    const req = httpMock.expectOne('/api/v1/usuarios');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({
      codHttp: 201,
      codDetRespuesta: '000',
      menDetRespuesta: 'Creado',
      data: null,
      errors: null,
      meta: null,
    });
  });
});
