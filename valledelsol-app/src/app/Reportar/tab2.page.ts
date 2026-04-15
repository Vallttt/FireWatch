import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  IonContent, IonCard, IonCardHeader, IonCardTitle, IonCardContent,
  IonLabel, IonTextarea, IonButton,
  IonIcon, IonSegment, IonSegmentButton,
  IonHeader, IonToolbar, IonButtons, IonMenuButton, IonTitle,
  IonList, IonItem,
  ToastController
} from '@ionic/angular/standalone';

import { addIcons } from 'ionicons';
import {
  locationOutline, flameOutline, leafOutline,
  paperPlaneOutline, warning, listOutline,
  timerOutline, navigate, ellipse, appsOutline, alertCircleOutline,
  trashOutline, timeOutline
} from 'ionicons/icons';

import * as L from 'leaflet';
import { ReportService, ReporteResponse, SeverityLevel } from '../services/report.service';

@Component({
  selector: 'app-tab2',
  templateUrl: 'tab2.page.html',
  styleUrls: ['tab2.page.scss'],
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    IonContent, IonCard, IonCardHeader, IonCardTitle, IonCardContent,
    IonLabel, IonTextarea, IonButton,
    IonIcon, IonSegment, IonSegmentButton,
    IonHeader, IonToolbar, IonButtons, IonMenuButton, IonTitle,
    IonList, IonItem
  ]
})
export class Tab2Page {
  clasificacionInicial: string = '';
  tipoDesc: string = '';
  severidad: string = 'media';
  descripcion: string = '';
  map: L.Map | undefined;
  marker: L.Marker | undefined;
  latLng = { lat: -33.4489, lng: -70.6693 };

  isAdmin = false;

  // Historial de reportes (cargado desde backend)
  historialReportes: ReporteResponse[] = [];

  headerHidden: boolean = false;
  private lastScroll: number = 0;

  onContentScroll(e: any) {
    const current = e.detail.scrollTop;
    if (current > 80 && current > this.lastScroll) {
      this.headerHidden = true;
    } else if (current < this.lastScroll - 4 || current < 40) {
      this.headerHidden = false;
    }
    this.lastScroll = current;
  }

  constructor(
    private toastController: ToastController,
    private reportService: ReportService
  ) {
    addIcons({
      ellipse, locationOutline, navigate, timerOutline, listOutline,
      alertCircleOutline, paperPlaneOutline, appsOutline, leafOutline,
      flameOutline, warning, trashOutline, timeOutline
    });
  }

  ionViewDidEnter() {
    const role = localStorage.getItem('userRole');
    this.isAdmin = (role === 'admin');
    setTimeout(() => { this.cargarMapa(); }, 200);

    // Cargar historial desde backend si es admin
    if (this.isAdmin) {
      this.cargarHistorial();
    }
  }

  private cargarHistorial() {
    this.reportService.listarReportes().subscribe({
      next: (reportes) => {
        this.historialReportes = reportes;
      },
      error: (err) => {
        console.warn('No se pudo cargar historial de reportes', err);
      }
    });
  }

  private async showToast(message: string, color: string = 'primary') {
    const toast = await this.toastController.create({
      message,
      duration: 2200,
      position: 'top',
      color
    });
    await toast.present();
  }

