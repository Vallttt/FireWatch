import { Component, OnInit } from '@angular/core';
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
  imagesOutline, eyeOutline, closeOutline, chevronBackOutline, chevronForwardOutline
} from 'ionicons/icons';

import * as L from 'leaflet';
import { ReportService, ReporteResponse, ReporteMediaItem, SeverityLevel } from '../services/report.service';
import { GeolocationService } from '../services/geolocation.service';
import { ZonesAssetService, ComunaZone } from '../services/zones-asset.service';
import { GeoService, ZoneResponse } from '../services/geo.service';

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
export class Tab2Page implements OnInit {
  clasificacionInicial: string = '';
  tipoDesc: string = '';
  severidad: string = 'media';
  descripcion: string = '';
  map: L.Map | undefined;
  marker: L.Marker | undefined;
  latLng = { lat: -33.4489, lng: -70.6693 };
  zones: ComunaZone[] = [];
  zoneLayers: L.Layer[] = [];
  mainBounds: L.LatLngBounds | null = null;
  /** Zona (comuna real) que contiene el punto seleccionado actualmente. */
  zonaSeleccionada: ComunaZone | null = null;

  isAdmin = false;

  // historial de reportes para mostrar en el panel lateral (solo admins)
  historialReportes: ReporteResponse[] = [];

  // media seleccionada para subir con el reporte
  selectedFiles: File[] = [];
  filePreviews: MediaPreview[] = [];
  private readonly MAX_FILES = 5;
  private readonly MAX_SIZE_MB = 20;

  // reporte actualmente seleccionado para mostrar en el detalle
  reporteSeleccionado: ReporteResponse | null = null;
  mediaDelReporte: ReporteMediaItem[] = [];
  cargandoMedia = false;

