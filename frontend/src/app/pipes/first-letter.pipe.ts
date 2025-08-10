import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'firstLetter',
  standalone: true
})
export class FirstLetterPipe implements PipeTransform {
  transform(value: string): string {
    if (!value || value.length < 1) {
      return '';
    }
    return value.charAt(0);
  }
}
