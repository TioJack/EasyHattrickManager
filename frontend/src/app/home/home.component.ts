import {Component, OnInit} from '@angular/core';
import {NgIf} from '@angular/common';
import {DataResponse} from '../services/model/data-response';
import {DataService} from '../services/data.service';
import {HeaderComponent} from '../header/header.component';
import {PlayerListComponent} from '../player-list/player-list.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [NgIf, HeaderComponent, PlayerListComponent],
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {
  loading: boolean = false;
  errorMessage: string | null = null;
  dataResponse: DataResponse | null = null;

  constructor(private dataService: DataService) {
  }

  ngOnInit(): void {
    this.loading = true;
    this.dataService.gatData().subscribe({
      next: (response: DataResponse) => {
        this.loading = false;
        this.dataResponse = response;
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = 'Error cargando los datos. Por favor, intenta nuevamente.';
      }
    });
  }
}