  // estado del lightbox, -1 si está cerrado o el índice del media mostrado si está abierto
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
    private zonesAssetService: ZonesAssetService,
    private geoService: GeoService
  ) {
    addIcons({
      ellipse, locationOutline, navigate, timerOutline, listOutline,
      alertCircleOutline, paperPlaneOutline, appsOutline, leafOutline,
      flameOutline, warning, trashOutline, timeOutline,
      imageOutline, closeCircle, videocamOutline,
      imagesOutline, eyeOutline, closeOutline,
      chevronBackOutline, chevronForwardOutline
    });
  }

  ngOnInit() {
    setTimeout(() => { this.loadMap(); }, 200);
  }

  ionViewDidEnter() {
    const role = localStorage.getItem('userRole');
    this.isAdmin = (role === 'admin');
    setTimeout(() => { this.loadMap(); }, 200);
    if (this.isAdmin) { this.loadHistory(); }
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
  //  MAPA 
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
    this.marker.on('dragend', async () => {
    const p = this.marker?.getLatLng();

    if (!p) return;

    const zona = this.zonesAssetService.findZoneContaining(p.lat, p.lng, this.zones);
    if (!zona) {
      await this.showToast('El reporte debe estar dentro del área de cobertura (Santiago y comunas cercanas).', 'warning');

      this.marker?.setLatLng([this.latLng.lat, this.latLng.lng]);
      return;
    }

    this.zonaSeleccionada = zona;
    this.latLng.lat = p.lat;
    this.latLng.lng = p.lng;
});
    this.getMyLocation();
    this.loadZonesForReportMap();
  }

  /** Límites comunales reales (asset local — zone-service aún no tiene datos reales). */
  private loadZonesForReportMap() {
    this.zonesAssetService.getZones().subscribe({
      next: (zones) => {
        this.zones = zones;
        this.renderReportZones();

        // Determina en qué comuna cae el marcador inicial (ubicación por defecto / GPS)
        this.zonaSeleccionada = this.zonesAssetService.findZoneContaining(this.latLng.lat, this.latLng.lng, this.zones);
      },
      error: (err) => {
        console.error('No se pudieron cargar las comunas en Reportar', err);
      }
    });
  }

  private renderReportZones() {
    if (!this.map) return;

    this.zoneLayers.forEach((layer: L.Layer) => layer.remove());
    this.zoneLayers = [];
    this.mainBounds = null;

    this.zones.forEach((zone) => {
      if (!zone.geometry) return;

      const esProvincia = zone.zoneType === 'PROVINCE';

      const layer = L.geoJSON(zone.geometry, {
        style: {
          color: zone.color || '#3388ff',
          weight: 2,
          dashArray: esProvincia ? '6 4' : undefined,
          fillOpacity: esProvincia ? 0 : 0.2
        }
      }).addTo(this.map!);

      this.zoneLayers.push(layer);

      if (esProvincia) {
        this.mainBounds = layer.getBounds();
        this.map!.fitBounds(this.mainBounds, { padding: [20, 20], animate: false });
      }
    });
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
  //  TEMPLATES DE REPORTE
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
  //  MEDIA (selección local y subida al backend)
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

  private clearMedia() {
    this.filePreviews.forEach(p => URL.revokeObjectURL(p.url));
    this.selectedFiles = []; this.filePreviews = [];
  }

  // ------------------------------------------------------------------ //
  //  ENVÍO DE REPORTE
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

    const userId    = localStorage.getItem('userId')    || undefined;
    const userEmail = localStorage.getItem('userEmail') || 'Anónimo';

    const zona = this.zonesAssetService.findZoneContaining(this.latLng.lat, this.latLng.lng, this.zones);
    if (!zona) {
      await this.showToast('No puedes enviar reportes fuera del área de cobertura (Santiago y comunas cercanas).', 'warning');
      return;
    }
    this.zonaSeleccionada = zona;

    this.reportService.crearReporte({
      userId, usuarioReportante: userEmail,
      descripcion: this.descripcion,
      zoneId: zona.id,
      longitude: this.latLng.lng, latitude: this.latLng.lat,
      severity: this.mapSeverity(this.severidad)
    }).subscribe({
      next: async (res) => {
        // Subir media si se seleccionó algo, pero no esperar a que termine para mostrar el éxito del reporte
        if (this.selectedFiles.length > 0) {
          const filesToUpload = [...this.selectedFiles];
          this.reportService.subirMedia(res.id, filesToUpload).subscribe({
            next: () => {
              // Actualizar el conteo de media en el reporte del historial para que se muestre el ícono correspondiente
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

        // Si el reporte cae dentro de una zona operativa real que aún no
        // tiene ruta de evacuación, se genera sola (el admin no la crea a mano).
        this.tryAutoGenerateEvacuationRoute(this.latLng.lat, this.latLng.lng, zona);
      },
      error: async (err) => {
        console.error('Error submitting report', err);
        const msg = err.status === 0 ? 'No se pudo conectar con el servidor' : 'Error al enviar el reporte';
        await this.showToast(msg, 'danger');
      }
    });
  }

  /**
   * Cuando se registra un reporte, si su ubicación cae dentro de una zona
   * operativa real (creada por el admin) que aún no tiene ruta de evacuación,
   * se genera sola: origen = centro de la zona, destino = centro de la comuna
   * donde está (el "punto de encuentro"), camino real por calles vía OSRM. No
   * hay creación manual de rutas — el admin solo crea zonas.
   */
  private tryAutoGenerateEvacuationRoute(lat: number, lng: number, comuna: ComunaZone) {
    this.geoService.getZones().subscribe({
      next: (zones) => {
        const zona = zones.find(z => z.zoneType === 'OPERATIONAL' && this.pointInZoneGeoJson(lng, lat, z.geoJson));
        if (!zona) return;

        this.geoService.getEvacuationRoutesByZone(zona.id).subscribe({
          next: (rutas) => {
            if (rutas.length === 0) { this.generateAutoRoute(zona, comuna); }
          },
          error: () => this.generateAutoRoute(zona, comuna)
        });
      },
      error: (err) => console.warn('No se pudieron revisar las zonas para la ruta automática', err)
    });
  }

  private pointInZoneGeoJson(lng: number, lat: number, geoJson: string): boolean {
    let geometry: any;
    try { geometry = JSON.parse(geoJson); } catch { return false; }
    if (geometry.type !== 'Polygon') return false;

    const ring = geometry.coordinates[0];
    let inside = false;
    for (let i = 0, j = ring.length - 1; i < ring.length; j = i++) {
      const [xi, yi] = ring[i]; const [xj, yj] = ring[j];
      const intersect = ((yi > lat) !== (yj > lat)) && (lng < (xj - xi) * (lat - yi) / (yj - yi) + xi);
      if (intersect) inside = !inside;
    }
    return inside;
  }

  private computeCentroid(geometry: any): [number, number] | null {
    let ring: number[][] | null = null;
    if (geometry.type === 'Polygon') {
      ring = geometry.coordinates[0];
    } else if (geometry.type === 'MultiPolygon') {
      ring = geometry.coordinates.reduce((best: number[][] | null, poly: number[][][]) =>
        (!best || poly[0].length > best.length) ? poly[0] : best, null);
    }
    if (!ring || ring.length < 3) return null;

    const pts = ring.slice(0, ring.length - 1);
    const cx = pts.reduce((s, p) => s + p[0], 0) / pts.length;
    const cy = pts.reduce((s, p) => s + p[1], 0) / pts.length;
    return [cx, cy];
  }

  private async generateAutoRoute(zona: ZoneResponse, comuna: ComunaZone) {
    let zoneGeometry: any;
    try { zoneGeometry = JSON.parse(zona.geoJson); } catch { return; }

    const origin = this.computeCentroid(zoneGeometry);
    const destino = this.computeCentroid(comuna.geometry as any);
    if (!origin || !destino) return;

    try {
      const url = `https://router.project-osrm.org/route/v1/foot/${origin[0]},${origin[1]};${destino[0]},${destino[1]}?overview=full&geometries=geojson`;
      const res = await fetch(url);
      const data = await res.json();
      if (data.code !== 'Ok' || !data.routes?.[0]) return;

      // El backend limita el nombre a 50 caracteres (min 4) — se recorta si la zona tiene un nombre largo.
      let name = `Ruta — ${zona.name}`;
      if (name.length > 50) { name = name.slice(0, 50); }

      this.geoService.createEvacuationRoute({
        name,
        description: `Ruta de evacuación generada automáticamente al registrarse un reporte en la zona ${zona.name}, hacia ${comuna.name}.`,
        geoJson: JSON.stringify(data.routes[0].geometry),
        zoneId: zona.id
      }).subscribe({
        error: (err) => console.warn('No se pudo generar la ruta automática', err)
      });
    } catch (err) {
      console.warn('No se pudo calcular la ruta automática (¿sin conexión?)', err);
    }
  }

  // ------------------------------------------------------------------ //
  //  REPORTE DETAIL PANEL (solo admins)
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
  //  HISTORIAL DE REPORTES (solo admins)
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
