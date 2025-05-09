import {Component} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {AuthService} from '../services/auth.service';
import {FormsModule} from '@angular/forms';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, NgIf, RouterLink],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  user = {username: '', password: ''};
  confirmPassword: string = '';
  errorMessage: string | null = null;

  constructor(private authService: AuthService, private router: Router) {
  }

  onRegister(): void {
    if (this.user.password !== this.confirmPassword) {
      this.errorMessage = 'Las contraseñas no coinciden.';
      return;
    }
    this.authService.check(this.user).subscribe({
      next: (response) => {
        window.location.href = response;
      },
      error: (error) => {
        if (error.status === 409) {
          this.errorMessage = 'El usuario ya existe. Por favor, elige otro nombre de usuario.';
        } else {
          this.errorMessage = 'Error al registrar al usuario.';
        }
      }
    });
  }

}
