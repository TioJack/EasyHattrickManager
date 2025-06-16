import {Component, OnInit} from '@angular/core';
import {PlayService} from '../services/play.service';
import {PlayerInfo} from '../services/model/data-response';
import {NgForOf, NgIf} from '@angular/common';
import {PlayerCardComponent} from '../player-card/player-card.component';

@Component({
  selector: 'app-player-list',
  standalone: true,
  imports: [
    NgIf,
    NgForOf,
    PlayerCardComponent
  ],
  templateUrl: './player-list.component.html'
})
export class PlayerListComponent implements OnInit {
  players: PlayerInfo[] = [];

  constructor(private playService: PlayService) {
  }

  ngOnInit(): void {
    this.playService.players$.subscribe(players => {
      this.players = players;
    })
  }
}
