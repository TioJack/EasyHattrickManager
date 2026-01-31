import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {SkillLevelPipe} from '../pipes/skill-level.pipe';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-skill',
  imports: [
    SkillLevelPipe,
    NgIf
  ],
  templateUrl: './skill.component.html',
  styleUrls: ['./skill.component.scss']
})
export class SkillComponent implements OnInit, OnChanges {
  @Input() skill?: number;
  @Input() change?: number;
  @Input() max?: number;
  percentage: number = 100;
  width: string = '100px';

  ngOnInit(): void {
    this.updateVisuals();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['skill'] || changes['max']) {
      this.updateVisuals();
    }
  }

  private updateVisuals(): void {
    if (this.skill != undefined && this.max != undefined) {
      if (this.skill < this.max) {
        this.percentage = this.skill * 100 / this.max;
      } else {
        this.percentage = 100;
      }
      if (this.max < 20) {
        this.width = '50px';
      } else {
        this.width = '100px';
      }
    }
  }

}
