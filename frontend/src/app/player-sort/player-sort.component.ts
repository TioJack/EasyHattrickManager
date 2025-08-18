import {Component, OnInit} from '@angular/core';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';
import {TranslatePipe} from '@ngx-translate/core';
import {PlayerSort} from '../services/model/data-response';
import {PlayService} from '../services/play.service';
import {UserConfigService} from '../services/user-config.service';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-player-sort',
  imports: [
    FirstCapitalizePipe,
    TranslatePipe,
    FormsModule
  ],
  templateUrl: './player-sort.component.html'
})
export class PlayerSortComponent implements OnInit {
  projectName: string = '';
  sort: PlayerSort = {mode: 'asc', criteria: 'id'};

  constructor(
    private playService: PlayService,
    private userConfigService: UserConfigService) {
  }

  ngOnInit(): void {
    this.playService.selectedProject$.subscribe(project => {
      if (project) {
        this.projectName = project.name;
        if (!project.sort) {
          this.sort = {mode: 'asc', criteria: 'id'};
        } else {
          this.sort = project.sort;
          if (!this.sort.mode) {
            this.sort.mode = 'asc';
          }
        }
      }
    });
  }

  onSortModeChange(newMode: string): void {
    const currentConfig = this.userConfigService.getUserConfig();
    if (currentConfig) {
      const updatedConfig = {...currentConfig};
      const activeProject = updatedConfig.projects.find(
        project => project.name === this.projectName
      );
      if (activeProject) {
        if (!activeProject.sort) {
          activeProject.sort = {mode: newMode, criteria: 'id'};
        } else {
          activeProject.sort.mode = newMode;
        }
      }
      this.userConfigService.setAndSaveUserConfig(updatedConfig);
      this.playService.update();
    }
  }

  onCriteriaChange(event: Event): void {
    const newCriteria = (event.target as HTMLInputElement).value || 'id';
    const currentConfig = this.userConfigService.getUserConfig();
    if (currentConfig) {
      const updatedConfig = {...currentConfig};
      const activeProject = updatedConfig.projects.find(
        project => project.name === this.projectName
      );
      if (activeProject) {
        if (!activeProject.sort) {
          activeProject.sort = {mode: 'asc', criteria: newCriteria};
        } else {
          activeProject.sort.criteria = newCriteria;
        }
      }
      this.userConfigService.setAndSaveUserConfig(updatedConfig);
      this.playService.update();
    }
  }

}
