import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  function configurar(token: string | null): void {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getToken']);
    authServiceSpy.getToken.and.returnValue(token);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceSpy },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  }

  afterEach(() => httpMock.verify());

  it('agrega el header Authorization Bearer cuando hay token', () => {
    configurar('tok-123');
    http.get('/api/v1/usuarios').subscribe();

    const req = httpMock.expectOne('/api/v1/usuarios');
    expect(req.request.headers.get('Authorization')).toBe('Bearer tok-123');
    req.flush({});
  });

  it('no agrega Authorization cuando no hay token', () => {
    configurar(null);
    http.get('/api/v1/usuarios').subscribe();

    const req = httpMock.expectOne('/api/v1/usuarios');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });
});
