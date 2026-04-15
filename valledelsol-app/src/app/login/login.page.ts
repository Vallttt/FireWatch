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
  logInOutline, personAddOutline, eyeOutline, eyeOffOutline, warningOutline
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
export class LoginPage {
  modo: string = 'login';
  email: string = '';
  password: string = '';
  nombre: string = '';
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
    addIcons({flame,mailOutline,lockClosedOutline,personOutline,logInOutline,personAddOutline,eyeOutline,eyeOffOutline,warningOutline});
  }

  cambiarModo(modo: string | number | undefined) {
    if (typeof modo !== 'string') return;
    this.modo = modo;
  }

  async iniciarSesion() {
    if (!this.email || !this.password) {
      await this.mostrarToast('Completa todos los campos', 'warning');
      return;
    }

    this.loading = true;
    localStorage.removeItem('emergencyMode');

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: (res) => {
        this.loading = false;
        // El servicio ya guarda token y role en localStorage
        if (res.role === 'ADMIN') {
          window.location.href = '/dashboard';
        } else {
          window.location.href = '/reportar';
        }
      },
      error: async (err) => {
        this.loading = false;
        console.error('Login error', err);
        const msg = err.status === 0
          ? 'No se pudo conectar con el servidor'
          : (err.error?.message || 'Credenciales incorrectas');
        await this.mostrarToast(msg, 'danger');
      }
    });
  }

  async registrarse() {
    if (!this.nombre || !this.email || !this.password || !this.confirmPassword) {
      await this.mostrarToast('Completa todos los campos', 'warning');
      return;
    }

    if (this.password !== this.confirmPassword) {
      await this.mostrarToast('Las contraseñas no coinciden', 'danger');
      return;
    }

    this.loading = true;
    localStorage.removeItem('emergencyMode');

    // Separar nombre y apellido
    const parts = this.nombre.trim().split(' ');
    const firstName = parts[0];
    const lastName = parts.slice(1).join(' ') || firstName;

    this.authService.register({
      firstName,
      lastName,
      email: this.email,
      password: this.password
    }).subscribe({
      next: async (res) => {
        this.loading = false;
        await this.mostrarToast('Cuenta creada exitosamente. Iniciando sesión...', 'success');

        // Auto-login después del registro
        this.authService.login({ email: this.email, password: this.password }).subscribe({
          next: (loginRes) => {
            if (loginRes.role === 'ADMIN') {
              window.location.href = '/dashboard';
            } else {
              window.location.href = '/reportar';
            }
          },
          error: async () => {
            await this.mostrarToast('Cuenta creada. Inicia sesión manualmente.', 'warning');
          }
        });
      },
      error: async (err) => {
        this.loading = false;
        console.error('Register error', err);
        const msg = err.status === 0
          ? 'No se pudo conectar con el servidor'
          : (err.error?.message || 'Error al registrar');
        await this.mostrarToast(msg, 'danger');
      }
    });
  }

  async ingresarEmergencia() {
    this.loadingEmergencia = true;
    await this.mostrarToast('Abriendo canal rojo de Emergencias...', 'danger');

    setTimeout(() => {
      this.loadingEmergencia = false;
      localStorage.setItem('userRole', 'citizen');
      localStorage.setItem('emergencyMode', 'true');
      window.location.href = '/reportar';
    }, 1200);
  }

  togglePassword() { this.showPassword = !this.showPassword; }
  toggleConfirmPassword() { this.showConfirmPassword = !this.showConfirmPassword; }

  private async mostrarToast(mensaje: string, color: string = 'primary') {
    const toast = await this.toastController.create({
      message: mensaje,
      duration: 2200,
      position: 'top',
      color
    });
    await toast.present();
  }
}
