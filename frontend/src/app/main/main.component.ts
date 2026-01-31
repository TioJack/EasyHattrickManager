import {Component, OnInit} from '@angular/core';
import {PlayService, ViewMode} from '../services/play.service';
import {PlayerInfo, Project, StaffInfo, TrainingInfo, UserConfig} from '../services/model/data-response';
import {DatePipe, NgForOf, NgIf} from '@angular/common';
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
import {StagePlayerParticipation, TeamTrainingRequest, TeamTrainingResponse, TrainingStage} from '../services/model/team-training';
import {WeekInfo} from '../player/model/week-info';
import {SalaryPipe} from '../pipes/salary.pipe';
import {NumberSeparatorPipe} from '../pipes/number-separator.pipe';
import {UserConfigService} from '../services/user-config.service';

@Component({
  selector: 'app-main',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    DatePipe,
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
  teamTrainingResponse: TeamTrainingResponse | null = null;
  endWeekInfo: WeekInfo | null = null;
  userConfig: UserConfig | null = null;
  plannerResultByPlayerId: Record<number, PlayerInfo> = {};
  private plannerRequestTimer: number | null = null;
  private latestResponseWeek: number | null = null;
  private hoverWeekByPlayerId: Record<number, number> = {};
  private hoverTimelinePercentByPlayerId: Record<number, number> = {};
  private lastProjectKey: string | null = null;
  private readonly timelinePopupWidth = 240;
  protected readonly DEFAULT_DATE_FORMAT = DEFAULT_DATE_FORMAT;

  constructor(
    private playService: PlayService,
    private dataService: DataService,
    private userConfigService: UserConfigService,
    private translateService: TranslateService
  ) {
  }

  ngOnInit(): void {
    this.playService.players$.subscribe(players => {
      this.players = players;
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
      const nextKey = this.buildProjectKey(project);
      if (nextKey !== this.lastProjectKey) {
        this.resetPlannerState();
        this.lastProjectKey = nextKey;
      }
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
    this.scheduleTeamTrainingRequest();
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

  private requestTeamTraining(): void {
    if (!this.players.length || !this.trainingPlans.length) {
      return;
    }
    const request = this.buildTeamTrainingRequest();
    if (!request) {
      return;
    }
    this.dataService.teamTraining(request).subscribe({
      next: response => {
        this.teamTrainingResponse = response;
        this.plannerResultByPlayerId = this.buildPlannerResultMap(response);
        this.endWeekInfo = response.endWeek ?? null;
      },
      error: error => {
        console.error('teamTraining request failed', error);
        this.plannerResultByPlayerId = {};
        this.latestResponseWeek = null;
        this.endWeekInfo = null;
      }
    });
  }

  private scheduleTeamTrainingRequest(): void {
    if (this.plannerRequestTimer !== null) {
      clearTimeout(this.plannerRequestTimer);
    }
    this.plannerRequestTimer = window.setTimeout(() => {
      this.plannerRequestTimer = null;
      this.requestTeamTraining();
    }, 250);
  }

  private resetPlannerState(): void {
    if (this.plannerRequestTimer !== null) {
      clearTimeout(this.plannerRequestTimer);
      this.plannerRequestTimer = null;
    }
    this.trainingPlans = [];
    this.trainingPlanPercents = {};
    this.teamTrainingResponse = null;
    this.endWeekInfo = null;
    this.plannerResultByPlayerId = {};
    this.latestResponseWeek = null;
    this.hoverWeekByPlayerId = {};
    this.hoverTimelinePercentByPlayerId = {};
  }

  private buildProjectKey(project: Project | null): string | null {
    if (!project) {
      return null;
    }
    return [
      project.name,
      project.teamId,
      project.iniSeason,
      project.iniWeek,
      project.endSeason ?? '',
      project.endWeek ?? ''
    ].join('|');
  }

  private buildTeamTrainingRequest(): TeamTrainingRequest | null {
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
      participations
    };
  }

  private getCurrentWeekInfo(): WeekInfo {
    return this.playService.getCurrentWeekInfo() ?? {
      season: 0,
      week: 0,
      date: new Date().toISOString()
    };
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
    const players = this.teamTrainingResponse.weekPlayers[week] ?? [];
    return players.find(player => player.id === playerId) ?? null;
  }

  getMaxHtmsRanges(playerId: number): Array<{leftPercent: number; widthPercent: number}> {
    const weekCount = this.getPlannerWeekCount();
    if (!this.teamTrainingResponse?.weekPlayers || weekCount <= 0) {
      return [];
    }
    const htmsByWeek: Array<number | null> = [];
    for (let week = 1; week <= weekCount; week++) {
      const players = this.teamTrainingResponse.weekPlayers[week] ?? [];
      const player = players.find(candidate => candidate.id === playerId);
      const htms = player?.playerSubSkill?.htms ?? player?.htms ?? null;
      htmsByWeek.push(htms);
    }
    const maxHtms = htmsByWeek.reduce((max, value) => {
      if (value == null) {
        return max;
      }
      if (max == null) {
        return value;
      }
      return Math.max(max, value);
    }, null as number | null);
    if (maxHtms == null || !Number.isFinite(maxHtms)) {
      return [];
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
    return ranges.map(range => {
      const leftPercent = (range.start - 1) * percentPerWeek;
      const widthPercent = (range.end - range.start + 1) * percentPerWeek;
      return {leftPercent, widthPercent};
    });
  }

  private getMaxHtmsWeek(playerId: number, weekCount: number): number | null {
    let bestWeek: number | null = null;
    let bestHtms = -Infinity;
    for (let week = 1; week <= weekCount; week++) {
      const players = this.teamTrainingResponse?.weekPlayers?.[week] ?? [];
      const player = players.find(candidate => candidate.id === playerId);
      if (!player) {
        continue;
      }
      const htms = player.playerSubSkill?.htms ?? player.htms ?? 0;
      if (htms > bestHtms) {
        bestHtms = htms;
        bestWeek = week;
      }
    }
    return bestWeek;
  }

  private isMaxHtmsWeek(playerId: number, week: number, weekCount: number): boolean {
    if (!this.teamTrainingResponse?.weekPlayers || week <= 0 || weekCount <= 0) {
      return false;
    }
    let maxHtms: number | null = null;
    for (let i = 1; i <= weekCount; i++) {
      const players = this.teamTrainingResponse.weekPlayers[i] ?? [];
      const player = players.find(candidate => candidate.id === playerId);
      const htms = player?.playerSubSkill?.htms ?? player?.htms ?? null;
      if (htms == null) {
        continue;
      }
      maxHtms = maxHtms == null ? htms : Math.max(maxHtms, htms);
    }
    if (maxHtms == null) {
      return false;
    }
    const players = this.teamTrainingResponse.weekPlayers[week] ?? [];
    const player = players.find(candidate => candidate.id === playerId);
    const htms = player?.playerSubSkill?.htms ?? player?.htms ?? null;
    return htms != null && htms === maxHtms;
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
