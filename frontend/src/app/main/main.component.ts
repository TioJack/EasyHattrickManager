import {Component, OnInit} from '@angular/core';
import {PlayService, ViewMode} from '../services/play.service';
import {PlayerInfo, Project, StaffInfo, TrainingInfo, UserConfig} from '../services/model/data-response';
import {DatePipe, DecimalPipe, LowerCasePipe, NgForOf, NgIf} from '@angular/common';
import {PlayerCardComponent} from '../player-card/player-card.component';
import {StaffComponent} from '../staff/staff.component';
import {TrainingComponent} from '../training/training.component';
import {PlayerStatsComponent} from '../player-stats/player-stats.component';
import {PlayerCardCompactComponent} from '../player-card-compact/player-card-compact.component';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';
import {FormsModule} from '@angular/forms';
import {DEFAULT_DATE_FORMAT, trainingTypeColor} from '../constants/global.constant';
import {DataService} from '../services/data.service';
import {
  BestFormationCriteria,
  FormationRating,
  MatchDetail,
  StagePlayerParticipation,
  TeamTrainingProgressResponse,
  TeamTrainingRequest,
  TeamTrainingResponse,
  TrainingStage
} from '../services/model/team-training';
import {WeekInfo} from '../player/model/week-info';
import {SalaryPipe} from '../pipes/salary.pipe';
import {NumberSeparatorPipe} from '../pipes/number-separator.pipe';
import {UserConfigService} from '../services/user-config.service';
import {Subscription} from 'rxjs';

interface LineupPoint {
  x: number;
  y: number;
}

interface LineupTrap {
  id: string;
  points: LineupPoint[];
}

interface RatingZoneTrap {
  zoneId: string;
  labelKey: string;
  points: LineupPoint[];
}

interface LineupDirectionMarker {
  band: string;
  triangle: string;
}

interface FixedFormationOption {
  code: string;
  name: string;
}

interface HatStatsChartPoint {
  week: number;
  value: number;
  xPercent: number;
  yPercent: number;
  isPrime: boolean;
  isHover: boolean;
}

