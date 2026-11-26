import {Subject, throwError} from 'rxjs';
import {PlayerInfo, TeamExtendedInfo, WeeklyInfo} from './model/data-response';
import {SubskillEditService} from './subskill-edit.service';
import {DataService} from './data.service';

describe('Manual sublevel editing', () => {
  let editor: SubskillEditService;
  let data: jasmine.SpyObj<DataService>;
  let play: any;
  let player: PlayerInfo;
  let week: WeeklyInfo;

  beforeEach(() => {
    player = {id: 4, age: 20, ageDays: 42, keeperSkill: 5, playerForm: 7, experience: 6,
      playerSubSkill: {keeper: 0.123456, defender: 0.5}, playerFormInfo: {form: 8.4}, playerExpInfo: {exp: 6.25}} as PlayerInfo;
    week = {season: 90, week: 1, players: [player]} as WeeklyInfo;
    play = {getCurrentWeekInfo: () => week, getSelectedTeam: () => ({team: {id: 2}, weeklyData: [week]} as TeamExtendedInfo), update: jasmine.createSpy('update')};
    data = jasmine.createSpyObj('DataService', ['saveSubskills']);
    editor = new SubskillEditService(data, play);
    editor.start();
  });

  it('keeps drafts separate and discards without requests or precision loss', () => {
    editor.set(player, 'keeper', '75', '12');
    expect(editor.text(player, 'keeper', '12')).toBe('75');
    expect(player.playerSubSkill!.keeper).toBe(0.123456);
    editor.finish();
    expect(data.saveSubskills).not.toHaveBeenCalled();
    expect(player.playerSubSkill!.keeper).toBe(0.123456);
    expect(editor.dirty).toBeFalse();
    expect(play.subskillEditing).toBeFalse();
  });

  it('sends the exact previous fraction, updates server calculations and exits after success', () => {
    editor.set(player, 'keeper', '75', '12');
    const response = new Subject<PlayerInfo[]>();
    data.saveSubskills.and.returnValue(response);
    const done = jasmine.createSpy('done');
    editor.save(done);
    expect(editor.saving).toBeTrue();
    expect(editor.editing).toBeTrue();
    expect(data.saveSubskills).toHaveBeenCalledOnceWith({teamId: 2, season: 90, week: 1,
      changes: [{playerId: 4, skill: 'keeper', oldValue: 0.123456, newValue: 0.75}]});
    response.next([{...player, playerSubSkill: {...player.playerSubSkill!, keeper: 0.75, htms: 999}}]);
    expect(player.playerSubSkill!.htms).toBe(999);
    expect(player.keeperSkill).toBe(5);
    expect(editor.editing).toBeFalse();
    expect(done).toHaveBeenCalled();
    expect(play.update).toHaveBeenCalled();
  });

  it('preserves drafts when saving fails or conflicts', () => {
    editor.set(player, 'keeper', '75', '12');
    data.saveSubskills.and.returnValue(throwError(() => ({status: 409})));
    editor.save(() => fail('must remain in edit mode'));
    expect(editor.error).toBe('ehm.subskill-conflict');
    expect(editor.text(player, 'keeper', '12')).toBe('75');
    expect(editor.saving).toBeFalse();
    expect(editor.editing).toBeTrue();
  });

  it('validates empty and invalid decimals and allows a corrected retry', () => {
    for (const value of ['', '-1', 'abc', '100', '1.2']) {
      editor.set(player, 'keeper', value, '12');
      expect(editor.valid).toBeFalse();
      editor.save(() => fail('invalid field'));
    }
    expect(data.saveSubskills).not.toHaveBeenCalled();
    editor.set(player, 'keeper', '09', '12');
    expect(editor.valid).toBeTrue();
  });

  it('removes reverted drafts and preserves unedited decimal precision', () => {
    editor.set(player, 'keeper', '75', '12');
    editor.set(player, 'keeper', '12', '12');
    expect(editor.dirty).toBeFalse();
    expect(player.playerSubSkill!.keeper).toBe(0.123456);
    editor.save(() => {});
    expect(data.saveSubskills).not.toHaveBeenCalled();
    expect(editor.editing).toBeFalse();
  });

  it('uses the same visible form fraction as the card and table', () => {
    expect(editor.value(player, 'form')).toBe(0.99);
    player.playerForm = 8;
    expect(editor.value(player, 'form')).toBe(0);
    expect(editor.value(player, 'experience')).toBe(0.25);
  });

  it('recalculates draft HTMS without mutating saved values and restores it after reset or discard', () => {
    const saved = player.playerSubSkill!;
    const initial = {...editor.getHtms(player)!};
    editor.set(player, 'keeper', '99', '12');
    const updated = editor.getHtms(player)!;
    expect(updated.htms).toBeGreaterThan(initial.htms);
    expect(updated.htms28).toBeGreaterThan(initial.htms28);
    expect(player.playerSubSkill).toBe(saved);
    expect(saved.keeper).toBe(0.123456);
    expect(data.saveSubskills).not.toHaveBeenCalled();
    editor.reset(player, 'keeper');
    expect(editor.getHtms(player)).toEqual(initial);
    editor.set(player, 'keeper', '99', '12');
    editor.finish();
    expect(editor.getHtms(player)).toBe(saved);
  });

  it('uses all valid draft skills together and keeps HTMS independent of stamina, form and experience', () => {
    const initial = editor.getHtms(player)!;
    editor.set(player, 'form', '50', '99');
    editor.set(player, 'stamina', '50', '00');
    editor.set(player, 'experience', '50', '25');
    expect(editor.getHtms(player)).toEqual(initial);
    editor.set(player, 'keeper', '99', '12');
    const keeperOnly = editor.getHtms(player)!;
    editor.set(player, 'defender', '99', '50');
    expect(editor.getHtms(player)!.htms).toBeGreaterThan(keeperOnly.htms);
    editor.set(player, 'defender', '', '50');
    expect(editor.getHtms(player)).toEqual(keeperOnly);
  });

  it('requires the exit dialog to resolve navigation and supports keeping the draft', async () => {
    editor.exitRequested.subscribe(() => editor.cancelExit());
    expect(await editor.requestLeave()).toBeFalse();
    expect(editor.editing).toBeTrue();
  });
});
