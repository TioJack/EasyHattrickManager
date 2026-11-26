import {calculateSubskillHtms, Htms} from '../utils/htms';
import {Injectable} from '@angular/core';
import {Subject} from 'rxjs';
import {DataService} from './data.service';
import {PlayService} from './play.service';
import {PlayerInfo, PlayerSubSkillInfo, WeeklyInfo} from './model/data-response';

export type EditableSubskill = Exclude<keyof PlayerSubSkillInfo, 'htms' | 'htms28'> | 'form' | 'experience';
export interface SubskillChange {playerId: number; skill: EditableSubskill; oldValue: number; newValue: number;}

@Injectable({providedIn: 'root'})
export class SubskillEditService {
  editing = false;
  saving = false;
  error = '';
  readonly exitRequested = new Subject<void>();
  private leaveResolver?: (leave: boolean) => void;
  private week?: WeeklyInfo;
  private teamId?: number;
  private htmsPreviews = new Map<number, Htms>();
  private drafts = new Map<string, {player: PlayerInfo; skill: EditableSubskill; text: string; oldValue: number}>();

  constructor(private data: DataService, private play: PlayService) {}

  start(): void {
    const current = this.play.getCurrentWeekInfo();
    const team = this.play.getSelectedTeam();
    this.week = team?.weeklyData.find(w => w.season === current?.season && w.week === current?.week);
    if (!this.week || !team) return;
    this.teamId = team.team.id;
    this.htmsPreviews.clear();
    this.editing = this.play.subskillEditing = true;
    this.error = '';
  }

  get dirty(): boolean { return this.drafts.size > 0; }
  get valid(): boolean { return [...this.drafts.values()].every(d => /^\d{1,2}$/.test(d.text)); }

  value(player: PlayerInfo, skill: EditableSubskill): number {
    if (skill === 'experience') return (player.playerExpInfo?.exp ?? player.experience) - player.experience;
    if (skill === 'form') {
      const form = player.playerFormInfo?.form ?? player.playerForm;
      if (player.playerForm >= 8 || form <= player.playerForm) return 0;
      return Math.floor(form) === player.playerForm ? form - player.playerForm : 0.99;
    }
    return player.playerSubSkill?.[skill] ?? 0;
  }

  text(player: PlayerInfo, skill: EditableSubskill, fallback: string): string {
    return this.drafts.get(`${player.id}:${skill}`)?.text ?? fallback;
  }

  getHtms(player: PlayerInfo): Htms | undefined {
    if (!this.editing) return player.playerSubSkill;
    let preview = this.htmsPreviews.get(player.id);
    if (!preview) {
      const subskills: Partial<PlayerSubSkillInfo> = {...player.playerSubSkill};
      for (const draft of this.drafts.values()) {
        if (draft.player.id === player.id && draft.skill !== 'form' && draft.skill !== 'experience'
          && /^\d{1,2}$/.test(draft.text)) {
          subskills[draft.skill] = Number(draft.text) / 100;
        }
      }
      preview = calculateSubskillHtms(player, subskills);
      this.htmsPreviews.set(player.id, preview);
    }
    return preview;
  }

  set(player: PlayerInfo, skill: EditableSubskill, text: string, initial: string): void {
    if (!this.editing || this.saving) return;
    this.htmsPreviews.delete(player.id);
    const key = `${player.id}:${skill}`;
    if (/^\d{1,2}$/.test(text) && Number(text) === Number(initial)) {
      this.drafts.delete(key);
    } else {
      this.drafts.set(key, {player, skill, text, oldValue: this.value(player, skill)});
    }
    this.error = '';
  }

  reset(player: PlayerInfo, skill: EditableSubskill): void {
    this.drafts.delete(`${player.id}:${skill}`);
    this.htmsPreviews.delete(player.id);
  }

  finish(): void {
    this.drafts.clear();
    this.htmsPreviews.clear();
    this.editing = this.play.subskillEditing = false;
    this.error = '';
    this.leaveResolver?.(true);
    this.leaveResolver = undefined;
  }

  cancelExit(): void {
    this.leaveResolver?.(false);
    this.leaveResolver = undefined;
  }

  requestLeave(): boolean | Promise<boolean> {
    if (!this.editing) return true;
    return new Promise(resolve => {
      this.leaveResolver = resolve;
      this.exitRequested.next();
    });
  }

  save(done: () => void): void {
    if (!this.valid || this.saving || !this.week || this.teamId == null) return;
    if (!this.dirty) { this.finish(); done(); return; }
    const changes: SubskillChange[] = [...this.drafts.values()].map(d => ({
      playerId: d.player.id, skill: d.skill, oldValue: d.oldValue, newValue: Number(d.text) / 100
    }));
    this.saving = true;
    this.error = '';
    this.data.saveSubskills({teamId: this.teamId, season: this.week.season, week: this.week.week, changes}).subscribe({
      next: players => {
        players.forEach(updated => {
          const target = this.week!.players.find(p => p.id === updated.id);
          if (target) {
            target.playerSubSkill = updated.playerSubSkill;
            target.playerFormInfo = updated.playerFormInfo;
            target.playerExpInfo = updated.playerExpInfo;
          }
        });
        this.saving = false;
        this.finish();
        this.play.update();
        done();
      },
      error: response => {
        this.saving = false;
        this.error = response.status === 409 ? 'ehm.subskill-conflict' : 'ehm.subskill-save-error';
      }
    });
  }
}
