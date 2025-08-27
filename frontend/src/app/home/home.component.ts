import {Component, OnInit} from '@angular/core';
import {NgIf} from '@angular/common';
import {DataResponse} from '../services/model/data-response';
import {DataService} from '../services/data.service';
import {HeaderComponent} from '../header/header.component';
import {MainComponent} from '../main/main.component';
import {TranslatePipe} from '@ngx-translate/core';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [NgIf, HeaderComponent, MainComponent, TranslatePipe, FirstCapitalizePipe],
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {
  loading: boolean = false;
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
      }
    });
  }
}
