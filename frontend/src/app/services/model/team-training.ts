import {PlayerInfo} from './data-response';
import {WeekInfo} from '../../player/model/week-info';

export interface TeamTrainingRequest {
  iniWeek: WeekInfo;
  players: TeamTrainingPlayer[];
  stages: TrainingStage[];
  participations: StagePlayerParticipation[];
  bestFormationCriteria?: BestFormationCriteria;
  fixedFormationCode?: string | null;
  matchDetail?: MatchDetail;
  calculateBestFormation?: boolean;
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
  weekFormationRatings?: Record<number, FormationRating>;
  bestFormationRating?: FormationRating;
  bestWeek?: number;
}

export interface TeamTrainingProgressResponse {
  totalWeeks: number;
  calculatedWeeks: number;
  percent: number;
  inFlight: boolean;
  done: boolean;
}

export type BestFormationCriteria =
  'HATSTATS' | 'RIGHT_DEFENSE' | 'CENTRAL_DEFENSE' | 'LEFT_DEFENSE' | 'DEFENSE' | 'MIDFIELD' | 'MIDFIELD3'
  | 'RIGHT_ATTACK' | 'CENTRAL_ATTACK' | 'LEFT_ATTACK' | 'ATTACK' | 'PEASO_STATS';

export interface MatchDetail {
  tactic: 'NORMAL' | 'PRESSING' | 'COUNTER_ATTACKS' | 'ATTACK_IN_THE_MIDDLE' | 'ATTACK_IN_WINGS' | 'PLAY_CREATIVELY' | 'LONG_SHOTS';
  teamAttitude: 'PIC' | 'PIN' | 'MOTS';
  teamSpirit: 'LIKE_THE_COLD_WAR' | 'MURDEROUS' | 'FURIOUS' | 'IRRITATED' | 'COMPOSED' | 'CALM'
    | 'CONTENT' | 'SATISFIED' | 'DELIRIOUS' | 'WALKING_ON_CLOUDS' | 'PARADISE_ON_EARTH';
  teamSubSpirit: number;
  teamConfidence: 'NON_EXISTENT' | 'DISASTROUS' | 'WRETCHED' | 'POOR' | 'DECENT' | 'STRONG'
    | 'WONDERFUL' | 'SLIGHTLY_EXAGGERATED' | 'EXAGGERATED' | 'COMPLETELY_EXAGGERATED';
  teamSubConfidence: number;
  sideMatch: 'HOME' | 'AWAY_DERBY' | 'AWAY';
  styleOfPlay: number;
}

export interface FormationRating {
  formation: string;
  players: PlayerRating[];
  rating: Ratings;
}

export interface PlayerRating {
  playerId: number;
  position: { role: string; behaviour: string };
  rating: Ratings;
}

export interface Ratings {
  rightDefense: number;
  centralDefense: number;
  leftDefense: number;
  midfield: number;
  rightAttack: number;
  centralAttack: number;
  leftAttack: number;
  attack?: number;
  defense?: number;
  hatStats?: number;
  midfield3?: number;
  peasoStats?: number;
}
