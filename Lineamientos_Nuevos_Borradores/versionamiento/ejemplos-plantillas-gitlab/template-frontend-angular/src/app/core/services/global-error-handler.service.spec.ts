import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { GlobalErrorHandler } from './global-error-handler.service';
import { environment } from '../../../environments/environment';

describe('GlobalErrorHandler', () => {
  let handler: GlobalErrorHandler;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        GlobalErrorHandler,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    handler = TestBed.inject(GlobalErrorHandler);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('no envía el error al backend cuando production es false', () => {
    expect(environment.production).toBeFalse();
    handler.handleError(new Error('falla de prueba'));
    httpMock.expectNone(() => true);
  });
});
