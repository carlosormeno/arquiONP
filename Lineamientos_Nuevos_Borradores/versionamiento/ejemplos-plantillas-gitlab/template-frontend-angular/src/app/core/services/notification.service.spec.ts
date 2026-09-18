import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';

import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  let service: NotificationService;
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;

  beforeEach(() => {
    snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    TestBed.configureTestingModule({
      providers: [{ provide: MatSnackBar, useValue: snackBarSpy }],
    });
    service = TestBed.inject(NotificationService);
  });

  it('showSuccess() abre un snackbar con panelClass snack-success', () => {
    service.showSuccess('Operación exitosa');
    expect(snackBarSpy.open).toHaveBeenCalledWith(
      'Operación exitosa',
      'Cerrar',
      jasmine.objectContaining({ panelClass: ['snack-success'] }),
    );
  });

  it('showError() abre un snackbar con panelClass snack-error', () => {
    service.showError('Ocurrió un error');
    expect(snackBarSpy.open).toHaveBeenCalledWith(
      'Ocurrió un error',
      'Cerrar',
      jasmine.objectContaining({ panelClass: ['snack-error'] }),
    );
  });

  it('showWarning() abre un snackbar con panelClass snack-warning', () => {
    service.showWarning('Advertencia');
    expect(snackBarSpy.open).toHaveBeenCalledWith(
      'Advertencia',
      'Cerrar',
      jasmine.objectContaining({ panelClass: ['snack-warning'] }),
    );
  });
});
