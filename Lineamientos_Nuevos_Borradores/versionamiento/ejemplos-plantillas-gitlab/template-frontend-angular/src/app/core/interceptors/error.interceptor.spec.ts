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
import { Router } from '@angular/router';

import { errorInterceptor } from './error.interceptor';
import { NotificationService } from '../services/notification.service';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let routerSpy: jasmine.SpyObj<Router>;
  let notifierSpy: jasmine.SpyObj<NotificationService>;

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    notifierSpy = jasmine.createSpyObj('NotificationService', [
      'showError',
      'showSuccess',
      'showWarning',
    ]);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: Router, useValue: routerSpy },
        { provide: NotificationService, useValue: notifierSpy },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // Los cuatro tests suscriben con un callback `error` vacío a propósito: el interceptor
  // ya maneja el error (redirige o notifica) y a la suscripción del test solo le interesa
  // verificar ese efecto, no el valor propagado — sin este callback, Jasmine reportaría
  // el error como no manejado. Excepción puntual y documentada a no-empty-function.

  it('redirige a /login ante 401', () => {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    http.get('/api/v1/usuarios').subscribe({ error: () => {} });
    httpMock.expectOne('/api/v1/usuarios').flush(null, { status: 401, statusText: 'Unauthorized' });
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('redirige a /error/403 ante 403', () => {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    http.get('/api/v1/usuarios').subscribe({ error: () => {} });
    httpMock.expectOne('/api/v1/usuarios').flush(null, { status: 403, statusText: 'Forbidden' });
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/error/403']);
  });

  it('llama a NotificationService.showError ante 500', () => {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    http.get('/api/v1/usuarios').subscribe({ error: () => {} });
    httpMock
      .expectOne('/api/v1/usuarios')
      .flush(null, { status: 500, statusText: 'Internal Server Error' });
    expect(notifierSpy.showError).toHaveBeenCalled();
  });

  it('llama a NotificationService.showError ante 429 sin redirigir', () => {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    http.get('/api/v1/usuarios').subscribe({ error: () => {} });
    httpMock
      .expectOne('/api/v1/usuarios')
      .flush(null, { status: 429, statusText: 'Too Many Requests' });
    expect(notifierSpy.showError).toHaveBeenCalled();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });
});
