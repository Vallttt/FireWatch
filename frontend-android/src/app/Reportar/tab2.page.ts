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
  trashOutline, timeOutline, imageOutline, closeCircle, videocamOutline,
  cameraOutline, imagesOutline, eyeOutline, closeOutline,
  chevronBackOutline, chevronForwardOutline
} from 'ionicons/icons';

import * as L from 'leaflet';
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';
import { ReportService, ReporteResponse, ReporteMediaItem, SeverityLevel } from '../services/report.service';
import { GeolocationService } from '../services/geolocation.service';
import { GeoService } from '../services/geo.service';

export interface MediaPreview {
  url: string;
  type: 'image' | 'video';
  name: string;
}

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
  mainZoneId: string | null = null;

  isAdmin = false;

  // Report history (loaded from backend)
  historialReportes: ReporteResponse[] = [];

  // Media to attach to the next submission
  selectedFiles: File[] = [];
  filePreviews: MediaPreview[] = [];
  private readonly MAX_FILES = 5;
  private readonly MAX_SIZE_MB = 20;

  // Report detail modal
  reporteSeleccionado: ReporteResponse | null = null;
  mediaDelReporte: ReporteMediaItem[] = [];
  cargandoMedia = false;

  // Lightbox
  lightboxIndex = -1;

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
    private reportService: ReportService,
    private geoSvc: GeolocationService,
    private geoService: GeoService
  ) {
    addIcons({
      ellipse, locationOutline, navigate, timerOutline, listOutline,
      alertCircleOutline, paperPlaneOutline, appsOutline, leafOutline,
      flameOutline, warning, trashOutline, timeOutline,
      imageOutline, closeCircle, videocamOutline, cameraOutline,
      imagesOutline, eyeOutline, closeOutline,
      chevronBackOutline, chevronForwardOutline
    });
  }

  ionViewDidEnter() {
    const role = localStorage.getItem('userRole');
    this.isAdmin = (role === 'admin');
    setTimeout(() => { this.loadMap(); }, 200);
    if (this.isAdmin) { this.loadHistory(); }
    this.loadMainZone();
  }

  private loadMainZone() {
    this.geoService.getMainZone().subscribe({
      next: (zone) => { this.mainZoneId = zone.id; },
      error: (err) => console.warn('No se pudo obtener la zona principal', err)
    });
  }

  private loadHistory() {
    this.reportService.listarReportes().subscribe({
      next: (reportes) => { this.historialReportes = reportes; },
      error: (err) => { console.warn('Could not load report history', err); }
    });
  }

  private async showToast(message: string, color: string = 'primary') {
    const toast = await this.toastController.create({ message, duration: 2200, position: 'top', color });
    await toast.present();
  }

  // ------------------------------------------------------------------ //
  //  MAP
  // ------------------------------------------------------------------ //

  loadMap() {
    if (this.map) return;

    this.map = L.map('reportMap', { zoomControl: false, attributionControl: false })
      .setView([this.latLng.lat, this.latLng.lng], 15);
    L.control.attribution({ prefix: false }).addTo(this.map);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19, attribution: '© OpenStreetMap · FireWatch'
    }).addTo(this.map);

    const fireIcon = L.divIcon({
      className: 'modern-marker-wrapper',
      html: `<div class="modern-marker"><div class="marker-pulse"></div><div class="marker-pin">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="22" height="22" fill="#ffffff">
          <path d="M381.83 203.18c-3.7-2-8.32 0-9.46 4.06-1.66 5.93-3.85 12.07-6.6 18.34-1.6 3.65-6.36 4.4-8.93 1.4-37.83-44.13-46.66-101.31-29.21-149.6 4.46-12.34 11.18-26.05 21.51-39.3 3.41-4.37-.43-10.52-5.9-9.45C229.29 50.22 137.35 152.83 158 270.74a157.62 157.62 0 0 0 5.45 22.12c2 6.06-5 11-10 7.13-22-17-37.31-40.18-45-67.4-1.42-5-8.2-6-10.55-1.34-10.34 20.32-15.86 43-15.91 66.19C81.8 414.55 159.74 480 256.21 480c104.34 0 188.36-75.71 178.73-180.1-3.49-37.82-22.85-74.4-53.11-96.72z"/>
        </svg></div></div>`,
      iconSize: [50, 60], iconAnchor: [25, 60]
    });

    this.marker = L.marker([this.latLng.lat, this.latLng.lng], { draggable: true, icon: fireIcon }).addTo(this.map);
    this.marker.on('dragend', () => {
      const p = this.marker?.getLatLng();
      if (p) { this.latLng.lat = p.lat; this.latLng.lng = p.lng; }
    });
    this.getMyLocation();
  }

  async getMyLocation() {
    if (!this.map) { this.loadMap(); }
    try {
      const pos = await this.geoSvc.getCurrentPosition();
      this.latLng.lat = pos.lat;
      this.latLng.lng = pos.lng;
      this.map?.flyTo([pos.lat, pos.lng], 16, { duration: 1 });
      this.marker?.setLatLng([pos.lat, pos.lng]);
    } catch (err: any) {
      console.warn('Location unavailable:', err?.message);
      this.showToast(err?.message || 'No se pudo obtener la ubicación', 'warning');
    }
  }

  // ------------------------------------------------------------------ //
  //  TEMPLATES
  // ------------------------------------------------------------------ //

  useTemplate(tipo: string | number | undefined) {
    if (typeof tipo !== 'string') return;
    if (tipo === 'Forestal' || tipo === 'Estructural' || tipo === 'Urbano') {
      this.tipoDesc = tipo; this.severidad = 'media'; this.updateTemplateDescription();
    }
  }

  onSeverityChange() { this.updateTemplateDescription(); }

  private updateTemplateDescription() {
    if (this.tipoDesc) {
      this.descripcion = `Incendio ${this.tipoDesc}, severidad: ${this.severidad}`;
    }
  }

  // ------------------------------------------------------------------ //
  //  MEDIA (local selection)
  // ------------------------------------------------------------------ //

  onFilesSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;
    Array.from(input.files).forEach(file => {
      if (this.selectedFiles.length >= this.MAX_FILES) {
        this.showToast(`Máximo ${this.MAX_FILES} archivos permitidos`, 'warning'); return;
      }
      if (file.size > this.MAX_SIZE_MB * 1024 * 1024) {
        this.showToast(`"${file.name}" supera los ${this.MAX_SIZE_MB} MB`, 'warning'); return;
      }
      this.selectedFiles.push(file);
      this.filePreviews.push({
        url: URL.createObjectURL(file),
        type: file.type.startsWith('video/') ? 'video' : 'image',
        name: file.name
      });
    });
    input.value = '';
  }

  removeFile(index: number) {
    URL.revokeObjectURL(this.filePreviews[index].url);
    this.selectedFiles.splice(index, 1);
    this.filePreviews.splice(index, 1);
  }

  async takePicture() {
    if (this.selectedFiles.length >= this.MAX_FILES) {
      this.showToast(`Máximo ${this.MAX_FILES} archivos permitidos`, 'warning');
      return;
    }
    try {
      const photo = await Camera.getPhoto({
        quality: 85,
        allowEditing: false,
        resultType: CameraResultType.DataUrl,
        source: CameraSource.Camera
      });

      if (!photo.dataUrl) return;

      // Convert dataUrl to File for upload
      const response = await fetch(photo.dataUrl);
      const blob = await response.blob();
      const filename = `foto_${Date.now()}.${photo.format ?? 'jpeg'}`;
      const file = new File([blob], filename, { type: `image/${photo.format ?? 'jpeg'}` });

      if (file.size > this.MAX_SIZE_MB * 1024 * 1024) {
        this.showToast(`La foto supera los ${this.MAX_SIZE_MB} MB`, 'warning');
        return;
      }

      this.selectedFiles.push(file);
      this.filePreviews.push({ url: photo.dataUrl, type: 'image', name: filename });
    } catch (err: any) {
      // User cancelled — no toast needed
      if (err?.message && !err.message.toLowerCase().includes('cancel')) {
        this.showToast('No se pudo abrir la cámara', 'warning');
      }
    }
  }

  private clearMedia() {
    this.filePreviews.forEach(p => URL.revokeObjectURL(p.url));
    this.selectedFiles = []; this.filePreviews = [];
  }

  // ------------------------------------------------------------------ //
  //  SUBMIT
  // ------------------------------------------------------------------ //

  private mapSeverity(sev: string): SeverityLevel {
    const map: Record<string, SeverityLevel> = {
      'baja': 'LOW', 'media': 'MEDIUM', 'alta': 'HIGH', 'critica': 'CRITICAL'
    };
    return map[sev] || 'MEDIUM';
  }

  async submitReport() {
    if (!this.descripcion) {
      await this.showToast('Por favor agregue una descripción del incidente', 'warning'); return;
    }

    if (!this.mainZoneId) {
      await this.showToast('No se pudo determinar la zona principal. Intenta de nuevo.', 'danger');
      return;
    }

    const userId    = localStorage.getItem('userId')    || undefined;
    const userEmail = localStorage.getItem('userEmail') || 'Anónimo';

    this.reportService.crearReporte({
      userId, usuarioReportante: userEmail,
      descripcion: this.descripcion,
      zoneId: this.mainZoneId,
      longitude: this.latLng.lng, latitude: this.latLng.lat,
      severity: this.mapSeverity(this.severidad)
    }).subscribe({
      next: async (res) => {
        // Upload media files if any
        if (this.selectedFiles.length > 0) {
          const filesToUpload = [...this.selectedFiles];
          this.reportService.subirMedia(res.id, filesToUpload).subscribe({
            next: () => {
              res.mediaCount = filesToUpload.length;
            },
            error: (err) => console.warn('Could not upload media', err)
          });
        }

        this.historialReportes.unshift(res);
        await this.showToast(
          this.selectedFiles.length > 0
            ? `Reporte enviado con ${this.selectedFiles.length} archivo(s)`
            : 'Reporte enviado exitosamente',
          'success'
        );
        this.descripcion = ''; this.clasificacionInicial = '';
        this.tipoDesc = ''; this.severidad = 'media';
        this.clearMedia();
      },
      error: async (err) => {
        console.error('Error submitting report', err);
        const msg = err.status === 0 ? 'No se pudo conectar con el servidor' : 'Error al enviar el reporte';
        await this.showToast(msg, 'danger');
      }
    });
  }

  // ------------------------------------------------------------------ //
  //  REPORT DETAIL MODAL
  // ------------------------------------------------------------------ //

  openReporteDetalle(reporte: ReporteResponse) {
    this.reporteSeleccionado = reporte;
    this.mediaDelReporte = [];
    this.lightboxIndex = -1;

    if (reporte.mediaCount > 0) {
      this.cargandoMedia = true;
      this.reportService.obtenerMedia(reporte.id).subscribe({
        next: (items) => { this.mediaDelReporte = items; this.cargandoMedia = false; },
        error: (err) => { console.warn('Could not load media', err); this.cargandoMedia = false; }
      });
    }
  }

  closeReporteDetalle() {
    this.reporteSeleccionado = null;
    this.mediaDelReporte = [];
    this.lightboxIndex = -1;
  }

  // ------------------------------------------------------------------ //
  //  LIGHTBOX
  // ------------------------------------------------------------------ //

  openLightbox(index: number) { this.lightboxIndex = index; }
  closeLightbox() { this.lightboxIndex = -1; }

  prevMedia() {
    if (this.lightboxIndex > 0) this.lightboxIndex--;
  }

  nextMedia() {
    if (this.lightboxIndex < this.mediaDelReporte.length - 1) this.lightboxIndex++;
  }

  // ------------------------------------------------------------------ //
  //  HISTORY ACTIONS
  // ------------------------------------------------------------------ //

  async deleteReport(reporte: ReporteResponse) {
    this.reportService.eliminarReporte(reporte.id).subscribe({
      next: async () => {
        this.historialReportes = this.historialReportes.filter(r => r.id !== reporte.id);
        if (this.reporteSeleccionado?.id === reporte.id) { this.closeReporteDetalle(); }
        await this.showToast('Reporte eliminado', 'warning');
      },
      error: async (err) => {
        console.error('Error deleting report', err);
        await this.showToast('Error al eliminar el reporte', 'danger');
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

  mapSeverityLabel(sev: SeverityLevel | string | undefined): string {
    if (!sev) return 'sin clasificar';
    const map: Record<string, string> = {
      'LOW': 'baja', 'MEDIUM': 'media', 'HIGH': 'alta', 'CRITICAL': 'critica'
    };
    return map[sev] || sev;
  }
}
