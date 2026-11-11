import {CommonModule} from '@angular/common';
import {Component, Input, OnChanges, OnDestroy, SimpleChanges} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Subscription} from 'rxjs';
import {DataService} from '../services/data.service';
import {PlayerInfo, ProjectTrainingPlanner} from '../services/model/data-response';
import {MatchDetail, Ratings} from '../services/model/team-training';

interface Slot {
  id: number;
  label: string;
  role: string;
  group: 'keeper' | 'defence' | 'midfield' | 'attack';
  playerId: number | null;
  behaviour: number;
}
interface BenchSlot { id: number; label: string; playerId: number | null; }
interface Choice { value: string; label: string; disabled?: boolean; }
interface LineupState {
  slots: Array<{id: number; playerId: number | null; behaviour: number}>;
  bench: Array<{id: number; playerId: number | null}>;
  tactic: MatchDetail['tactic']; teamAttitude: MatchDetail['teamAttitude'];
  styleOfPlay: number; weather: number; venue: number; spirit: number; confidence: number;
  captainId: number | null; setPieceTakerId: number | null;
}
const BEHAVIOURS = ['NORMAL', 'OFFENSIVE', 'DEFENSIVE', 'TOWARDS_MIDDLE', 'TOWARDS_WING'];
const SPIRITS: MatchDetail['teamSpirit'][] = ['LIKE_THE_COLD_WAR', 'MURDEROUS', 'FURIOUS', 'IRRITATED', 'COMPOSED', 'CALM', 'CONTENT', 'SATISFIED', 'DELIRIOUS', 'WALKING_ON_CLOUDS', 'PARADISE_ON_EARTH'];
const CONFIDENCES: MatchDetail['teamConfidence'][] = ['NON_EXISTENT', 'DISASTROUS', 'WRETCHED', 'POOR', 'DECENT', 'STRONG', 'WONDERFUL', 'SLIGHTLY_EXAGGERATED', 'EXAGGERATED', 'COMPLETELY_EXAGGERATED'];
const POSITIONS: Array<[string, string, Slot['group']]> = [
  ['KEEPER', 'Portero', 'keeper'], ['RIGHT_BACK', 'Defensor lateral derecho', 'defence'],
  ['RIGHT_CENTRAL_DEFENDER', 'Defensa central derecho', 'defence'], ['MIDDLE_CENTRAL_DEFENDER', 'Defensa central', 'defence'],
  ['LEFT_CENTRAL_DEFENDER', 'Defensa central izquierdo', 'defence'], ['LEFT_BACK', 'Defensor lateral izquierdo', 'defence'],
  ['RIGHT_WINGER', 'Extremo derecho', 'midfield'], ['RIGHT_INNER_MIDFIELD', 'Mediocentro derecho', 'midfield'],
  ['MIDDLE_INNER_MIDFIELD', 'Mediocentro', 'midfield'], ['LEFT_INNER_MIDFIELD', 'Mediocentro izquierdo', 'midfield'],
  ['LEFT_WINGER', 'Extremo izquierdo', 'midfield'], ['RIGHT_FORWARD', 'Delantero derecho', 'attack'],
  ['MIDDLE_FORWARD', 'Delantero centro', 'attack'], ['LEFT_FORWARD', 'Delantero izquierdo', 'attack']
];
const BENCH_LABELS = ['Portero', 'Defensa Central', 'Defensa Lateral', 'Medio Centro', 'Delantero', 'Extremo', 'Extra'];

