import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {PlayerInfo, TeamExtendedInfo} from './model/data-response';

@Injectable({
  providedIn: 'root'
})
export class PlayService {
  private selectedTeamSubject = new BehaviorSubject<TeamExtendedInfo | null>(null);
  private selectedSeasonSubject = new BehaviorSubject<number | null>(null);
  private selectedWeekSubject = new BehaviorSubject<number | null>(null);
  private playersSubject = new BehaviorSubject<PlayerInfo[]>([]);

  selectedTeam$ = this.selectedTeamSubject.asObservable();
  selectedSeason$ = this.selectedSeasonSubject.asObservable();
  selectedWeek$ = this.selectedWeekSubject.asObservable();
  players$ = this.playersSubject.asObservable();

  selectTeam(team: TeamExtendedInfo): void {
    this.selectedTeamSubject.next(team);
    this.clearSeasonWeek();
  }

  getWeekBounds(): { min: number; max: number } {
    const team = this.selectedTeamSubject.value;
    if (team && team.weeklyData.length > 0) {
      return {min: 0, max: team.weeklyData.length - 1};
    }
    return {min: -1, max: -1};
  }

  selectSeasonAndWeek(season: number, week: number): void {
    this.selectedSeasonSubject.next(season);
    this.selectedWeekSubject.next(week);
    const team = this.selectedTeamSubject.value;
    if (team) {
      const matchingWeek = team.weeklyData.find(
        data => data.season === season && data.week === week
      );
      if (matchingWeek) {
        const players = matchingWeek.players.sort((a, b) => a.id - b.id);
        this.playersSubject.next(players);
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

}
