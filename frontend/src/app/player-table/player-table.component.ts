import {SubskillInputComponent} from '../subskill-input/subskill-input.component';
import {EditableSubskill, SubskillEditService} from '../services/subskill-edit.service';
import {AsyncPipe, NgForOf, NgIf, NgStyle} from '@angular/common';
import {Component, Input} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslatePipe} from '@ngx-translate/core';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';
import {FirstLetterPipe} from '../pipes/first-letter.pipe';
import {NumberSeparatorPipe} from '../pipes/number-separator.pipe';
import {SalaryPipe} from '../pipes/salary.pipe';
import {TruncateDecimalsPipe} from '../pipes/truncate-decimals.pipe';
import {PlayerInfo, PlayerSubSkillInfo, PlayerTrainingInfo, TrainingInfo} from '../services/model/data-response';
import {UserConfigService} from '../services/user-config.service';
import {trainingTypeColor} from '../constants/global.constant';

interface SkillColumn {
  key: 'leadership' | 'experience' | 'playerForm' | 'loyalty' | 'staminaSkill' | 'keeperSkill' |
    'defenderSkill' | 'playmakerSkill' | 'wingerSkill' | 'passingSkill' | 'scorerSkill' | 'setPiecesSkill';
  label: string;
  sub?: Exclude<keyof PlayerSubSkillInfo, 'htms' | 'htms28'>;
  training?: keyof PlayerTrainingInfo;
  groupStart?: boolean;
}

@Component({
  selector: 'app-player-table',
  standalone: true,
  imports: [SubskillInputComponent, AsyncPipe, NgForOf, NgIf, NgStyle, RouterLink, TranslatePipe, FirstCapitalizePipe,
    FirstLetterPipe, NumberSeparatorPipe, SalaryPipe],
  templateUrl: './player-table.component.html',
  styleUrls: ['./player-table.component.scss']
})
export class PlayerTableComponent {
  @Input() players: PlayerInfo[] = [];
  @Input() training: TrainingInfo | null = null;
  readonly userConfig$: UserConfigService['userConfig$'];
  readonly skillColumns: SkillColumn[] = [
    {key: 'leadership', label: 'ht.skill-leadership', groupStart: true},
    {key: 'experience', label: 'ht.skill-experience'},
    {key: 'playerForm', label: 'ht.skill-form'},
    {key: 'staminaSkill', label: 'ht.skill-stamina', sub: 'stamina'},
    {key: 'loyalty', label: 'ehm.loyalty'},
    {key: 'keeperSkill', label: 'ht.skill-keeper', sub: 'keeper', training: 'keeper', groupStart: true},
    {key: 'defenderSkill', label: 'ht.skill-defender', sub: 'defender', training: 'defender'},
    {key: 'playmakerSkill', label: 'ht.skill-playmaker', sub: 'playmaker', training: 'playmaker'},
    {key: 'wingerSkill', label: 'ht.skill-winger', sub: 'winger', training: 'winger'},
    {key: 'passingSkill', label: 'ht.skill-passer', sub: 'passing', training: 'passing'},
    {key: 'scorerSkill', label: 'ht.skill-scorer', sub: 'scorer', training: 'scorer'},
    {key: 'setPiecesSkill', label: 'ht.skill-kicker', sub: 'setPieces', training: 'setPieces'}
  ];
  private readonly truncateDecimals = new TruncateDecimalsPipe();

  constructor(private userConfigService: UserConfigService, public subskillEditor: SubskillEditService) {
    this.userConfig$ = this.userConfigService.userConfig$;
  }

  trackByPlayerId(_index: number, player: PlayerInfo): number {
    return player.id;
  }

  editableSkill(column: SkillColumn): EditableSubskill | null {
    return column.sub ?? (column.key === 'playerForm' ? 'form' : column.key === 'experience' ? 'experience' : null);
  }

  getSkillParts(player: PlayerInfo, column: SkillColumn, showSubSkills: boolean): string[] {
    let value = player[column.key];
    const hasSublevel = showSubSkills && (column.sub != null || column.key === 'playerForm' || column.key === 'experience');
    if (hasSublevel && column.sub) {
      return [String(value), this.truncateDecimals.transform(player.playerSubSkill?.[column.sub] ?? 0, 2).split('.')[1]];
    } else if (hasSublevel && column.key === 'experience') {
      value = player.playerExpInfo?.exp ?? value;
    } else if (hasSublevel && column.key === 'playerForm' && value < 8) {
      const form = player.playerFormInfo?.form ?? value;
      value = Math.floor(form) === value ? form : (form > value ? value + 0.99 : value);
    }
    return this.truncateDecimals.transform(value, hasSublevel ? 2 : 0).split('.');
  }

  getTrainingStyle(player: PlayerInfo, column: SkillColumn, showTrainingInfo: boolean): Record<string, string> {
    const percent = column.training ? player.playerTraining?.[column.training] : 0;
    if (!showTrainingInfo || !percent) {
      return {};
    }
    const color = trainingTypeColor(this.training?.trainingType);
    const rgb = [1, 3, 5].map(offset => parseInt(color.slice(offset, offset + 2), 16));
    return {
      'background-color': `rgba(${rgb.join(',')}, ${percent / 100})`,
      color: percent > 70 ? '#f7f8f7' : 'inherit',
      '--player-subskill-color': percent > 70 ? '#f7f8f7' : 'var(--bs-primary)'
    };
  }
}
