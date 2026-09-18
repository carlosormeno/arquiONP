import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';

import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isAuthenticated']);
    routerSpy = jasmine.createSpyObj('Router', ['createUrlTree']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
    });
  });

  function ejecutarGuard() {
    return TestBed.runInInjectionContext(() =>
      authGuard(null as never, null as never),
    );
  }

  it('permite el acceso con sesión activa', () => {
    authServiceSpy.isAuthenticated.and.returnValue(true);
    expect(ejecutarGuard()).toBeTrue();
  });

  it('bloquea el acceso y redirige a /login sin sesión', () => {
    authServiceSpy.isAuthenticated.and.returnValue(false);
    const urlTree = {} as UrlTree;
    routerSpy.createUrlTree.and.returnValue(urlTree);

    const resultado = ejecutarGuard();

    expect(routerSpy.createUrlTree).toHaveBeenCalledWith(['/login']);
    expect(resultado).toBe(urlTree);
  });
});
