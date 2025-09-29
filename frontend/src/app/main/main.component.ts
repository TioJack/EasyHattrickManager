import {Component, OnInit} from '@angular/core';
import {PlayService} from '../services/play.service';
import {PlayerInfo, TrainingInfo} from '../services/model/data-response';
import {NgForOf} from '@angular/common';
import {PlayerCardComponent} from '../player-card/player-card.component';
import {StaffComponent} from '../staff/staff.component';
import {TrainingComponent} from '../training/training.component';
import {PlayerStatsComponent} from '../player-stats/player-stats.component';

@Component({
  selector: 'app-main',
  standalone: true,
  imports: [
    NgForOf,
    PlayerCardComponent,
    StaffComponent,
    TrainingComponent,
    PlayerStatsComponent
  ],
  templateUrl: './main.component.html'
})
export class MainComponent implements OnInit {
  players: PlayerInfo[] = [];
  training: TrainingInfo | null = null;

  constructor(private playService: PlayService) {
  }

  ngOnInit(): void {
    this.playService.players$.subscribe(players => {
      this.players = players;
    })
    this.playService.training$.subscribe(training => {
      this.training = training;
    });
  }
}
