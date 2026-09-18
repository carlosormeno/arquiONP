import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { ShellComponent } from './shell.component';

describe('ShellComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ShellComponent, NoopAnimationsModule],
      providers: [provideRouter([])],
    });
  });

  it('se crea correctamente con el menú abierto por defecto', () => {
    const fixture = TestBed.createComponent(ShellComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.menuAbierto()).toBeTrue();
  });

  it('toggleMenu() invierte el estado del menú', () => {
    const fixture = TestBed.createComponent(ShellComponent);
    fixture.detectChanges();

    fixture.componentInstance.toggleMenu();

    expect(fixture.componentInstance.menuAbierto()).toBeFalse();
  });
});
