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
    const raw = Number(value);
    if (!Number.isFinite(raw)) return '';
    // Truncate the canonical decimal representation to avoid losing a hundredth
    // when a saved value such as 0.29 becomes 28.999999999999996 after scaling.
    const [coefficient, exponent = '0'] = Math.abs(raw).toString().split('e');
    const [integer, fraction = ''] = coefficient.split('.');
    const numerals = integer + fraction;
    const point = integer.length + Number(exponent);
    const whole = point > 0 ? numerals.padEnd(point, '0').slice(0, point) : '0';
    const decimals = (point < 0 ? '0'.repeat(-point) + numerals : numerals.slice(point))
      .padEnd(digits, '0').slice(0, digits);
    return `${raw < 0 ? '-' : ''}${whole}${digits ? '.' + decimals : ''}`;
  }
}