@Component({
  selector: 'app-main',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    DatePipe,
    DecimalPipe,
    LowerCasePipe,
    PlayerCardComponent,
    StaffComponent,
    TrainingComponent,
    PlayerStatsComponent,
    PlayerCardCompactComponent,
    TranslatePipe,
    FirstCapitalizePipe,
    FormsModule,
    SalaryPipe,
    NumberSeparatorPipe
  ],
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss']
})
export class MainComponent implements OnInit {
  players: PlayerInfo[] = [];
  basePlayers: PlayerInfo[] = [];
  training: TrainingInfo | null = null;
  staff: StaffInfo | null = null;
  viewMode: ViewMode = 'players';
  trainingPlans: Array<{
    typeId: number;
    weeks: number;
    coach: number;
    assistants: number;
    intensity: number;
    stamina: number;
  }> = [];
  trainingPlanPercents: Record<number, number[]> = {};
  newTrainingTypeId = 4;
  newTrainingWeeks = 16;
  newTrainingCoach = 0;
  newTrainingAssistants = 0;
  newTrainingIntensity = 0;
  newTrainingStamina = 0;
  durationOptions = Array.from({length: 160}, (_, i) => i + 1);
  trainerLevelOptions = [1, 2, 3, 4, 5];
  assistantOptions = Array.from({length: 11}, (_, i) => i);
  intensityOptions = Array.from({length: 101}, (_, i) => i);
  staminaOptions = Array.from({length: 101}, (_, i) => i);
  trainingTypes = [2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
  trainingTypeColor = trainingTypeColor;
  autoRefreshBestFormation = false;
  bestFormationCriteria: BestFormationCriteria = 'HATSTATS';
  fixedFormationCode: string | null = null;
  matchDetail: MatchDetail = {
    tactic: 'NORMAL',
    teamAttitude: 'PIN',
    teamSpirit: 'CALM',
    teamSubSpirit: 0.5,
    teamConfidence: 'STRONG',
    teamSubConfidence: 0.5,
    sideMatch: 'AWAY',
    styleOfPlay: 0
  };
  bestFormationCriteriaOptions: BestFormationCriteria[] = [
    'HATSTATS',
    'DEFENSE',
    'MIDFIELD3',
    'ATTACK',
    'RIGHT_DEFENSE',
    'CENTRAL_DEFENSE',
    'LEFT_DEFENSE',
    'MIDFIELD',
    'RIGHT_ATTACK',
    'CENTRAL_ATTACK',
    'LEFT_ATTACK'
  ];
  fixedFormationOptions: FixedFormationOption[] = [
    {code: 'F_5_5_0_A', name: '5-5-0'},
    {code: 'F_5_4_1_A', name: '5-4-1'},
    {code: 'F_5_3_2_A', name: '5-3-2'},
    {code: 'F_5_2_3_A', name: '5-2-3'},
    {code: 'F_4_5_1_A', name: '4-5-1'},
    {code: 'F_4_4_2_A', name: '4-4-2'},
    {code: 'F_4_3_3_A', name: '4-3-3'},
    {code: 'F_3_5_2_A', name: '3-5-2'},
    {code: 'F_3_4_3_A', name: '3-4-3'},
    {code: 'F_2_5_3_A', name: '2-5-3'}
  ];
  tacticOptions: Array<MatchDetail['tactic']> = [
    'NORMAL',
    'PRESSING',
    'COUNTER_ATTACKS',
    'ATTACK_IN_THE_MIDDLE',
    'ATTACK_IN_WINGS',
    'PLAY_CREATIVELY',
    'LONG_SHOTS'
  ];
  teamAttitudeOptions: Array<MatchDetail['teamAttitude']> = ['PIC', 'PIN', 'MOTS'];
  teamSpiritOptions: Array<MatchDetail['teamSpirit']> = [
    'LIKE_THE_COLD_WAR',
    'MURDEROUS',
    'FURIOUS',
    'IRRITATED',
    'COMPOSED',
    'CALM',
    'CONTENT',
    'SATISFIED',
    'DELIRIOUS',
    'WALKING_ON_CLOUDS',
    'PARADISE_ON_EARTH'
  ];
  teamConfidenceOptions: Array<MatchDetail['teamConfidence']> = [
    'NON_EXISTENT',
    'DISASTROUS',
    'WRETCHED',
    'POOR',
    'DECENT',
    'STRONG',
    'WONDERFUL',
    'SLIGHTLY_EXAGGERATED',
    'EXAGGERATED',
    'COMPLETELY_EXAGGERATED'
  ];
  sideMatchOptions: Array<MatchDetail['sideMatch']> = ['HOME', 'AWAY_DERBY', 'AWAY'];
  styleOfPlayOptions = Array.from({length: 21}, (_, i) => -100 + (i * 10));
  subSpiritOptions = Array.from({length: 10}, (_, i) => Number((i / 10).toFixed(1)));
  readonly lineupTraps: LineupTrap[] = [
    {id: 't1', points: [{x: 285, y: 32}, {x: 364, y: 32}, {x: 366, y: 69}, {x: 283, y: 69}]},
    {id: 't2', points: [{x: 70, y: 90}, {x: 153, y: 90}, {x: 143, y: 143}, {x: 57, y: 143}]},
    {id: 't3', points: [{x: 190, y: 90}, {x: 272, y: 90}, {x: 269, y: 143}, {x: 182, y: 143}]},
    {id: 't4', points: [{x: 283, y: 90}, {x: 366, y: 90}, {x: 368, y: 143}, {x: 281, y: 143}]},
    {id: 't5', points: [{x: 377, y: 90}, {x: 460, y: 90}, {x: 467, y: 143}, {x: 380, y: 143}]},
    {id: 't6', points: [{x: 497, y: 90}, {x: 579, y: 90}, {x: 593, y: 143}, {x: 507, y: 143}]},
    {id: 't7', points: [{x: 50, y: 172}, {x: 137, y: 172}, {x: 126, y: 232}, {x: 36, y: 232}]},
    {id: 't8', points: [{x: 179, y: 172}, {x: 265, y: 172}, {x: 262, y: 232}, {x: 172, y: 232}]},
    {id: 't9', points: [{x: 281, y: 172}, {x: 368, y: 172}, {x: 370, y: 232}, {x: 279, y: 232}]},
    {id: 't10', points: [{x: 384, y: 172}, {x: 470, y: 172}, {x: 478, y: 232}, {x: 387, y: 232}]},
    {id: 't11', points: [{x: 514, y: 172}, {x: 600, y: 172}, {x: 614, y: 232}, {x: 524, y: 232}]},
    {id: 't12', points: [{x: 168, y: 259}, {x: 262, y: 259}, {x: 258, y: 323}, {x: 160, y: 323}]},
    {id: 't13', points: [{x: 278, y: 259}, {x: 372, y: 259}, {x: 374, y: 323}, {x: 275, y: 323}]},
    {id: 't14', points: [{x: 388, y: 259}, {x: 481, y: 259}, {x: 491, y: 323}, {x: 391, y: 323}]}
  ];
  readonly ratingZoneTraps: RatingZoneTrap[] = [
    {
      zoneId: 'RIGHT_DEFENSE',
      labelKey: 'ht.rating-sector-rightdefense',
      points: [{x: 120, y: 72}, {x: 240, y: 72}, {x: 233, y: 142}, {x: 103, y: 142}]
    },
    {
      zoneId: 'CENTRAL_DEFENSE',
      labelKey: 'ht.rating-sector-centraldefense',
      points: [{x: 249, y: 72}, {x: 400, y: 72}, {x: 406, y: 142}, {x: 243, y: 142}]
    },
    {
      zoneId: 'LEFT_DEFENSE',
      labelKey: 'ht.rating-sector-leftdefense',
      points: [{x: 410, y: 72}, {x: 530, y: 72}, {x: 547, y: 142}, {x: 416, y: 142}]
    },
    {
      zoneId: 'MIDFIELD',
      labelKey: 'ht.rating-sector-midfield',
      points: [{x: 101, y: 150}, {x: 549, y: 150}, {x: 567, y: 227}, {x: 82, y: 227}]
    },
    {
      zoneId: 'RIGHT_ATTACK',
      labelKey: 'ht.rating-sector-rightattack',
      points: [{x: 80, y: 238}, {x: 224, y: 238}, {x: 217, y: 310}, {x: 64, y: 310}]
    },
    {
      zoneId: 'CENTRAL_ATTACK',
      labelKey: 'ht.rating-sector-centralattack',
      points: [{x: 234, y: 238}, {x: 416, y: 238}, {x: 422, y: 310}, {x: 228, y: 310}]
    },
    {
      zoneId: 'LEFT_ATTACK',
      labelKey: 'ht.rating-sector-leftattack',
      points: [{x: 426, y: 238}, {x: 570, y: 238}, {x: 585, y: 310}, {x: 433, y: 310}]
    }
  ];
  teamTrainingResponse: TeamTrainingResponse | null = null;
  endWeekInfo: WeekInfo | null = null;
  userConfig: UserConfig | null = null;
  plannerResultByPlayerId: Record<number, PlayerInfo> = {};
  showPlannerPlayers = true;
  showPlannerRatings = true;
  isTeamTrainingLoading = false;
  teamTrainingProgressTotalWeeks = 0;
  teamTrainingProgressCalculatedWeeks = 0;
  teamTrainingProgressPercent = 0;
  private plannerRequestTimer: number | null = null;
  private scheduledCalculateBestFormation = false;
  private teamTrainingRequestToken = 0;
  private teamTrainingRequestSub: Subscription | null = null;
  private teamTrainingProgressSub: Subscription | null = null;
  private teamTrainingProgressTimer: number | null = null;
  private inFlightTeamTrainingRequestKey: string | null = null;
  private lastSuccessfulTeamTrainingRequestKey: string | null = null;
  private readonly plannerFallbackDate = new Date().toISOString();
  private latestResponseWeek: number | null = null;
  private plannerPlayersByWeek: Record<number, Record<number, PlayerInfo>> = {};
  private plannerHtmsByPlayerId: Record<number, Array<number | null>> = {};
  private plannerMaxHtmsByPlayerId: Record<number, number | null> = {};
  private plannerMaxHtmsRangesByPlayerId: Record<number, Array<{leftPercent: number; widthPercent: number}>> = {};
  private hoverWeekByPlayerId: Record<number, number> = {};
  private hoverTimelinePercentByPlayerId: Record<number, number> = {};
  bestFormationTimelineHoverWeek: number | null = null;
  bestFormationTimelineHoverPercent: number | null = null;
  private selectedProject: Project | null = null;
  private readonly timelinePopupWidth = 240;
  private readonly lineupBandWidth = 8;
  private readonly lineupTriangleHeight = 11;
  private readonly roleTrapByRole: Record<string, string> = {
    KEEPER: 't1',
    LEFT_BACK: 't6',
    LEFT_CENTRAL_DEFENDER: 't5',
    MIDDLE_CENTRAL_DEFENDER: 't4',
    RIGHT_CENTRAL_DEFENDER: 't3',
    RIGHT_BACK: 't2',
    LEFT_WINGER: 't11',
    LEFT_INNER_MIDFIELD: 't10',
    MIDDLE_INNER_MIDFIELD: 't9',
    RIGHT_INNER_MIDFIELD: 't8',
    RIGHT_WINGER: 't7',
    LEFT_FORWARD: 't14',
    MIDDLE_FORWARD: 't13',
    RIGHT_FORWARD: 't12'
  };
  protected readonly DEFAULT_DATE_FORMAT = DEFAULT_DATE_FORMAT;

  constructor(
    private playService: PlayService,
    private dataService: DataService,
    private userConfigService: UserConfigService,
    private translateService: TranslateService
  ) {
  }

  ngOnInit(): void {
    this.ensureVisibleBestFormationCriteria();
    this.playService.players$.subscribe(players => {
      this.basePlayers = players;
      this.refreshPlayers();
      this.ensurePlayerPlans();
      if (this.trainingPlans.length > 0) {
        this.scheduleTeamTrainingRequest();
      } else {
        this.plannerResultByPlayerId = {};
      }
    })
    this.playService.staff$.subscribe(staff => {
      this.staff = staff;
      this.newTrainingCoach = this.getDefaultCoach();
      this.newTrainingAssistants = this.getDefaultAssistants();
      if (this.trainingPlans.length > 0) {
        this.scheduleTeamTrainingRequest();
      }
    });
    this.playService.training$.subscribe(training => {
      this.training = training;
      const trainingType = this.training?.trainingType;
      if (trainingType !== undefined && this.trainingTypes.includes(trainingType)) {
        this.newTrainingTypeId = trainingType;
      }
      this.newTrainingIntensity = this.training?.trainingLevel ?? 0;
      this.newTrainingStamina = this.training?.staminaTrainingPart ?? 0;
      if (this.trainingPlans.length > 0) {
        this.scheduleTeamTrainingRequest();
      }
    });
    this.playService.viewMode$.subscribe(mode => {
      this.viewMode = mode;
    });
    this.playService.selectedProject$.subscribe(project => {
      if (project?.name !== this.selectedProject?.name || project?.teamId !== this.selectedProject?.teamId) {
        this.resetPlannerState();
      }
      this.selectedProject = project;
    });
    this.userConfigService.userConfig$.subscribe(config => {
      this.userConfig = config;
    });
  }

  addTrainingPlan(): void {
    const weeks = Math.max(1, Math.trunc(this.newTrainingWeeks || 0));
    const coach = Math.max(1, Math.trunc(this.newTrainingCoach || 1));
    const assistants = Math.max(0, Math.trunc(this.newTrainingAssistants || 0));
    const intensity = Math.max(0, Math.trunc(this.newTrainingIntensity || 0));
    const stamina = Math.max(0, Math.trunc(this.newTrainingStamina || 0));
    if (!Number.isFinite(weeks) || weeks <= 0) {
      return;
    }
    this.trainingPlans.push({
      typeId: this.newTrainingTypeId,
      weeks,
      coach,
      assistants,
      intensity,
      stamina
    });
    const stageIndex = this.trainingPlans.length - 1;
    this.applyDefaultParticipationForStage(stageIndex, this.newTrainingTypeId);

    this.requestTeamTraining();
  }

  getTotalTrainingWeeks(): number {
    return this.trainingPlans.reduce((sum, plan) => sum + (plan.weeks || 0), 0);
  }

  getTotalTrainingSeasons(): number {
    return this.getTotalTrainingWeeks() / 16;
  }

  getTotalWagesPaid(): number {
    const weeks = this.teamTrainingResponse?.weekPlayers;
    if (!weeks) {
      return 0;
    }
    let total = 0;
    for (const players of Object.values(weeks)) {
      for (const player of players) {
        total += player.salary ?? 0;
      }
    }
    return total;
  }

  removeTrainingPlan(index: number): void {
    if (index < 0 || index >= this.trainingPlans.length) {
      return;
    }
    this.trainingPlans.splice(index, 1);
    for (const playerId of Object.keys(this.trainingPlanPercents)) {
      const percents = this.trainingPlanPercents[Number(playerId)];
      if (percents) {
        percents.splice(index, 1);
      }
    }
    this.requestTeamTraining();
  }

  moveTrainingPlan(index: number, delta: number): void {
    const targetIndex = index + delta;
    if (index < 0 || targetIndex < 0 || index >= this.trainingPlans.length || targetIndex >= this.trainingPlans.length) {
      return;
    }
    const [stage] = this.trainingPlans.splice(index, 1);
    this.trainingPlans.splice(targetIndex, 0, stage);
    for (const playerId of Object.keys(this.trainingPlanPercents)) {
      const percents = this.trainingPlanPercents[Number(playerId)];
      if (percents) {
        const [percent] = percents.splice(index, 1);
        percents.splice(targetIndex, 0, percent ?? 100);
      }
    }
    this.requestTeamTraining();
  }

  onStageUpdated(): void {
    this.scheduleTeamTrainingRequest();
  }

  onStageLoadUpdated(): void {
    this.scheduleTeamTrainingRequest(true);
  }

  onStageTypeChanged(index: number): void {
    const plan = this.trainingPlans[index];
    if (!plan) {
      return;
    }
    this.applyDefaultParticipationForStage(index, plan.typeId);
    this.scheduleTeamTrainingRequest();
  }

  get totalTrainingWeeks(): number {
    return this.trainingPlans.reduce((sum, plan) => sum + plan.weeks, 0);
  }

  getPlanPercent(playerId: number, index: number): number {
    return this.trainingPlanPercents[playerId]?.[index] ?? 100;
  }

  setPlanPercent(playerId: number, index: number, value: number): void {
    if (!this.trainingPlanPercents[playerId]) {
      this.trainingPlanPercents[playerId] = [];
    }
    this.trainingPlanPercents[playerId][index] = Math.max(0, Math.min(100, Number(value)));
  }

  onPlanPercentCommit(): void {
    this.scheduleTeamTrainingRequest();
  }

  onRatingsInputChanged(): void {
    this.ensureVisibleBestFormationCriteria();
    if (this.autoRefreshBestFormation) {
      this.scheduleTeamTrainingRequest(true);
    }
  }

  onAutoRefreshBestFormationChanged(): void {
    if (this.autoRefreshBestFormation) {
      this.scheduleTeamTrainingRequest(true);
    }
  }

  calculateBestFormationOnDemand(): void {
    this.ensureVisibleBestFormationCriteria();
    this.requestTeamTraining(true);
  }

  private ensurePlayerPlans(): void {
    for (const player of this.players) {
      const current = this.trainingPlanPercents[player.id] ?? [];
      const next = [...current];
      while (next.length < this.trainingPlans.length) {
        next.push(100);
      }
      this.trainingPlanPercents[player.id] = next;
    }
  }

  private applyDefaultParticipationForStage(stageIndex: number, typeId: number): void {
    this.players.forEach((player, index) => {
      if (!this.trainingPlanPercents[player.id]) {
        this.trainingPlanPercents[player.id] = [];
      }
      this.trainingPlanPercents[player.id][stageIndex] = this.getDefaultParticipationValue(typeId, index);
    });
  }

  private getDefaultParticipationValue(typeId: number, playerIndex: number): number {
    switch (typeId) {
      case 2: // SET_PIECES
        return playerIndex < 22 ? 100 : 0;
      case 3: // DEFENDING
        return playerIndex < 10 ? 100 : 0;
      case 4: // SCORING
        return playerIndex < 6 ? 100 : 0;
      case 5: // WINGER
        return playerIndex < 4 ? 100 : (playerIndex < 8 ? 50 : 0);
      case 6: // SCORING_SET_PIECES
        return playerIndex < 22 ? 100 : 0;
      case 7: // PASSING
        return playerIndex < 16 ? 100 : 0;
      case 8: // PLAY_MAKING
        return playerIndex < 6 ? 100 : (playerIndex < 10 ? 50 : 0);
      case 9: // GOALKEEPING
        return playerIndex < 2 ? 100 : 0;
      case 10: // PASSING_EXTENSIVE
        return playerIndex < 20 ? 100 : 0;
      case 11: // DEFENDING_EXTENSIVE
        return playerIndex < 22 ? 100 : 0;
      case 12: // WINGER_EXTENSIVE
        return playerIndex < 10 ? 100 : 0;
      default:
        return 0;
    }
  }

  getTrainingTextColor(typeId: number): string {
    const hex = trainingTypeColor(typeId).replace('#', '');
    if (hex.length !== 6) {
      return '#fff';
    }
    const r = parseInt(hex.slice(0, 2), 16) / 255;
    const g = parseInt(hex.slice(2, 4), 16) / 255;
    const b = parseInt(hex.slice(4, 6), 16) / 255;
    const luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
    return luminance > 0.6 ? '#1f2937' : '#fff';
  }

  togglePlannerPlayers(): void {
    this.showPlannerPlayers = !this.showPlannerPlayers;
  }

  togglePlannerRatings(): void {
    this.showPlannerRatings = !this.showPlannerRatings;
  }

  private requestTeamTraining(calculateBestFormation = this.autoRefreshBestFormation): void {
    if (!this.players.length || !this.trainingPlans.length) {
      this.teamTrainingRequestSub?.unsubscribe();
      this.teamTrainingRequestSub = null;
      this.stopTeamTrainingProgressPolling(true);
      this.inFlightTeamTrainingRequestKey = null;
      this.isTeamTrainingLoading = false;
      return;
    }
    const request = this.buildTeamTrainingRequest(calculateBestFormation);
    if (!request) {
      this.teamTrainingRequestSub?.unsubscribe();
      this.teamTrainingRequestSub = null;
      this.stopTeamTrainingProgressPolling(true);
      this.inFlightTeamTrainingRequestKey = null;
      this.isTeamTrainingLoading = false;
      return;
    }
    const requestKey = JSON.stringify(request);
    if (requestKey === this.inFlightTeamTrainingRequestKey || requestKey === this.lastSuccessfulTeamTrainingRequestKey) {
      return;
    }
    const requestToken = ++this.teamTrainingRequestToken;
    this.inFlightTeamTrainingRequestKey = requestKey;
    this.isTeamTrainingLoading = true;
    this.startTeamTrainingProgressPolling(request);
    this.teamTrainingRequestSub = this.dataService.teamTraining(request).subscribe({
      next: response => {
        if (requestToken !== this.teamTrainingRequestToken) {
          return;
        }
        this.lastSuccessfulTeamTrainingRequestKey = requestKey;
        this.inFlightTeamTrainingRequestKey = null;
        this.teamTrainingRequestSub = null;
        this.applyTeamTrainingResponse(response);
        this.stopTeamTrainingProgressPolling(false);
        this.isTeamTrainingLoading = false;
      },
      error: error => {
        if (requestToken !== this.teamTrainingRequestToken) {
          return;
        }
        this.inFlightTeamTrainingRequestKey = null;
        this.teamTrainingRequestSub = null;
        console.error('teamTraining request failed', error);
        this.plannerResultByPlayerId = {};
        this.latestResponseWeek = null;
        this.endWeekInfo = null;
        this.resetPlannerCaches();
        this.onBestFormationTimelineLeave();
        this.stopTeamTrainingProgressPolling(true);
        this.isTeamTrainingLoading = false;
      }
    });
  }

  private scheduleTeamTrainingRequest(calculateBestFormation = this.autoRefreshBestFormation): void {
    this.scheduledCalculateBestFormation = this.scheduledCalculateBestFormation || calculateBestFormation;
    if (this.plannerRequestTimer !== null) {
      clearTimeout(this.plannerRequestTimer);
    }
    this.plannerRequestTimer = window.setTimeout(() => {
      this.plannerRequestTimer = null;
      const shouldCalculateBestFormation = this.scheduledCalculateBestFormation;
      this.scheduledCalculateBestFormation = false;
      this.requestTeamTraining(shouldCalculateBestFormation);
    }, 250);
  }

  private resetPlannerState(): void {
    if (this.plannerRequestTimer !== null) {
      clearTimeout(this.plannerRequestTimer);
      this.plannerRequestTimer = null;
    }
    this.scheduledCalculateBestFormation = false;
    this.teamTrainingRequestSub?.unsubscribe();
    this.teamTrainingRequestSub = null;
    this.stopTeamTrainingProgressPolling(true);
    this.teamTrainingRequestToken++;
    this.inFlightTeamTrainingRequestKey = null;
    this.lastSuccessfulTeamTrainingRequestKey = null;
    this.isTeamTrainingLoading = false;
    this.trainingPlans = [];
    this.trainingPlanPercents = {};
    this.teamTrainingResponse = null;
    this.endWeekInfo = null;
    this.plannerResultByPlayerId = {};
    this.latestResponseWeek = null;
    this.resetPlannerCaches();
    this.hoverWeekByPlayerId = {};
    this.hoverTimelinePercentByPlayerId = {};
    this.onBestFormationTimelineLeave();
  }

  private buildTeamTrainingRequest(calculateBestFormation: boolean): TeamTrainingRequest | null {
    const stages: TrainingStage[] = this.trainingPlans.map((plan, index) => {
      const training = this.mapTrainingTypeToStage(plan.typeId);
      return {
        id: index + 1,
        duration: plan.weeks,
        coach: plan.coach,
        assistants: plan.assistants,
        intensity: plan.intensity,
        stamina: plan.stamina,
        training
      };
    });

    const players = this.players.map(player => ({
      player,
      inclusionWeek: 1
    }));

    const participations: StagePlayerParticipation[] = [];
    for (const player of this.players) {
      const percents = this.trainingPlanPercents[player.id] ?? [];
      for (let i = 0; i < this.trainingPlans.length; i++) {
        participations.push({
          stageId: i + 1,
          playerId: player.id,
          participation: percents[i] ?? 100
        });
      }
    }

    return {
      iniWeek: this.getCurrentWeekInfo(),
      players,
      stages,
      participations,
      calculateBestFormation,
      bestFormationCriteria: this.bestFormationCriteria,
      fixedFormationCode: this.fixedFormationCode,
      matchDetail: this.matchDetail
    };
  }

  private getCurrentWeekInfo(): WeekInfo {
    return this.playService.getCurrentWeekInfo() ?? {
      season: 0,
      week: 0,
      date: this.plannerFallbackDate
    };
  }

  private applyTeamTrainingResponse(response: TeamTrainingResponse): void {
    this.teamTrainingResponse = response;
    this.plannerResultByPlayerId = this.buildPlannerResultMap(response);
    this.buildPlannerCaches(response);
    this.endWeekInfo = response.endWeek ?? null;
  }

  private startTeamTrainingProgressPolling(request: TeamTrainingRequest): void {
    this.stopTeamTrainingProgressPolling(false);
    this.updateTeamTrainingProgressFromRequest(request);
    this.teamTrainingProgressTimer = window.setInterval(() => {
      if (!this.isTeamTrainingLoading) {
        this.stopTeamTrainingProgressPolling(false);
        return;
      }
      if (this.teamTrainingProgressSub) {
        return;
      }
      this.teamTrainingProgressSub = this.dataService.teamTrainingProgress(request).subscribe({
        next: progress => {
          this.applyTeamTrainingProgress(progress);
        },
        error: () => {
          this.teamTrainingProgressSub = null;
        },
        complete: () => {
          this.teamTrainingProgressSub = null;
        }
      });
    }, 350);
  }

  private stopTeamTrainingProgressPolling(resetProgress: boolean): void {
    if (this.teamTrainingProgressTimer !== null) {
      clearInterval(this.teamTrainingProgressTimer);
      this.teamTrainingProgressTimer = null;
    }
    this.teamTrainingProgressSub?.unsubscribe();
    this.teamTrainingProgressSub = null;
    if (resetProgress) {
      this.teamTrainingProgressTotalWeeks = 0;
      this.teamTrainingProgressCalculatedWeeks = 0;
      this.teamTrainingProgressPercent = 0;
      return;
    }
    this.teamTrainingProgressCalculatedWeeks = this.teamTrainingProgressTotalWeeks;
    this.teamTrainingProgressPercent = this.teamTrainingProgressTotalWeeks > 0 ? 100 : 0;
  }

  private updateTeamTrainingProgressFromRequest(request: TeamTrainingRequest): void {
    const totalWeeks = (request.stages ?? []).reduce((sum, stage) => sum + (stage.duration ?? 0), 0);
    this.teamTrainingProgressTotalWeeks = Math.max(0, totalWeeks);
    this.teamTrainingProgressCalculatedWeeks = 0;
    this.teamTrainingProgressPercent = 0;
  }

  private applyTeamTrainingProgress(progress: TeamTrainingProgressResponse): void {
    this.teamTrainingProgressTotalWeeks = Math.max(0, progress.totalWeeks ?? 0);
    this.teamTrainingProgressCalculatedWeeks = Math.max(0, Math.min(progress.calculatedWeeks ?? 0, this.teamTrainingProgressTotalWeeks));
    const rawPercent = Number.isFinite(progress.percent) ? progress.percent : 0;
    this.teamTrainingProgressPercent = Math.max(0, Math.min(100, Math.round(rawPercent)));
  }

  private buildPlannerResultMap(response: TeamTrainingResponse): Record<number, PlayerInfo> {
    const weekKeys = Object.keys(response.weekPlayers || {});
    if (weekKeys.length === 0) {
      this.latestResponseWeek = null;
      return {};
    }
    const latestWeek = Math.max(...weekKeys.map(key => Number(key)).filter(Number.isFinite));
    this.latestResponseWeek = Number.isFinite(latestWeek) ? latestWeek : null;
    const weekPlayers = response.weekPlayers[latestWeek] ?? [];
    const map: Record<number, PlayerInfo> = {};
    for (const player of weekPlayers) {
      map[player.id] = player;
    }
    return map;
  }

  private buildPlannerCaches(response: TeamTrainingResponse): void {
    this.resetPlannerCaches();
    const weekPlayers = response.weekPlayers ?? {};
    const weekKeys = Object.keys(weekPlayers).map(key => Number(key)).filter(Number.isFinite);
    if (weekKeys.length === 0) {
      return;
    }
    const weekCount = Math.max(...weekKeys);
    for (const week of weekKeys) {
      const players = weekPlayers[week] ?? [];
      const map: Record<number, PlayerInfo> = {};
      for (const player of players) {
        map[player.id] = player;
      }
      this.plannerPlayersByWeek[week] = map;
    }

    if (weekCount <= 0) {
      return;
    }

    for (const player of this.players) {
      const htmsByWeek: Array<number | null> = [];
      let maxHtms: number | null = null;
      for (let week = 1; week <= weekCount; week++) {
        const weekMap = this.plannerPlayersByWeek[week];
        const weekPlayer = weekMap ? weekMap[player.id] : undefined;
        const htms = weekPlayer?.playerSubSkill?.htms ?? weekPlayer?.htms ?? null;
        htmsByWeek.push(htms);
        if (htms != null) {
          maxHtms = maxHtms == null ? htms : Math.max(maxHtms, htms);
        }
      }
      this.plannerHtmsByPlayerId[player.id] = htmsByWeek;
      this.plannerMaxHtmsByPlayerId[player.id] = maxHtms;

      if (maxHtms == null || !Number.isFinite(maxHtms)) {
        this.plannerMaxHtmsRangesByPlayerId[player.id] = [];
        continue;
      }

      const ranges: Array<{start: number; end: number}> = [];
      let currentStart: number | null = null;
      for (let i = 0; i < htmsByWeek.length; i++) {
        const week = i + 1;
        const value = htmsByWeek[i];
        if (value === maxHtms) {
          if (currentStart == null) {
            currentStart = week;
          }
        } else if (currentStart != null) {
          ranges.push({start: currentStart, end: week - 1});
          currentStart = null;
        }
      }
      if (currentStart != null) {
        ranges.push({start: currentStart, end: weekCount});
      }
      const percentPerWeek = 100 / weekCount;
      this.plannerMaxHtmsRangesByPlayerId[player.id] = ranges.map(range => {
        const leftPercent = (range.start - 1) * percentPerWeek;
        const widthPercent = (range.end - range.start + 1) * percentPerWeek;
        return {leftPercent, widthPercent};
      });
    }
  }

  private resetPlannerCaches(): void {
    this.plannerPlayersByWeek = {};
    this.plannerHtmsByPlayerId = {};
    this.plannerMaxHtmsByPlayerId = {};
    this.plannerMaxHtmsRangesByPlayerId = {};
  }

  getPlannerResultPlayer(playerId: number): PlayerInfo | null {
    return this.plannerResultByPlayerId[playerId] ?? null;
  }

  getPlannerWeekCount(): number {
    if (this.latestResponseWeek && this.latestResponseWeek > 0) {
      return Math.max(1, this.latestResponseWeek);
    }
    if (this.totalTrainingWeeks > 0) {
      return this.totalTrainingWeeks;
    }
    return 0;
  }

  onTimelineMove(event: MouseEvent, playerId: number): void {
    const weekCount = this.getPlannerWeekCount();
    if (weekCount <= 0) {
      return;
    }
    const target = event.currentTarget as HTMLElement | null;
    if (!target) {
      return;
    }
    const rect = target.getBoundingClientRect();
    const rawX = event.clientX - rect.left;
    const x = Math.max(0, Math.min(rect.width, rawX));
    const percent = rect.width > 0 ? x / rect.width : 0;
    const week = Math.max(1, Math.min(weekCount, Math.round(percent * (weekCount - 1)) + 1));
    this.hoverWeekByPlayerId[playerId] = week;
    const container = target.closest('.planner-center-body') as HTMLElement | null;
    const containerRect = container?.getBoundingClientRect();
    if (containerRect && containerRect.width > 0) {
      const popupHalf = this.timelinePopupWidth / 2;
      const minPercent = (popupHalf / containerRect.width) * 100;
      const maxPercent = 100 - minPercent;
      const rawPercent = ((event.clientX - containerRect.left) / containerRect.width) * 100;
      const clampedPercent = Math.max(minPercent, Math.min(maxPercent, rawPercent));
      this.hoverTimelinePercentByPlayerId[playerId] = clampedPercent;
    } else {
      this.hoverTimelinePercentByPlayerId[playerId] = percent * 100;
    }
  }

  onTimelineLeave(playerId: number): void {
    delete this.hoverWeekByPlayerId[playerId];
    delete this.hoverTimelinePercentByPlayerId[playerId];
  }

  onBestFormationTimelineMove(event: MouseEvent): void {
    const weekCount = this.getPlannerWeekCount();
    if (weekCount <= 0) {
      this.onBestFormationTimelineLeave();
      return;
    }
    const target = event.currentTarget as HTMLElement | null;
    if (!target) {
      return;
    }
    const rect = target.getBoundingClientRect();
    const rawX = event.clientX - rect.left;
    const x = Math.max(0, Math.min(rect.width, rawX));
    const percent = rect.width > 0 ? x / rect.width : 0;
    this.bestFormationTimelineHoverWeek = Math.max(1, Math.min(weekCount, Math.round(percent * (weekCount - 1)) + 1));
    this.bestFormationTimelineHoverPercent = percent * 100;
  }

  onBestFormationTimelineLeave(): void {
    this.bestFormationTimelineHoverWeek = null;
    this.bestFormationTimelineHoverPercent = null;
  }

  getTimelineWeek(playerId: number): number | null {
    return this.hoverWeekByPlayerId[playerId] ?? null;
  }

  getTimelinePercent(playerId: number): number {
    const percent = this.hoverTimelinePercentByPlayerId[playerId];
    if (percent === undefined) {
      return 0;
    }
    return percent;
  }

  isPrimeWeek(playerId: number): boolean {
    const week = this.hoverWeekByPlayerId[playerId];
    const weekCount = this.getPlannerWeekCount();
    if (!week || weekCount <= 0) {
      return false;
    }
    return this.isMaxHtmsWeek(playerId, week, weekCount);
  }

  getTimelinePopupPlayer(playerId: number): PlayerInfo | null {
    const week = this.hoverWeekByPlayerId[playerId];
    if (!week || !this.teamTrainingResponse?.weekPlayers) {
      return null;
    }
    if (this.latestResponseWeek && week > this.latestResponseWeek) {
      return null;
    }
    const playersById = this.plannerPlayersByWeek[week];
    return playersById ? playersById[playerId] ?? null : null;
  }

  getMaxHtmsRanges(playerId: number): Array<{leftPercent: number; widthPercent: number}> {
    return this.plannerMaxHtmsRangesByPlayerId[playerId] ?? [];
  }

  private isMaxHtmsWeek(playerId: number, week: number, weekCount: number): boolean {
    if (!this.teamTrainingResponse?.weekPlayers || week <= 0 || weekCount <= 0) {
      return false;
    }
    const maxHtms = this.plannerMaxHtmsByPlayerId[playerId];
    if (maxHtms == null) {
      return false;
    }
    const htms = this.plannerHtmsByPlayerId[playerId]?.[week - 1] ?? null;
    return htms != null && htms === maxHtms;
  }

  trackByPlayerId(_index: number, player: PlayerInfo): number {
    return player.id;
  }

  trackByStageIndex(index: number): number {
    return index;
  }

  getTrainingTypeIdForWeek(week: number): number | null {
    if (week <= 0 || this.trainingPlans.length === 0) {
      return null;
    }
    let accumulated = 0;
    for (const plan of this.trainingPlans) {
      accumulated += plan.weeks;
      if (week <= accumulated) {
        return plan.typeId;
      }
    }
    return this.trainingPlans[this.trainingPlans.length - 1]?.typeId ?? null;
  }

  private getDefaultCoach(): number {
    return this.staff?.trainer?.skillLevel ?? 0;
  }

  private getDefaultAssistants(): number {
    return this.staff?.staffMembers
      ?.filter(member => member.type === 1)
      ?.reduce((sum, member) => sum + (member.level ?? 0), 0) ?? 0;
  }

  getDurationLabel(weeks: number): string {
    if (weeks % 16 === 0) {
      const seasons = weeks / 16;
      const seasonKey = seasons === 1 ? 'ehm.season' : 'ehm.seasons';
      return `${weeks} (${seasons} ${this.translateService.instant(seasonKey)})`;
    }
    return `${weeks}`;
  }

  getTrainerLevelTranslationKey(level: number): string {
    return `ht.skill-level-${level + 3}`;
  }

  getTimelineColor(playerId: number): string {
    const week = this.hoverWeekByPlayerId[playerId];
    if (!week) {
      return 'rgba(0, 0, 0, 0.08)';
    }
    const typeId = this.getTrainingTypeIdForWeek(week);
    return trainingTypeColor(typeId ?? undefined);
  }

  private refreshPlayers(): void {
    this.players = [...this.basePlayers];
  }

  private ensureVisibleBestFormationCriteria(): void {
    if (!this.bestFormationCriteriaOptions.includes(this.bestFormationCriteria)) {
      this.bestFormationCriteria = 'HATSTATS';
    }
  }

  getCriteriaLabel(criteria: BestFormationCriteria): string {
    switch (criteria) {
      case 'HATSTATS':
        return this.translateService.instant('ehm.criteria-hatstats');
      case 'PEASO_STATS':
        return this.translateService.instant('ehm.criteria-peaso');
      case 'MIDFIELD':
        return this.translateService.instant('ht.rating-sector-midfield');
      case 'MIDFIELD3':
        return this.translateService.instant('ehm.criteria-midfield-hatstats');
      case 'DEFENSE':
        return this.translateService.instant('ehm.criteria-defense-hatstats');
      case 'ATTACK':
        return this.translateService.instant('ehm.criteria-attack-hatstats');
      case 'RIGHT_DEFENSE':
        return this.translateService.instant('ht.rating-sector-rightdefense');
      case 'CENTRAL_DEFENSE':
        return this.translateService.instant('ht.rating-sector-centraldefense');
      case 'LEFT_DEFENSE':
        return this.translateService.instant('ht.rating-sector-leftdefense');
      case 'RIGHT_ATTACK':
        return this.translateService.instant('ht.rating-sector-rightattack');
      case 'CENTRAL_ATTACK':
        return this.translateService.instant('ht.rating-sector-centralattack');
      case 'LEFT_ATTACK':
        return this.translateService.instant('ht.rating-sector-leftattack');
      default:
        return criteria;
    }
  }

  getFixedFormationOptionLabel(option: FixedFormationOption): string {
    return option.name;
  }

  getTacticTranslationKey(tactic: MatchDetail['tactic']): string {
    switch (tactic) {
      case 'NORMAL':
        return 'ht.tactic-types-0';
      case 'PRESSING':
        return 'ht.tactic-types-1';
      case 'COUNTER_ATTACKS':
        return 'ht.tactic-types-2';
      case 'ATTACK_IN_THE_MIDDLE':
        return 'ht.tactic-types-3';
      case 'ATTACK_IN_WINGS':
        return 'ht.tactic-types-4';
      case 'PLAY_CREATIVELY':
        return 'ht.tactic-types-7';
      case 'LONG_SHOTS':
        return 'ht.tactic-types-8';
      default:
        return 'ht.tactic-types-0';
    }
  }

  getTeamAttitudeTranslationKey(teamAttitude: MatchDetail['teamAttitude']): string {
    switch (teamAttitude) {
      case 'PIC':
        return 'ht.team-attitude--1';
      case 'PIN':
        return 'ht.team-attitude-0';
      case 'MOTS':
        return 'ht.team-attitude-1';
      default:
        return 'ht.team-attitude-0';
    }
  }

  getTeamSpiritTranslationKey(teamSpirit: MatchDetail['teamSpirit']): string {
    const indexBySpirit: Record<MatchDetail['teamSpirit'], number> = {
      LIKE_THE_COLD_WAR: 0,
      MURDEROUS: 1,
      FURIOUS: 2,
      IRRITATED: 3,
      COMPOSED: 4,
      CALM: 5,
      CONTENT: 6,
      SATISFIED: 7,
      DELIRIOUS: 8,
      WALKING_ON_CLOUDS: 9,
      PARADISE_ON_EARTH: 10
    };
    return `ht.team-spirit-${indexBySpirit[teamSpirit]}`;
  }

  getTeamConfidenceTranslationKey(teamConfidence: MatchDetail['teamConfidence']): string {
    const indexByConfidence: Record<MatchDetail['teamConfidence'], number> = {
      NON_EXISTENT: 0,
      DISASTROUS: 1,
      WRETCHED: 2,
      POOR: 3,
      DECENT: 4,
      STRONG: 5,
      WONDERFUL: 6,
      SLIGHTLY_EXAGGERATED: 7,
      EXAGGERATED: 8,
      COMPLETELY_EXAGGERATED: 9
    };
    return `ht.confidence-${indexByConfidence[teamConfidence]}`;
  }

  getSideMatchLabel(sideMatch: MatchDetail['sideMatch']): string {
    switch (sideMatch) {
      case 'HOME':
        return this.translateService.instant('ehm.stadium-home');
      case 'AWAY_DERBY':
        return this.translateService.instant('ehm.stadium-derby');
      case 'AWAY':
        return this.translateService.instant('ehm.stadium-away');
      default:
        return sideMatch;
    }
  }

  getStyleOfPlayLabel(value: number): string {
    if (value === 0) {
      return this.translateService.instant('ehm.style-neutral');
    }
    const abs = Math.abs(value);
    return value < 0
      ? `${abs}% ${this.translateService.instant('ehm.style-defensive')}`
      : `${abs}% ${this.translateService.instant('ehm.style-offensive')}`;
  }

  getBestFormationPlayers(): Array<{left: number; top: number; shortName: string; tooltip: string}> {
    const formation = this.getActiveFormationRating();
    if (!formation?.players?.length) {
      return [];
    }
    return formation.players.map(playerRating => {
      const role = this.getPositionRole(playerRating.position);
      const coordinates = this.getRoleCoordinates(role);
      return {
        left: coordinates.left,
        top: coordinates.top,
        shortName: this.getPlayerShortName(playerRating.playerId),
        tooltip: this.getPlayerTooltip(playerRating.playerId)
      };
    });
  }

  getLineupTrapPoints(trap: LineupTrap): string {
    return trap.points.map(point => `${point.x},${point.y}`).join(' ');
  }

  getLineupDirectionMarkers(): LineupDirectionMarker[] {
    const formation = this.getActiveFormationRating();
    if (!formation?.players?.length) {
      return [];
    }
    const markers: LineupDirectionMarker[] = [];
    const used = new Set<string>();
    for (const playerRating of formation.players) {
      const role = this.getPositionRole(playerRating.position);
      const behaviour = this.getPositionBehaviour(playerRating.position);
      const trapId = this.roleTrapByRole[role];
      if (!trapId) {
        continue;
      }
      const trap = this.lineupTraps.find(item => item.id === trapId);
      if (!trap) {
        continue;
      }
      const sides = this.getTrapSidesForBehaviour(role, behaviour, trap);
      for (const side of sides) {
        const markerKey = `${trapId}-${side}`;
        if (used.has(markerKey)) {
          continue;
        }
        used.add(markerKey);
        const band = this.buildBandPolygon(trap.points, side, this.lineupBandWidth);
        const triangle = this.buildTrianglePolygon(trap.points, side, this.lineupTriangleHeight);
        if (band && triangle) {
          markers.push({band, triangle});
        }
      }
    }
    return markers;
  }

  getRatingZoneTrapPoints(trap: RatingZoneTrap): string {
    return trap.points.map(point => `${point.x},${point.y}`).join(' ');
  }

  getRatingZoneTrapCenter(trap: RatingZoneTrap): {left: number; top: number} {
    const avgX = trap.points.reduce((sum, point) => sum + point.x, 0) / trap.points.length;
    const avgY = trap.points.reduce((sum, point) => sum + point.y, 0) / trap.points.length;
    return {
      left: (avgX / 650) * 100,
      top: (avgY / 340) * 100
    };
  }

  getBestFormationZoneValue(zoneId: string): number | null {
    const rating = this.getActiveFormationRating()?.rating;
    switch (zoneId) {
      case 'LEFT_DEFENSE':
        return rating?.leftDefense ?? null;
      case 'CENTRAL_DEFENSE':
        return rating?.centralDefense ?? null;
      case 'RIGHT_DEFENSE':
        return rating?.rightDefense ?? null;
      case 'MIDFIELD':
        return rating?.midfield ?? null;
      case 'LEFT_ATTACK':
        return rating?.leftAttack ?? null;
      case 'CENTRAL_ATTACK':
        return rating?.centralAttack ?? null;
      case 'RIGHT_ATTACK':
        return rating?.rightAttack ?? null;
      default:
        return null;
    }
  }

  getBestFormationSummary(): string {
    const formation = this.getActiveFormationRating();
    if (!formation) {
      return '-';
    }
    return formation.formation;
  }

  getDisplayedFormationWeekText(): string {
    const week = this.getDisplayedFormationWeek();
    return `${this.translateService.instant('ehm.week')}: ${week != null && week > 0 ? week : '-'}`;
  }

  getDisplayedFormationSeasonWeekText(): string {
    const seasonWeek = this.getDisplayedFormationSeasonWeek();
    if (!seasonWeek) {
      return '-';
    }
    return `${this.translateService.instant('ehm.season')} ${seasonWeek.season} ${this.translateService.instant('ehm.week')} ${seasonWeek.week}`;
  }

  getDisplayedFormationDate(): Date | null {
    const displayedWeek = this.getDisplayedFormationWeek();
    const endWeek = this.endWeekInfo;
    const weekCount = this.getPlannerWeekCount();
    if (!displayedWeek || displayedWeek <= 0 || !endWeek || weekCount <= 0 || !endWeek.date) {
      return null;
    }
    const endDate = new Date(endWeek.date);
    if (Number.isNaN(endDate.getTime())) {
      return null;
    }
    const offsetFromEnd = weekCount - displayedWeek;
    endDate.setUTCDate(endDate.getUTCDate() - (offsetFromEnd * 7));
    return endDate;
  }

  getBestFormationWeekText(): string {
    const bestWeek = this.teamTrainingResponse?.bestWeek;
    return `${this.translateService.instant('ehm.best-week')}: ${bestWeek != null && bestWeek > 0 ? bestWeek : '-'}`;
  }

  getBestFormationHatStatsPoints(): HatStatsChartPoint[] {
    return this.getBestFormationSelectedCriteriaGeometry().points;
  }

  getBestFormationHatStatsPath(): string {
    return this.getBestFormationSelectedCriteriaGeometry().path;
  }

  getBestFormationPrimeRanges(): Array<{leftPercent: number; widthPercent: number}> {
    const weekCount = this.getPlannerWeekCount();
    if (weekCount <= 0) {
      return [];
    }
    const primeWeeks = this.getBestFormationPrimeWeeks(weekCount);
    if (primeWeeks.length === 0) {
      return [];
    }
    const percentPerWeek = 100 / weekCount;
    const ranges: Array<{leftPercent: number; widthPercent: number}> = [];
    let start = primeWeeks[0];
    let prev = primeWeeks[0];
    for (let i = 1; i < primeWeeks.length; i++) {
      const week = primeWeeks[i];
      if (week === prev + 1) {
        prev = week;
        continue;
      }
      ranges.push({
        leftPercent: (start - 1) * percentPerWeek,
        widthPercent: (prev - start + 1) * percentPerWeek
      });
      start = week;
      prev = week;
    }
    ranges.push({
      leftPercent: (start - 1) * percentPerWeek,
      widthPercent: (prev - start + 1) * percentPerWeek
    });
    return ranges;
  }

  getBestFormationRelevantStats(): Array<{criteria: BestFormationCriteria; label: string; value: number | null}> {
    const rating = this.getActiveFormationRating()?.rating;
    return this.bestFormationCriteriaOptions.map(criteria => ({
      criteria,
      label: this.getCriteriaLabel(criteria),
      value: this.getRatingValueByCriteria(criteria, rating)
    }));
  }

  getDisplayedWeekSalary(): number | null {
    const week = this.getDisplayedFormationWeek() ?? this.latestResponseWeek;
    if (!week || week <= 0) {
      return null;
    }
    const weekPlayers = this.teamTrainingResponse?.weekPlayers?.[week] ?? [];
    if (!weekPlayers.length) {
      return null;
    }
    return weekPlayers.reduce((sum, player) => sum + (player.salary ?? 0), 0);
  }

  private getRatingValueByCriteria(criteria: BestFormationCriteria, rating: FormationRating['rating'] | undefined): number | null {
    switch (criteria) {
      case 'HATSTATS':
        return rating?.hatStats ?? null;
      case 'PEASO_STATS':
        return rating?.peasoStats ?? null;
      case 'MIDFIELD':
        return rating?.midfield ?? null;
      case 'MIDFIELD3':
        return rating?.midfield3 ?? null;
      case 'DEFENSE':
        return rating?.defense ?? null;
      case 'ATTACK':
        return rating?.attack ?? null;
      case 'RIGHT_DEFENSE':
        return rating?.rightDefense ?? null;
      case 'CENTRAL_DEFENSE':
        return rating?.centralDefense ?? null;
      case 'LEFT_DEFENSE':
        return rating?.leftDefense ?? null;
      case 'RIGHT_ATTACK':
        return rating?.rightAttack ?? null;
      case 'CENTRAL_ATTACK':
        return rating?.centralAttack ?? null;
      case 'LEFT_ATTACK':
        return rating?.leftAttack ?? null;
      default:
        return null;
    }
  }

  getBestFormationZoneRows(): Array<{labelKey: string; value: number | null}> {
    const rating = this.getActiveFormationRating()?.rating;
    return [
      {labelKey: 'ht.rating-sector-leftdefense', value: rating?.leftDefense ?? null},
      {labelKey: 'ht.rating-sector-centraldefense', value: rating?.centralDefense ?? null},
      {labelKey: 'ht.rating-sector-rightdefense', value: rating?.rightDefense ?? null},
      {labelKey: 'ht.rating-sector-midfield', value: rating?.midfield ?? null},
      {labelKey: 'ht.rating-sector-leftattack', value: rating?.leftAttack ?? null},
      {labelKey: 'ht.rating-sector-centralattack', value: rating?.centralAttack ?? null},
      {labelKey: 'ht.rating-sector-rightattack', value: rating?.rightAttack ?? null}
    ];
  }

  isBestFormationPrimeHover(): boolean {
    const hoveredWeek = this.getHoveredFormationWeek();
    if (hoveredWeek == null) {
      return false;
    }
    const weekCount = this.getPlannerWeekCount();
    if (weekCount <= 0) {
      return false;
    }
    return this.getBestFormationPrimeWeeks(weekCount).includes(hoveredWeek);
  }

  private getFormationRatingForWeek(week: number | null | undefined): FormationRating | null {
    if (!week || week <= 0) {
      return null;
    }
    return this.teamTrainingResponse?.weekFormationRatings?.[week] ?? null;
  }

  private getBestFormationSelectedCriteriaSeries(): Array<{week: number; value: number}> {
    const weekCount = this.getPlannerWeekCount();
    if (weekCount <= 0) {
      return [];
    }
    const series: Array<{week: number; value: number}> = [];
    for (let week = 1; week <= weekCount; week++) {
      const value = this.getRatingValueByCriteria(this.bestFormationCriteria, this.getFormationRatingForWeek(week)?.rating);
      if (typeof value !== 'number' || !Number.isFinite(value)) {
        continue;
      }
      series.push({week, value});
    }
    return series;
  }

  private getBestFormationSelectedCriteriaGeometry(): {points: HatStatsChartPoint[]; path: string} {
    const weekCount = this.getPlannerWeekCount();
    if (weekCount <= 0) {
      return {points: [], path: ''};
    }
    const series = this.getBestFormationSelectedCriteriaSeries();
    if (!series.length) {
      return {points: [], path: ''};
    }
    const min = Math.min(...series.map(item => item.value));
    const max = Math.max(...series.map(item => item.value));
    const bestWeek = this.teamTrainingResponse?.bestWeek ?? null;
    const hoveredWeek = this.getHoveredFormationWeek();
    const points: HatStatsChartPoint[] = series.map(item => ({
      week: item.week,
      value: item.value,
      xPercent: this.getWeekPercent(item.week, weekCount),
      yPercent: this.getHatStatsYPercent(item.value, min, max),
      isPrime: bestWeek != null && bestWeek > 0 && item.week === bestWeek,
      isHover: hoveredWeek != null && item.week === hoveredWeek
    }));
    let path = '';
    let previousWeek = -1;
    points.forEach(point => {
      const prefix = previousWeek > 0 && point.week === previousWeek + 1 ? 'L' : 'M';
      path += `${path ? ' ' : ''}${prefix} ${point.xPercent} ${point.yPercent}`;
      previousWeek = point.week;
    });
    return {points, path};
  }

  private getBestFormationPrimeWeeks(weekCount: number): number[] {
    if (weekCount <= 0) {
      return [];
    }
    const bestValue = this.getRatingValueByCriteria(this.bestFormationCriteria, this.teamTrainingResponse?.bestFormationRating?.rating);
    if (typeof bestValue !== 'number' || !Number.isFinite(bestValue)) {
      return [];
    }
    const primeWeeks: number[] = [];
    for (let week = 1; week <= weekCount; week++) {
      const weekValue = this.getRatingValueByCriteria(this.bestFormationCriteria, this.getFormationRatingForWeek(week)?.rating);
      if (typeof weekValue === 'number' && Number.isFinite(weekValue) && weekValue === bestValue) {
        primeWeeks.push(week);
      }
    }
    return primeWeeks;
  }

  private getWeekPercent(week: number, weekCount: number): number {
    if (weekCount <= 1) {
      return 50;
    }
    return ((week - 1) / (weekCount - 1)) * 100;
  }

  private getHatStatsYPercent(value: number, min: number, max: number): number {
    if (!Number.isFinite(value) || !Number.isFinite(min) || !Number.isFinite(max) || max <= min) {
      return 50;
    }
    const normalized = (value - min) / (max - min);
    return 90 - (normalized * 80);
  }

  private getHoveredFormationWeek(): number | null {
    const hoveredWeek = this.bestFormationTimelineHoverWeek;
    if (!hoveredWeek || hoveredWeek <= 0) {
      return null;
    }
    return this.getFormationRatingForWeek(hoveredWeek) ? hoveredWeek : null;
  }

  private getDisplayedFormationWeek(): number | null {
    const hoveredWeek = this.getHoveredFormationWeek();
    if (hoveredWeek != null) {
      return hoveredWeek;
    }
    const bestWeek = this.teamTrainingResponse?.bestWeek;
    if (bestWeek != null && bestWeek > 0) {
      return bestWeek;
    }
    const weekKeys = Object.keys(this.teamTrainingResponse?.weekFormationRatings ?? {})
      .map(value => Number(value))
      .filter(value => Number.isFinite(value) && value > 0);
    if (weekKeys.length === 0) {
      return null;
    }
    return Math.max(...weekKeys);
  }

  private getDisplayedFormationSeasonWeek(): {season: number; week: number} | null {
    const displayedWeek = this.getDisplayedFormationWeek();
    const endWeek = this.endWeekInfo;
    const weekCount = this.getPlannerWeekCount();
    if (!displayedWeek || displayedWeek <= 0 || !endWeek || weekCount <= 0) {
      return null;
    }
    const offsetFromEnd = weekCount - displayedWeek;
    return this.shiftSeasonWeek(endWeek.season, endWeek.week, -offsetFromEnd);
  }

  private shiftSeasonWeek(season: number, week: number, deltaWeeks: number): {season: number; week: number} | null {
    if (!Number.isFinite(season) || !Number.isFinite(week) || !Number.isFinite(deltaWeeks)) {
      return null;
    }
    const absoluteWeek = (Math.trunc(season) * 16) + (Math.trunc(week) - 1) + Math.trunc(deltaWeeks);
    if (absoluteWeek < 0) {
      return null;
    }
    return {
      season: Math.floor(absoluteWeek / 16),
      week: (absoluteWeek % 16) + 1
    };
  }

  private getActiveFormationRating(): FormationRating | null {
    const hoveredWeek = this.getHoveredFormationWeek();
    if (hoveredWeek != null) {
      return this.getFormationRatingForWeek(hoveredWeek);
    }
    if (this.teamTrainingResponse?.bestFormationRating) {
      return this.teamTrainingResponse.bestFormationRating;
    }
    const displayedWeek = this.getDisplayedFormationWeek();
    return this.getFormationRatingForWeek(displayedWeek);
  }

  private getPositionRole(position: string | {role?: string} | null | undefined): string {
    if (!position) {
      return 'UNKNOWN';
    }
    if (typeof position !== 'string') {
      return position.role ?? 'UNKNOWN';
    }
    if (position.includes('_')) {
      return position;
    }
    const roleByPosition: Record<string, string> = {
      KP: 'KEEPER',
      MCD: 'MIDDLE_CENTRAL_DEFENDER',
      MCDO: 'MIDDLE_CENTRAL_DEFENDER',
      RCD: 'RIGHT_CENTRAL_DEFENDER',
      RCDO: 'RIGHT_CENTRAL_DEFENDER',
      RCDTW: 'RIGHT_CENTRAL_DEFENDER',
      LCD: 'LEFT_CENTRAL_DEFENDER',
      LCDO: 'LEFT_CENTRAL_DEFENDER',
      LCDTW: 'LEFT_CENTRAL_DEFENDER',
      RB: 'RIGHT_BACK',
      RBD: 'RIGHT_BACK',
      RBO: 'RIGHT_BACK',
      RBTM: 'RIGHT_BACK',
      LB: 'LEFT_BACK',
      LBD: 'LEFT_BACK',
      LBO: 'LEFT_BACK',
      LBTM: 'LEFT_BACK',
      RW: 'RIGHT_WINGER',
      RWD: 'RIGHT_WINGER',
      RWO: 'RIGHT_WINGER',
      RWTM: 'RIGHT_WINGER',
      LW: 'LEFT_WINGER',
      LWD: 'LEFT_WINGER',
      LWO: 'LEFT_WINGER',
      LWTM: 'LEFT_WINGER',
      MIM: 'MIDDLE_INNER_MIDFIELD',
      MIMD: 'MIDDLE_INNER_MIDFIELD',
      MIMO: 'MIDDLE_INNER_MIDFIELD',
      RIM: 'RIGHT_INNER_MIDFIELD',
      RIMD: 'RIGHT_INNER_MIDFIELD',
      RIMO: 'RIGHT_INNER_MIDFIELD',
      RIMTW: 'RIGHT_INNER_MIDFIELD',
      LIM: 'LEFT_INNER_MIDFIELD',
      LIMD: 'LEFT_INNER_MIDFIELD',
      LIMO: 'LEFT_INNER_MIDFIELD',
      LIMTW: 'LEFT_INNER_MIDFIELD',
      MFW: 'MIDDLE_FORWARD',
      MFWD: 'MIDDLE_FORWARD',
      RFW: 'RIGHT_FORWARD',
      RFWD: 'RIGHT_FORWARD',
      RFWTW: 'RIGHT_FORWARD',
      LFW: 'LEFT_FORWARD',
      LFWD: 'LEFT_FORWARD',
      LFWTW: 'LEFT_FORWARD'
    };
    return roleByPosition[position] ?? position;
  }

  private getPositionBehaviour(position: string | {behaviour?: string} | null | undefined): string {
    if (!position) {
      return 'UNKNOWN';
    }
    if (typeof position !== 'string') {
      return position.behaviour ?? 'UNKNOWN';
    }
    const behaviourByPosition: Record<string, string> = {
      MCDO: 'OFFENSIVE',
      RCDO: 'OFFENSIVE',
      RCDTW: 'TOWARDS_WING',
      LCDO: 'OFFENSIVE',
      LCDTW: 'TOWARDS_WING',
      RBD: 'DEFENSIVE',
      RBO: 'OFFENSIVE',
      RBTM: 'TOWARDS_MIDDLE',
      LBD: 'DEFENSIVE',
      LBO: 'OFFENSIVE',
      LBTM: 'TOWARDS_MIDDLE',
      RWD: 'DEFENSIVE',
      RWO: 'OFFENSIVE',
      RWTM: 'TOWARDS_MIDDLE',
      LWD: 'DEFENSIVE',
      LWO: 'OFFENSIVE',
      LWTM: 'TOWARDS_MIDDLE',
      MIMD: 'DEFENSIVE',
      MIMO: 'OFFENSIVE',
      RIMD: 'DEFENSIVE',
      RIMO: 'OFFENSIVE',
      RIMTW: 'TOWARDS_WING',
      LIMD: 'DEFENSIVE',
      LIMO: 'OFFENSIVE',
      LIMTW: 'TOWARDS_WING',
      MFWD: 'DEFENSIVE',
      RFWD: 'DEFENSIVE',
      RFWTW: 'TOWARDS_WING',
      LFWD: 'DEFENSIVE',
      LFWTW: 'TOWARDS_WING'
    };
    return behaviourByPosition[position] ?? 'NORMAL';
  }

  private getRoleCoordinates(role: string): {left: number; top: number} {
    const trapId = this.roleTrapByRole[role];
    const trap = this.lineupTraps.find(item => item.id === trapId);
    if (!trap) {
      return {left: 50, top: 50};
    }
    const avgX = trap.points.reduce((sum, point) => sum + point.x, 0) / trap.points.length;
    const avgY = trap.points.reduce((sum, point) => sum + point.y, 0) / trap.points.length;
    return {
      left: (avgX / 650) * 100,
      top: (avgY / 340) * 100
    };
  }

  private getTrapSidesForBehaviour(role: string, behaviour: string, trap: LineupTrap): number[] {
    switch (behaviour) {
      case 'OFFENSIVE':
        if (this.isInvertedVerticalDirectionRole(role)) {
          return [2];
        }
        if (this.isDefenderRole(role)) {
          return [2];
        }
        return [0];
      case 'DEFENSIVE':
        if (this.isInvertedVerticalDirectionRole(role)) {
          return [0];
        }
        if (this.isForwardRole(role)) {
          return [0];
        }
        return [2];
      case 'TOWARDS_MIDDLE': {
        const centerX = trap.points.reduce((sum, point) => sum + point.x, 0) / trap.points.length;
        if (centerX < 325) {
          return [1];
        }
        if (centerX > 325) {
          return [3];
        }
        return [0];
      }
      case 'TOWARDS_WING': {
        const centerX = trap.points.reduce((sum, point) => sum + point.x, 0) / trap.points.length;
        if (centerX < 325) {
          return [3];
        }
        if (centerX > 325) {
          return [1];
        }
        return [1, 3];
      }
      default:
        return [];
    }
  }

  private isForwardRole(role: string): boolean {
    return role.endsWith('FORWARD');
  }

  private isDefenderRole(role: string): boolean {
    return role.endsWith('BACK') || role.endsWith('DEFENDER');
  }

  private isInvertedVerticalDirectionRole(role: string): boolean {
    return role.endsWith('BACK')
      || role.endsWith('WINGER')
      || role === 'MIDDLE_INNER_MIDFIELD'
      || role === 'RIGHT_INNER_MIDFIELD'
      || role === 'LEFT_INNER_MIDFIELD';
  }

  private buildBandPolygon(points: LineupPoint[], sideIndex: number, width: number): string | null {
    if (points.length < 4) {
      return null;
    }
    const count = points.length;
    const p0 = points[sideIndex % count];
    const p1 = points[(sideIndex + 1) % count];
    const dx = p1.x - p0.x;
    const dy = p1.y - p0.y;
    const length = Math.hypot(dx, dy);
    if (!Number.isFinite(length) || length === 0) {
      return null;
    }
    const leftNormal = {x: -dy / length, y: dx / length};
    const mid = {x: (p0.x + p1.x) / 2, y: (p0.y + p1.y) / 2};
    const probe = {x: mid.x + leftNormal.x * width, y: mid.y + leftNormal.y * width};
    const inward = this.isPointInPolygon(probe, points) ? leftNormal : {x: -leftNormal.x, y: -leftNormal.y};
    const offsetX = inward.x * width;
    const offsetY = inward.y * width;
    const o0 = {x: p0.x + offsetX, y: p0.y + offsetY};
    const o1 = {x: p1.x + offsetX, y: p1.y + offsetY};
    const prev = points[(sideIndex - 1 + count) % count];
    const next = points[(sideIndex + 2) % count];
    const i0 = this.intersectLines(o0, o1, prev, p0);
    const i1 = this.intersectLines(o0, o1, p1, next);
    if (!i0 || !i1) {
      return `${p0.x},${p0.y} ${p1.x},${p1.y} ${o1.x},${o1.y} ${o0.x},${o0.y}`;
    }
    return `${p0.x},${p0.y} ${p1.x},${p1.y} ${i1.x},${i1.y} ${i0.x},${i0.y}`;
  }

  private buildTrianglePolygon(points: LineupPoint[], sideIndex: number, height: number): string | null {
    if (points.length < 3) {
      return null;
    }
    const count = points.length;
    const p0 = points[sideIndex % count];
    const p1 = points[(sideIndex + 1) % count];
    const dx = p1.x - p0.x;
    const dy = p1.y - p0.y;
    const length = Math.hypot(dx, dy);
    if (!Number.isFinite(length) || length === 0) {
      return null;
    }
    const ux = dx / length;
    const uy = dy / length;
    const leftNormal = {x: -uy, y: ux};
    const mid = {x: (p0.x + p1.x) / 2, y: (p0.y + p1.y) / 2};
    const probe = {x: mid.x + leftNormal.x * height, y: mid.y + leftNormal.y * height};
    const inward = this.isPointInPolygon(probe, points) ? leftNormal : {x: -leftNormal.x, y: -leftNormal.y};
    const outward = {x: -inward.x, y: -inward.y};
    const tip = {x: mid.x + outward.x * height, y: mid.y + outward.y * height};
    return `${p0.x},${p0.y} ${p1.x},${p1.y} ${tip.x},${tip.y}`;
  }

  private isPointInPolygon(point: LineupPoint, polygon: LineupPoint[]): boolean {
    let inside = false;
    for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
      const xi = polygon[i].x;
      const yi = polygon[i].y;
      const xj = polygon[j].x;
      const yj = polygon[j].y;
      const intersect = ((yi > point.y) !== (yj > point.y))
        && (point.x < (((xj - xi) * (point.y - yi)) / (yj - yi) + xi));
      if (intersect) {
        inside = !inside;
      }
    }
    return inside;
  }

