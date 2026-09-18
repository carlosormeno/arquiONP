import { DateFormatPipe } from './date-format.pipe';

describe('DateFormatPipe', () => {
  const pipe = new DateFormatPipe();

  it('formatea una fecha ISO a dd/mm/aaaa', () => {
    expect(pipe.transform('2026-03-05T00:00:00')).toBe('05/03/2026');
  });

  it('retorna cadena vacía para valores nulos o inválidos', () => {
    expect(pipe.transform(null)).toBe('');
    expect(pipe.transform(undefined)).toBe('');
    expect(pipe.transform('no-es-una-fecha')).toBe('');
  });
});
