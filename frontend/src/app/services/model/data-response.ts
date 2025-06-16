export interface DataResponse {
  version: string;
  user: User;
  teams: TeamExtendedInfo[];
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
  trainerId: number;
  trainerName: string;
  trainerType: number;
  trainerLeadership: number;
  trainerSkillLevel: number;
  trainerStatus: number;

  staff1Id?: number;
  staff1Name?: string;
  staff1Type?: number;
  staff1Level?: number;
  staff1HofPlayerId?: number;

  staff2Id?: number;
  staff2Name?: string;
  staff2Type?: number;
  staff2Level?: number;
  staff2HofPlayerId?: number;

  staff3Id?: number;
  staff3Name?: string;
  staff3Type?: number;
  staff3Level?: number;
  staff3HofPlayerId?: number;

  staff4Id?: number;
  staff4Name?: string;
  staff4Type?: number;
  staff4Level?: number;
  staff4HofPlayerId?: number;
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
  playerForm: number;
  experience: number;
  loyalty: number;
  motherClubBonus: boolean;
  leadership: number;
  salary: number;
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
}
