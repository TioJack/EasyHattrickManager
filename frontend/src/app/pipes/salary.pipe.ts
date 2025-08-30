import {Pipe, PipeTransform} from '@angular/core';
import {Currency} from '../services/model/data-response';

@Pipe({
  name: 'salary'
})
export class SalaryPipe implements PipeTransform {

  transform(value: number, currency: Currency, separator: string = ' '): string {
    const rate = currency?.currencyRate ?? 1;
    const amount = Math.round(value / rate);
    const formatted = this.formatWithSeparator(amount, separator);
    return `${formatted} ${currency.currencyCode}`;
  }

  private formatWithSeparator(num: number, separator: string): string {
    const sign = num < 0 ? '-' : '';
    const intStr = Math.trunc(Math.abs(num)).toString();
    const withSep = intStr.replace(/\B(?=(\d{3})+(?!\d))/g, separator);
    return sign + withSep;
  }


}
