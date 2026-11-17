import {fakeAsync, tick} from '@angular/core/testing';
import {MainComponent} from './main.component';
import {PlayerInfo, PlayerSort, Project, UserConfig} from '../services/model/data-response';
import {PlayService} from '../services/play.service';

describe('Planner custom players', () => {
  let component: MainComponent;
  let state: any;
  let play: any;
  let configService: any;
  const week = {season: 90, week: 3, date: '2026-11-17T12:00:00Z'};

  beforeEach(() => {
    play = jasmine.createSpyObj('PlayService', ['getCurrentWeekInfo', 'sortPlayers', 'setPlannerCanSave', 'setPlannerDirty', 'setPlannerCanClear', 'updateSelectedProject', 'goToSeasonAndWeek']);
    play.getCurrentWeekInfo.and.returnValue(week);
    play.sortPlayers.and.callFake((players: PlayerInfo[]) => players);
    configService = jasmine.createSpyObj('UserConfigService', ['getUserConfig', 'setUserConfig', 'saveUserConfig']);
    component = new MainComponent(play, {} as any, configService, {
      instant: (key: string, params: {suffix?: string; name?: string}) => key === 'ehm.confirm-delete-custom-player'
        ? `¿Eliminar a «${params.name}» de esta planificación?` : `jugador ${params.suffix}`
    } as any);
    component.viewMode = 'training-planner';
    state = component as any;
    spyOn(state, 'scheduleTeamTrainingRequest');
    component.basePlayers = [{id: 123, firstName: 'Real'} as PlayerInfo];
    state.refreshPlayers();
  });

  it('inserts new players at the beginning in edit mode, including when the list was collapsed', fakeAsync(() => {
    component.showPlannerPlayers = false;
    component.addCustomPlayer();
    component.addCustomPlayer();
    tick();
    expect(component.players.map(player => player.id)).toEqual([-2, 123, -1]);
    expect(component.customPlayers.map(player => player.firstName)).toEqual(['jugador A', 'jugador B']);
    const player = component.customPlayers[0];
    expect([player.staminaSkill, player.keeperSkill, player.defenderSkill, player.playmakerSkill,
      player.wingerSkill, player.passingSkill, player.scorerSkill, player.setPiecesSkill,
      player.playerForm, player.experience, player.loyalty, player.leadership, player.specialty]).toEqual([5, 1, 5, 5, 5, 5, 5, 1, 7, 1, 1, 1, 0]);
    expect(component.editingCustomPlayerIds.has(-2)).toBeTrue();
    expect(component.showPlannerPlayers).toBeTrue();
    expect(play.setPlannerCanSave).toHaveBeenCalledWith(false);
  }));

  it('continues naming beyond Z and keeps custom names on reload', fakeAsync(() => {
    component.customPlayers = [{id: -26, firstName: 'My custom name'} as PlayerInfo];
    component.addCustomPlayer();
    tick();
    expect(component.customPlayers[0].firstName).toBe('My custom name');
    expect(component.customPlayers[1].firstName).toBe('jugador AA');
  }));

  it('sorts real and custom players together in both directions without stages', () => {
    const sorter = new PlayService(configService);
    let sort: PlayerSort = {criteria: 'age', mode: 'asc'};
    play.sortPlayers.and.callFake((players: PlayerInfo[]) => sorter.sortPlayers(players, sort));
    component.basePlayers = [{id: 123, age: 20}, {id: 456, age: 30}] as PlayerInfo[];
    component.customPlayers = [{id: -1, age: 25}] as PlayerInfo[];
    state.refreshPlayers();
    expect(component.players.map(player => player.id)).toEqual([123, -1, 456]);
    sort = {...sort, mode: 'desc'};
    state.refreshPlayers();
    expect(component.players.map(player => player.id)).toEqual([456, -1, 123]);
    expect(component.basePlayers.map(player => player.id)).toEqual([123, 456]);
    component.viewMode = 'players';
    state.refreshPlayers();
    expect(component.players.map(player => player.id)).toEqual([123, 456]);
  });

  it('sorts a planner with historical players without changing inclusion weeks or percentages', () => {
    const sorter = new PlayService(configService);
    play.sortPlayers.and.callFake((players: PlayerInfo[]) => sorter.sortPlayers(players, {criteria: 'age', mode: 'asc'}));
    const first = {id: 123, age: 20} as PlayerInfo;
    const later = {id: 456, age: 30} as PlayerInfo;
    component.basePlayers = [first];
    component.customPlayers = [{id: -1, age: 25}] as PlayerInfo[];
    component.trainingPlans = [{typeId: 4, weeks: 4, coach: 5, assistants: 10, intensity: 100, stamina: 10}];
    component.trainingPlanPercents = {123: [100], 456: [0], [-1]: [50]};
    state.selectedTeam = {weeklyData: [
      {...week, players: [first]},
      {...week, week: 4, players: [first]},
      {...week, week: 5, players: [first, later]}
    ]};
    state.refreshPlayers();
    expect(component.players.map(player => player.id)).toEqual([123, -1, 456]);
    expect(state.plannerPlayerWindowsByPlayerId[456].inclusionWeek).toBe(2);
    expect(state.plannerPlayerWindowsByPlayerId[-1].inclusionWeek).toBe(1);
    const request = state.buildTeamTrainingRequest(false);
    expect(request.participations.find((entry: any) => entry.playerId === -1).participation).toBe(50);
    expect(request.participations.find((entry: any) => entry.playerId === 456).participation).toBe(0);
  });

  it('places a newly added player first and applies the selected sort when its card is saved', fakeAsync(() => {
    const sorter = new PlayService(configService);
    play.sortPlayers.and.callFake((players: PlayerInfo[]) => sorter.sortPlayers(players, {criteria: 'scorerSkill', mode: 'asc'}));
    component.basePlayers = [{id: 123, scorerSkill: 1}] as PlayerInfo[];
    component.addCustomPlayer();
    tick();
    expect(component.players.map(player => player.id)).toEqual([-1, 123]);
    component.saveCustomPlayer(component.customPlayers[0]);
    expect(component.players.map(player => player.id)).toEqual([123, -1]);
  }));

  it('supports custom full names and shirt numbers with the shared sorter', () => {
    const sorter = new PlayService(configService);
    let sort: PlayerSort = {criteria: 'name', mode: 'asc'};
    play.sortPlayers.and.callFake((players: PlayerInfo[]) => sorter.sortPlayers(players, sort));
    component.basePlayers = [{id: 123, firstName: 'Ana', lastName: 'Pérez', playerNumber: 9}] as PlayerInfo[];
    component.customPlayers = [{id: -1, firstName: 'Zoe López', lastName: '', playerNumber: 2}] as PlayerInfo[];
    state.refreshPlayers();
    expect(component.players.map(player => player.id)).toEqual([123, -1]);
    sort = {criteria: 'playerNumber', mode: 'asc'};
    state.refreshPlayers();
    expect(component.players.map(player => player.id)).toEqual([-1, 123]);
  });

  it('preserves skills and participation when saving and reloading a plan', fakeAsync(() => {
    const project = {name: 'Plan', teamId: 1, iniSeason: 90, iniWeek: 3} as Project;
    const config = {projects: [project]} as UserConfig;
    configService.getUserConfig.and.returnValue(config);
    state.selectedProject = project;
    component.trainingPlans = [{typeId: 4, weeks: 4, coach: 5, assistants: 10, intensity: 100, stamina: 10}];
    component.addCustomPlayer();
    tick();
    const player = {...component.customPlayers[0], firstName: 'Prospect', age: 19, scorerSkill: 12};
    component.saveCustomPlayer(player);
    component.trainingPlanPercents[-1] = [50];
    state.savePlannerState();
    expect(configService.saveUserConfig).toHaveBeenCalled();
    expect(project.planner!.customPlayers![0].scorerSkill).toBe(12);
    state.resetPlannerState();
    state.loadPlannerState(project);
    expect(component.players.map(player => player.id)).toEqual([123, -1]);
    expect(component.players[1].firstName).toBe('Prospect');
    expect(component.trainingPlanPercents[-1]).toEqual([50]);
    expect(component.editingCustomPlayerIds.size).toBe(0);
    const request = state.buildTeamTrainingRequest(false);
    expect(request.players[1]).toEqual({player, inclusionWeek: 1});
    expect(request.participations.find((item: any) => item.playerId === -1).participation).toBe(50);
  }));

  it('keeps custom players outside real-player filters and historical departures', fakeAsync(() => {
    component.addCustomPlayer();
    tick();
    const player = component.customPlayers[0];
    component.saveCustomPlayer(player);
    state.selectedProject = {planner: {filter: {mode: 'inclusive', playerIds: [123]}}};
    state.selectedTeam = {weeklyData: [{...week, players: component.basePlayers}, {...week, week: 4, players: []}]};
    component.trainingPlans = [{weeks: 4} as any];
    state.refreshPlayers();
    state.refreshPlayers();
    expect(component.players.map(p => p.id)).toEqual([-1]);
    expect(state.getPlannerTrainingPlayers()).toEqual([{player, inclusionWeek: 1}]);
  }));

  it('uses the saved custom players when rebuilding a saved plan request', fakeAsync(() => {
    component.addCustomPlayer();
    tick();
    const player = component.customPlayers[0];
    const planner = {...state.buildProjectPlannerState(), iniSeason: 90, iniWeek: 3};
    state.selectedTeam = {weeklyData: [{...week, players: component.basePlayers}]};
    component.customPlayers = [];
    const request = state.buildTeamTrainingRequestFromPlanner(planner);
    expect(request.players.some((entry: any) => entry.player.id === player.id)).toBeTrue();
  }));

  it('allows saving a custom roster before adding stages and clears it with the plan', fakeAsync(() => {
    const project = {name: 'Plan', teamId: 1} as Project;
    state.selectedProject = project;
    configService.getUserConfig.and.returnValue({projects: [project]});
    component.addCustomPlayer();
    tick();
    component.saveCustomPlayer(component.customPlayers[0]);
    expect(play.setPlannerCanSave).toHaveBeenCalledWith(true);
    state.savePlannerState();
    expect(project.planner!.customPlayers!.length).toBe(1);
    state.clearPlannerState();
    expect(component.customPlayers).toEqual([]);
    expect(component.players.map(p => p.id)).toEqual([123]);
  }));

  it('does not store stale results after editing a player', fakeAsync(() => {
    component.addCustomPlayer();
    tick();
    component.teamTrainingResponse = {endWeek: week, weekPlayers: {1: []}};
    state.lastSuccessfulTeamTrainingRequestKey = 'previous roster';
    expect(state.buildProjectPlannerState().teamTrainingResponse).toBeUndefined();
  }));

  it('keeps the player and participation when deletion is cancelled', fakeAsync(() => {
    component.addCustomPlayer();
    tick();
    component.trainingPlanPercents[-1] = [50];
    const confirm = spyOn(window, 'confirm').and.returnValue(false);
    component.deleteCustomPlayer(-1);
    expect(confirm).toHaveBeenCalledWith('¿Eliminar a «jugador A» de esta planificación?');
    expect(component.customPlayers.length).toBe(1);
    expect(component.trainingPlanPercents[-1]).toEqual([50]);
    expect(component.editingCustomPlayerIds.has(-1)).toBeTrue();
    expect(state.scheduleTeamTrainingRequest).not.toHaveBeenCalled();
  }));

  it('removes a confirmed player, clears obsolete results and persists the deletion', fakeAsync(() => {
    const project = {name: 'Plan', teamId: 1, iniSeason: 90, iniWeek: 3} as Project;
    configService.getUserConfig.and.returnValue({projects: [project]});
    state.selectedProject = project;
    component.trainingPlans = [{typeId: 4, weeks: 4, coach: 5, assistants: 10, intensity: 100, stamina: 10}];
    component.addCustomPlayer();
    component.addCustomPlayer();
    tick();
    component.saveCustomPlayer({...component.customPlayers[0], firstName: 'Full player name', playerNumber: 10});
    component.saveCustomPlayer(component.customPlayers[1]);
    component.trainingPlanPercents[-1] = [50];
    component.trainingPlanPercents[-2] = [100];
    state.savePlannerState();
    expect(project.planner!.customPlayers![0].playerNumber).toBe(10);
    state.loadPlannerState(project);
    expect(component.customPlayers[0].firstName).toBe('Full player name');
    expect(component.customPlayers[0].playerNumber).toBe(10);
    component.teamTrainingResponse = {endWeek: week, weekPlayers: {1: component.players}};
    spyOn(window, 'confirm').and.returnValue(true);
    component.deleteCustomPlayer(-1);
    expect(component.players.map(player => player.id)).toEqual([123, -2]);
    expect(component.trainingPlanPercents[-1]).toBeUndefined();
    expect(component.trainingPlanPercents[-2]).toEqual([100]);
    expect(component.teamTrainingResponse).toBeNull();
    expect(state.scheduleTeamTrainingRequest).toHaveBeenCalled();
    state.savePlannerState();
    state.resetPlannerState();
    state.loadPlannerState(project);
    expect(component.customPlayers.map(player => player.id)).toEqual([-2]);
  }));

  it('can save the deletion of the final custom player even without training stages', fakeAsync(() => {
    const project = {name: 'Plan', teamId: 1} as Project;
    state.selectedProject = project;
    configService.getUserConfig.and.returnValue({projects: [project]});
    component.basePlayers = [];
    component.addCustomPlayer();
    tick();
    component.saveCustomPlayer(component.customPlayers[0]);
    state.savePlannerState();
    spyOn(window, 'confirm').and.returnValue(true);
    component.deleteCustomPlayer(-1);
    expect(component.players).toEqual([]);
    expect(component.editingCustomPlayerIds.size).toBe(0);
    expect(play.setPlannerCanSave).toHaveBeenCalledWith(true);
    state.savePlannerState();
    expect(project.planner?.customPlayers?.length ?? 0).toBe(0);
  }));
});
