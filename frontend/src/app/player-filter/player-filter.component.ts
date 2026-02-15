import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {PlayerFilter, PlayerInfo, UserConfig} from '../services/model/data-response';
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
export class PlayerFilterComponent implements OnInit, OnDestroy {
  @Input() players: PlayerInfo[] | undefined;
  projectName: string = '';
  filter: PlayerFilter = {mode: 'exclusive', playerIds: []};
  selectedPlayerIds = new Set<number>();
  private persistDebounceTimer: number | null = null;
  private pendingConfigToSave: UserConfig | null = null;
  private readonly persistDebounceMs = 300;

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
        this.refreshSelectedIds();
      }
    });
  }

  ngOnDestroy(): void {
    if (this.persistDebounceTimer != null) {
      window.clearTimeout(this.persistDebounceTimer);
      this.persistDebounceTimer = null;
    }
    if (this.pendingConfigToSave) {
      this.playService.update();
      this.userConfigService.saveUserConfig(this.pendingConfigToSave);
      this.pendingConfigToSave = null;
    }
  }

  onFilterModeChange(newMode: string): void {
    this.filter.mode = newMode as 'inclusive' | 'exclusive';
    this.persistFilter();
  }

  onPlayerChange(player: PlayerInfo, event: Event): void {
    const isChecked = (event.target as HTMLInputElement).checked;
    if (isChecked) {
      this.selectedPlayerIds.add(player.id);
    } else {
      this.selectedPlayerIds.delete(player.id);
    }
    this.filter.playerIds = Array.from(this.selectedPlayerIds);
    this.persistFilter();
  }

  selectAll(): void {
    const allIds = (this.players ?? []).map(p => p.id);
    this.filter.playerIds = [...allIds];
    this.refreshSelectedIds();
    this.persistFilter();
  }

  deselectAll(): void {
    this.filter.playerIds = [];
    this.refreshSelectedIds();
    this.persistFilter();
  }

  invertSelection(): void {
    const all = new Set((this.players ?? []).map(p => p.id));
    const selected = new Set(this.filter.playerIds);
    const inverted: number[] = [];
    all.forEach(id => {
      if (!selected.has(id)) {
        inverted.push(id);
      }
    });
    this.filter.playerIds = inverted;
    this.refreshSelectedIds();
    this.persistFilter();
  }

  isPlayerSelected(playerId: number): boolean {
    return this.selectedPlayerIds.has(playerId);
  }

  trackByPlayerId(_index: number, player: PlayerInfo): number {
    return player.id;
  }

  private persistFilter(): void {
    const currentConfig = this.userConfigService.getUserConfig();
    if (!currentConfig) {
      return;
    }
    const updatedConfig = {...currentConfig};
    const activeProject = updatedConfig.projects.find(
      (project) => project.name === this.projectName
    );
    if (activeProject) {
      if (!activeProject.filter) {
        activeProject.filter = {mode: this.filter.mode, playerIds: []};
      }
      activeProject.filter.playerIds = [...this.filter.playerIds];
      activeProject.filter.mode = this.filter.mode ?? 'exclusive';
    }
    this.userConfigService.setUserConfig(updatedConfig);
    this.scheduleSave(updatedConfig);
  }

  private refreshSelectedIds(): void {
    this.selectedPlayerIds = new Set(this.filter.playerIds ?? []);
  }

  private scheduleSave(config: UserConfig): void {
    if (this.persistDebounceTimer != null) {
      window.clearTimeout(this.persistDebounceTimer);
    }
    this.pendingConfigToSave = JSON.parse(JSON.stringify(config)) as UserConfig;
    this.persistDebounceTimer = window.setTimeout(() => {
      this.playService.update();
      if (this.pendingConfigToSave) {
        this.userConfigService.saveUserConfig(this.pendingConfigToSave);
        this.pendingConfigToSave = null;
      }
      this.persistDebounceTimer = null;
    }, this.persistDebounceMs);
  }

}
