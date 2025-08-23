import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {PlayerInfo, Project, TeamExtendedInfo} from './model/data-response';

@Injectable({
  providedIn: 'root'
})
export class PlayService {
  private selectedProjectSubject = new BehaviorSubject<Project | null>(null);
  private selectedTeamSubject = new BehaviorSubject<TeamExtendedInfo | null>(null);
  private selectedSeasonSubject = new BehaviorSubject<number | null>(null);
  private selectedWeekSubject = new BehaviorSubject<number | null>(null);
  private playersSubject = new BehaviorSubject<PlayerInfo[]>([]);

  selectedProject$ = this.selectedProjectSubject.asObservable();
  selectedTeam$ = this.selectedTeamSubject.asObservable();
  selectedSeason$ = this.selectedSeasonSubject.asObservable();
  selectedWeek$ = this.selectedWeekSubject.asObservable();
  players$ = this.playersSubject.asObservable();

  selectProject(project: Project): void {
    this.selectedProjectSubject.next(project);
  }

  selectTeam(team: TeamExtendedInfo): void {
    this.selectedTeamSubject.next(team);
    this.clearSeasonWeek();
  }

  private selectSeasonAndWeek(season: number, week: number): void {
    this.selectedSeasonSubject.next(season);
    this.selectedWeekSubject.next(week);
    const team = this.selectedTeamSubject.value;
    if (team) {
      const matchingWeek = team.weeklyData.find(
        data => data.season === season && data.week === week
      );
      if (matchingWeek) {
        const filter = this.selectedProjectSubject.value?.filter;
        let filteredPlayers = matchingWeek.players;
        if (filter) {
          if (filter.mode === 'inclusive') {
            filteredPlayers = filteredPlayers.filter(player => filter.playerIds.includes(player.id));
          } else if (filter.mode === 'exclusive') {
            filteredPlayers = filteredPlayers.filter(player => !filter.playerIds.includes(player.id));
          }
        }
        const sort = this.selectedProjectSubject.value?.sort;
        let sortedPlayers = filteredPlayers;
        if (sort) {
          sortedPlayers = filteredPlayers.sort((a, b) => {
            let valueA;
            let valueB;
            if (sort.criteria === 'name') {
              valueA = `${a.firstName || ''} ${a.nickName || ''} ${a.lastName || ''}`.trim();
              valueB = `${b.firstName || ''} ${b.nickName || ''} ${b.lastName || ''}`.trim();
            } else if (sort.criteria === 'age') {
              valueA = (a.age || 0) + ((a.ageDays || 0) / 111);
              valueB = (b.age || 0) + ((b.ageDays || 0) / 111);
            } else {
              valueA = (a as any)[sort.criteria] !== undefined ? (a as any)[sort.criteria] : null;
              valueB = (b as any)[sort.criteria] !== undefined ? (b as any)[sort.criteria] : null;
            }

            if (valueA == null && valueB != null) return 1;
            if (valueA != null && valueB == null) return -1;
            if (valueA == null && valueB == null) {
              return sort.mode === 'asc' ? a.id - b.id : b.id - a.id;
            }

            if (typeof valueA === 'string' && typeof valueB === 'string') {
              const result = valueA.localeCompare(valueB);
              if (result !== 0) {
                return sort.mode === 'asc' ? result : -result;
              }
            }

            if (valueA < valueB) return sort.mode === 'asc' ? -1 : 1;
            if (valueA > valueB) return sort.mode === 'asc' ? 1 : -1;

            return sort.mode === 'asc' ? a.id - b.id : b.id - a.id;
          });
        }
        this.playersSubject.next(sortedPlayers);
      } else {
        this.playersSubject.next([]);
      }
    }
  }

  private clearSeasonWeek(): void {
    this.selectedSeasonSubject.next(null);
    this.selectedWeekSubject.next(null);
    this.playersSubject.next([]);
  }

  clearAll(): void {
    this.selectedTeamSubject.next(null);
    this.clearSeasonWeek();
  }

  onFirstWeek() {
    const project = this.selectedProjectSubject.value;
    if (project) {
      this.selectSeasonAndWeek(project.iniSeason, project.iniWeek);
    }
  }

