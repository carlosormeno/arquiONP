import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('no está autenticado sin sesión previa', () => {
    expect(service.isAuthenticated()).toBeFalse();
    expect(service.getToken()).toBeNull();
  });

  it('login() guarda la sesión y expone el token', () => {
    service.login({ usuario: 'jperez', clave: 'secreto' }).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/auth/login'));
    req.flush({
      codHttp: 200,
      codDetRespuesta: '000',
      menDetRespuesta: 'Éxito',
      data: { token: 'tok-123', nombre: 'Juan Pérez', rol: 'ADMIN' },
      errors: null,
      meta: null,
    });

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.getToken()).toBe('tok-123');
  });

  it('logout() limpia la sesión', () => {
    service.login({ usuario: 'jperez', clave: 'secreto' }).subscribe();
    const req = httpMock.expectOne((r) => r.url.endsWith('/auth/login'));
    req.flush({
      codHttp: 200,
      codDetRespuesta: '000',
      menDetRespuesta: 'Éxito',
      data: { token: 'tok-123', nombre: 'Juan Pérez', rol: 'ADMIN' },
      errors: null,
      meta: null,
    });

    service.logout();

    expect(service.isAuthenticated()).toBeFalse();
    expect(service.getToken()).toBeNull();
  });
});