  private intersectLines(a1: LineupPoint, a2: LineupPoint, b1: LineupPoint, b2: LineupPoint): LineupPoint | null {
    const x1 = a1.x;
    const y1 = a1.y;
    const x2 = a2.x;
    const y2 = a2.y;
    const x3 = b1.x;
    const y3 = b1.y;
    const x4 = b2.x;
    const y4 = b2.y;
    const denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
    if (!Number.isFinite(denom) || denom === 0) {
      return null;
    }
    const det1 = x1 * y2 - y1 * x2;
    const det2 = x3 * y4 - y3 * x4;
    const px = (det1 * (x3 - x4) - (x1 - x2) * det2) / denom;
    const py = (det1 * (y3 - y4) - (y1 - y2) * det2) / denom;
    return {x: px, y: py};
  }

  private getPlayerShortName(playerId: number): string {
    const player = this.players.find(item => item.id === playerId);
    if (!player) {
      return `${playerId}`;
    }
    if (player.lastName) {
      return player.lastName.substring(0, 14);
    }
    if (player.nickName) {
      return player.nickName.substring(0, 14);
    }
    if (player.firstName) {
      return player.firstName.substring(0, 14);
    }
    return `${player.id}`;
  }

  private getPlayerTooltip(playerId: number): string {
    const player = this.players.find(item => item.id === playerId);
    if (!player) {
      return `(${playerId})`;
    }
    const name = [player.firstName, player.nickName ? `'${player.nickName}'` : '', player.lastName]
      .map(value => (value ?? '').trim())
      .filter(value => value.length > 0)
      .join(' ');
    const fullName = name || `${player.id}`;
    return `${fullName} (${player.id})`;
  }

  private mapTrainingTypeToStage(typeId: number): string {
    switch (typeId) {
      case 2:
        return 'SET_PIECES';
      case 3:
        return 'DEFENDING';
      case 4:
        return 'SCORING';
      case 5:
        return 'WINGER';
      case 6:
        return 'SCORING_SET_PIECES';
      case 7:
        return 'PASSING';
      case 8:
        return 'PLAY_MAKING';
      case 9:
        return 'GOALKEEPING';
      case 10:
        return 'PASSING_EXTENSIVE';
      case 11:
        return 'DEFENDING_EXTENSIVE';
      case 12:
        return 'WINGER_EXTENSIVE';
      default:
        throw new Error(`Unsupported training type: ${typeId}`);
    }
  }
}
