import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'numberSeparator',
  standalone: true
})
export class NumberSeparatorPipe implements PipeTransform {
  transform(value: number | string | null | undefined, separator: string = ' '): string {
    if (value === null || value === undefined) return '';
    const num = Number(value);
    if (!Number.isFinite(num)) return String(value);
    const sign = num < 0 ? '-' : '';
    const intStr = Math.trunc(Math.abs(num)).toString();
    const withSep = intStr.replace(/\B(?=(\d{3})+(?!\d))/g, separator);
    return sign + withSep;
  }
}
