import {Component, Input, OnInit} from '@angular/core';
import {NgIf} from '@angular/common';
import {TranslatePipe} from '@ngx-translate/core';
import {FirstLetterPipe} from '../pipes/first-letter.pipe';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';
import {NumberSeparatorPipe} from '../pipes/number-separator.pipe';
import {SalaryPipe} from '../pipes/salary.pipe';
import {PlayerInfo, UserConfig} from '../services/model/data-response';
import {UserConfigService} from '../services/user-config.service';
import {SkillComponent} from '../skill/skill.component';
import {TruncateDecimalsPipe} from '../pipes/truncate-decimals.pipe';

@Component({
  selector: 'app-player-card-compact',
  standalone: true,
  imports: [NgIf, TranslatePipe, FirstLetterPipe, FirstCapitalizePipe, NumberSeparatorPipe, SalaryPipe, SkillComponent],
  templateUrl: './player-card-compact.component.html',
  styleUrls: ['./player-card-compact.component.scss']
})
export class PlayerCardCompactComponent implements OnInit {
  @Input() player?: PlayerInfo;
  userConfig: UserConfig | null = null;
  private readonly truncateDecimalsPipe = new TruncateDecimalsPipe();

  constructor(private userConfigService: UserConfigService) {
  }

  ngOnInit(): void {
    this.userConfigService.userConfig$.subscribe(config => {
      this.userConfig = config;
    });
  }

  getDecimalParts(value: number | null | undefined, digits: number = 2): { whole: string; decimal: string } | null {
    const formatted = this.truncateDecimalsPipe.transform(value, digits);
    if (!formatted) {
      return null;
    }
    const [whole, decimal] = formatted.split('.');
    return {whole, decimal: decimal ?? ''};
  }
}
