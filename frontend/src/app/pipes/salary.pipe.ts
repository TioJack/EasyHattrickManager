import {Pipe, PipeTransform} from '@angular/core';
import {Currency} from '../services/model/data-response';

@Pipe({
  name: 'salary'
})
export class SalaryPipe implements PipeTransform {

  transform(value: number, currency: Currency): string {
    return '' + (value / currency.currencyRate).toFixed(0) + ' ' + currency.currencyCode;
  }

}
