import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forgot-password.page.html',
  styleUrls: ['./forgot-password.page.scss']
})
export class ForgotPasswordPage {
  step = 1;
  email = '';
  code = '';
  newPassword = '';
  loading = false;
  success = '';
  error = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  sendCode(): void {
    this.loading = true;
    this.error = '';
    this.success = '';

    this.authService.forgotPassword(this.email).subscribe({
      next: () => {
        this.loading = false;
        this.step = 2;
        this.success = 'Código enviado al correo.';
      },
      error: () => {
        this.loading = false;
        this.error = 'No se pudo enviar el código.';
      }
    });
  }

  resetPassword(): void {
    this.loading = true;
    this.error = '';
    this.success = '';

    this.authService.resetPassword(this.email, this.code, this.newPassword).subscribe({
      next: () => {
        this.loading = false;
        this.success = 'Contraseña actualizada correctamente. Redirigiendo al login...';

        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1800);
      },
      error: () => {
        this.loading = false;
        this.error = 'No se pudo actualizar la contraseña.';
      }
    });
  }
}