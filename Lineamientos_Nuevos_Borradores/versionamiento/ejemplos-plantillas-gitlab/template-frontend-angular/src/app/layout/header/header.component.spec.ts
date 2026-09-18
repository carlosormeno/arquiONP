import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { HeaderComponent } from './header.component';
import { AuthService } from '../../core/services/auth.service';

describe('HeaderComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HeaderComponent, NoopAnimationsModule],
      providers: [provideRouter([])],
    });
  });

  it('se crea correctamente', () => {
    const fixture = TestBed.createComponent(HeaderComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('emite toggleMenu al presionar el botón de menú', () => {
    const fixture = TestBed.createComponent(HeaderComponent);
    fixture.detectChanges();

    let emitido = false;
    fixture.componentInstance.toggleMenu.subscribe(() => (emitido = true));

    const boton = fixture.nativeElement.querySelector('button[aria-label="Mostrar u ocultar el menú"]') as HTMLButtonElement;
    boton.click();

    expect(emitido).toBeTrue();
  });

  it('cerrarSesion() limpia la sesión de AuthService', () => {
    const fixture = TestBed.createComponent(HeaderComponent);
    fixture.detectChanges();
    const authService = TestBed.inject(AuthService);
    spyOn(authService, 'logout');

    fixture.componentInstance.cerrarSesion();

    expect(authService.logout).toHaveBeenCalled();
  });
});
