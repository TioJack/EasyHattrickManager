import {Component} from '@angular/core';
import {NgIf} from '@angular/common';
import {TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {provideTranslateService} from '@ngx-translate/core';
import {BehaviorSubject, of, Subject} from 'rxjs';
import {PlayerCardComponent} from '../player-card/player-card.component';
import {PlayerTableComponent} from '../player-table/player-table.component';
import {SubskillControlsComponent} from './subskill-controls.component';
import {SubskillEditService} from '../services/subskill-edit.service';
import {DataService} from '../services/data.service';
import {PlayService} from '../services/play.service';
import {UserConfigService} from '../services/user-config.service';
import {PlayerInfo, UserConfig} from '../services/model/data-response';

@Component({standalone: true, imports: [NgIf, PlayerCardComponent, PlayerTableComponent, SubskillControlsComponent],
  template: `<app-subskill-controls />
    <app-player-card *ngIf="!table" [player]="player" />
    <app-player-table *ngIf="table" [players]="[player]" />`})
class EditorHost {
  table = false;
  player = {id: 4, firstName: 'Test', lastName: 'Player', age: 20, ageDays: 0, salary: 1000,
    experience: 5, leadership: 5, loyalty: 5, playerForm: 7, injuryLevel: -1, countryId: 1,
    staminaSkill: 7, keeperSkill: 6, defenderSkill: 6, playmakerSkill: 6, wingerSkill: 6,
    passingSkill: 6, scorerSkill: 6, setPiecesSkill: 6, playerSubSkill: {keeper: .12567}, changes: {}} as PlayerInfo;
}

describe('Player sublevel edit controls and views', () => {
  let fixture: ReturnType<typeof TestBed.createComponent<EditorHost>>;
  let editor: SubskillEditService;
  let save: jasmine.Spy;

  beforeEach(() => {
    save = jasmine.createSpy('save').and.returnValue(of([]));
    TestBed.configureTestingModule({imports: [EditorHost], providers: [provideTranslateService(), provideRouter([]),
      {provide: DataService, useValue: {saveSubskills: save}},
      {provide: UserConfigService, useValue: {userConfig$: new BehaviorSubject({showSubSkills: false,
        showTrainingInfo: false, currency: {currencyRate: 1, currencyCode: 'EUR'}} as UserConfig)}},
      {provide: PlayService, useValue: {getCurrentWeekInfo: () => ({season: 90, week: 1}),
        getSelectedTeam: () => ({team: {id: 2}, weeklyData: [{season: 90, week: 1, players: [fixture.componentInstance.player]}]}), update: () => {}}}
    ]});
    fixture = TestBed.createComponent(EditorHost);
    editor = TestBed.inject(SubskillEditService);
    fixture.detectChanges();
  });

  function input(): HTMLInputElement { return fixture.nativeElement.querySelector('input[aria-label^="ht.skill-keeper"]'); }
  function edit(): void {
    fixture.nativeElement.querySelector('#editSubskillsButton').click();
    fixture.detectChanges();
  }
  function type(value: string): void {
    input().value = value;
    input().dispatchEvent(new Event('input'));
    fixture.detectChanges();
  }

  it('shows decimals while editing and preserves the draft when switching from card to table', () => {
    expect(input()).toBeNull();
    edit();
    expect(input().value).toBe('12');
    type('75');
    fixture.componentInstance.table = true;
    fixture.detectChanges();
    expect(input().value).toBe('75');
    expect(fixture.componentInstance.player.playerSubSkill!.keeper).toBe(.12567);
    expect(save).not.toHaveBeenCalled();
  });

  it('displays saved decimals exactly in both views without rounding the integer level', () => {
    fixture.componentInstance.player.keeperSkill = 1;
    fixture.componentInstance.player.playerSubSkill!.keeper = .57;
    edit();
    expect(input().value).toBe('57');
    fixture.componentInstance.table = true;
    fixture.detectChanges();
    expect(input().value).toBe('57');
  });

  it('steps one hundredth with Up and Down in both views and clamps at 00 and 99', () => {
    edit();
    for (const table of [false, true]) {
      fixture.componentInstance.table = table;
      fixture.detectChanges();
      type('12');
      const key = (name: string) => {
        const event = new KeyboardEvent('keydown', {key: name, bubbles: true, cancelable: true});
        input().dispatchEvent(event);
        fixture.detectChanges();
        expect(event.defaultPrevented).toBeTrue();
      };
      key('ArrowUp');
      expect(input().value).toBe('13');
      key('ArrowDown');
      expect(input().value).toBe('12');
      type('99');
      key('ArrowUp');
      expect(input().value).toBe('99');
      type('00');
      key('ArrowDown');
      expect(input().value).toBe('00');
      type('');
      key('ArrowUp');
      expect(input().value).toBe('13');
    }
    expect(fixture.componentInstance.player.keeperSkill).toBe(6);
    expect(fixture.componentInstance.player.playerSubSkill!.keeper).toBe(.12567);
    expect(save).not.toHaveBeenCalled();
  });

  it('updates HTMS and HTMS28 immediately on typing and stepping in card and table', () => {
    edit();
    const cardValues = () => Array.from(fixture.nativeElement.querySelectorAll('.htms-table tr.green td'))
      .map((cell: any) => cell.textContent.trim()).filter(text => text !== '|');
    expect(cardValues()).toEqual(['652', '1818']);
    type('75');
    expect(cardValues()).toEqual(['666', '1832']);
    fixture.componentInstance.table = true;
    fixture.detectChanges();
    const tableValues = () => Array.from(fixture.nativeElement.querySelectorAll('tbody tr > td.text-end > div.subskill'))
      .map((cell: any) => cell.textContent.trim().replace(/[^0-9-]/g, ''));
    expect(tableValues()).toEqual(['666', '1832']);
    input().dispatchEvent(new KeyboardEvent('keydown', {key: 'ArrowUp', bubbles: true, cancelable: true}));
    fixture.detectChanges();
    expect(input().value).toBe('76');
    expect(tableValues()).toEqual(['666', '1832']);
    type('99');
    expect(tableValues()).toEqual(['672', '1838']);
    expect(fixture.componentInstance.player.playerSubSkill!.keeper).toBe(.12567);
    expect(save).not.toHaveBeenCalled();
  });

  it('offers save, discard and continue; discarding restores the normal view', () => {
    edit();
    type('75');
    fixture.nativeElement.querySelector('#saveSubskillsButton').click();
    fixture.detectChanges();
    const dialog: HTMLDialogElement = fixture.nativeElement.querySelector('dialog');
    expect(dialog.open).toBeTrue();
    expect(dialog.querySelectorAll('button').length).toBe(3);
    (dialog.querySelector('.btn-outline-secondary') as HTMLButtonElement).click();
    expect(dialog.open).toBeFalse();
    expect(editor.editing).toBeTrue();
    fixture.nativeElement.querySelector('#saveSubskillsButton').click();
    (dialog.querySelector('.btn-outline-danger') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(editor.editing).toBeFalse();
    expect(input()).toBeNull();
    expect(fixture.componentInstance.player.playerSubSkill!.keeper).toBe(.12567);
    expect(save).not.toHaveBeenCalled();
  });

  it('keeps edit mode active until the save request completes and disables duplicate saves', () => {
    const response = new Subject<PlayerInfo[]>();
    save.and.returnValue(response);
    edit();
    type('75');
    fixture.nativeElement.querySelector('#saveSubskillsButton').click();
    fixture.detectChanges();
    const dialog: HTMLDialogElement = fixture.nativeElement.querySelector('dialog');
    (dialog.querySelector('.btn-success') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(dialog.open).toBeTrue();
    expect(editor.editing).toBeTrue();
    expect(input().disabled).toBeTrue();
    expect((dialog.querySelector('.btn-success') as HTMLButtonElement).disabled).toBeTrue();
    response.next([{...fixture.componentInstance.player, playerSubSkill: {keeper: .75} as any}]);
    fixture.detectChanges();
    expect(dialog.open).toBeFalse();
    expect(editor.editing).toBeFalse();
    expect(fixture.componentInstance.player.playerSubSkill!.keeper).toBe(.75);
  });

  it('marks empty input invalid and lets Escape restore the untouched value', () => {
    edit();
    type('');
    expect(input().getAttribute('aria-invalid')).toBe('true');
    input().dispatchEvent(new KeyboardEvent('keydown', {key: 'Escape'}));
    fixture.detectChanges();
    expect(input().value).toBe('12');
    expect(editor.dirty).toBeFalse();
  });
});
