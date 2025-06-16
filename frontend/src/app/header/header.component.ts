import {Component, Input, OnInit} from '@angular/core';
import {AuthService} from '../services/auth.service';
import {TeamExtendedInfo} from '../services/model/data-response';
import {NgForOf} from '@angular/common';
import {PlayService} from '../services/play.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    NgForOf
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  @Input() dataResponse: any;
  selectedTeam: TeamExtendedInfo | null = null;
  selectedWeekIndex: number = 0;

  constructor(
    private authService: AuthService,
    private playService: PlayService
  ) {
  }

  ngOnInit(): void {
    if (this.dataResponse.teams.length > 0) {
      this.selectTeam(this.dataResponse.teams[0]);
    }
  }

  logout(): void {
    this.authService.logout();
  }

  downloadData(): void {
    if (this.dataResponse) {
      const dataStr = JSON.stringify(this.dataResponse, null, 2);
      const blob = new Blob([dataStr], {type: 'application/json'});
      const now = new Date();
      const dateStr = `${now.getFullYear()}-${(now.getMonth() + 1).toString().padStart(2, '0')}-${now.getDate().toString().padStart(2, '0')}`;
      const fileName = `hattrick_data_${dateStr}.json`;
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      a.click();
      window.URL.revokeObjectURL(url);
    }
  }

  selectTeam(team: TeamExtendedInfo): void {
    this.selectedTeam = team;
    this.playService.selectTeam(team);
    this.selectedWeekIndex = this.playService.getWeekBounds().max;
    this.notifyWeekSelection();
  }

  onFirstWeek(): void {
    const {min} = this.playService.getWeekBounds();
    if (this.selectedWeekIndex > min) {
      this.selectedWeekIndex = min;
      this.notifyWeekSelection();
    }
  }

  onPreviousSeason(): void {
    const {min} = this.playService.getWeekBounds();
    console.log('onPreviousSeason', this.selectedWeekIndex, min);
    if (this.selectedWeekIndex - 16 > min) {
      this.selectedWeekIndex = this.selectedWeekIndex - 16;
      this.notifyWeekSelection();
    } else if (this.selectedWeekIndex > min) {
      this.selectedWeekIndex = min;
      this.notifyWeekSelection();
    }
  }

  onPreviousWeek(): void {
    const {min} = this.playService.getWeekBounds();
    if (this.selectedWeekIndex > min) {
      this.selectedWeekIndex--;
      this.notifyWeekSelection();
    }
  }

  onNextWeek(): void {
    const {max} = this.playService.getWeekBounds();
    if (this.selectedWeekIndex < max) {
      this.selectedWeekIndex++;
      this.notifyWeekSelection();
    }
  }

  onNextSeason(): void {
    const {max} = this.playService.getWeekBounds();
    if (this.selectedWeekIndex + 16 < max) {
      this.selectedWeekIndex = this.selectedWeekIndex + 16;
      this.notifyWeekSelection();
    } else if (this.selectedWeekIndex < max) {
      this.selectedWeekIndex = max;
      this.notifyWeekSelection();
    }
  }

  onLastWeek(): void {
    const {max} = this.playService.getWeekBounds();
    if (this.selectedWeekIndex < max) {
      this.selectedWeekIndex = max;
      this.notifyWeekSelection();
    }
  }

  notifyWeekSelection(): void {
    if (this.selectedTeam) {
      const selectedWeekData = this.selectedTeam.weeklyData[this.selectedWeekIndex];
      this.playService.selectSeasonAndWeek(selectedWeekData.season, selectedWeekData.week);
    }
  }
}
