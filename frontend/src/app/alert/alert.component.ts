import {Component} from '@angular/core';
import {NgIf} from '@angular/common';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-alert',
  standalone: true,
  imports: [
    NgIf,
    FirstCapitalizePipe,
    TranslatePipe
  ],
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.scss']
})
export class AlertComponent {
  alertMessage: string | null = null;
  alertType: 'success' | 'danger' | 'warning' | 'info' = 'success';
  private alertTimeout: any;

  showAlert(message: string, type: 'success' | 'danger' | 'warning' | 'info', duration: number = 3000): void {
    this.alertMessage = message;
    this.alertType = type;
    if (this.alertTimeout) {
      clearTimeout(this.alertTimeout);
    }
    this.alertTimeout = setTimeout(() => {
      this.closeAlert();
    }, duration); // Duración en milisegundos
  }

  closeAlert(): void {
    this.alertMessage = null;
    if (this.alertTimeout) {
      clearTimeout(this.alertTimeout);
    }
  }
}