@Component({
  selector: 'app-lineup-simulator', standalone: true, imports: [CommonModule, FormsModule],
  templateUrl: './lineup-simulator.component.html',
  styleUrls: ['./lineup-simulator.component.scss', './lineup-simulator-controls.scss']
})
export class LineupSimulatorComponent implements OnChanges, OnDestroy {
  @Input() players: PlayerInfo[] = [];
  @Input() planner: ProjectTrainingPlanner | null = null;
  @Input() trainingType = 4;
  @Input() storageKey = 'ehm-lineup:default';
  readonly slots: Slot[] = POSITIONS.map(([role, label, group], i) => ({id: 100 + i, role, label, group, playerId: null, behaviour: 0}));
  readonly bench: BenchSlot[] = Array.from({length: 14}, (_, i) => ({id: 200 + i, label: BENCH_LABELS[i % 7], playerId: null}));
  readonly formations = ['5-5-0', '5-4-1', '5-3-2', '5-2-3', '4-5-1', '4-4-2', '4-3-3', '3-5-2', '3-4-3', '2-5-3'];
  readonly tacticOptions: Choice[] = [
    {value: 'NORMAL', label: 'Normal'}, {value: 'PRESSING', label: 'Presionar'},
    {value: 'COUNTER_ATTACKS', label: 'Contraataques'}, {value: 'ATTACK_IN_THE_MIDDLE', label: 'Atacar por el centro'},
    {value: 'ATTACK_IN_WINGS', label: 'Atacar por las bandas'}, {value: 'PLAY_CREATIVELY', label: 'Jugar creativamente'},
    {value: 'LONG_SHOTS', label: 'Tiros lejanos'}
  ];
  readonly spiritOptions = [{value: -1, label: 'Nivel actual'}, ...['¡Paraíso en la Tierra!', 'Por las nubes', 'Eufóricos', 'Encantados', 'Contentos', 'Calmados', 'Serenos', 'Susceptibles', 'Tensos', 'Muy agresivos', 'Como en la Guerra Fría'].map((label, i) => ({value: 10 - i, label}))];
  readonly confidenceOptions = [{value: -1, label: 'Nivel actual'}, ...['Desmedida', 'Exagerada', 'Muy alta', 'Alta', 'Sólida', 'Decente', 'Baja', 'Muy baja', 'Por los suelos', 'Inexistente'].map((label, i) => ({value: 9 - i, label}))];
  readonly sortOptions: Choice[] = [
    ...[['playerCategoryId','Categoría'],['playerNumber','Número de camiseta'],['firstName','Nombre'],['lastName','Apellido'],['age','Edad'],['tsi','TSI'],['salary','Salario'],['playerForm','Forma'],['staminaSkill','Resistencia'],['experience','Experiencia'],['leadership','Liderazgo'],['keeperSkill','Portería'],['defenderSkill','Defensa'],['playmakerSkill','Jugadas'],['wingerSkill','Lateral'],['passingSkill','Pases'],['scorerSkill','Anotación'],['setPiecesSkill','Balón parado'],['trainedThisWeek','Entrenado esta semana']].map(([value,label]) => ({value,label})),
    {value: 'positions', label: '--- Posiciones ---', disabled: true},
    ...['Portero','Defensa','Defensa (Ofensivo)','Defensa (Hacia Lateral)','Defensor lateral','Defensor lateral (Defensivo)','Defensor lateral (Ofensivo)','Defensor lateral (Hacia Medio)','Mediocentro','Mediocentro (Defensivo)','Mediocentro (Ofensivo)','Mediocentro (Hacia Lateral)','Extremo','Extremo (Defensivo)','Extremo (Ofensivo)','Extremo (Hacia Medio)','Delantero','Delantero (Defensivo)','Delantero (Hacia Lateral)'].map((label,i) => ({value: `position:${i}`,label}))
  ];
  tactic: MatchDetail['tactic'] = 'NORMAL';
  teamAttitude: MatchDetail['teamAttitude'] = 'PIN';
  styleOfPlay = 0; weather = 2; venue = 2; spirit = -1; confidence = -1;
  trainingMode = false; flipped = false; showHelp = false; showBenchHelp = false; formationOpen = false;
  pitchView: 'pitch' | 'stars' | 'sectors' = 'pitch';
  rosterView: 'compact' | 'smart' | 'detailed' = 'compact';
  sortBy = 'playerNumber'; sortReverse = false; notice = '';
  captainId: number | null = null; setPieceTakerId: number | null = null; selectedPlayerId: number | null = null;
  activeSlot: Slot | null = null; activeBench: BenchSlot | null = null;
  detailedPlayer: PlayerInfo | null = null;
  showCustomPlayer = false; showImportPlan = false; importWeek = 0;
  customPlayers: PlayerInfo[] = []; importedPlayers: PlayerInfo[] | null = null;
  customPlayer: PlayerInfo = this.newCustomPlayer();
  ratings: Ratings | null = null; calculating = false; error = false;
  ratingMemory: {lineup: LineupState; values: number[]} | null = null;
  menuX = 0; menuY = 0; menuOnLeft = false;
  private calculationTimer: ReturnType<typeof setTimeout> | null = null;
  private calculationSubscription?: Subscription;
  private calculationSequence = 0;
  private draggedPlayerId: number | null = null;
  private draggedSourceId: number | null = null;
  private activeStorageKey = '';

