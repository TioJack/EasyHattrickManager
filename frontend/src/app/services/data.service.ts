import {Injectable} from '@angular/core';
import {map, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {Currency, DataResponse, Language, PlayerInfo, UserConfig} from './model/data-response';

@Injectable({
  providedIn: 'root'
})
export class DataService {
  private apiUrl = '/backend/data';

  constructor(private http: HttpClient) {
  }

  gatData(): Observable<DataResponse> {
    return this.http.get<DataResponse>(this.apiUrl).pipe(
      map((dataResponse: DataResponse) => {
        dataResponse.teams.forEach(team => {
          team.weeklyData.forEach((weekData, index) => {
            const previousWeek = index > 0 ? team.weeklyData[index - 1] : null;
            if (previousWeek) {
              weekData.players.forEach(player => {
                const previousPlayer: PlayerInfo | null = previousWeek.players.find(prev => prev.id === player.id) ?? null;
                if (previousPlayer) {
                  this.computeChanges(player, previousPlayer);
                }
              });
            }
          });
        });
        console.log(dataResponse);
        return dataResponse;
      })
    );
  }

  private changeFields: (keyof PlayerInfo)[] = [
    'tsi',
    'playerForm',
    'experience',
    'loyalty',
    'leadership',
    'salary',
    'staminaSkill',
    'keeperSkill',
    'playmakerSkill',
    'scorerSkill',
    'passingSkill',
    'wingerSkill',
    'defenderSkill',
    'setPiecesSkill',
    'htms',
    'htms28'
  ];

  private computeChanges(currentPlayer: PlayerInfo, previousPlayer: PlayerInfo): void {
    currentPlayer.changes = {};
    this.changeFields.forEach(field => {
      const currentField: any = currentPlayer[field] ?? 0;
      const previousField: any = previousPlayer[field] ?? 0;
      if (currentPlayer.changes) {
        currentPlayer.changes[field] = currentField - previousField;
      }
    });
  }

  getLanguages(): Observable<Language[]> {
    return this.http.get<Language[]>(`${this.apiUrl}/languages`);
  }

  getCurrencies(): Observable<Currency[]> {
    return this.http.get<Currency[]>(`${this.apiUrl}/currencies`);
  }

  saveUserConfig(config: UserConfig): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/user-config`, config);
  }

  update(): Observable<void> {
    return this.http.get<void>(`${this.apiUrl}/update`);
  }

}
