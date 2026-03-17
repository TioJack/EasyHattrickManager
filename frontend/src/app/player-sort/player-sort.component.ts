import {Component, OnInit} from '@angular/core';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';
import {TranslatePipe} from '@ngx-translate/core';
import {PlayerSort, Project} from '../services/model/data-response';
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
    this.sort.mode = newMode;
    const currentConfig = this.userConfigService.getUserConfig();
    if (currentConfig) {
      let selectedProject: Project | null = null;
      const updatedConfig = {
        ...currentConfig,
        projects: currentConfig.projects.map(project => {
          if (project.name !== this.projectName) {
            return project;
          }
          selectedProject = {
            ...project,
            sort: {
              mode: newMode,
              criteria: project.sort?.criteria || 'id'
            }
          };
          return selectedProject;
        })
      };
      if (selectedProject) {
        this.userConfigService.setUserConfig(updatedConfig);
        this.playService.updateSelectedProject(selectedProject);
        this.userConfigService.saveUserConfig(updatedConfig);
      }
    }
  }

  onCriteriaChange(event: Event): void {
    const newCriteria = (event.target as HTMLInputElement).value || 'id';
    this.sort.criteria = newCriteria;
    const currentConfig = this.userConfigService.getUserConfig();
    if (currentConfig) {
      let selectedProject: Project | null = null;
      const updatedConfig = {
        ...currentConfig,
        projects: currentConfig.projects.map(project => {
          if (project.name !== this.projectName) {
            return project;
          }
          selectedProject = {
            ...project,
            sort: {
              mode: project.sort?.mode || 'asc',
              criteria: newCriteria
            }
          };
          return selectedProject;
        })
      };
      if (selectedProject) {
        this.userConfigService.setUserConfig(updatedConfig);
        this.playService.updateSelectedProject(selectedProject);
        this.userConfigService.saveUserConfig(updatedConfig);
      }
    }
  }

}
