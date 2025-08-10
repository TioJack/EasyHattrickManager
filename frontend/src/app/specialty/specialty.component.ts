import {Component, Input} from '@angular/core';
import {NgIf} from '@angular/common';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-specialty',
  standalone: true,
  imports: [NgIf, TranslatePipe],
  templateUrl: './specialty.component.html',
  styleUrls: ['./specialty.component.scss']
})
export class SpecialtyComponent {
  @Input() specialty?: number;

}
