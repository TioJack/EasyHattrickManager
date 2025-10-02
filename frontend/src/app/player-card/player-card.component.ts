import {Component, Input, OnInit} from '@angular/core';
import {PlayerInfo, PlayerTrainingInfo, TrainingInfo, UserConfig} from '../services/model/data-response';
import {LowerCasePipe, NgIf, NgStyle} from '@angular/common';
import {UserConfigService} from '../services/user-config.service';
import {SalaryPipe} from '../pipes/salary.pipe';
import {SkillComponent} from '../skill/skill.component';
import {TranslatePipe} from '@ngx-translate/core';
import {FirstLetterPipe} from '../pipes/first-letter.pipe';
import {SpecialtyComponent} from '../specialty/specialty.component';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';
import {NumberSeparatorPipe} from '../pipes/number-separator.pipe';
import {trainingTypeColor} from '../constants/training-colors.constant';
import {DEFAULT_AVATAR} from '../constants/avatar.constant';

@Component({
  selector: 'app-player-card',
  standalone: true,
  imports: [NgIf, SalaryPipe, SkillComponent, TranslatePipe, FirstLetterPipe, SpecialtyComponent, LowerCasePipe, FirstCapitalizePipe, NumberSeparatorPipe, NgStyle],
  templateUrl: './player-card.component.html',
  styleUrls: ['./player-card.component.scss']
})
export class PlayerCardComponent implements OnInit {
  @Input() player?: PlayerInfo;
  @Input() training?: TrainingInfo | null | undefined;
  userConfig: UserConfig | null | undefined;

  constructor(private userConfigService: UserConfigService) {
  }

  ngOnInit(): void {
    this.userConfigService.userConfig$.subscribe(userConfig => {
      this.userConfig = userConfig;
    });
  }

  getTraining(property: keyof PlayerTrainingInfo): string {
    if (!this.userConfig?.showTrainingInfo) {
      return '';
    }
    if (this.player?.playerTraining && this.player.playerTraining[property]) {
      return this.player.playerTraining[property].toFixed(2);
    }
    return '';
  }

  getBackgroundColor(property: keyof PlayerTrainingInfo): object {
    if (!this.userConfig?.showTrainingInfo) {
      return {};
    }
    if (this.player?.playerTraining && this.player.playerTraining[property]) {
      const baseColor = trainingTypeColor(this.training?.trainingType);
      const r = parseInt(baseColor.slice(1, 3), 16);
      const g = parseInt(baseColor.slice(3, 5), 16);
      const b = parseInt(baseColor.slice(5, 7), 16);
      const alpha = this.player.playerTraining[property] / 100;
      if (alpha > 0.7) {
        return {
          'background-color': `rgba(${r}, ${g}, ${b}, ${alpha})`,
          'color': '#f7f8f7',
        };
      }
      return {'background-color': `rgba(${r}, ${g}, ${b}, ${alpha})`};
    }
    return {};
  }

  onImageError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = DEFAULT_AVATAR;
  }

}
