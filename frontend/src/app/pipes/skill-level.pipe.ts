import {inject, Pipe, PipeTransform} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';

@Pipe({
  name: 'skillLevel',
  standalone: true,
  pure: false
})
export class SkillLevelPipe implements PipeTransform {
  private translateService = inject(TranslateService);

  transform(value: number): string {
    if (0 <= value && value <= 20) {
      return this.translateService.instant('ht.skill-level-' + value);
    } else if (20 < value) {
      return this.translateService.instant('ht.skill-level-20') + ' (+' + (value - 20) + ')';
    }
    return '' + value;
  }

}
