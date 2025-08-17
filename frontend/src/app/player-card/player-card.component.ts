import {Component, Input, OnInit} from '@angular/core';
import {PlayerInfo, UserConfig} from '../services/model/data-response';
import {LowerCasePipe, NgIf} from '@angular/common';
import {UserConfigService} from '../services/user-config.service';
import {SalaryPipe} from '../pipes/salary.pipe';
import {SkillComponent} from '../skill/skill.component';
import {TranslatePipe} from '@ngx-translate/core';
import {FirstLetterPipe} from '../pipes/first-letter.pipe';
import {SpecialtyComponent} from '../specialty/specialty.component';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';

@Component({
  selector: 'app-player-card',
  standalone: true,
  imports: [NgIf, SalaryPipe, SkillComponent, TranslatePipe, FirstLetterPipe, SpecialtyComponent, LowerCasePipe, FirstCapitalizePipe],
  templateUrl: './player-card.component.html',
  styleUrls: ['./player-card.component.scss']
})
export class PlayerCardComponent implements OnInit {
  @Input() player?: PlayerInfo;
  userConfig: UserConfig | null | undefined;

  constructor(private userConfigService: UserConfigService) {
  }

  ngOnInit(): void {
    this.userConfigService.userConfig$.subscribe(userConfig => {
      this.userConfig = userConfig;
    });
  }

  onImageError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = 'assets/defaultAvatar.png';
  }

}
