import {inject, Pipe, PipeTransform} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';

@Pipe({
  name: 'skillLevelDecimals',
  standalone: true,
  pure: false
})
export class SkillLevelPipe implements PipeTransform {
  private translateService = inject(TranslateService);

  transform(value: number): string {
    const n = Number(value);
    const integer = n < 0 ? Math.ceil(n) : Math.trunc(n);
    const integerStr = this.translateService.instant('ht.skill-level-' + integer);
    const decimal = Math.abs(n - integer);
    let decimalStr;
    if (decimal < 0.25) {
      decimalStr = this.translateService.instant('ehm.very-low');
    } else if (decimal < 0.5) {
      decimalStr = this.translateService.instant('ehm.low');
    } else if (decimal < 0.75) {
      decimalStr = this.translateService.instant('ehm.high');
    } else {
      decimalStr = this.translateService.instant('ehm.very-high');
    }
    return integerStr + ' (' + decimalStr + ')';
  }

}
