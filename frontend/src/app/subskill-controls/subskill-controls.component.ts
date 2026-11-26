import {Component, ElementRef, HostListener, OnDestroy, ViewChild} from '@angular/core';
import {NgIf} from '@angular/common';
import {TranslatePipe} from '@ngx-translate/core';
import {Subscription} from 'rxjs';
import {SubskillEditService} from '../services/subskill-edit.service';

@Component({
  selector: 'app-subskill-controls', standalone: true, imports: [NgIf, TranslatePipe],
  template: `<button *ngIf="!editor.editing; else saveButton" class="btn btn-outline-primary" type="button"
    id="editSubskillsButton" [title]="'ehm.edit-subskills' | translate" [attr.aria-label]="'ehm.edit-subskills' | translate"
    (click)="editor.start()"><i class="bi bi-person-lines-fill" aria-hidden="true"></i></button>
    <ng-template #saveButton><button class="btn btn-success" type="button" id="saveSubskillsButton"
      [title]="'ehm.finish-subskills' | translate" [attr.aria-label]="'ehm.finish-subskills' | translate"
      [disabled]="editor.saving" (click)="open()"><i class="bi bi-floppy" aria-hidden="true"></i></button></ng-template>
    <dialog #dialog aria-labelledby="subskillExitTitle" (cancel)="cancel($event)">
      <h2 id="subskillExitTitle" class="h6">{{ 'ehm.finish-subskills' | translate }}</h2>
      <p>{{ 'ehm.subskill-exit-question' | translate }}</p>
      <p *ngIf="!editor.valid" class="text-danger" role="alert">{{ 'ehm.subskill-range' | translate }}</p>
      <p *ngIf="editor.error" class="text-danger" role="alert">{{ editor.error | translate }}</p>
      <div class="d-flex gap-2 justify-content-end flex-wrap">
        <button class="btn btn-outline-secondary btn-sm" type="button" [disabled]="editor.saving" (click)="close()">{{ 'ehm.keep-editing-subskills' | translate }}</button>
        <button class="btn btn-outline-danger btn-sm" type="button" [disabled]="editor.saving" (click)="discard()">{{ 'ehm.discard-subskills' | translate }}</button>
        <button class="btn btn-success btn-sm" type="button" [disabled]="!editor.valid || editor.saving" (click)="save()">
          <span *ngIf="editor.saving" class="spinner-border spinner-border-sm me-1" aria-hidden="true"></span>{{ 'ehm.save-exit-subskills' | translate }}
        </button>
      </div>
    </dialog>`,
  styles: [`:host {display: inline-flex;} dialog {border: 1px solid var(--bs-border-color); border-radius: .5rem;
    padding: 1.25rem; max-width: min(32rem, calc(100vw - 2rem)); color: var(--bs-body-color);
    background: var(--bs-body-bg); box-shadow: 0 .5rem 2rem #0002; text-align: left;}
    dialog::backdrop { background: #0004; }`]
})
export class SubskillControlsComponent implements OnDestroy {
  @ViewChild('dialog', {static: true}) dialog!: ElementRef<HTMLDialogElement>;
  private subscription: Subscription;
  constructor(public editor: SubskillEditService) {
    this.subscription = editor.exitRequested.subscribe(() => this.open());
  }
  open(): void { if (!this.dialog.nativeElement.open) this.dialog.nativeElement.showModal(); }
  close(): void { this.editor.cancelExit(); this.dialog.nativeElement.close(); }
  cancel(event: Event): void { event.preventDefault(); if (!this.editor.saving) this.close(); }
  discard(): void { this.dialog.nativeElement.close(); this.editor.finish(); }
  save(): void { this.editor.save(() => this.dialog.nativeElement.close()); }
  @HostListener('window:beforeunload', ['$event'])
  beforeUnload(event: BeforeUnloadEvent): void { if (this.editor.dirty || this.editor.saving) event.preventDefault(); }
  ngOnDestroy(): void { this.subscription.unsubscribe(); }
}
