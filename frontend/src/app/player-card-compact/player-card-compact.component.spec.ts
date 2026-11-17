import {TestBed} from '@angular/core/testing';
import {provideTranslateService} from '@ngx-translate/core';
import {of, Subject, throwError} from 'rxjs';
import {DataService} from '../services/data.service';
import {PlayerCardCompactComponent} from './player-card-compact.component';
import {UserConfigService} from '../services/user-config.service';
import {PlayerInfo} from '../services/model/data-response';

describe('Inline custom player editor', () => {
  let dataService: jasmine.SpyObj<DataService>;
  beforeEach(() => {
    dataService = jasmine.createSpyObj('DataService', ['calculateCustomPlayer']);
    dataService.calculateCustomPlayer.and.callFake((player: PlayerInfo) => of({...player, tsi: 12345, salary: 6780, htms: 456, htms28: 2100}));
  });
  beforeEach(() => TestBed.configureTestingModule({
    imports: [PlayerCardCompactComponent],
    providers: [provideTranslateService(), {provide: DataService, useFactory: () => dataService}, {provide: UserConfigService, useValue: {userConfig$: of({currency: {currencyRate: 10, currencyCode: 'EUR'}})}}]
  }));

  async function editor(overrides: Partial<PlayerInfo> = {}) {
    const fixture = TestBed.createComponent(PlayerCardCompactComponent);
    fixture.componentRef.setInput('player', {
      id: -1, firstName: 'Prospect', lastName: '', age: 17, ageDays: 0, salary: 1000,
      playerForm: 7, experience: 1, loyalty: 1, specialty: 0,
      staminaSkill: 5, keeperSkill: 1, defenderSkill: 5, playmakerSkill: 5, wingerSkill: 5,
      passingSkill: 5, scorerSkill: 5, setPiecesSkill: 1, ...overrides
    } as PlayerInfo);
    fixture.componentRef.setInput('editable', true);
    fixture.detectChanges();
    fixture.componentRef.setInput('editing', true);
    fixture.detectChanges();
    await fixture.whenStable();
    return fixture;
  }

  it('edits the full name in one field and saves the shirt number', async () => {
    const fixture = await editor({firstName: 'Juan', lastName: 'García', playerNumber: 7});
    const saved = spyOn(fixture.componentInstance.playerSave, 'emit');
    const root: HTMLElement = fixture.nativeElement;
    const name = root.querySelector<HTMLInputElement>('input[name="name"]')!;
    expect(name.value).toBe('Juan García');
    expect(root.querySelector('input[name="lastName"]')).toBeNull();
    name.value = 'Luis Pérez López';
    name.dispatchEvent(new Event('input'));
    const number = root.querySelector<HTMLInputElement>('input[name="playerNumber"]')!;
    number.value = '10';
    number.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    root.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    const player = saved.calls.mostRecent().args[0]!;
    expect(player.firstName).toBe('Luis Pérez López');
    expect(player.lastName).toBe('');
    expect(player.nickName).toBe('');
    expect(player.playerNumber).toBe(10);
  });

  it('lets the user clear an optional shirt number and rejects an invalid number', async () => {
    const fixture = await editor({playerNumber: 7});
    const saved = spyOn(fixture.componentInstance.playerSave, 'emit');
    const root: HTMLElement = fixture.nativeElement;
    const number = root.querySelector<HTMLInputElement>('input[name="playerNumber"]')!;
    for (const invalid of ['100', '1.5']) {
      number.value = invalid;
      number.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      root.querySelector<HTMLFormElement>('form')!.dispatchEvent(new Event('submit', {cancelable: true}));
      expect(saved).not.toHaveBeenCalled();
    }
    number.value = '';
    number.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    root.querySelector<HTMLFormElement>('form')!.dispatchEvent(new Event('submit', {cancelable: true}));
    expect(saved.calls.mostRecent().args[0]!.playerNumber).toBeUndefined();
  });

  it('offers deletion beside save and edit, and hides it for real players', async () => {
    const fixture = await editor();
    const remove = spyOn(fixture.componentInstance.deleteRequested, 'emit');
    const root: HTMLElement = fixture.nativeElement;
    root.querySelector<HTMLElement>('.bi-trash')!.closest('button')!.click();
    expect(remove).toHaveBeenCalledTimes(1);
    fixture.componentRef.setInput('editing', false);
    fixture.detectChanges();
    root.querySelector<HTMLElement>('.bi-trash')!.closest('button')!.click();
    expect(remove).toHaveBeenCalledTimes(2);
    fixture.componentRef.setInput('editable', false);
    fixture.componentRef.setInput('player', {...fixture.componentInstance.player!, id: 123});
    fixture.detectChanges();
    expect(root.querySelector('.bi-trash')).toBeNull();
    expect(root.querySelector('.custom-player-card')).toBeNull();
  });

  it('saves decimal skills and uses calculated TSI, salary and HTMS', async () => {
    const fixture = await editor();
    const saved = spyOn(fixture.componentInstance.playerSave, 'emit');
    const root: HTMLElement = fixture.nativeElement;
    const scorer = root.querySelector<HTMLInputElement>('input[name="scorerSkill"]')!;
    scorer.value = '12.75';
    scorer.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    root.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    expect(saved).toHaveBeenCalled();
    const player = saved.calls.mostRecent().args[0]!;
    expect(player.scorerSkill).toBe(12);
    expect(player.playerSubSkill!.scorer).toBe(0.75);
    expect(player.tsi).toBe(12345);
    expect(player.salary).toBe(6780);
    expect(player.htms).toBe(456);
    expect(player.htms28).toBe(2100);
    expect(dataService.calculateCustomPlayer).toHaveBeenCalled();
    expect(root.querySelector('input[name="salary"]')).toBeNull();
    expect(fixture.componentInstance.player!.scorerSkill).toBe(5);
  });

  it('allows saving zero skills, form, experience and loyalty', async () => {
    const fixture = await editor({staminaSkill: 0, keeperSkill: 0, defenderSkill: 0,
      playmakerSkill: 0, wingerSkill: 0, passingSkill: 0, scorerSkill: 0, setPiecesSkill: 0,
      playerForm: 0, experience: 0, loyalty: 0});
    const saved = spyOn(fixture.componentInstance.playerSave, 'emit');
    fixture.nativeElement.querySelector('button[type="submit"]').click();
    expect(saved).toHaveBeenCalled();
    expect(dataService.calculateCustomPlayer.calls.mostRecent().args[0].playerForm).toBe(0);
    expect(dataService.calculateCustomPlayer.calls.mostRecent().args[0].loyalty).toBe(0);
  });

  it('keeps the player editable after a calculation error and lets the user retry', async () => {
    const fixture = await editor();
    const saved = spyOn(fixture.componentInstance.playerSave, 'emit');
    dataService.calculateCustomPlayer.and.returnValue(throwError(() => new Error('offline')));
    const root: HTMLElement = fixture.nativeElement;
    root.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    fixture.detectChanges();
    expect(saved).not.toHaveBeenCalled();
    expect(fixture.componentInstance.isSavingPlayer).toBeFalse();
    expect(root.querySelector('[role="alert"]')).not.toBeNull();
    dataService.calculateCustomPlayer.and.callFake(player => of({...player, salary: 6780}));
    root.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    expect(saved).toHaveBeenCalled();
  });

  it('prevents duplicate submissions while the calculation is pending', async () => {
    const fixture = await editor();
    dataService.calculateCustomPlayer.and.returnValue(new Subject<PlayerInfo>());
    const root: HTMLElement = fixture.nativeElement;
    root.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    fixture.detectChanges();
    expect(root.querySelector<HTMLButtonElement>('button[type="submit"]')!.disabled).toBeTrue();
    expect(root.querySelector<HTMLFieldSetElement>('fieldset')!.disabled).toBeTrue();
    expect(dataService.calculateCustomPlayer).toHaveBeenCalledTimes(1);
  });

  it('rejects an invalid age and a whitespace-only name', async () => {
    const fixture = await editor();
    const saved = spyOn(fixture.componentInstance.playerSave, 'emit');
    const root: HTMLElement = fixture.nativeElement;
    for (const [name, value] of [['ageDays', '112'], ['name', '   ']]) {
      const input = root.querySelector<HTMLInputElement>(`input[name="${name}"]`)!;
      input.value = value;
      input.dispatchEvent(new Event('input'));
    }
    fixture.detectChanges();
    root.querySelector<HTMLFormElement>('form')!.dispatchEvent(new Event('submit', {cancelable: true}));
    fixture.detectChanges();
    expect(saved).not.toHaveBeenCalled();
    expect(root.querySelector('[role="alert"]')).not.toBeNull();
  });

  it('shows the edit pencil and a tinted card for a custom player', async () => {
    const fixture = await editor();
    fixture.componentRef.setInput('editing', false);
    fixture.detectChanges();
    const root: HTMLElement = fixture.nativeElement;
    expect(root.querySelector('a')).toBeNull();
    expect(root.querySelector('.custom-player-card')).not.toBeNull();
    expect(root.querySelector('.bi-pencil')).not.toBeNull();
    expect(root.textContent).toContain('Prospect');
  });
});
