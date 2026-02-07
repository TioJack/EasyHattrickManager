import {AfterViewInit, Component, ElementRef, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {PlayerInfo, PlayerTrainingInfo, TrainingInfo, UserConfig} from '../services/model/data-response';
import {LowerCasePipe, NgIf, NgStyle} from '@angular/common';
import {UserConfigService} from '../services/user-config.service';
import {SalaryPipe} from '../pipes/salary.pipe';
import {SkillComponent} from '../skill/skill.component';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {FirstLetterPipe} from '../pipes/first-letter.pipe';
import {SpecialtyComponent} from '../specialty/specialty.component';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';
import {NumberSeparatorPipe} from '../pipes/number-separator.pipe';
import {DEFAULT_AVATAR, trainingTypeColor} from '../constants/global.constant';
import {RouterLink} from '@angular/router';
import {TruncateDecimalsPipe} from '../pipes/truncate-decimals.pipe';

@Component({
  selector: 'app-player-card',
  standalone: true,
  imports: [NgIf, SalaryPipe, SkillComponent, TranslatePipe, FirstLetterPipe, SpecialtyComponent, LowerCasePipe, FirstCapitalizePipe, NumberSeparatorPipe, NgStyle, RouterLink],
  templateUrl: './player-card.component.html',
  styleUrls: ['./player-card.component.scss']
})
export class PlayerCardComponent implements OnInit, AfterViewInit, OnDestroy {
  @Input() player?: PlayerInfo;
  @Input() training?: TrainingInfo | null | undefined;
  userConfig: UserConfig | null | undefined;
  @ViewChild('setPiecesLabel') setPiecesLabel?: ElementRef<HTMLElement>;
  private resizeObserver?: ResizeObserver;
  private textObserver?: MutationObserver;
  private rafId?: number;
  private readonly truncateDecimalsPipe = new TruncateDecimalsPipe();

  constructor(
    private userConfigService: UserConfigService,
    private translateService: TranslateService
  ) {
  }

  ngOnInit(): void {
    this.userConfigService.userConfig$.subscribe(userConfig => {
      this.userConfig = userConfig;
    });
  }

  ngAfterViewInit(): void {
    this.setupSetPiecesObservers();
    this.scheduleSetPiecesUpdate();
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
    this.textObserver?.disconnect();
    if (!this.resizeObserver) {
      window.removeEventListener('resize', this.handleWindowResize);
    }
    if (this.rafId != null) {
      cancelAnimationFrame(this.rafId);
    }
  }

  getTraining(property: keyof PlayerTrainingInfo): string {
    if (!this.userConfig?.showTrainingInfo) {
      return '';
    }
    if (this.player?.playerTraining && this.player.playerTraining[property]) {
      return this.player.playerTraining[property].toFixed(2);
    }
    return '';
  }

  getBackgroundColor(property: keyof PlayerTrainingInfo): object {
    if (!this.userConfig?.showTrainingInfo) {
      return {};
    }
    if (this.player?.playerTraining && this.player.playerTraining[property]) {
      const baseColor = trainingTypeColor(this.training?.trainingType);
      const r = parseInt(baseColor.slice(1, 3), 16);
      const g = parseInt(baseColor.slice(3, 5), 16);
      const b = parseInt(baseColor.slice(5, 7), 16);
      const alpha = this.player.playerTraining[property] / 100;
      if (alpha > 0.7) {
        return {
          'background-color': `rgba(${r}, ${g}, ${b}, ${alpha})`,
          'color': '#f7f8f7',
        };
      }
      return {'background-color': `rgba(${r}, ${g}, ${b}, ${alpha})`};
    }
    return {};
  }

  getDecimalColor(property: keyof PlayerTrainingInfo): object {
    if (!this.userConfig?.showTrainingInfo) {
      return {};
    }
    const value = this.player?.playerTraining?.[property];
    if (value) {
      const alpha = value / 100;
      if (alpha > 0.7) {
        return {'color': '#f7f8f7'};
      }
    }
    return {};
  }

  onImageError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = DEFAULT_AVATAR;
  }

  getDecimalParts(value: number | null | undefined, digits: number = 2): { whole: string; decimal: string } | null {
    const formatted = this.truncateDecimalsPipe.transform(value, digits);
    if (!formatted) {
      return null;
    }
    const [whole, decimal] = formatted.split('.');
    return {whole, decimal: decimal ?? ''};
  }

  getFormWithSublevel(): number {
    const visualForm = this.player?.playerForm ?? 0;
    if (visualForm === 8) {
      return 8;
    }
    const calculatedForm = this.player?.playerFormInfo?.form;
    if (calculatedForm == null) {
      return visualForm;
    }
    if (Math.floor(calculatedForm) === visualForm) {
      return calculatedForm;
    }
    if (visualForm < 8 && calculatedForm > visualForm) {
      return visualForm + 0.99;
    }
    return visualForm;
  }

  getExpectedFormTooltip(): string {
    const formInfo = this.player?.playerFormInfo;
    if (!formInfo) {
      return '';
    }
    const expected = this.truncateDecimalsPipe.transform(formInfo.expectedForm, 2);
    const calculated = this.truncateDecimalsPipe.transform(formInfo.form, 2);
    const hidden = this.truncateDecimalsPipe.transform(formInfo.hiddenForm, 2);
    if (!expected || !calculated || !hidden) {
      return '';
    }
    const calculatedLabel = this.translateService.instant('ehm.form-calculated');
    const hiddenLabel = this.translateService.instant('ehm.form-hidden');
    const expectedLabel = this.translateService.instant('ehm.form-expected');
    return `${calculatedLabel} ${calculated}\n${hiddenLabel} ${hidden}\n${expectedLabel} ${expected}`;
  }

  private setupSetPiecesObservers(): void {
    const label = this.setPiecesLabel?.nativeElement;
    if (!label) {
      return;
    }
    const observeTarget = label.parentElement ?? label;
    if (typeof ResizeObserver !== 'undefined') {
      this.resizeObserver = new ResizeObserver(() => this.scheduleSetPiecesUpdate());
      this.resizeObserver.observe(observeTarget);
    } else {
      window.addEventListener('resize', this.handleWindowResize);
    }
    this.textObserver = new MutationObserver(() => this.scheduleSetPiecesUpdate());
    this.textObserver.observe(label, {characterData: true, childList: true, subtree: true});
  }

  private handleWindowResize = (): void => {
    this.scheduleSetPiecesUpdate();
  };

  private scheduleSetPiecesUpdate(): void {
    if (this.rafId != null) {
      cancelAnimationFrame(this.rafId);
    }
    this.rafId = requestAnimationFrame(() => this.updateSetPiecesLabel());
  }

  private updateSetPiecesLabel(): void {
    const label = this.setPiecesLabel?.nativeElement;
    if (!label) {
      return;
    }
    const truncateLength = 3;
    const currentText = label.textContent ?? '';
    const storedText = label.dataset['fullText'] ?? '';
    let fullText = storedText;
    if (!storedText) {
      fullText = currentText;
      label.dataset['fullText'] = fullText;
    } else if (currentText && currentText !== storedText && currentText.length > truncateLength) {
      fullText = currentText;
      label.dataset['fullText'] = fullText;
    }
    label.textContent = fullText;
    label.removeAttribute('title');
    const isOverflow = label.scrollWidth > label.clientWidth;
    if (isOverflow && fullText.length > truncateLength) {
      label.textContent = fullText.slice(0, truncateLength);
      label.setAttribute('title', fullText);
    }
  }

}
