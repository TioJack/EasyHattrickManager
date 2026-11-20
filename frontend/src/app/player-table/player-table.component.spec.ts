import {TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {provideTranslateService} from '@ngx-translate/core';
import {BehaviorSubject} from 'rxjs';
import {PlayerTableComponent} from './player-table.component';
import {UserConfigService} from '../services/user-config.service';

describe('Player table with zero skills', () => {
  const zeroPlayer = {
    id: 123, firstName: 'Coach', lastName: 'Zero', countryId: 1,
    age: 40, ageDays: 0, tsi: 0, salary: 0, specialty: 0,
    injuryLevel: -1, cards: 0, playerCategoryId: 0,
    leadership: 0, experience: 0, playerForm: 0, loyalty: 0,
    staminaSkill: 0, keeperSkill: 0, defenderSkill: 0, playmakerSkill: 0,
    wingerSkill: 0, passingSkill: 0, scorerSkill: 0, setPiecesSkill: 0,
    htms: 0, htms28: 0, playerSubSkill: null, playerFormInfo: null, changes: {}
  };
  const config = new BehaviorSubject({
    showSubSkills: false, showTrainingInfo: true,
    currency: {currencyRate: 1, currencyCode: 'EUR'}
  });

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PlayerTableComponent],
      providers: [provideRouter([]), provideTranslateService(),
        {provide: UserConfigService, useValue: {userConfig$: config}}]
    });
  });

  for (const playerTraining of [null, undefined]) {
    for (const showSubSkills of [false, true]) {
      it(`renders zero skills and subsequent rows with training=${playerTraining} and subskills=${showSubSkills}`, () => {
        config.next({...config.value, showTrainingInfo: true, showSubSkills});
        const fixture = TestBed.createComponent(PlayerTableComponent);
        fixture.componentRef.setInput('players', [
          {...zeroPlayer, playerTraining},
          {...zeroPlayer, id: 456, firstName: 'Next', keeperSkill: 7, playerTraining: {keeper: 75.5}}
        ]);

        expect(() => fixture.detectChanges()).not.toThrow();
        const root: HTMLElement = fixture.nativeElement;
        const rows = root.querySelectorAll('tbody tr');
        expect(rows.length).toBe(2);
        const skills = rows[0].querySelectorAll<HTMLElement>('.skill-cell');
        expect(skills.length).toBe(12);
        skills.forEach((cell, index) => {
          const hasSublevel = showSubSkills && index !== 0 && index !== 4;
          expect(cell.textContent?.trim()).toBe(hasSublevel ? '0.00' : '0');
          expect(cell.title).not.toContain('%');
        });
        const nextKeeper = rows[1].querySelectorAll<HTMLElement>('.skill-cell')[5];
        expect(nextKeeper.textContent?.trim()).toBe(showSubSkills ? '7.00' : '7');
        expect(nextKeeper.title).toContain('75.50%');
        expect(nextKeeper.style.backgroundColor).not.toBe('');

        config.next({...config.value, showTrainingInfo: false});
        fixture.detectChanges();
        expect(nextKeeper.title).not.toContain('%');
        expect(nextKeeper.style.backgroundColor).toBe('');
      });
    }
  }

  it('shows the calculated experience sublevel and falls back to the known integer when it is hidden', () => {
    config.next({...config.value, showSubSkills: true});
    const fixture = TestBed.createComponent(PlayerTableComponent);
    fixture.componentRef.setInput('players', [{...zeroPlayer, experience: 6, playerExpInfo: {exp: 6.0385}}]);
    fixture.detectChanges();
    const experience: HTMLElement = fixture.nativeElement.querySelectorAll('.skill-cell')[1];
    expect(experience.textContent?.trim()).toBe('6.03');
    config.next({...config.value, showSubSkills: false});
    fixture.detectChanges();
    expect(experience.textContent?.trim()).toBe('6');
  });

  it('truncates experience to two decimal places without displaying an unobserved level-up', () => {
    config.next({...config.value, showSubSkills: true});
    const fixture = TestBed.createComponent(PlayerTableComponent);
    fixture.componentRef.setInput('players', [
      {...zeroPlayer, playerExpInfo: {exp: 0.35 / 100}},
      {...zeroPlayer, experience: 6, playerExpInfo: {exp: 6.999999999999999}}
    ]);
    fixture.detectChanges();
    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows[0].querySelectorAll('.skill-cell')[1].textContent.trim()).toBe('0.00');
    expect(rows[1].querySelectorAll('.skill-cell')[1].textContent.trim()).toBe('6.99');
  });
});
