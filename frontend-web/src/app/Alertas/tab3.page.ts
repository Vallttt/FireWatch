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

  canalMail: boolean = true;

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
    this.loadHistory();
  }

  async deleteAlert(alerta: Notificacion) {
    this.alertService.eliminarAlerta(alerta.id).subscribe({
      next: async () => {
        this.historialAlertas = this.historialAlertas.filter(a => a.id !== alerta.id);
        await this.showToast('Alerta eliminada', 'warning');
      },
      error: async (err) => {
        console.error('Error deleting alert', err);
        await this.showToast('Error al eliminar la alerta', 'danger');
      }
    });
  }

  private loadHistory() {
    this.loadingHistorial = true;
    this.alertService.historial().subscribe({
      next: (data) => {
        this.historialAlertas = data.sort((a, b) =>
          new Date(b.fechaEnvio).getTime() - new Date(a.fechaEnvio).getTime()
        );
        this.loadingHistorial = false;
      },
      error: (err) => {
        console.warn('Could not load alert history', err);
        this.loadingHistorial = false;
      }
    });
  }

  formatDate(fecha: string): string {
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

  applyProtocol(protocolo: string | number | undefined) {
    if (typeof protocolo !== 'string') return;

    const templates: { [key: string]: { tipo: string; mensaje: string } } = {
      'Evacuación': {
        tipo: 'Evacuación',
        mensaje: 'EVACUACIÓN INMEDIATA: Se ha detectado un incendio en su área. Diríjase al punto de reunión más cercano y siga las instrucciones del personal de emergencia.'
      },
      'Incendio': {
        tipo: 'Emergencia',
        mensaje: 'ALERTA DE INCENDIO: Se ha reportado un brote de incendio en su sector. Manténgase alerta y siga las instrucciones oficiales de los bomberos.'
      },
      'Prevención': {
        tipo: 'Preventiva',
        mensaje: 'PREVENCIÓN: Condiciones climáticas adversas. Evite quemas y mantenga libres las áreas cercanas a vegetación seca.'
      },
      'Controlado': {
        tipo: 'Informativo',
        mensaje: 'INFORMACIÓN: El incendio reportado ha sido contenido. Puede retomar sus actividades normales. Fin de la alerta roja.'
      }
    };

    const template = templates[protocolo];
    if (template) {
      this.tipo = template.tipo;
      this.mensaje = template.mensaje;
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

  async sendAlert() {
    if (!this.mensaje) {
      await this.showToast('Por favor escriba el cuerpo del mensaje', 'warning');
      return;
    }

    this.loading = true;

    this.alertService.enviarAlerta({
      mensaje: this.mensaje,
      tipo: this.tipo || 'General',
      canalEmail: this.canalMail
    }).subscribe({
      next: async () => {
        this.loading = false;
        await this.showToast('Alerta enviada exitosamente', 'success');
        this.mensaje = '';
        this.protocoloSeleccionado = '';
        this.loadHistory();
      },
      error: async (err) => {
        this.loading = false;
        console.error('Error sending alert', err);
        if (err.status === 0) {
          await this.showToast('Sin conexión al servidor', 'danger');
        } else {
          await this.showToast('El gateway rechazó la solicitud', 'danger');
        }
      }
    });
  }
}
