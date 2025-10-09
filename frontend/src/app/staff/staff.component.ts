import {Component, OnInit} from '@angular/core';
import {StaffInfo} from '../services/model/data-response';
import {PlayService} from '../services/play.service';
import {LowerCasePipe, NgForOf, NgIf} from '@angular/common';
import {StaffLevelComponent} from '../staff-level/staff-level.component';
import {TranslatePipe} from '@ngx-translate/core';
import {DEFAULT_AVATAR} from '../constants/global.constant';

@Component({
  selector: 'app-staff',
  imports: [
    NgIf,
    StaffLevelComponent,
    TranslatePipe,
    LowerCasePipe,
    NgForOf
  ],
  templateUrl: './staff.component.html',
  styleUrls: ['./staff.component.scss']
})
export class StaffComponent implements OnInit {
  staff: StaffInfo | null = null;
  teamId: number | null = null;

  constructor(private playService: PlayService) {
  }

  ngOnInit(): void {
    this.playService.staff$.subscribe(staff => {
      this.staff = staff;
    });
    this.playService.selectedTeam$.subscribe(team => {
      if (team) {
        this.teamId = team.team.id;
      }
    });
  }

  onImageError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = DEFAULT_AVATAR;
  }

}
