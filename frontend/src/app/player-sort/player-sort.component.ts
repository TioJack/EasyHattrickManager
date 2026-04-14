import {Component, OnInit} from '@angular/core';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';
import {TranslatePipe} from '@ngx-translate/core';
import {PlayerSort, Project, ProjectTrainingPlanner} from '../services/model/data-response';
import {PlayService, ViewMode} from '../services/play.service';
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
  viewMode: ViewMode = 'players';

  constructor(
    private playService: PlayService,
    private userConfigService: UserConfigService) {
  }

  ngOnInit(): void {
    this.playService.viewMode$.subscribe(mode => {
      this.viewMode = mode;
      this.loadSortFromSelection();
    });
    this.playService.selectedProject$.subscribe(project => {
      if (project) {
        this.projectName = project.name;
        this.loadSortFromSelection();
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
          if (project.name !== this.projectName || project.teamId !== this.playService.getSelectedProject()?.teamId) {
            return project;
          }
          const planner = this.ensurePlanner(project.planner);
          selectedProject = {
            ...project,
            ...(this.viewMode === 'training-planner'
              ? {
                  planner: {
                    ...planner,
                    sort: {
                      mode: newMode,
                      criteria: project.planner?.sort?.criteria || project.sort?.criteria || 'id'
                    }
                  }
                }
              : {
                  sort: {
                    mode: newMode,
                    criteria: project.sort?.criteria || 'id'
                  }
                })
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
          if (project.name !== this.projectName || project.teamId !== this.playService.getSelectedProject()?.teamId) {
            return project;
          }
          const planner = this.ensurePlanner(project.planner);
          selectedProject = {
            ...project,
            ...(this.viewMode === 'training-planner'
              ? {
                  planner: {
                    ...planner,
                    sort: {
                      mode: project.planner?.sort?.mode || project.sort?.mode || 'asc',
                      criteria: newCriteria
                    }
                  }
                }
              : {
                  sort: {
                    mode: project.sort?.mode || 'asc',
                    criteria: newCriteria
                  }
                })
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

  private loadSortFromSelection(): void {
    const project = this.playService.getSelectedProject();
    if (!project) {
      return;
    }
    const activeSort = this.viewMode === 'training-planner'
      ? project.planner?.sort
      : project.sort;
    this.sort = {
      mode: activeSort?.mode || 'asc',
      criteria: activeSort?.criteria || 'id'
    };
  }

  private ensurePlanner(planner?: ProjectTrainingPlanner): ProjectTrainingPlanner {
    return planner ?? {
      trainingPlans: [],
      trainingPlanPercents: {},
      autoRefreshBestFormation: false,
      bestFormationCriteria: 'HATSTATS',
      matchDetail: {
        tactic: 'NORMAL',
        teamAttitude: 'PIN',
        teamSpirit: 'CALM',
        teamSubSpirit: 0.5,
        teamConfidence: 'STRONG',
        teamSubConfidence: 0.5,
        sideMatch: 'AWAY',
        styleOfPlay: 0
      }
    };
  }

}
