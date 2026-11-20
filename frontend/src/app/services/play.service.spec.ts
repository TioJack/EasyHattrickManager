import {firstValueFrom, of} from 'rxjs';
import {DataService} from './data.service';
import {PlayService} from './play.service';
import {UserConfigService} from './user-config.service';
import {PlayerInfo, UserConfig} from './model/data-response';

describe('Player list layout preference', () => {
  let data: jasmine.SpyObj<DataService>;
  let configService: UserConfigService;
  let play: PlayService;
  const initialConfig: UserConfig = {
    languageId: 6, dateFormat: 'dd/MM/yyyy', showSubSkills: true, showTrainingInfo: true,
    currency: {countryId: 1, currencyName: 'Euro', currencyCode: 'EUR', currencyRate: 1},
    projects: [{name: 'Team', teamId: 1, iniSeason: 90, iniWeek: 1,
      filter: {mode: 'exclusive', playerIds: [123]}, sort: {mode: 'asc', criteria: 'name'}}]
  };

  beforeEach(() => {
    data = jasmine.createSpyObj('DataService', ['saveUserConfig']);
    data.saveUserConfig.and.returnValue(of(undefined));
    configService = new UserConfigService(data);
    play = new PlayService(configService);
  });

  it('defaults to cards with a legacy configuration', async () => {
    configService.setUserConfig(initialConfig);
    expect(await firstValueFrom(play.playerListLayout$)).toBe('cards');
    expect(data.saveUserConfig).not.toHaveBeenCalled();
  });

  it('saves both layouts and restores each on a fresh page without changing other settings', async () => {
    configService.setUserConfig(initialConfig);
    for (const layout of ['table', 'cards'] as const) {
      play.togglePlayerListLayout();
      const saved = data.saveUserConfig.calls.mostRecent().args[0];
      expect(saved).toEqual({...initialConfig, playerListLayout: layout});
      expect(await firstValueFrom(play.playerListLayout$)).toBe(layout);

      const reloadedConfig = new UserConfigService(data);
      const reloadedPlay = new PlayService(reloadedConfig);
      reloadedConfig.setUserConfig(JSON.parse(JSON.stringify(saved)));
      expect(await firstValueFrom(reloadedPlay.playerListLayout$)).toBe(layout);
    }
    expect(data.saveUserConfig).toHaveBeenCalledTimes(2);
    expect(initialConfig.playerListLayout).toBeUndefined();
  });

  it('keeps the selected layout when another preference changes', async () => {
    configService.setUserConfig({...initialConfig, playerListLayout: 'table'});
    configService.setAndSaveUserConfig({...configService.getUserConfig()!, showSubSkills: false});
    expect(await firstValueFrom(play.playerListLayout$)).toBe('table');
    expect(data.saveUserConfig.calls.mostRecent().args[0].playerListLayout).toBe('table');
  });
});

describe('Player sorting with subskills', () => {
  let play: PlayService;

  beforeEach(() => {
    play = new PlayService(new UserConfigService({} as DataService));
  });

  it('sorts experience using its calculated sublevel, including a zero value', () => {
    const players = [
      {id: 1, experience: 6, playerExpInfo: {exp: 6.9}},
      {id: 2, experience: 6, playerExpInfo: {exp: 6.035}},
      {id: 3, experience: 0, playerExpInfo: {exp: 0}},
      {id: 4, experience: 6}
    ] as PlayerInfo[];
    expect(play.sortPlayers(players, {criteria: 'experience', mode: 'asc'}).map(player => player.id))
      .toEqual([3, 4, 2, 1]);
    expect(play.sortPlayers(players, {criteria: 'experience', mode: 'desc'}).map(player => player.id))
      .toEqual([1, 2, 4, 3]);
  });

  const skills = [
    ['staminaSkill', 'stamina'], ['keeperSkill', 'keeper'], ['defenderSkill', 'defender'],
    ['playmakerSkill', 'playmaker'], ['wingerSkill', 'winger'], ['passingSkill', 'passing'],
    ['scorerSkill', 'scorer'], ['setPiecesSkill', 'setPieces']
  ];

  for (const [criteria, subSkill] of skills) {
    it(`sorts ${criteria} by the full skill in both directions, including zero and missing subskills`, () => {
      const players = [
        {id: 1, [criteria]: 7, playerSubSkill: {[subSkill]: 0.9}},
        {id: 2, [criteria]: 7, playerSubSkill: {[subSkill]: 0.1}},
        {id: 3, [criteria]: 8, playerSubSkill: {[subSkill]: 0}},
        {id: 4, [criteria]: 0, playerSubSkill: {[subSkill]: 0.75}},
        {id: 5, [criteria]: 0, playerSubSkill: null},
        {id: 6, [criteria]: null, playerSubSkill: {[subSkill]: 0.5}},
        {id: 7, [criteria]: 7},
        {id: 8, [criteria]: 7, playerSubSkill: {}},
        {id: 9, [criteria]: 0, playerSubSkill: {[subSkill]: 0}}
      ] as unknown as PlayerInfo[];
      const original = structuredClone(players);

      expect(play.sortPlayers(players, {criteria, mode: 'asc'}).map(player => player.id))
        .toEqual([5, 9, 4, 7, 8, 2, 1, 3, 6]);
      expect(play.sortPlayers(players, {criteria, mode: 'desc'}).map(player => player.id))
        .toEqual([3, 1, 2, 8, 7, 4, 9, 5, 6]);
      expect(players).toEqual(original);
    });
  }

  it('uses subskills when their display is disabled', () => {
    const configService = new UserConfigService({} as DataService);
    configService.setUserConfig({showSubSkills: false} as UserConfig);
    play = new PlayService(configService);
    const players = [
      {id: 1, scorerSkill: 7, playerSubSkill: {scorer: 0.9}},
      {id: 2, scorerSkill: 7, playerSubSkill: {scorer: 0.1}}
    ] as PlayerInfo[];
    expect(play.sortPlayers(players, {criteria: 'scorerSkill', mode: 'asc'}).map(player => player.id)).toEqual([2, 1]);
  });
});
