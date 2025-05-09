import {Component, OnInit} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {AuthService} from '../services/auth.service';
import {catchError, of, tap} from 'rxjs';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {
  username: string = '';
  password: string = '';
  errorMessage: string | null = null;

  constructor(private authService: AuthService, private router: Router) {
  }

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/home']);
    }
  }

  onSubmit() {
    this.authService
      .login(this.username, this.password)
      .pipe(
        tap((token: string) => {
          this.authService.saveToken(token);
          this.router.navigate(['/home']);
        }),
        catchError((error) => {
          this.errorMessage = 'Credenciales incorrectas, intenta nuevamente.';
          return of(null);
        })
      )
      .subscribe();
  }
}
