import {Component, Input} from '@angular/core';
import {TranslatePipe} from '@ngx-translate/core';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';

@Component({
  selector: 'app-staff-level',
  imports: [
    TranslatePipe,
    FirstCapitalizePipe
  ],
  templateUrl: './staff-level.component.html'
})
export class StaffLevelComponent {
  @Input() type: number = 0;
  @Input() level: number = 0;

  get images(): number[] {
    const count = Number.isFinite(this.level) && this.level > 0 ? this.level : 0;
    return Array.from({length: count}, (_, i) => i);
  }

  src(): string {
    return `assets/staff/${this.type}.png`;
  }

}
