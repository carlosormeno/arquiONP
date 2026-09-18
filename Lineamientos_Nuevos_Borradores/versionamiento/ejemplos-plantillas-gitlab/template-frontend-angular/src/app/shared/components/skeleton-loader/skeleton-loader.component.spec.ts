import { TestBed } from '@angular/core/testing';

import { SkeletonLoaderComponent } from './skeleton-loader.component';

describe('SkeletonLoaderComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [SkeletonLoaderComponent] });
  });

  it('se crea correctamente', () => {
    const fixture = TestBed.createComponent(SkeletonLoaderComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('aplica el alto recibido como input', () => {
    const fixture = TestBed.createComponent(SkeletonLoaderComponent);
    fixture.componentRef.setInput('height', '400px');
    fixture.detectChanges();
    const div = (fixture.nativeElement as HTMLElement).querySelector('.onp-skeleton') as HTMLElement;
    expect(div.style.height).toBe('400px');
  });
});
