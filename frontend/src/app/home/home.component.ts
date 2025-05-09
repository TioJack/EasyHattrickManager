import {Component, OnInit} from '@angular/core';
import {NgIf} from '@angular/common';
import {DataResponse} from './model/data-response';
import {DataService} from '../services/data.service';
import {AuthService} from '../services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [NgIf],
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {
  dataResponse: DataResponse | null = null;

  constructor(private authService: AuthService, private dataService: DataService) {
  }

  ngOnInit(): void {
    this.dataService.gatData().subscribe({
      next: (response: DataResponse) => {
        this.dataResponse = response;
      },
      error: (error) => {
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
