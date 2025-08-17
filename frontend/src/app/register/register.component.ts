import {Component} from '@angular/core';
import {RouterLink} from '@angular/router';
import {AuthService} from '../services/auth.service';
import {FormsModule} from '@angular/forms';
import {NgIf} from '@angular/common';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, NgIf, RouterLink, TranslatePipe, FirstCapitalizePipe],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  user = {username: '', password: ''};
  confirmPassword: string = '';
  errorMessage: string | null = null;

  constructor(private authService: AuthService, private translateService: TranslateService) {
  }

  onRegister(): void {
    if (this.user.password !== this.confirmPassword) {
      this.translateService.get('ehm.password-no-match').subscribe((translation: string) => {
        this.errorMessage = translation;
      });
      return;
    }
    this.authService.check(this.user).subscribe({
      next: (response) => {
        window.location.href = response;
      },
      error: (error) => {
        if (error.status === 409) {
          this.translateService.get('ehm.user-exists').subscribe((translation: string) => {
            this.errorMessage = translation;
          });
        } else {
          this.translateService.get('ehm.error-register').subscribe((translation: string) => {
            this.errorMessage = translation;
          });
        }
      }
    });
  }

}
