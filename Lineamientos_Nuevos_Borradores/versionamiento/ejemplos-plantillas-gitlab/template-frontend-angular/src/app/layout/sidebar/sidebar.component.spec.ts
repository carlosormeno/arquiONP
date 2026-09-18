import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { SidebarComponent } from './sidebar.component';

describe('SidebarComponent', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [SidebarComponent, NoopAnimationsModule],
      providers: [provideRouter([])],
    });
  });

  afterEach(() => localStorage.clear());

  it('se crea correctamente y arranca expandido por defecto', () => {
    const fixture = TestBed.createComponent(SidebarComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.collapsed()).toBeFalse();
  });

  it('toggleCollapsed() invierte y persiste el estado en localStorage', () => {
    const fixture = TestBed.createComponent(SidebarComponent);
    fixture.detectChanges();

    fixture.componentInstance.toggleCollapsed();
    fixture.detectChanges();

    expect(fixture.componentInstance.collapsed()).toBeTrue();
    expect(localStorage.getItem('onp.sidebar.collapsed')).toBe('true');
  });

  it('expone los ítems del menú configurado', () => {
    const fixture = TestBed.createComponent(SidebarComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.items().length).toBeGreaterThan(0);
  });
});
