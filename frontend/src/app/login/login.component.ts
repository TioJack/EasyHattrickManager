import {Component, OnInit} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {AuthService} from '../services/auth.service';
import {catchError, of, tap} from 'rxjs';
import {TranslatePipe} from '@ngx-translate/core';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslatePipe, FirstCapitalizePipe],
  templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {
  username: string = '';
  password: string = '';
  error: boolean = false;

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
          this.error = true;
          return of(null);
        })
      )
      .subscribe();
  }
}
