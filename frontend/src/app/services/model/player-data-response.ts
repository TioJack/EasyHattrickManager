import {PlayerInfo, UserConfig} from './data-response';

export interface PlayerDataResponse {
  weeklyData: PlayerWeeklyInfo[];
  userConfig: UserConfig;
}

export interface PlayerWeeklyInfo {
  season: number;
  week: number;
  date: string;
  player: PlayerInfo;
}
