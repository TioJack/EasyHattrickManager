import {Component, HostListener, Input, OnInit, ViewChild} from '@angular/core';
import {AuthService} from '../services/auth.service';
import {PlayerInfo, Project, TeamExtendedInfo, WeeklyInfo} from '../services/model/data-response';
import {AsyncPipe, DatePipe, NgForOf} from '@angular/common';
import {PlayService} from '../services/play.service';
import {UserConfigService} from '../services/user-config.service';
import {TranslatePipe} from '@ngx-translate/core';
import {LanguageComponent} from '../language/language.component';
import {CurrencyComponent} from '../currency/currency.component';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';
import {RouterLink} from '@angular/router';
import {PlayerFilterComponent} from '../player-filter/player-filter.component';
import {PlayerSortComponent} from '../player-sort/player-sort.component';
import {DataService} from '../services/data.service';
import {AlertComponent} from '../alert/alert.component';
import {DateFormatComponent} from '../date-format/date-format.component';
import {ShowTrainingInfoComponent} from '../show-training-info/show-training-info.component';
import {ShowSubSkillsComponent} from '../show-sub-skills/show-sub-skills.component';
import {DEFAULT_DATE_FORMAT} from '../constants/global.constant';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [NgForOf, TranslatePipe, LanguageComponent, CurrencyComponent, FirstCapitalizePipe, AsyncPipe, RouterLink, PlayerFilterComponent, PlayerSortComponent, AlertComponent, DatePipe, DateFormatComponent, ShowTrainingInfoComponent, ShowSubSkillsComponent],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  @ViewChild('alert') alertComponent!: AlertComponent;
  @Input() dataResponse: any;
  selectedProject: Project | null = null;
  selectedTeam: TeamExtendedInfo | null = null;
  dateFormat: string | null = null;

  constructor(
    private authService: AuthService,
    protected playService: PlayService,
    private userConfigService: UserConfigService,
    private dataService: DataService
  ) {
  }

  ngOnInit(): void {
    if (this.dataResponse.userConfig) {
      this.userConfigService.setUserConfig(this.dataResponse.userConfig);
    }
    if (this.dataResponse.userConfig.projects.length > 0) {
      this.selectProject(this.dataResponse.userConfig.projects[0]);
    }
    this.userConfigService.userConfig$.subscribe(config => {
      if (config && config.dateFormat != null) {
        this.dateFormat = config.dateFormat;
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }

  downloadData(): void {
    if (this.dataResponse) {
      const dataStr = JSON.stringify(this.dataResponse, null, 2);
      const blob = new Blob([dataStr], {type: 'application/json'});
      const now = new Date();
      const dateStr = `${now.getFullYear()}-${(now.getMonth() + 1).toString().padStart(2, '0')}-${now.getDate().toString().padStart(2, '0')}`;
      const fileName = `hattrick_data_${dateStr}.json`;
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      a.click();
      window.URL.revokeObjectURL(url);
    }
  }

  update(): void {
    this.dataService.update().subscribe({
      next: () => {
        this.alertComponent.showAlert('ehm.update-ok', 'success');
      },
      error: (error) => {
        this.alertComponent.showAlert('ehm.update-fail', 'danger');
      }
    });
  }

  selectProject(project: Project): void {
    this.selectedProject = project;
    this.playService.selectProject(project);
    const team = this.dataResponse.teams.find((team: TeamExtendedInfo) => team.team.id === project.teamId);
    if (!team) {
      return;
    }
    this.selectedTeam = team;
    this.playService.selectTeam(team);
    this.playService.onLastWeek();
  }

  @HostListener('window:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent): void {
    if (event.key === 'ArrowRight') {
      this.playService.onNextWeek();
    }
    if (event.key === 'ArrowLeft') {
      this.playService.onPreviousWeek();
    }
  }

  getFilteredPlayers(): PlayerInfo[] {
    if (!this.selectedProject || !this.dataResponse?.teams) {
      return [];
    }
    const projectTeam = this.dataResponse.teams.find((team: TeamExtendedInfo) => team.team.id == this.selectedProject?.teamId);
    if (!projectTeam) {
      return [];
    }
    const {iniSeason, iniWeek, endSeason, endWeek} = this.selectedProject;
    const filteredPlayers: PlayerInfo[] = [];
    projectTeam.weeklyData.forEach((weekData: WeeklyInfo) => {
      if ((weekData.season > iniSeason || (weekData.season == iniSeason && weekData.week >= iniWeek)) &&
        (endSeason == undefined || endWeek == undefined || weekData.season < endSeason || (weekData.season == endSeason && weekData.week <= endWeek))) {
        filteredPlayers.push(...weekData.players);
      }
    });
    return Array.from(new Map(filteredPlayers.map(player => [player.id, player])).values()).sort((a, b) => a.id - b.id);
  }

  protected readonly DEFAULT_DATE_FORMAT = DEFAULT_DATE_FORMAT;
}
