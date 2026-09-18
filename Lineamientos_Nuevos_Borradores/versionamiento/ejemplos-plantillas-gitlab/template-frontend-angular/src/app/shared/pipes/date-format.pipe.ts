import { Pipe, PipeTransform } from '@angular/core';

/**
 * Formatea una fecha en el patrón corto usado en tablas y reportes ONP
 * (`dd/mm/aaaa`). Ejemplo de pipe compartido — ver `LIN-FE-ANG-001 §5.1`.
 */
@Pipe({
  name: 'onpDateFormat',
  standalone: true,
})
export class DateFormatPipe implements PipeTransform {
  transform(value: string | Date | null | undefined): string {
    if (!value) {
      return '';
    }
    const fecha = value instanceof Date ? value : new Date(value);
    if (Number.isNaN(fecha.getTime())) {
      return '';
    }
    const dia = String(fecha.getDate()).padStart(2, '0');
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const anio = fecha.getFullYear();
    return `${dia}/${mes}/${anio}`;
  }
}
