import {Component, DestroyRef, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NgForOf, NgIf} from '@angular/common';
import {FormsModule, NgForm} from '@angular/forms';
import {TranslatePipe} from '@ngx-translate/core';
import {FirstLetterPipe} from '../pipes/first-letter.pipe';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';
import {NumberSeparatorPipe} from '../pipes/number-separator.pipe';
import {SalaryPipe} from '../pipes/salary.pipe';
import {PlayerInfo, PlayerSubSkillInfo, UserConfig} from '../services/model/data-response';
import {UserConfigService} from '../services/user-config.service';
import {SkillComponent} from '../skill/skill.component';
import {TruncateDecimalsPipe} from '../pipes/truncate-decimals.pipe';
import {DataService} from '../services/data.service';

@Component({
  selector: 'app-player-card-compact',
  standalone: true,
  imports: [NgIf, NgForOf, FormsModule, TranslatePipe, FirstLetterPipe, FirstCapitalizePipe, NumberSeparatorPipe, SalaryPipe, SkillComponent],
  templateUrl: './player-card-compact.component.html',
  styleUrls: ['./player-card-compact.component.scss']
})
export class PlayerCardCompactComponent implements OnInit, OnChanges {
  @Input() player?: PlayerInfo;
  @Input() editable = false;
  @Input() editing = false;
  @Output() editRequested = new EventEmitter<void>();
  @Output() playerSave = new EventEmitter<PlayerInfo>();
  @Output() deleteRequested = new EventEmitter<void>();
  draft?: PlayerInfo;
  isSavingPlayer = false;
  playerCalculationFailed = false;
  readonly specialtyOptions = [0, 1, 2, 3, 4, 5, 6, 8];
  readonly skillFields: Array<{key: keyof PlayerInfo; sub: keyof PlayerSubSkillInfo; label: string; max: number}> = [
    {key: 'staminaSkill', sub: 'stamina', label: 'ht.skill-stamina', max: 9},
    {key: 'keeperSkill', sub: 'keeper', label: 'ht.skill-keeper', max: 20.99},
    {key: 'defenderSkill', sub: 'defender', label: 'ht.skill-defender', max: 20.99},
    {key: 'playmakerSkill', sub: 'playmaker', label: 'ht.skill-playmaker', max: 20.99},
    {key: 'wingerSkill', sub: 'winger', label: 'ht.skill-winger', max: 20.99},
    {key: 'passingSkill', sub: 'passing', label: 'ht.skill-passer', max: 20.99},
    {key: 'scorerSkill', sub: 'scorer', label: 'ht.skill-scorer', max: 20.99},
    {key: 'setPiecesSkill', sub: 'setPieces', label: 'ht.skill-kicker', max: 20.99}
  ];
  draftSkills: Record<string, number> = {};
  userConfig: UserConfig | null = null;
  private readonly truncateDecimalsPipe = new TruncateDecimalsPipe();

  constructor(private userConfigService: UserConfigService, private dataService: DataService, private destroyRef: DestroyRef) {
  }

  ngOnInit(): void {
    this.userConfigService.userConfig$.subscribe(config => {
      this.userConfig = config;
    });
    if (this.editing) {
      this.initializeDraft();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.editing && this.player && (changes['editing'] || changes['player'])) {
      this.initializeDraft();
    }
  }

  private initializeDraft(): void {
    if (!this.player) {
      return;
    }
    this.draft = structuredClone(this.player);
    this.draft.firstName = this.getPlayerFullName(this.player);
    this.draft.lastName = '';
    this.draft.nickName = '';
    this.playerCalculationFailed = false;
    for (const field of this.skillFields) {
      this.draftSkills[field.key] = Number((Number(this.player[field.key]) + (this.player.playerSubSkill?.[field.sub] ?? 0)).toFixed(2));
    }
  }

  savePlayer(form: NgForm): void {
    if (this.isSavingPlayer) {
      return;
    }
    if (!this.draft || form.invalid || !this.draft.firstName.trim()) {
      form.control.markAllAsTouched();
      return;
    }
    const player = structuredClone(this.draft);
    player.firstName = player.firstName.trim();
    player.lastName = '';
    player.nickName = '';
    if (player.playerNumber == null) {
      delete player.playerNumber;
    }
    player.playerSubSkill = {...player.playerSubSkill, htms: 0, htms28: 0} as PlayerSubSkillInfo;
    for (const field of this.skillFields) {
      const value = this.draftSkills[field.key];
      Object.assign(player, {[field.key]: Math.floor(value)});
      player.playerSubSkill[field.sub] = Number((value - Math.floor(value)).toFixed(2));
    }
    player.playerFormInfo = undefined;
    player.playerExpInfo = undefined;
    this.isSavingPlayer = true;
    this.playerCalculationFailed = false;
    this.dataService.calculateCustomPlayer(player).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: calculated => {
        this.isSavingPlayer = false;
        this.playerSave.emit({...player, ...calculated, changes: {}});
      },
      error: () => {
        this.isSavingPlayer = false;
        this.playerCalculationFailed = true;
      }
    });
  }

  getDecimalParts(value: number | null | undefined, digits: number = 2): { whole: string; decimal: string } | null {
    const formatted = this.truncateDecimalsPipe.transform(value, digits);
    if (!formatted) {
      return null;
    }
    const [whole, decimal] = formatted.split('.');
    return {whole, decimal: decimal ?? ''};
  }

  getPlayerFullName(player: PlayerInfo): string {
    const nickname = player.nickName ? ` '${player.nickName}'` : '';
    return `${player.firstName}${nickname} ${player.lastName}`.trim();
  }
}
