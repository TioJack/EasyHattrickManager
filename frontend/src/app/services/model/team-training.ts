import {PlayerInfo} from './data-response';
import {WeekInfo} from '../../player/model/week-info';

export interface TeamTrainingRequest {
  iniWeek: WeekInfo;
  players: TeamTrainingPlayer[];
  stages: TrainingStage[];
  participations: StagePlayerParticipation[];
}

export interface TeamTrainingPlayer {
  player: PlayerInfo;
  inclusionWeek: number;
}

export interface TrainingStage {
  id: number;
  duration: number;
  coach: number;
  assistants: number;
  intensity: number;
  stamina: number;
  training: string;
}

export interface StagePlayerParticipation {
  stageId: number;
  playerId: number;
  participation: number;
}

export interface TeamTrainingResponse {
  endWeek: WeekInfo;
  weekPlayers: Record<number, PlayerInfo[]>;
}
