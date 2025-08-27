import {Component, OnInit} from '@angular/core';
import {NgIf} from '@angular/common';
import {PlayService} from '../services/play.service';
import {PlayerStats} from '../services/model/player-stats';
import {TranslatePipe} from '@ngx-translate/core';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';
import {SalaryPipe} from '../pipes/salary.pipe';
import {UserConfig} from '../services/model/data-response';
import {UserConfigService} from '../services/user-config.service';
import {FirstLetterPipe} from '../pipes/first-letter.pipe';
import {SkillLevelPipe} from '../pipes/skill-level-decimals.pipe';

@Component({
  selector: 'app-player-stats',
  imports: [
    NgIf,
    TranslatePipe,
    FirstCapitalizePipe,
    SalaryPipe,
    FirstLetterPipe,
    SkillLevelPipe
  ],
  templateUrl: './player-stats.component.html'
})
export class PlayerStatsComponent implements OnInit {
  playerStats: PlayerStats | null = null;
  userConfig: UserConfig | null | undefined;

  protected readonly Math = Math;

  constructor(
    private playService: PlayService,
    private userConfigService: UserConfigService
  ) {
  }

  ngOnInit(): void {
    this.playService.playerStats$.subscribe(playerStats => {
      if (playerStats) {
        this.playerStats = playerStats;
      }
    });
    this.userConfigService.userConfig$.subscribe(userConfig => {
      this.userConfig = userConfig;
    });
  }

  numberToYearsDays(ageYears: number): { years: number; days: number } {
    const years = Math.trunc(ageYears);
    let days = Math.trunc((ageYears - years) * 112);
    if (days >= 112) {
      return {years: years + 1, days: 0};
    }
    if (days < 0) {
      return {years: years - 1, days: 112 + days};
    }
    return {years, days};
  }

}
