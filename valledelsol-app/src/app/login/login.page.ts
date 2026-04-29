import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {
  IonContent, IonIcon, IonInput, IonButton, IonSegment, IonSegmentButton,
  IonLabel, ToastController
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  flame, mailOutline, lockClosedOutline, personOutline,
  logInOutline, personAddOutline, eyeOutline, eyeOffOutline, warningOutline, callOutline
} from 'ionicons/icons';

import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: 'login.page.html',
  styleUrls: ['login.page.scss'],
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    IonContent, IonIcon, IonInput, IonButton, IonSegment, IonSegmentButton,
    IonLabel
  ]
})
// El componente LoginPage maneja tanto el inicio de sesión como el registro de nuevos usuarios, con validaciones básicas y feedback mediante toasts. También incluye un modo de emergencia que simula un inicio de sesión rápido para ciudadanos en situaciones críticas. Las funciones están organizadas por secciones para facilitar su comprensión y mantenimiento.
export class LoginPage {
  modo: string = 'login';
  email: string = '';
  password: string = '';
  nombre: string = '';
  phone: string = '';
  confirmPassword: string = '';
  showPassword: boolean = false;
  showConfirmPassword: boolean = false;
  loading: boolean = false;
  loadingEmergencia: boolean = false;

  constructor(
    private router: Router,
    private toastController: ToastController,
    private authService: AuthService
  ) {
    addIcons({flame,mailOutline,lockClosedOutline,personOutline,logInOutline,personAddOutline,eyeOutline,eyeOffOutline,warningOutline,callOutline});
  }

  switchMode(modo: string | number | undefined) {
    if (typeof modo !== 'string') return;
    this.modo = modo;
  }

  // ------------------------------------------------------------------ //
  //  LOGIN
  // ------------------------------------------------------------------ //
  async login() {
    if (!this.email || !this.password) {
      await this.showToast('Por favor complete todos los campos', 'warning');
      return;
    }

    this.loading = true;
    localStorage.removeItem('emergencyMode');

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.role === 'ADMIN') {
          window.location.href = '/dashboard';
        } else {
          window.location.href = '/reportar';
        }
      },
      error: async (err) => {
        this.loading = false;
        console.error('Login error', err);
        let msg: string;
        if (err.status === 0) {
          msg = 'No se pudo conectar con el servidor';
        } else if (err.status === 401) {
          msg = 'Correo o contraseña incorrectos';
        } else if (err.status === 403) {
          msg = 'La cuenta de usuario no está habilitada';
        } else {
          msg = err.error?.message || 'Error al iniciar sesión';
        }
        await this.showToast(msg, 'danger');
      }
    });
  }

  // ------------------------------------------------------------------ //
  //  REGISTRO
  // ------------------------------------------------------------------ //
  async register() {
    if (!this.nombre || !this.phone || !this.email || !this.password || !this.confirmPassword) {
      await this.showToast('Por favor complete todos los campos', 'warning');
      return;
    }
    if (this.phone.replace(/\D/g, '').length < 8) {
      await this.showToast('Ingrese un número de teléfono válido', 'warning');
      return;
    }
    if (this.password !== this.confirmPassword) {
      await this.showToast('Las contraseñas no coinciden', 'danger');
      return;
    }

    this.loading = true;
    localStorage.removeItem('emergencyMode');

    // El backend espera firstName y lastName por separado, así que hacemos una división simple del campo nombre completo. Si solo se ingresa un nombre, se usará como firstName y lastName quedará igual para evitar campos vacíos.
    const parts = this.nombre.trim().split(' ');
    const firstName = parts[0];
    const lastName = parts.slice(1).join(' ') || firstName;

    this.authService.register({
      firstName,
      lastName,
      email: this.email,
      password: this.password,
      phone: this.phone
    }).subscribe({
      next: async (res) => {
        this.loading = false;
        await this.showToast('Cuenta creada exitosamente. Iniciando sesión...', 'success');

        // Auto login después de registro exitoso, pero no esperar a que termine para mostrar el toast de éxito
        this.authService.login({ email: this.email, password: this.password }).subscribe({
          next: (loginRes) => {
            if (loginRes.role === 'ADMIN') {
              window.location.href = '/dashboard';
            } else {
              window.location.href = '/reportar';
            }
          },
          error: async () => {
            await this.showToast('Cuenta creada. Inicia sesión manualmente.', 'warning');
          }
        });
      },
      error: async (err) => {
        this.loading = false;
        console.error('Register error', err);
        let msg: string;
        if (err.status === 0) {
          msg = 'No se pudo conectar con el servidor';
        } else if (err.status === 400) {
          msg = err.error?.message || 'El correo ya está registrado';
        } else {
          msg = err.error?.message || 'Error al registrarse';
        }
        await this.showToast(msg, 'danger');
      }
    });
  }
  // ------------------------------------------------------------------ //
  //  MODO EMERGENCIA
  // ------------------------------------------------------------------ //
  async enterEmergency() {
    this.loadingEmergencia = true;
    await this.showToast('Abriendo canal de emergencia...', 'danger');

    setTimeout(() => {
      this.loadingEmergencia = false;
      localStorage.setItem('userRole', 'citizen');
      localStorage.setItem('emergencyMode', 'true');
      window.location.href = '/reportar';
    }, 1200);
  }

  togglePassword() { this.showPassword = !this.showPassword; }
  toggleConfirmPassword() { this.showConfirmPassword = !this.showConfirmPassword; }

  private async showToast(message: string, color: string = 'primary') {
    const toast = await this.toastController.create({
      message,
      duration: 2200,
      position: 'top',
      color
    });
    await toast.present();
  }
}
