import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="page">
      <div class="card">
        <h1>Recuperar contraseña</h1>
        <p *ngIf="step === 1">Ingresa tu correo para recibir un código de recuperación.</p>
        <p *ngIf="step === 2">Ingresa el código recibido y tu nueva contraseña.</p>

        <div *ngIf="step === 1">
          <input [(ngModel)]="email" type="email" placeholder="Correo electrónico" />
          <button (click)="sendCode()" [disabled]="loading">Enviar código</button>
        </div>

        <div *ngIf="step === 2">
          <input [(ngModel)]="code" type="text" placeholder="Código recibido" />
          <input [(ngModel)]="newPassword" type="password" placeholder="Nueva contraseña" />
          <button (click)="resetPassword()" [disabled]="loading">Cambiar contraseña</button>
        </div>

        <p class="success" *ngIf="success">{{ success }}</p>
        <p class="error" *ngIf="error">{{ error }}</p>

        <a routerLink="/login">Volver al login</a>
      </div>
    </div>
  `,
  styles: [`
    .page { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 24px; }
    .card { width: 100%; max-width: 420px; padding: 24px; border-radius: 16px; box-shadow: 0 8px 30px rgba(0,0,0,.12); }
    input { width: 100%; margin: 8px 0; padding: 12px; border-radius: 8px; border: 1px solid #ccc; }
    button { width: 100%; margin-top: 12px; padding: 12px; border: 0; border-radius: 8px; cursor: pointer; }
    .success { color: green; }
    .error { color: #b00020; }
    a { display: block; margin-top: 16px; text-align: center; }
  `]
})
export class ForgotPasswordPage {
  step = 1;
  email = '';
  code = '';
  newPassword = '';
  loading = false;
  success = '';
  error = '';

  constructor(private authService: AuthService) {}

  sendCode(): void {
    this.loading = true;
    this.error = '';
    this.success = '';

    this.authService.forgotPassword({ email: this.email }).subscribe({
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

    this.authService.resetPassword({
      email: this.email,
      code: this.code,
      newPassword: this.newPassword
    }).subscribe({
      next: () => {
        this.loading = false;
        this.success = 'Contraseña actualizada correctamente.';
      },
      error: () => {
        this.loading = false;
        this.error = 'No se pudo actualizar la contraseña.';
      }
    });
  }
}