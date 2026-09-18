import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LoginComponent, NoopAnimationsModule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => httpMock.verify());

  it('marca el formulario como inválido si los campos están vacíos', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();

    fixture.componentInstance.onSubmit();

    expect(fixture.componentInstance.frmLogin.invalid).toBeTrue();
  });

  it('llama al servicio de login y navega a /home cuando las credenciales son correctas', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    spyOn(router, 'navigate');

    fixture.componentInstance.frmLogin.setValue({ usuario: 'jperez', clave: 'secreto' });
    fixture.componentInstance.onSubmit();

    const req = httpMock.expectOne((r) => r.url.endsWith('/auth/login'));
    req.flush({
      codHttp: 200,
      codDetRespuesta: '000',
      menDetRespuesta: 'Éxito',
      data: { token: 'tok-123', nombre: 'Juan Pérez', rol: 'ADMIN' },
      errors: null,
      meta: null,
    });

    expect(router.navigate).toHaveBeenCalledWith(['/home']);
  });
});
