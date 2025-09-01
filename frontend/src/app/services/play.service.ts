import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {PlayerInfo, Project, StaffInfo, TeamExtendedInfo, TrainingInfo} from './model/data-response';
import {TrainingStats} from './model/training-stats';
import {PlayerStats} from './model/player-stats';

@Injectable({
  providedIn: 'root'
})
export class PlayService {
  private selectedProjectSubject = new BehaviorSubject<Project | null>(null);
  private selectedTeamSubject = new BehaviorSubject<TeamExtendedInfo | null>(null);
  private selectedSeasonSubject = new BehaviorSubject<number | null>(null);
  private selectedWeekSubject = new BehaviorSubject<number | null>(null);
  private playersSubject = new BehaviorSubject<PlayerInfo[]>([]);
  private dateSubject = new BehaviorSubject<string | null>(null);
  private staffSubject = new BehaviorSubject<StaffInfo | null>(null);
  private trainingSubject = new BehaviorSubject<TrainingInfo | null>(null);
  private trainingStatsSubject = new BehaviorSubject<TrainingStats | null>(null);
  private playerStatsSubject = new BehaviorSubject<PlayerStats | null>(null);

  selectedProject$ = this.selectedProjectSubject.asObservable();
  selectedTeam$ = this.selectedTeamSubject.asObservable();
  selectedSeason$ = this.selectedSeasonSubject.asObservable();
  selectedWeek$ = this.selectedWeekSubject.asObservable();
  players$ = this.playersSubject.asObservable();
  date$ = this.dateSubject.asObservable();
  staff$ = this.staffSubject.asObservable();
  training$ = this.trainingSubject.asObservable();
  trainingStats$ = this.trainingStatsSubject.asObservable();
  playerStats$ = this.playerStatsSubject.asObservable();


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
        this.dateSubject.next(matchingWeek.date);
        this.staffSubject.next(matchingWeek.staff);
        this.trainingSubject.next(matchingWeek.training);
        this.trainingStatsSubject.next(this.computeTrainingStats(team, this.selectedProjectSubject.value, season, week));
        this.playerStatsSubject.next(this.computePlayerStats(sortedPlayers));
      } else {
        this.playersSubject.next([]);
        this.dateSubject.next(null);
        this.staffSubject.next(null);
        this.trainingSubject.next(null);
        this.trainingStatsSubject.next(null);
        this.playerStatsSubject.next(null);
      }
    }
  }

  private clearSeasonWeek(): void {
    this.selectedSeasonSubject.next(null);
    this.selectedWeekSubject.next(null);
    this.playersSubject.next([]);
    this.staffSubject.next(null);
    this.playerStatsSubject.next(null);
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
    if (project != null && currentSeason != null && currentWeek != null) {
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
    if (project != null && currentSeason != null && currentWeek != null) {
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
    if (currentSeason != null && currentWeek != null) {
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
    if (currentSeason != null && currentWeek != null) {
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

  private computeTrainingStats(
    team: TeamExtendedInfo,
    project: Project | null,
    toSeason: number,
    toWeek: number
  ): TrainingStats {
    const start = project
      ? {season: project.iniSeason, week: project.iniWeek}
      : this.getFirstSeasonAndWeekFromTeam(team);

    const endLimit = project?.endSeason && project?.endWeek
      ? {season: project.endSeason, week: project.endWeek}
      : this.getLastSeasonAndWeekFromTeam(team);

    const end = this.minSeasonWeek({season: toSeason, week: toWeek}, endLimit);

    const inRange = team.weeklyData.filter(w =>
      this.isBetweenInclusive({season: w.season, week: w.week}, start, end)
    );

    const weeks = inRange.length;

    const trainingCounts: Record<number, number> = {};
    for (const w of inRange) {
      const type = w.training?.trainingType ?? -1;
      trainingCounts[type] = (trainingCounts[type] ?? 0) + 1;
    }

    return {weeks, trainings: trainingCounts};
  }

  private getFirstSeasonAndWeekFromTeam(team: TeamExtendedInfo): { season: number, week: number } {
    const first = team.weeklyData[0];
    return {season: first.season, week: first.week};
  }

  private getLastSeasonAndWeekFromTeam(team: TeamExtendedInfo): { season: number, week: number } {
    const last = team.weeklyData[team.weeklyData.length - 1];
    return {season: last.season, week: last.week};
  }

  private isBetweenInclusive(
    x: { season: number; week: number },
    a: { season: number; week: number },
    b: { season: number; week: number }
  ): boolean {
    return this.compareSeasonWeek(a, x) <= 0 && this.compareSeasonWeek(x, b) <= 0;
  }

  private minSeasonWeek(
    a: { season: number; week: number },
    b: { season: number; week: number }
  ): { season: number; week: number } {
    return this.compareSeasonWeek(a, b) <= 0 ? a : b;
  }

  private compareSeasonWeek(
    x: { season: number; week: number },
    y: { season: number; week: number }
  ): number {
    if (x.season !== y.season) return x.season - y.season;
    return x.week - y.week;
  }

  private computePlayerStats(players: PlayerInfo[]): PlayerStats {
    const count = players.length;

    let sumTSI = 0;
    let sumWage = 0;
    let sumAge = 0;
    let sumForm = 0;
    let sumStamina = 0;
    let sumExperience = 0;

    for (const player of players) {
      sumTSI += player.tsi ?? 0;
      sumWage += player.salary ?? 0;
      const ageYears = (player.age ?? 0) + ((player.ageDays ?? 0) / 111);
      sumAge += ageYears;
      sumForm += player.playerForm ?? 0;
      sumStamina += player.staminaSkill ?? 0;
      sumExperience += player.experience ?? 0;
    }

    const total = {
      players: count,
      tsi: sumTSI,
      wage: sumWage
    };

    const average = {
      tsi: count ? sumTSI / count : 0,
      wage: count ? sumWage / count : 0,
      age: count ? sumAge / count : 0,
      form: count ? sumForm / count : 0,
      stamina: count ? sumStamina / count : 0,
      experience: count ? sumExperience / count : 0
    };

    return {total, average};
  }

}
