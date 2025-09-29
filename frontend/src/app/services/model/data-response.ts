export interface DataResponse {
  version: string;
  user: User;
  teams: TeamExtendedInfo[];
  userConfig: UserConfig;
}

export interface User {
  id: number;
  name: string;
}

export interface TeamExtendedInfo {
  team: TeamInfo;
  league: LeagueInfo;
  weeklyData: WeeklyInfo[];
}

export interface TeamInfo {
  id: number;
  name: string;
}

export interface LeagueInfo {
  id: number;
  name: string;
}

export interface WeeklyInfo {
  season: number;
  week: number;
  date: string;
  training: TrainingInfo;
  staff: StaffInfo;
  players: PlayerInfo[];
}

export interface TrainingInfo {
  trainingType: number;
  trainingLevel: number;
  staminaTrainingPart: number;
}

export interface StaffInfo {
  trainer: TrainerInfo;
  staffMembers: StaffMemberInfo[];
}

export interface TrainerInfo {
  id: number;
  name: string;
  type: number;
  leadership: number;
  skillLevel: number;
  status: number;
  startDate: string;
  cost: number;
}

export interface StaffMemberInfo {
  id: number;
  name: string;
  type: number;
  level: number;
  hofPlayerId: number;
  startDate: string;
  cost: number;
}

export interface PlayerInfo {
  id: number;
  firstName: string;
  nickName: string;
  lastName: string;
  agreeability: number;
  aggressiveness: number;
  honesty: number;
  specialty: number;
  countryId: number;
  playerNumber?: number;
  age: number;
  ageDays: number;
  tsi: number;
  arrivalDate: string;
  playerForm: number;
  experience: number;
  loyalty: number;
  motherClubBonus: boolean;
  leadership: number;
  salary: number;
  transferListed: boolean;
  cards: number;
  injuryLevel: number;
  staminaSkill: number;
  keeperSkill: number;
  playmakerSkill: number;
  scorerSkill: number;
  passingSkill: number;
  wingerSkill: number;
  defenderSkill: number;
  setPiecesSkill: number;
  htms: number;
  htms28: number;
  playerCategoryId: number;
  playerTraining: PlayerTrainingInfo;
}

export interface PlayerTrainingInfo {
  keeper: number;
  defender: number;
  playmaker: number;
  winger: number;
  passing: number;
  scorer: number;
  setPieces: number;
}

export interface UserConfig {
  languageId: number;
  currency: Currency;
  dateFormat: string;
  showTrainingInfo: boolean;
  projects: Project[];
}

export interface Currency {
  countryId: number;
  currencyName: string;
  currencyCode: string;
  currencyRate: number;
}

export interface Language {
  id: number;
  name: string;
}

export interface Project {
  name: string;
  teamId: number;
  iniSeason: number;
  iniWeek: number;
  endSeason?: number;
  endWeek?: number;
  filter: PlayerFilter;
  sort: PlayerSort;
}

export interface PlayerFilter {
  mode: string;
  playerIds: number[];
}

export interface PlayerSort {
  mode: string;
  criteria: string;
}
