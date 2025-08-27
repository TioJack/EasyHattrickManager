export interface PlayerStats {
  total: TotalStats;
  average: AverageStats;
}

export interface TotalStats {
  players: number;
  tsi: number;
  wage: number;
}

export interface AverageStats {
  tsi: number;
  wage: number;
  age: number;
  form: number;
  stamina: number;
  experience: number;
}
