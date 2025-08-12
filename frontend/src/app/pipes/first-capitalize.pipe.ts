import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'firstCapitalize',
  standalone: true
})
export class FirstCapitalizePipe implements PipeTransform {
  transform(value: string): string {
    if (!value) {
      return '';
    }
    return value.charAt(0).toUpperCase() + value.slice(1);
  }
}

