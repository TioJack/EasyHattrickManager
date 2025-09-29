const TRAINING_TYPE_COLORS: Record<number, string> = {
  0: '#78909C',  // General (form)
  1: '#66BB6A',  // Stamina
  2: '#546E7A',  // Set pieces
  3: '#7E57C2',  // Defending
  4: '#D84315',  // Scoring
  5: '#26A69A',  // Winger
  6: '#BF6D3A',  // Scoring and Set Pieces
  7: '#F9A825',  // Passing
  8: '#2E7D32',  // Playmaking
  9: '#2F6C8E',  // Keeper
  10: '#FFB74D', // Passing (Def + Mid)
  11: '#5C6BC0', // Defending (GK, Def + Mid)
  12: '#4DB6AC', // Winger (Wingers + Attackers)
  13: '#9575CD'  // Individual
};

const DEFAULT_TRAINING_COLOR = '#9E9E9E';

export function trainingTypeColor(typeId: number | null | undefined): string {
  if (typeId == null) return DEFAULT_TRAINING_COLOR;
  return TRAINING_TYPE_COLORS[typeId] ?? DEFAULT_TRAINING_COLOR;
}
