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
  loading: boolean = false;
  errorMessage: string | null = null;
  dataResponse: DataResponse | null = null;

  constructor(private authService: AuthService, private dataService: DataService) {
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

}
