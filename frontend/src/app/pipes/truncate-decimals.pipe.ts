import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'truncateDecimals',
  standalone: true
})
export class TruncateDecimalsPipe implements PipeTransform {
  transform(value: number | null | undefined, digits: number = 2): string {
    if (value == null || Number.isNaN(Number(value))) {
      return '';
    }
    const factor = Math.pow(10, digits);
    const raw = Number(value);
    const truncated = raw >= 0 ? Math.floor(raw * factor) : Math.ceil(raw * factor);
    return (truncated / factor).toFixed(digits);
  }
}