  cargarMapa() {
    if (this.map) return;

    this.map = L.map('reportMap', {
      zoomControl: false,
      attributionControl: false
    }).setView([this.latLng.lat, this.latLng.lng], 15);

    L.control.attribution({ prefix: false }).addTo(this.map);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap · FireWatch'
    }).addTo(this.map);

    const fireIcon = L.divIcon({
      className: 'modern-marker-wrapper',
      html: `
        <div class="modern-marker">
          <div class="marker-pulse"></div>
          <div class="marker-pin">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="22" height="22" fill="#ffffff">
              <path d="M381.83 203.18c-3.7-2-8.32 0-9.46 4.06-1.66 5.93-3.85 12.07-6.6 18.34-1.6 3.65-6.36 4.4-8.93 1.4-37.83-44.13-46.66-101.31-29.21-149.6 4.46-12.34 11.18-26.05 21.51-39.3 3.41-4.37-.43-10.52-5.9-9.45C229.29 50.22 137.35 152.83 158 270.74a157.62 157.62 0 0 0 5.45 22.12c2 6.06-5 11-10 7.13-22-17-37.31-40.18-45-67.4-1.42-5-8.2-6-10.55-1.34-10.34 20.32-15.86 43-15.91 66.19C81.8 414.55 159.74 480 256.21 480c104.34 0 188.36-75.71 178.73-180.1-3.49-37.82-22.85-74.4-53.11-96.72z"/>
            </svg>
          </div>
        </div>
      `,
      iconSize: [50, 60],
      iconAnchor: [25, 60]
    });

    this.marker = L.marker(
      [this.latLng.lat, this.latLng.lng],
      { draggable: true, icon: fireIcon }
    ).addTo(this.map);

    this.marker.on('dragend', () => {
      const position = this.marker?.getLatLng();
      if (position) {
        this.latLng.lat = position.lat;
        this.latLng.lng = position.lng;
      }
    });

    this.obtenerMiUbicacionReal();
  }

  obtenerMiUbicacionReal() {
    if (!this.map) {
      this.cargarMapa();
    }

    this.map?.flyTo([this.latLng.lat, this.latLng.lng], 15, { duration: 0.8 });

    if (!navigator.geolocation) {
      this.showToast('Geolocalización no disponible en este dispositivo', 'warning');
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        this.latLng.lat = position.coords.latitude;
        this.latLng.lng = position.coords.longitude;
        this.map?.flyTo([this.latLng.lat, this.latLng.lng], 16, { duration: 1 });
        this.marker?.setLatLng([this.latLng.lat, this.latLng.lng]);
      },
      (err) => {
        console.warn('Permiso de ubicación denegado.', err);
        this.showToast('No se pudo obtener tu ubicación. Usando ubicación por defecto.', 'danger');
      },
      { enableHighAccuracy: true, timeout: 8000 }
    );
  }

  usarPlantilla(tipo: string | number | undefined) {
    if (typeof tipo !== 'string') return;
    if (tipo === 'Forestal' || tipo === 'Estructural' || tipo === 'Urbano') {
      this.tipoDesc = tipo;
      this.severidad = 'media';
      this.descripcion = `Incendio ${tipo.toLowerCase()} de severidad media`;
    }
  }

  /** Mapea severidad UI → enum backend */
  private mapSeverity(sev: string): SeverityLevel {
    const map: Record<string, SeverityLevel> = {
      'baja': 'LOW', 'media': 'MEDIUM', 'alta': 'HIGH', 'critica': 'CRITICAL'
    };
    return map[sev] || 'MEDIUM';
  }

  async reportar() {
    if (!this.descripcion) {
      await this.showToast('Agrega una descripción del incidente', 'warning');
      return;
    }

    const userId = localStorage.getItem('userId') || undefined;
    const userEmail = localStorage.getItem('userEmail') || 'Anónimo';

    this.reportService.crearReporte({
      userId: userId,
      usuarioReportante: userEmail,
      descripcion: this.descripcion,
      longitude: this.latLng.lng,
      latitude: this.latLng.lat,
      severity: this.mapSeverity(this.severidad)
    }).subscribe({
      next: async (res) => {
        this.historialReportes.unshift(res);
        await this.showToast('Reporte enviado correctamente', 'success');
        this.descripcion = '';
        this.clasificacionInicial = '';
        this.tipoDesc = '';
        this.severidad = 'media';
      },
      error: async (err) => {
        console.error('Error al enviar reporte', err);
        const msg = err.status === 0
          ? 'No se pudo conectar con el servidor'
          : 'Error al enviar el reporte';
        await this.showToast(msg, 'danger');
      }
    });
  }

  async eliminarReporte(reporte: ReporteResponse) {
    this.reportService.actualizarEstado(reporte.id, { estado: 'INACTIVE' }).subscribe({
      next: async () => {
        this.historialReportes = this.historialReportes.filter(r => r.id !== reporte.id);
        await this.showToast('Reporte eliminado', 'warning');
      },
      error: async (err) => {
        console.error('Error al eliminar reporte', err);
        await this.showToast('Error al eliminar el reporte', 'danger');
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

  /** Mapea severidad backend → etiqueta UI */
  mapSeverityLabel(sev: SeverityLevel | string): string {
    const map: Record<string, string> = {
      'LOW': 'baja', 'MEDIUM': 'media', 'HIGH': 'alta', 'CRITICAL': 'critica'
    };
    return map[sev] || sev;
  }
}
