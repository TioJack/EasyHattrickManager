import {AfterViewInit, Component, ElementRef, OnDestroy} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {debounceTime, fromEvent, Subscription} from 'rxjs';
import {TranslatePipe} from '@ngx-translate/core';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';

@Component({
  selector: 'app-manual',
  imports: [
    FormsModule,
    TranslatePipe,
    FirstCapitalizePipe
  ],
  templateUrl: './manual.component.html'
})
export class ManualComponent implements AfterViewInit, OnDestroy {
  private resizeSubscription: Subscription | null = null;

  constructor(private el: ElementRef) {
  }

  ngAfterViewInit(): void {
    const img01 = this.el.nativeElement.querySelector('#img01');
    const imageIds = ['img02', 'img03', 'img04', 'img05', 'img06', 'img07', 'img08'];
    if (img01) {
      const resizeLogic = () => {
        if (img01.complete) {
          const reductionPercentage = img01.clientWidth / img01.naturalWidth;
          imageIds.forEach((imageId: string) => {
            const image = this.el.nativeElement.querySelector(`#${imageId}`);
            const newWidth = image.naturalWidth * reductionPercentage;
            if (newWidth > 0) {
              image.style.width = `${newWidth}px`;
              image.style.height = 'auto';
            }
          });
        }
      };

      img01.addEventListener('load', () => resizeLogic());
      if (img01.complete) {
        resizeLogic();
      }

      this.resizeSubscription = fromEvent(window, 'resize')
        .pipe(debounceTime(200))
        .subscribe(() => resizeLogic());
    }
  }

  ngOnDestroy(): void {
    if (this.resizeSubscription) {
      this.resizeSubscription.unsubscribe();
    }
  }
}
