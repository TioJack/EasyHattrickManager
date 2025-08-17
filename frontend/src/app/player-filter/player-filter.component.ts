import {Component, Input, OnInit} from '@angular/core';
import {PlayerFilter, PlayerInfo} from '../services/model/data-response';
import {NgForOf, NgIf} from '@angular/common';
import {UserConfigService} from '../services/user-config.service';
import {PlayService} from '../services/play.service';
import {TranslatePipe} from '@ngx-translate/core';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';

@Component({
  selector: 'app-player-filter',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    TranslatePipe,
    FirstCapitalizePipe
  ],
  templateUrl: './player-filter.component.html',
  styleUrls: ['./player-filter.component.scss']
})
export class PlayerFilterComponent implements OnInit {
  @Input() players: PlayerInfo[] | undefined;
  projectName: string = '';
  filter: PlayerFilter = {mode: 'exclusive', playerIds: []};

  constructor(
    private playService: PlayService,
    private userConfigService: UserConfigService) {
  }

  ngOnInit(): void {
    this.playService.selectedProject$.subscribe(project => {
      if (project) {
        this.projectName = project.name;
        if (!project.filter) {
          this.filter = {mode: 'exclusive', playerIds: []};
        } else {
          this.filter = project.filter;
          if (!this.filter.mode) {
            this.filter.mode = 'exclusive';
          }
        }
      }
    });
  }

  onFilterModeChange(newMode: string): void {
    const currentConfig = this.userConfigService.getUserConfig();
    if (currentConfig) {
      const updatedConfig = {...currentConfig};
      const activeProject = updatedConfig.projects.find(
        project => project.name === this.projectName
      );
      if (activeProject) {
        if (!activeProject.filter) {
          activeProject.filter = {mode: newMode, playerIds: []};
        } else {
          activeProject.filter.mode = newMode;
        }
      }
      this.userConfigService.setAndSaveUserConfig(updatedConfig);
      this.playService.update();
    }
  }

  onPlayerChange(player: PlayerInfo, event: Event): void {
    const isChecked = (event.target as HTMLInputElement).checked;
    if (isChecked) {
      if (!this.filter.playerIds.includes(player.id)) {
        this.filter.playerIds.push(player.id);
      }
    } else {
      const playerIndex = this.filter.playerIds.indexOf(player.id);
      if (playerIndex > -1) {
        this.filter.playerIds.splice(playerIndex, 1);
      }
    }
    const currentConfig = this.userConfigService.getUserConfig();
    if (currentConfig) {
      const updatedConfig = {...currentConfig};
      const activeProject = updatedConfig.projects.find(
        (project) => project.name === this.projectName
      );
      if (activeProject) {
        if (!activeProject.filter) {
          activeProject.filter = {mode: this.filter.mode, playerIds: []};
        }
        activeProject.filter.playerIds = [...this.filter.playerIds];
      }
      this.userConfigService.setAndSaveUserConfig(updatedConfig);
      this.playService.update();
    }
  }

}
