import {Component, Input} from '@angular/core';
import {PlayerInfo} from '../services/model/data-response';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-player-card',
  standalone: true,
  imports: [NgIf],
  templateUrl: './player-card.component.html'
})
export class PlayerCardComponent {
  @Input() player?: PlayerInfo;
}