  constructor(private readonly dataService: DataService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['storageKey'] || !this.activeStorageKey) {
      this.activeStorageKey = this.storageKey;
      this.importedPlayers = null; this.customPlayers = []; this.resetAssignments();
      this.tactic='NORMAL'; this.teamAttitude='PIN'; this.styleOfPlay=0;
      this.weather=2; this.venue=2; this.spirit=-1; this.confidence=-1;
      this.ratingMemory=null; this.detailedPlayer=null; this.notice='';
      try {
        const saved = JSON.parse(localStorage.getItem(this.storageKey) || 'null');
        if (saved?.version === 1) {
          this.customPlayers = Array.isArray(saved.customPlayers) ? saved.customPlayers.filter((p: PlayerInfo) => p && p.id < 0 && p.firstName) : [];
          this.restoreState(saved.lineup);
        }
      } catch { /* Private browsing or invalid previous state must not prevent opening the simulator. */ }
    }
    this.pruneAssignments();
    this.scheduleCalculation();
  }

  ngOnDestroy(): void {
    if (this.calculationTimer) clearTimeout(this.calculationTimer);
    this.calculationSubscription?.unsubscribe();
  }

  get allPlayers(): PlayerInfo[] { return [...(this.importedPlayers ?? this.players), ...this.customPlayers]; }
  get sortedPlayers(): PlayerInfo[] {
    const list = [...this.allPlayers];
    const strings = ['firstName','lastName'];
    const ascending = strings.includes(this.sortBy) || ['playerNumber','playerCategoryId','age'].includes(this.sortBy);
    list.sort((a,b) => {
      let result: number;
      if (strings.includes(this.sortBy)) result = String(a[this.sortBy as keyof PlayerInfo] ?? '').localeCompare(String(b[this.sortBy as keyof PlayerInfo] ?? ''), 'es');
      else if (this.sortBy.startsWith('position:')) result = this.positionScore(b, Number(this.sortBy.split(':')[1])) - this.positionScore(a, Number(this.sortBy.split(':')[1]));
      else {
        const av = this.sortValue(a), bv = this.sortValue(b);
        result = ascending ? av - bv : bv - av;
      }
      return (result || this.fullName(a).localeCompare(this.fullName(b), 'es')) * (this.sortReverse ? -1 : 1);
    });
    return list;
  }
  get fieldCount(): number { return this.slots.filter(s => s.playerId != null).length; }
  get formation(): string { return ['defence','midfield','attack'].map(group => this.slots.filter(s => s.group === group && s.playerId != null).length).join('-'); }
  get isLegalFormation(): boolean { return this.fieldCount===11 && this.slots[0].playerId!=null && this.formations.includes(this.formation); }
  get recommendedPlayers(): PlayerInfo[] {
    const slot=this.activeSlot;if(!slot)return [];
    const type=slot.group==='keeper'?0:slot.role.includes('BACK')?4:slot.group==='defence'?1:slot.role.includes('WINGER')?12:slot.group==='midfield'?8:16;
    return this.allPlayers.filter(p=>p.id!==slot.playerId).sort((a,b)=>this.positionScore(b,type)-this.positionScore(a,type)).slice(0,3);
  }
  get ratingValues(): number[] {
    return ['rightDefense','centralDefense','leftDefense','midfield','rightAttack','centralAttack','leftAttack'].map(key => {
      const value = this.ratings?.[key as keyof Ratings]; return typeof value === 'number' && Number.isFinite(value) ? Math.max(.75, value) : .75;
    });
  }
  get importWeeks(): Array<{value: number; label: string}> {
    return Object.keys(this.planner?.teamTrainingResponse?.weekPlayers ?? {}).map(Number).filter(Number.isFinite).sort((a,b) => a-b).map(value => ({value, label: `Semana ${value}`}));
  }
  player(id: number | null): PlayerInfo | undefined { return this.allPlayers.find(p => p.id === id); }
  fullName(p: PlayerInfo): string { return [p.firstName, p.nickName ? `'${p.nickName}'` : '', p.lastName].filter(Boolean).join(' '); }
  shortName(p: PlayerInfo): string { return p.nickName || `${p.firstName?.charAt(0) ?? ''}. ${p.lastName}`; }
  category(p: PlayerInfo): string {
    if(p.playerCategoryId) return ['', 'POR', 'DL', 'DC', 'EXT', 'MC', 'DEL', 'S', 'R', 'E1', 'E2', 'EN1', 'EN2', 'FE'][p.playerCategoryId] ?? '';
    if(Math.max(p.keeperSkill,p.defenderSkill,p.playmakerSkill,p.wingerSkill,p.scorerSkill)<=1)return '';
    // Fallback role labels use the player's skills, independently of project categories.
    // They are not individual star predictions from Hattrick's server.
    const skills: Array<[number,string]> = [[p.keeperSkill,'POR'],[p.playmakerSkill,'MC'],[p.defenderSkill,'DC'],[p.wingerSkill+p.playmakerSkill*.25,'EXT'],[p.scorerSkill,'DEL']];
    return skills.sort((a,b)=>b[0]-a[0])[0][1];
  }
  formClass(p: PlayerInfo): string { return p.playerForm >= 7 ? 'form-veryhigh' : p.playerForm >= 5 ? 'form-high' : p.playerForm >= 3 ? 'form-low' : 'form-verylow'; }
  formDot(p: PlayerInfo, index: number): 'full' | 'half' | 'empty' { const rest = p.playerForm - (3-index)*2; return rest >= 2 ? 'full' : rest >= 1 ? 'half' : 'empty'; }
  specialtyTitle(p: PlayerInfo): string { return ['Sin especialidad','Técnico','Rápido','Potente','Imprevisible','Cabeceador','Resistente','','Apoyo'][p.specialty] ?? ''; }
  avatar(p: PlayerInfo): string { return p.id > 0 ? `/assets/avatars/${p.id}.png` : '/assets/defaultAvatar.png'; }
  onAvatarError(event: Event): void { const img = event.target as HTMLImageElement; if (!img.src.endsWith('/assets/defaultAvatar.png')) img.src = '/assets/defaultAvatar.png'; }
  isAssigned(id: number): boolean { return this.slots.some(s => s.playerId === id) || this.bench.some(s => s.playerId === id); }
  behaviourLabel(value: number): string { return ['Normal','Ofensivo','Defensivo','Hacia medio','Hacia lateral'][value] ?? 'Normal'; }
  allowedBehaviours(slot: Slot): number[] {
    if (slot.id === 100) return [0];
    if ([101,105,106,110].includes(slot.id)) return [0,1,2,3];
    if ([102,104].includes(slot.id)) return [0,1,4];
    if (slot.id === 103) return [0,1];
    if ([107,109].includes(slot.id)) return [0,1,2,4];
    if (slot.id === 108) return [0,1,2];
    if (slot.id === 112) return [0,2];
    return [0,2,4];
  }

  openSlot(slot: Slot, event?: Event): void {
    event?.stopPropagation();
    if (this.selectedPlayerId != null) { this.assign(slot, this.selectedPlayerId); this.selectedPlayerId = null; this.closeMenus(); return; }
    this.activeSlot = this.activeSlot === slot ? null : slot; this.activeBench = null; this.formationOpen = false;
    this.positionMenu(slot.id);
  }
  openBench(slot: BenchSlot, event?: Event): void {
    event?.stopPropagation();
    if (this.selectedPlayerId != null) { this.assignBench(slot, this.selectedPlayerId); this.selectedPlayerId = null; this.closeMenus(); return; }
    this.activeBench = this.activeBench === slot ? null : slot; this.activeSlot = null;
    this.positionMenu(slot.id);
  }
  selectPlayer(p: PlayerInfo, event?: Event): void {
    event?.stopPropagation();
    if (this.activeSlot) { this.assign(this.activeSlot, p.id); this.closeMenus(); }
    else if (this.activeBench) { this.assignBench(this.activeBench, p.id); this.closeMenus(); }
    else { this.selectedPlayerId = this.selectedPlayerId === p.id ? null : p.id; this.detailedPlayer = p; }
  }
  closeMenus(): void { this.activeSlot = null; this.activeBench = null; this.formationOpen = false; this.selectedPlayerId = null; }

  assign(slot: Slot, id: number, behaviour = slot.behaviour): void {
    if (!this.player(id)) return;
    const source = this.slots.find(s => s.playerId === id);
    if (!source && slot.playerId == null && this.fieldCount >= 11) { this.notice = 'Solo puedes alinear a once jugadores.'; return; }
    const displaced = slot.playerId;
    if (source && source !== slot) source.playerId = displaced;
    else if (!source && this.draggedSourceId != null && this.draggedSourceId >= 200 && displaced != null) {
      const benchSource = this.bench.find(b => b.id === this.draggedSourceId); if (benchSource && benchSource.id < 207) benchSource.playerId = displaced;
    }
    this.bench.filter(b => b.playerId === id).forEach(b => b.playerId = null);
    slot.playerId = id; slot.behaviour = this.allowedBehaviours(slot).includes(behaviour) ? behaviour : 0;
    this.notice = ''; this.pruneAssignments(); this.scheduleCalculation();
  }
  assignBench(slot: BenchSlot, id: number): void {
    if (!this.player(id)) return;
    if (slot.id >= 207) {
      if (!this.bench.slice(0,7).some(b => b.playerId === id) || this.bench[slot.id - 207].playerId === id) { this.notice = 'La segunda opción debe ser un suplente principal de otra posición.'; return; }
    } else this.slots.filter(s => s.playerId === id).forEach(s => s.playerId = null);
    slot.playerId = id; this.notice = ''; this.pruneAssignments(); this.scheduleCalculation();
  }
  removeSlot(slot: Slot): void { slot.playerId = null; slot.behaviour = 0; this.pruneAssignments(); this.scheduleCalculation(); }
  removeBench(slot: BenchSlot): void { slot.playerId = null; this.pruneAssignments(); this.scheduleCalculation(); }
  removePlayer(id: number): void { this.slots.filter(s => s.playerId === id).forEach(s => {s.playerId = null; s.behaviour = 0;}); this.bench.filter(b => b.playerId === id).forEach(b => b.playerId = null); this.pruneAssignments(); this.scheduleCalculation(); }
  startDrag(event: DragEvent, id: number, sourceSlotId?: number): void { this.draggedPlayerId = id; this.draggedSourceId = sourceSlotId ?? null; this.selectedPlayerId = null; if (event.dataTransfer) {event.dataTransfer.effectAllowed = 'move'; event.dataTransfer.setData('text/plain', String(id));} }
  allowDrop(event: DragEvent): void { event.preventDefault(); if (event.dataTransfer) event.dataTransfer.dropEffect = 'move'; }
  dropSlot(event: DragEvent, slot: Slot, behaviour = slot.behaviour): void { event.preventDefault(); event.stopPropagation(); const id = this.draggedPlayerId ?? Number(event.dataTransfer?.getData('text/plain')); if (id) this.assign(slot, id, behaviour); this.endDrag(); }
  dropBench(event: DragEvent, slot: BenchSlot): void { event.preventDefault(); event.stopPropagation(); const id = this.draggedPlayerId ?? Number(event.dataTransfer?.getData('text/plain')); if (id) this.assignBench(slot, id); this.endDrag(); }
  dropRoster(event: DragEvent): void { event.preventDefault(); event.stopPropagation(); const id = this.draggedPlayerId ?? Number(event.dataTransfer?.getData('text/plain')); if (id) this.removePlayer(id); this.endDrag(); }
  endDrag(): void { this.draggedPlayerId = null; this.draggedSourceId = null; }
  mirrorLineup(): void {
    [[101,105],[102,104],[106,110],[107,109],[111,113]].forEach(([a,b]) => {const left=this.slots[a-100],right=this.slots[b-100]; [left.playerId,right.playerId]=[right.playerId,left.playerId]; [left.behaviour,right.behaviour]=[right.behaviour,left.behaviour];});
    this.scheduleCalculation();
  }
  chooseFormation(code: string): void {
    if (!this.formations.includes(code)) return;
    const counts = code.split('-').map(Number), selected = this.slots.filter(s => s.playerId != null).map(s => ({id:s.playerId!,group:s.group}));
    const keeper = selected.find(p => p.group === 'keeper');
    const pool = selected.filter(p => p !== keeper);
    this.slots.forEach(s=>{s.playerId=null;s.behaviour=0;}); if (keeper) this.slots[0].playerId = keeper.id;
    const groups: Slot['group'][] = ['defence','midfield','attack'];
    const positions = [[101,105,102,104,103],[106,110,107,109,108],[111,113,112]];
    groups.forEach((group,i) => positions[i].slice(0,counts[i]).forEach(id => {const own=pool.findIndex(p => p.group===group);const [p]=pool.splice(own>=0?own:0,1);if(p)this.slots[id-100].playerId=p.id;}));
    this.formationOpen=false; this.pruneAssignments(); this.scheduleCalculation();
  }

  scheduleCalculation(): void {
    if (this.calculationTimer) clearTimeout(this.calculationTimer);
    this.calculationSequence++; this.calculationSubscription?.unsubscribe(); this.persist();
    this.calculationTimer = setTimeout(() => this.recalculate(), 160);
  }
  recalculate(): void {
    if (this.calculationTimer) clearTimeout(this.calculationTimer); this.calculationTimer=null;
    this.calculationSubscription?.unsubscribe(); const sequence=++this.calculationSequence;
    const fieldPlayers=this.slots.filter(s => s.playerId!=null && this.player(s.playerId)).map(s => ({player:this.player(s.playerId)!,role:s.role,behaviour:BEHAVIOURS[s.behaviour]}));
    if (!fieldPlayers.length) {this.ratings=null;this.error=false;this.calculating=false;return;}
    const previous=this.planner?.matchDetail;
    const matchDetail:MatchDetail={tactic:this.tactic,teamAttitude:this.teamAttitude,teamSpirit:this.spirit<0?(previous?.teamSpirit??'CALM'):SPIRITS[this.spirit],teamSubSpirit:this.spirit<0?(previous?.teamSubSpirit??.5):.5,teamConfidence:this.confidence<0?(previous?.teamConfidence??'STRONG'):CONFIDENCES[this.confidence],teamSubConfidence:this.confidence<0?(previous?.teamSubConfidence??.5):.5,sideMatch:this.venue===1?'HOME':this.venue===3?'AWAY_DERBY':'AWAY',styleOfPlay:Math.max(-100,Math.min(100,Number(this.styleOfPlay)*10))};
    this.calculating=true;this.error=false;
    this.calculationSubscription=this.dataService.rateLineup({fieldPlayers,benchPlayers:[],penaltyTakers:[],matchDetail}).subscribe({
      next:ratings=>{if(sequence!==this.calculationSequence)return;this.ratings=ratings;this.calculating=false;},
      error:()=>{if(sequence!==this.calculationSequence)return;this.ratings=null;this.calculating=false;this.error=true;}
    });
  }
  saveMemory(): void { if(this.error||this.calculating)return;this.ratingMemory={lineup:this.snapshot(),values:[...this.ratingValues]}; }
  clearMemory(): void { this.ratingMemory=null; }
  restoreMemory(): void { if(!this.ratingMemory)return;this.restoreState(this.ratingMemory.lineup);this.pruneAssignments();this.scheduleCalculation(); }
  copyLineup(): void { void this.copyText(this.slots.filter(s=>s.playerId!=null).map(s=>`${s.label}: ${this.fullName(this.player(s.playerId)!)} (${this.behaviourLabel(s.behaviour)})`).join('\n')+`\n${this.formation}\n${this.tacticOptions.find(t=>t.value===this.tactic)?.label??this.tactic}`); }
  copyRatings(): void { void this.copyText(['Defensa derecha','Defensa central','Defensa izquierda','Mediocampo','Ataque derecho','Ataque central','Ataque izquierdo'].map((label,i)=>`${label}: ${this.ratingValues[i].toFixed(2)}`).join('\n')); }
  startCustomPlayer(): void {this.customPlayer=this.newCustomPlayer();this.showCustomPlayer=true;}
  saveCustomPlayer(): void {
    const p=this.customPlayer;
    const skills=['keeperSkill','defenderSkill','playmakerSkill','wingerSkill','passingSkill','scorerSkill','setPiecesSkill','experience'] as const;
    if(!p.firstName.trim()||p.age<17||p.age>99||p.ageDays<0||p.ageDays>111||skills.some(k=>!Number.isFinite(p[k])||p[k]<0||p[k]>20)||p.playerForm<1||p.playerForm>8||p.staminaSkill<1||p.staminaSkill>9){this.notice='Revisa el nombre, la edad y los niveles del jugador.';return;}
    const id=Math.min(0,...this.customPlayers.map(c=>c.id))-1;
    this.customPlayers=[...this.customPlayers,{...p,id,firstName:p.firstName.trim(),lastName:p.lastName.trim()}];this.showCustomPlayer=false;this.notice='';this.persist();
  }
  deleteCustomPlayer(id:number):void{this.removePlayer(id);this.customPlayers=this.customPlayers.filter(p=>p.id!==id);this.persist();}
  importPlan():void {
    const weeks=this.planner?.teamTrainingResponse?.weekPlayers;
    const key=weeks?.[this.importWeek]?this.importWeek:this.importWeeks[0]?.value;
    if(key==null||!weeks?.[key]){this.notice='No hay una semana calculada disponible en el plan de entrenamiento.';return;}
    this.importWeek=key;this.importedPlayers=[...weeks[key]];this.resetAssignments();
    const formation=this.planner?.teamTrainingResponse?.weekFormationRatings?.[key];
    for(const p of formation?.players??[]){const slot=this.slots.find(s=>s.role===p.position.role);if(slot&&this.player(p.playerId)&&this.fieldCount<11){slot.playerId=p.playerId;const behaviour=BEHAVIOURS.indexOf(p.position.behaviour);slot.behaviour=this.allowedBehaviours(slot).includes(behaviour)?behaviour:0;}}
    if(this.planner?.matchDetail){this.tactic=this.planner.matchDetail.tactic;this.teamAttitude=this.planner.matchDetail.teamAttitude;this.styleOfPlay=Math.round(this.planner.matchDetail.styleOfPlay/10);}
    this.showImportPlan=false;this.notice=`Jugadores importados de la semana ${key}.`;this.scheduleCalculation();
  }

  private sortValue(p:PlayerInfo):number {
    if(this.sortBy==='playerNumber')return p.playerNumber&&p.playerNumber>0?p.playerNumber:1e6;
    if(this.sortBy==='age')return p.age+p.ageDays/112;
    if(this.sortBy==='trainedThisWeek')return Object.values(p.playerTraining??{}).reduce((a,b)=>a+Number(b||0),0);
    return Number(p[this.sortBy as keyof PlayerInfo])||0;
  }
  private positionScore(p:PlayerInfo, position:number):number {
    if(position===0)return p.keeperSkill*4+p.defenderSkill+p.setPiecesSkill*.2;
    if(position<=3)return p.defenderSkill*3+p.playmakerSkill*(position===2?1:.3)+p.wingerSkill*(position===3?1:0);
    if(position<=7)return p.defenderSkill*2+p.wingerSkill*(position===6?2:1)+p.playmakerSkill*.3;
    if(position<=11)return p.playmakerSkill*3+p.passingSkill+p.defenderSkill*(position===9?1:0)+p.wingerSkill*(position===11?1:0);
    if(position<=15)return p.wingerSkill*3+p.playmakerSkill+p.passingSkill+p.defenderSkill*(position===13?1:0);
    return p.scorerSkill*3+p.passingSkill+p.playmakerSkill*(position===17?1:0)+p.wingerSkill*(position===18?1:0);
  }
  private positionMenu(id:number):void {
    setTimeout(()=>{const root=document.querySelector('app-lineup-simulator #ngMatchorder'),anchor=root?.querySelector(`#position-${id}`),menu=root?.querySelector('.ehm-position-menu');if(!root||!anchor||!menu)return;const rr=root.getBoundingClientRect(),ar=anchor.getBoundingClientRect(),scale=rr.width/(root as HTMLElement).offsetWidth;const width=(menu as HTMLElement).offsetWidth;this.menuOnLeft=ar.right-rr.left>rr.width*.65;this.menuX=this.menuOnLeft?(ar.left-rr.left)/scale-width-10:(ar.right-rr.left)/scale+10;this.menuY=Math.max(0,(ar.top-rr.top)/scale);},0);
  }
  private pruneAssignments():void {
    const valid=new Set(this.allPlayers.map(p=>p.id)),seen=new Set<number>();
    for(const slot of this.slots){if(slot.playerId!=null&&(!valid.has(slot.playerId)||seen.has(slot.playerId)||seen.size>=11))slot.playerId=null;if(slot.playerId!=null)seen.add(slot.playerId);if(!this.allowedBehaviours(slot).includes(slot.behaviour))slot.behaviour=0;}
    for(const b of this.bench){if(b.playerId!=null&&(!valid.has(b.playerId)||seen.has(b.playerId)))b.playerId=null;}
    for(const b of this.bench.slice(7)){if(b.playerId!=null&&(!this.bench.slice(0,7).some(p=>p.playerId===b.playerId)||this.bench[b.id-207].playerId===b.playerId))b.playerId=null;}
    if(this.captainId!=null&&!seen.has(this.captainId))this.captainId=null;
    if(this.setPieceTakerId!=null&&(!seen.has(this.setPieceTakerId)||this.slots[0].playerId===this.setPieceTakerId))this.setPieceTakerId=null;
  }
  private resetAssignments():void {this.slots.forEach(s=>{s.playerId=null;s.behaviour=0;});this.bench.forEach(b=>b.playerId=null);this.captainId=null;this.setPieceTakerId=null;this.closeMenus();}
  private snapshot():LineupState {return {slots:this.slots.map(({id,playerId,behaviour})=>({id,playerId,behaviour})),bench:this.bench.map(({id,playerId})=>({id,playerId})),tactic:this.tactic,teamAttitude:this.teamAttitude,styleOfPlay:this.styleOfPlay,weather:this.weather,venue:this.venue,spirit:this.spirit,confidence:this.confidence,captainId:this.captainId,setPieceTakerId:this.setPieceTakerId};}
  private restoreState(s:LineupState):void {
    if(!s||!Array.isArray(s.slots)||!Array.isArray(s.bench))return;
    this.slots.forEach(slot=>{const old=s.slots.find(o=>o.id===slot.id);slot.playerId=typeof old?.playerId==='number'?old.playerId:null;slot.behaviour=Number(old?.behaviour)||0;});
    this.bench.forEach(slot=>{const old=s.bench.find(o=>o.id===slot.id);slot.playerId=typeof old?.playerId==='number'?old.playerId:null;});
    if(this.tacticOptions.some(t=>t.value===s.tactic))this.tactic=s.tactic;
    if(['PIN','PIC','MOTS'].includes(s.teamAttitude))this.teamAttitude=s.teamAttitude;
    this.styleOfPlay=Number.isFinite(s.styleOfPlay)?Math.max(-10,Math.min(10,s.styleOfPlay)):0;
    this.weather=[0,1,2,3].includes(s.weather)?s.weather:2;this.venue=[1,2,3,4].includes(s.venue)?s.venue:2;
    this.spirit=this.spiritOptions.some(o=>o.value===s.spirit)?s.spirit:-1;this.confidence=this.confidenceOptions.some(o=>o.value===s.confidence)?s.confidence:-1;
    this.captainId=s.captainId;this.setPieceTakerId=s.setPieceTakerId;
  }
  private persist():void {if(!this.activeStorageKey)return;try{localStorage.setItem(this.activeStorageKey,JSON.stringify({version:1,lineup:this.snapshot(),customPlayers:this.customPlayers}));}catch{/* Storage is optional. */}}
  private async copyText(text:string):Promise<void>{try{await navigator.clipboard.writeText(text);this.notice='Copiado al portapapeles.';}catch{this.notice='No se ha podido acceder al portapapeles.';}}
  private newCustomPlayer():PlayerInfo {return {id:-1,firstName:'',nickName:'',lastName:'',agreeability:3,aggressiveness:3,honesty:3,specialty:0,countryId:0,age:17,ageDays:0,tsi:0,arrivalDate:'',playerForm:7,experience:1,loyalty:20,motherClubBonus:false,leadership:3,salary:0,transferListed:false,cards:0,injuryLevel:-1,staminaSkill:7,keeperSkill:1,playmakerSkill:1,scorerSkill:1,passingSkill:1,wingerSkill:1,defenderSkill:1,setPiecesSkill:1,htms:0,htms28:0,playerCategoryId:0,playerTraining:{keeper:0,defender:0,playmaker:0,winger:0,passing:0,scorer:0,setPieces:0},changes:{}};}
}