  onPreviousSeason() {
    const project = this.selectedProjectSubject.value;
    const currentSeason = this.selectedSeasonSubject.value;
    const currentWeek = this.selectedWeekSubject.value;
    if (project && currentSeason && currentWeek) {
      let nextSeason = currentSeason - 1;
      let nextWeek = currentWeek;
      if (nextSeason == project.iniSeason) {
        if (nextWeek < project.iniWeek) {
          nextWeek = project.iniWeek;
        }
      }
      if (nextSeason < project.iniSeason) {
        nextSeason = project.iniSeason;
        nextWeek = project.iniWeek;
      }
      this.selectSeasonAndWeek(nextSeason, nextWeek);
    }
  }

  onPreviousWeek() {
    const project = this.selectedProjectSubject.value;
    const currentSeason = this.selectedSeasonSubject.value;
    const currentWeek = this.selectedWeekSubject.value;
    if (project && currentSeason && currentWeek) {
      let nextSeason = currentSeason;
      let nextWeek = currentWeek - 1;
      if (nextWeek == 0) {
        nextSeason = currentSeason - 1;
        nextWeek = 16;
      }
      if (nextSeason == project.iniSeason) {
        if (nextWeek < project.iniWeek) {
          nextWeek = project.iniWeek;
        }
      }
      if (nextSeason < project.iniSeason) {
        nextSeason = project.iniSeason;
        nextWeek = project.iniWeek;
      }
      this.selectSeasonAndWeek(nextSeason, nextWeek);
    }
  }

  onLastWeek() {
    const lastWeek = this.getLastSeasonAndWeek();
    this.selectSeasonAndWeek(lastWeek.season, lastWeek.week);
  }

  private getLastSeasonAndWeek(): { season: number, week: number } {
    const project = this.selectedProjectSubject.value;
    if (project) {
      if (project.endSeason && project.endWeek) {
        return {season: project.endSeason, week: project.endWeek};
      } else {
        const team = this.selectedTeamSubject.value;
        if (team) {
          const lastWeeklyData = team.weeklyData[team.weeklyData.length - 1];
          return {season: lastWeeklyData.season, week: lastWeeklyData.week};
        }
      }
    }
    return {season: -1, week: -1};
  }

  onNextSeason() {
    const lastWeek = this.getLastSeasonAndWeek();
    const currentSeason = this.selectedSeasonSubject.value;
    const currentWeek = this.selectedWeekSubject.value;
    if (currentSeason && currentWeek) {
      let nextSeason = currentSeason + 1;
      let nextWeek = currentWeek;
      if (nextSeason == lastWeek.season) {
        if (nextWeek > lastWeek.week) {
          nextWeek = lastWeek.week;
        }
      }
      if (nextSeason > lastWeek.season) {
        nextSeason = lastWeek.season;
        nextWeek = lastWeek.week;
      }
      this.selectSeasonAndWeek(nextSeason, nextWeek);
    }
  }

  onNextWeek() {
    const lastWeek = this.getLastSeasonAndWeek();
    const currentSeason = this.selectedSeasonSubject.value;
    const currentWeek = this.selectedWeekSubject.value;
    if (currentSeason && currentWeek) {
      let nextSeason = currentSeason;
      let nextWeek = currentWeek + 1;
      if (nextWeek == 17) {
        nextSeason = currentSeason + 1;
        nextWeek = 1;
      }
      if (nextSeason == lastWeek.season) {
        if (nextWeek > lastWeek.week) {
          nextWeek = lastWeek.week;
        }
      }
      if (nextSeason > lastWeek.season) {
        nextSeason = lastWeek.season;
        nextWeek = lastWeek.week;
      }
      this.selectSeasonAndWeek(nextSeason, nextWeek);
    }
  }

  update() {
    const currentSeason = this.selectedSeasonSubject.value;
    const currentWeek = this.selectedWeekSubject.value;
    if (currentSeason && currentWeek) {
      this.selectSeasonAndWeek(currentSeason, currentWeek);
    }
  }

}
