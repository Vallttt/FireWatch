import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import {
  IonContent, IonCard, IonCardHeader, IonCardTitle, IonCardContent,
  IonItem, IonLabel, IonTextarea, IonButton, IonList, IonBadge,
  IonIcon, IonToggle, ToastController, IonHeader, IonToolbar, IonButtons, IonMenuButton, IonTitle,
  IonSegment, IonSegmentButton
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  megaphoneOutline, warningOutline, flameOutline, shieldHalfOutline,
  checkmarkCircleOutline, warning, notificationsOutline, callOutline,
  mailOutline, paperPlaneOutline, ellipse, timeOutline, trashOutline
} from 'ionicons/icons';

import { AlertService, Notificacion } from '../services/alert.service';

@Component({
  selector: 'app-tab3',
  templateUrl: 'tab3.page.html',
  styleUrls: ['tab3.page.scss'],
  standalone: true,
  imports: [
    FormsModule, CommonModule,
    IonContent, IonCard, IonCardHeader, IonCardTitle, IonCardContent,
    IonItem, IonLabel, IonTextarea, IonButton, IonList, IonBadge,
    IonIcon, IonToggle, IonHeader, IonToolbar, IonButtons, IonMenuButton, IonTitle,
    IonSegment, IonSegmentButton
  ]
})
export class Tab3Page {
  tipo: string = '';
  mensaje: string = '';
  loading: boolean = false;
  protocoloSeleccionado: string = '';

  historialAlertas: Notificacion[] = [];
  loadingHistorial = false;

  // Canales de distribución
  canalPush: boolean = true;
  canalSms: boolean = false;
  canalMail: boolean = false;

  headerHidden: boolean = false;
  private lastScroll: number = 0;

  constructor(
    private toastController: ToastController,
    private alertService: AlertService
  ) {
    addIcons({
      ellipse, megaphoneOutline, warningOutline, flameOutline, shieldHalfOutline,
      checkmarkCircleOutline, warning, notificationsOutline, callOutline,
      mailOutline, paperPlaneOutline, timeOutline, trashOutline
    });
  }

  ionViewDidEnter() {
    this.cargarHistorial();
  }

  async eliminarAlerta(alerta: Notificacion) {
    this.alertService.eliminarAlerta(alerta.id).subscribe({
      next: async () => {
        this.historialAlertas = this.historialAlertas.filter(a => a.id !== alerta.id);
        await this.showToast('Alerta eliminada', 'warning');
      },
      error: async (err) => {
        console.error('Error al eliminar alerta', err);
        await this.showToast('Error al eliminar la alerta', 'danger');
      }
    });
  }

  private cargarHistorial() {
    this.loadingHistorial = true;
    this.alertService.historial().subscribe({
      next: (data) => {
        this.historialAlertas = data.sort((a, b) =>
          new Date(b.fechaEnvio).getTime() - new Date(a.fechaEnvio).getTime()
        );
        this.loadingHistorial = false;
      },
      error: (err) => {
        console.warn('No se pudo cargar historial de alertas', err);
        this.loadingHistorial = false;
      }
    });
  }

  formatearFecha(fecha: string): string {
    const d = new Date(fecha);
    return d.toLocaleDateString('es-CL', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  onContentScroll(e: any) {
    const current = e.detail.scrollTop;
    if (current > 80 && current > this.lastScroll) {
      this.headerHidden = true;
    } else if (current < this.lastScroll - 4 || current < 40) {
      this.headerHidden = false;
    }
    this.lastScroll = current;
  }

  // Aplica un protocolo predefinido al seleccionar el segment
  aplicarProtocolo(protocolo: string | number | undefined) {
    if (typeof protocolo !== 'string') return;

    const plantillas: { [key: string]: { tipo: string; mensaje: string } } = {
      'Evacuación': {
        tipo: 'Evacuación',
        mensaje: 'EVACUACIÓN INMEDIATA: Se ha detectado un incendio en su zona. Diríjase al punto de encuentro más cercano y siga las instrucciones del personal de emergencia.'
      },
      'Incendio': {
        tipo: 'Emergencia',
        mensaje: 'ALERTA DE INCENDIO: Se ha reportado un foco de incendio en su sector. Manténgase alerta y siga las instrucciones oficiales del cuerpo de bomberos.'
      },
      'Prevención': {
        tipo: 'Preventiva',
        mensaje: 'PREVENCIÓN: Condiciones climáticas adversas. Evite quemas y mantenga despejadas las áreas cercanas a vegetación seca.'
      },
      'Controlado': {
        tipo: 'Informativo',
        mensaje: 'INFORMACIÓN: El incendio reportado ha sido controlado. Puede retornar a sus actividades normales. Fin de la alerta roja.'
      }
    };

    const plantilla = plantillas[protocolo];
    if (plantilla) {
      this.tipo = plantilla.tipo;
      this.mensaje = plantilla.mensaje;
    }
  }

  private async showToast(message: string, color: string = 'primary') {
    const toast = await this.toastController.create({
      message,
      duration: 2800,
      position: 'top',
      color
    });
    await toast.present();
  }

  async enviarAlerta() {
    if (!this.mensaje) {
      await this.showToast('Escribe el cuerpo del mensaje de alerta', 'warning');
      return;
    }

    if (!this.canalPush && !this.canalSms && !this.canalMail) {
      await this.showToast('Selecciona al menos un canal de distribución', 'warning');
      return;
    }

    this.loading = true;

    this.alertService.enviarAlerta({
      mensaje: this.mensaje,
      tipo: this.tipo || 'General'
    }).subscribe({
      next: async () => {
        this.loading = false;
        await this.showToast('Alerta enviada exitosamente', 'success');
        this.mensaje = '';
        this.protocoloSeleccionado = '';
        this.cargarHistorial();
      },
      error: async (err) => {
        this.loading = false;
        console.error('Error al enviar alerta', err);

        if (err.status === 0) {
          await this.showToast('Sin conexión al servidor', 'danger');
        } else {
          await this.showToast('El gateway rechazó la solicitud', 'danger');
        }
      }
    });
  }
}
