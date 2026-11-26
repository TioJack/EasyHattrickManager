import {Component, Input} from '@angular/core';
import {NgIf} from '@angular/common';
import {TranslatePipe} from '@ngx-translate/core';
import {PlayerInfo} from '../services/model/data-response';
import {EditableSubskill, SubskillEditService} from '../services/subskill-edit.service';

@Component({
  selector: 'app-subskill-input', standalone: true, imports: [NgIf, TranslatePipe],
  template: `<span>.</span><input *ngIf="editor.editing && !locked; else display" type="text" inputmode="numeric"
    autocomplete="off" spellcheck="false" maxlength="2" pattern="[0-9]{1,2}"
    [value]="editor.text(player, skill, decimal)" [disabled]="editor.saving"
    [attr.aria-label]="(label | translate) + ' · ' + player.firstName + ' ' + player.lastName + ' · 00–99'"
    [attr.aria-invalid]="!valid" [title]="'ehm.subskill-range' | translate"
    (focus)="select($event)" (input)="change($event)" (blur)="pad($event)"
    (keydown.arrowup)="step($event, 1)" (keydown.arrowdown)="step($event, -1)"
    (keydown.enter)="blur($event)" (keydown.escape)="reset($event)">
    <ng-template #display>{{ decimal }}</ng-template>`,
  styles: [`:host { display: inline; white-space: nowrap; }
    input { width: 2ch; min-width: 2ch; box-sizing: content-box; padding: 0; margin: 0; border: 0;
      border-radius: 2px; outline: 1px solid transparent; background: transparent; color: inherit;
      font: inherit; line-height: inherit; text-align: left; vertical-align: baseline; }
    input:hover { outline-color: color-mix(in srgb, currentColor 20%, transparent); }
    input:focus { outline-color: color-mix(in srgb, currentColor 45%, transparent); }
    input[aria-invalid="true"] { outline-color: var(--bs-danger); }
    input:disabled { opacity: .65; }`]
})
export class SubskillInputComponent {
  @Input({required: true}) player!: PlayerInfo;
  @Input({required: true}) skill!: EditableSubskill;
  @Input() decimal = '00';
  @Input() label = '';
  @Input() locked = false;
  constructor(public editor: SubskillEditService) {}
  get valid(): boolean { return /^\d{1,2}$/.test(this.editor.text(this.player, this.skill, this.decimal)); }
  select(event: Event): void { (event.target as HTMLInputElement).select(); }
  change(event: Event): void {
    this.editor.set(this.player, this.skill, (event.target as HTMLInputElement).value, this.decimal);
  }
  step(event: Event, direction: number): void {
    event.preventDefault();
    event.stopPropagation();
    if (!this.editor.editing || this.editor.saving || this.locked) return;
    const input = event.target as HTMLInputElement;
    const current = /^\d{1,2}$/.test(input.value) ? Number(input.value) : Number(this.decimal);
    input.value = String(Math.max(0, Math.min(99, current + direction))).padStart(2, '0');
    this.change(event);
    input.select();
  }
  pad(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (/^\d{1,2}$/.test(input.value)) {
      input.value = input.value.padStart(2, '0');
      this.change(event);
    }
  }
  blur(event: Event): void { (event.target as HTMLInputElement).blur(); event.stopPropagation(); }
  reset(event: Event): void {
    this.editor.reset(this.player, this.skill);
    (event.target as HTMLInputElement).value = this.decimal;
    this.blur(event);
  }
}
