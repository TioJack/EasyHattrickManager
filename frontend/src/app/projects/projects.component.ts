import {Component, OnInit} from '@angular/core';
import {DataResponse, Project} from '../services/model/data-response';
import {CommonModule, NgForOf, NgIf} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {DataService} from '../services/data.service';
import {TranslatePipe} from '@ngx-translate/core';
import {FirstLetterPipe} from '../pipes/first-letter.pipe';
import {Router, RouterLink} from '@angular/router';
import {UserConfigService} from '../services/user-config.service';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    FormsModule,
    CommonModule,
    TranslatePipe,
    FirstLetterPipe,
    RouterLink,
    FirstCapitalizePipe
  ],
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.scss']
})
export class ProjectsComponent implements OnInit {
  loading: boolean = false;
  dataResponse: DataResponse | null = null;
  editableProjects: Project[] = [];

  constructor(
    private dataService: DataService,
    private userConfigService: UserConfigService,
    private router: Router) {
  }

  ngOnInit(): void {
    this.loading = true;
    this.dataService.getData().subscribe({
      next: (response: DataResponse) => {
        this.loading = false;
        this.dataResponse = response;
        this.editableProjects = JSON.parse(JSON.stringify(this.dataResponse.userConfig.projects));
      },
      error: (error) => {
        this.loading = false;
      }
    });
  }

  getTeamNameById(teamId: number): string {
    if (!this.dataResponse || !this.dataResponse.teams) {
      return '';
    }
    const team = this.dataResponse.teams.find(team => team.team.id == teamId);
    return team ? team.team.name : '' + teamId;
  }

  getWeeklyOptionsForTeam(teamId: number) {
    if (!this.dataResponse || !this.dataResponse.teams) {
      return [];
    }
    const teamInfo = this.dataResponse.teams.find(team => team.team.id == teamId);
    return teamInfo ? teamInfo.weeklyData : [];
  }

  updateIniSeasonAndWeek(project: Project, value: string): void {
    const [season, week] = value.split('-').map(v => parseInt(v, 10));
    project.iniSeason = season;
    project.iniWeek = week;
  }

  updateEndSeasonAndWeek(project: Project, value: string): void {
    const [season, week] = value.split('-').map(v => parseInt(v, 10));
    project.endSeason = season;
    project.endWeek = week;
  }

  toggleEndEnabled(project: Project) {
    if (project.endSeason != null && project.endWeek != null) {
      project.endSeason = undefined;
      project.endWeek = undefined;
    } else {
      const options = this.getWeeklyOptionsForTeam(project.teamId);
      if (options.length > 0) {
        const lastOption = options[options.length - 1];
        project.endSeason = lastOption.season;
        project.endWeek = lastOption.week;
      }
    }
  }

  onTeamChange(project: Project): void {
    const options = this.getWeeklyOptionsForTeam(project.teamId);
    if (options.length > 0) {
      project.iniSeason = options[0].season;
      project.iniWeek = options[0].week;
      project.endSeason = undefined;
      project.endWeek = undefined;
    }
  }

  addProject() {
    this.editableProjects.push({
      name: '',
      teamId: 0,
      iniSeason: 0,
      iniWeek: 0,
      filter: {mode: 'exclusive', playerIds: []},
      sort: {mode: 'asc', criteria: 'id'}
    });
  }

  removeProject(index: number) {
    this.editableProjects.splice(index, 1);
  }

  saveChanges() {
    if (this.dataResponse) {
      const currentConfig = this.dataResponse.userConfig;
      const updatedConfig = {...currentConfig, projects: this.editableProjects};
      this.userConfigService.setAndSaveUserConfig(updatedConfig);
      this.router.navigate(['/home']);
    }
  }

}
