import { Component, OnInit, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  IonContent, IonHeader, IonTitle, IonToolbar,
  IonButtons, IonMenuButton, IonCard,
  IonCardHeader, IonCardTitle, IonCardSubtitle, IonCardContent,
  IonList, IonItem, IonLabel, IonIcon, ToastController
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  flame, ellipse, shieldHalfOutline,
  addCircleOutline, trashOutline, arrowUndoOutline,
  checkmarkCircleOutline, closeCircleOutline,
  leafOutline, exitOutline, informationCircleOutline,
  alertCircleOutline
} from 'ionicons/icons';
import * as L from 'leaflet';

import {
  GeoService, BrigadeResponse, MappedReportResponse, EvacuationResponse,
  ZoneResponse
} from '../services/geo.service';
import { ReportService, ReporteResponse } from '../services/report.service';
import { ZonesAssetService, ComunaZone } from '../services/zones-asset.service';
import { GeolocationService } from '../services/geolocation.service';

@Component({
  selector: 'app-mapa',
  templateUrl: './mapa.page.html',
  styleUrls: ['./mapa.page.scss'],
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    IonContent, IonHeader, IonTitle, IonToolbar,
    IonButtons, IonMenuButton, IonCard,
    IonCardHeader, IonCardTitle, IonCardSubtitle, IonCardContent,
    IonList, IonItem, IonLabel, IonIcon
  ]
})
export class MapaPage implements OnInit {
  public map: L.Map | undefined;
  public zones: ComunaZone[] = [];
  public zoneLayers: L.Layer[] = [];
  public isAdmin = false;
  public focoSeleccionado: any = null;
  public routes: any[] = [];
  public routeLayers: L.Layer[] = [];

  public brigadasDisponibles: any[] = [];
  public brigadasOcupadas: { id: string; nombre: string; tiempoMinutos: number; emergenciaId: number }[] = [];
  public nuevaBrigadaNombre = '';

  public emergencias: any[] = [];
  public historialIncendios: ReporteResponse[] = [];
  private mainZoneId: string | null = null;

  /** Ruta de evacuación real de la comuna donde está el ciudadano (solo usuario, no admin). */
  public citizenZone: ComunaZone | null = null;
  public citizenEvacuationRoutes: EvacuationResponse[] = [];
  public loadingCitizenRoute = false;
  public citizenLocationError: string | null = null;
  private citizenRouteLayers: L.Layer[] = [];

  /* ----------  GESTIÓN DE ZONAS Y RUTAS (admin)  ---------- */
  public backendZones: ZoneResponse[] = [];
  public backendRoutes: EvacuationResponse[] = [];

  public drawingMode: 'none' | 'zone' | 'route' = 'none';
  /** Comuna predefinida (límite real) dentro de la cual el admin dibuja la zona. */
  public selectedComunaContext: ComunaZone | null = null;
  public drawingPoints: L.LatLng[] = [];
  private adminDrawingLayer: L.Layer | null = null;
  private adminDrawingMarkers: L.CircleMarker[] = [];

  public showZoneForm = false;
  public newZoneName = '';
  public newZoneDescription = '';
  public newZoneColor = '#3388ff';

  public showRouteForm = false;
  public newRouteName = '';
  public newRouteDescription = '';
  public selectedRouteZoneId = '';
  public routeTipo: 'ESTRUCTURAL' | 'FORESTAL' | 'URBANO' = 'ESTRUCTURAL';
  public routeSeveridad: 'BAJA' | 'MEDIA' | 'ALTA' = 'BAJA';

  private marcadoresLeaflet: L.Marker[] = [];
  private backendLoaded = false;

  constructor(
    private ngZone: NgZone,
    private toastController: ToastController,
    private geoService: GeoService,
    private reportService: ReportService,
    private zonesAssetService: ZonesAssetService,
    private geolocationService: GeolocationService
  ) {
    addIcons({
      flame, ellipse, shieldHalfOutline,
      addCircleOutline, trashOutline, arrowUndoOutline,
      checkmarkCircleOutline, closeCircleOutline,
      leafOutline, exitOutline, informationCircleOutline,
      alertCircleOutline
    });
  }

  ngOnInit() {
    const role = localStorage.getItem('userRole');
    this.isAdmin = (role === 'admin');
    setTimeout(() => { this.initSatelliteMap(); }, 200);
    this.loadZones();
    this.loadBackendData();

    if (this.isAdmin) { this.loadBackendZonesAndRoutes(); }
  }

  /** Límites comunales reales (asset local — zone-service aún no tiene datos reales). */
  private loadZones() {
    this.zonesAssetService.getZones().subscribe({
      next: (zones) => {
        this.zones = zones;
        const main = zones.find(z => z.zoneType === 'MAIN');
        this.mainZoneId = main ? main.id : null;
        if (this.map) { this.renderMapLayers(); }

        if (!this.isAdmin) { this.loadCitizenEvacuationRoute(); }
      },
      error: (err) => console.warn('No se pudieron cargar los límites comunales', err)
    });
  }

  /**
   * Ubica al ciudadano por GPS, determina en qué comuna está y trae del
   * backend (geo-service) la ruta de evacuación real configurada para esa
   * zona. Solo aplica a ciudadanos — el admin gestiona rutas, no las recibe.
   */
  async loadCitizenEvacuationRoute() {
    this.loadingCitizenRoute = true;
    this.citizenLocationError = null;

    try {
      const pos = await this.geolocationService.getCurrentPosition();
      const zona = this.zonesAssetService.findZoneContaining(pos.lat, pos.lng, this.zones);

      if (!zona) {
        this.citizenZone = null;
        this.citizenEvacuationRoutes = [];
        this.citizenLocationError = 'Tu ubicación actual está fuera del área de cobertura (Santiago y comunas cercanas).';
        this.loadingCitizenRoute = false;
        return;
      }

      this.citizenZone = zona;
      this.geoService.getEvacuationRoutesByZone(zona.id).subscribe({
        next: (rutas) => {
          this.citizenEvacuationRoutes = rutas || [];
          this.loadingCitizenRoute = false;
          if (this.map) { this.renderCitizenRoute(); }
        },
        error: (err) => {
          console.warn('No se pudo obtener la ruta de evacuación de la zona', err);
          this.citizenEvacuationRoutes = [];
          this.loadingCitizenRoute = false;
        }
      });
    } catch (err: any) {
      this.citizenLocationError = err?.message || 'No se pudo obtener tu ubicación.';
      this.loadingCitizenRoute = false;
    }
  }

  /** Dibuja en el mapa, resaltada, la ruta de evacuación de la zona del ciudadano. */
  private renderCitizenRoute() {
    if (!this.map) return;

    this.citizenRouteLayers.forEach((layer: L.Layer) => layer.remove());
    this.citizenRouteLayers = [];

    this.citizenEvacuationRoutes.forEach((route) => {
      if (!route.geoJson) return;
      const geo = JSON.parse(route.geoJson);

      const layer = L.geoJSON(geo, {
        style: { color: '#ff6a00', weight: 6, opacity: 0.95, dashArray: '1' }
      }).addTo(this.map!);
      layer.bindPopup(`Tu ruta de evacuación: ${route.name}`);
      this.citizenRouteLayers.push(layer);

      this.map!.fitBounds(layer.getBounds(), { padding: [40, 40] });
    });
  }

  ionViewDidEnter() {
    setTimeout(() => {
      this.initSatelliteMap();
      this.renderMapLayers();
      this.renderCitizenRoute();
    }, 300);
  }

  /* ================================================================
     LOAD DATA FROM BACKEND
     ================================================================ */
  private loadBackendData() {
    this.geoService.getMapData().subscribe({
      next: (data) => {
        this.routes = data.evacuationRoutes || [];

        this.backendLoaded = true;

        if (this.map) {
          this.renderMapLayers();
        }
        this.backendLoaded = true;

        // Map available brigades
        this.brigadasDisponibles = (data.brigades || [])
          .filter(b => b.status === 'AVAILABLE')
          .map(b => ({ id: b.id, nombre: b.name, tiempoMinutos: 10, backendId: b.id }));

        // Map report entries as emergencies
        this.emergencias = (data.reports || [])
          .filter(r => r.reportStatus === 'ACTIVE')
          .map((r, i) => ({
            id: i + 1,
            backendId: r.id,
            externalReportId: r.externalReportId,
            lat: r.latitude,
            lng: r.longitude,
            desc: `Incendio ${r.severity} (${r.externalReportId?.substring(0, 8) || 'N/A'})`,
            brigada: null,
            estado: 'activo',
            severity: r.severity
          }));

        // Reconnect DEPLOYED brigades with their fires (by coordinates)
        this.brigadasOcupadas = [];

        (data.brigades || [])
          .filter(b => b.status === 'DEPLOYED')
          .forEach(brigade => {
            const emergencia = this.emergencias.find(e =>
              Math.abs(e.lat - brigade.latitude) < 0.001 &&
              Math.abs(e.lng - brigade.longitude) < 0.001
            );
            if (emergencia) {
              emergencia.brigada = brigade.name;
              (this.brigadasOcupadas as any[]).push({
                id: brigade.id,
                nombre: brigade.name,
                tiempoMinutos: 10,
                backendId: brigade.id,
                emergenciaId: emergencia.id
              });
            } else if (this.mainZoneId) {
              // Brigade DEPLOYED with no active fire → reset to AVAILABLE
              this.geoService.updateBrigade(brigade.id, {
                name: brigade.name,
                institution: brigade.institution || 'Valle del Sol',
                status: 'AVAILABLE',
                latitude: -33.46,
                longitude: -70.65,
                zoneId: this.mainZoneId
              }).subscribe({
                next: () => {
                  this.brigadasDisponibles.push({
                    id: brigade.id,
                    nombre: brigade.name,
                    tiempoMinutos: 10,
                    backendId: brigade.id
                  });
                },
                error: (err) => console.warn('Could not reset orphaned brigade', err)
              });
            }
          });

        this.renderMarkers();
      },
      error: (err) => {
        console.warn('Could not connect to Geo Service.', err);
        this.showToast('No se pudieron cargar los datos del mapa', 'danger');
      }
    });

    this.reportService.listarReportes().subscribe({
      next: (reportes) => {
        this.historialIncendios = reportes.sort((a, b) =>
          new Date(b.fechaIncidente).getTime() - new Date(a.fechaIncidente).getTime()
        );
      },
      error: (err) => console.warn('Could not load report history', err)
    });
  }

  private async showToast(message: string, color: string = 'primary') {
    try {
      const toast = await this.toastController.create({
        message,
        duration: 2500,
        position: 'bottom',
        color
      });
      await toast.present();
    } catch (e) {
      console.warn('Toast error', e);
    }
  }

  /* ================================================================
     MAP
     ================================================================ */
  initSatelliteMap() {
  if (this.map) { this.renderMapLayers(); this.renderMarkers(); this.renderCitizenRoute(); return; }

  this.map = L.map('globalMap', {
    zoomControl: false,
    attributionControl: false
  }).setView([-33.4600, -70.6500], 12);

  L.control.attribution({ prefix: false }).addTo(this.map);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap · FireWatch',
    maxZoom: 18
  }).addTo(this.map);

  this.map.on('click', (e: L.LeafletMouseEvent) => this.onMapClickForDrawing(e));

  setTimeout(() => {
    this.map?.invalidateSize();

    // Dibuja lo que ya esté cargado (zonas y/o rutas); renderMapLayers() es
    // segura de llamar aunque alguno de los arreglos esté aún vacío.
    this.renderMapLayers();

    this.renderMarkers();
    this.renderCitizenRoute();
  }, 300);
}

  renderMarkers() {
    if (!this.map) return;

    this.marcadoresLeaflet.forEach(pin => pin.remove());
    this.marcadoresLeaflet = [];

    this.emergencias.forEach(fuego => {
      if (fuego.estado === 'finalizado') return;

      let claseElegida: string;
      if (fuego.estado === 'controlado') {
        claseElegida = 'fuego-controlado';
      } else if (fuego.brigada) {
        claseElegida = 'fuego-atendido';
      } else {
        claseElegida = 'fuego-critico';
      }

      const customIcon = L.divIcon({
        className: claseElegida,
        iconSize: [24, 24],
        iconAnchor: [12, 12]
      });

      const pin = L.marker([fuego.lat, fuego.lng], { icon: customIcon }).addTo(this.map!);
      this.marcadoresLeaflet.push(pin);

      pin.on('click', () => {
        this.ngZone.run(() => {
          this.focoSeleccionado = fuego;
        });
      });
    });
  }

  /* ================================================================
     BRIGADES
     ================================================================ */
  async assignBrigade(selectedBrigade: any) {
    if (!this.focoSeleccionado || this.focoSeleccionado.brigada) return;

    // Update status in backend if connected
    if (selectedBrigade.backendId && this.mainZoneId) {
      this.geoService.updateBrigade(selectedBrigade.backendId, {
        name: selectedBrigade.nombre,
        institution: 'Valle del Sol',
        status: 'DEPLOYED',
        latitude: this.focoSeleccionado.lat,
        longitude: this.focoSeleccionado.lng,
        zoneId: this.mainZoneId
      }).subscribe({
        error: (err) => console.warn('Could not update brigade in backend', err)
      });
    }

    this.focoSeleccionado.brigada = selectedBrigade.nombre;
    this.brigadasOcupadas.push({
      ...selectedBrigade,
      emergenciaId: this.focoSeleccionado.id
    });
    this.brigadasDisponibles = this.brigadasDisponibles.filter(
      b => b.id !== selectedBrigade.id
    );
    this.renderMarkers();
    await this.showToast(`${selectedBrigade.nombre} despachada`, 'success');
  }

  async unassignBrigade() {
    if (!this.focoSeleccionado || !this.focoSeleccionado.brigada) return;
    const brigadeName = this.focoSeleccionado.brigada;
    this.returnBrigade(this.focoSeleccionado);
    this.renderMarkers();
    await this.showToast(`${brigadeName} retirada del foco`, 'warning');
  }

  private returnBrigade(emergencia: any) {
    const occupied = this.brigadasOcupadas.find(b => b.emergenciaId === emergencia.id);
    if (occupied) {
      // Update in backend
      if ((occupied as any).backendId && this.mainZoneId) {
        this.geoService.updateBrigade((occupied as any).backendId, {
          name: occupied.nombre,
          institution: 'Valle del Sol',
          status: 'AVAILABLE',
          latitude: -33.46,
          longitude: -70.65,
          zoneId: this.mainZoneId
        }).subscribe({
          next: () => {
            this.geoService.getBrigades().subscribe({
              next: (brigades) => {
                this.brigadasDisponibles = brigades
                  .filter(b => b.status === 'AVAILABLE')
                  .map(b => ({ id: b.id, nombre: b.name, tiempoMinutos: 10, backendId: b.id }));
              }
            });
          },
          error: (err) => console.warn('Could not update brigade in backend', err)
        });
      }

      this.brigadasDisponibles.push({
        id: occupied.id,
        nombre: occupied.nombre,
        tiempoMinutos: occupied.tiempoMinutos
      });
      this.brigadasOcupadas = this.brigadasOcupadas.filter(
        b => b.emergenciaId !== emergencia.id
      );
    }
    emergencia.brigada = null;
  }

  async changeStatus(newStatus: string) {
    if (!this.focoSeleccionado) return;

    if (newStatus === 'finalizado' && this.focoSeleccionado.brigada) {
      this.returnBrigade(this.focoSeleccionado);
    }

    if (newStatus === 'finalizado') {
      const foco = this.focoSeleccionado;

      // Mark INACTIVE in Incident Service (via report.service) → updates dashboard.
      // El marcador se quita del mapa localmente (geo-service no expone borrado de mapped-reports).
      if (foco.externalReportId) {
        this.reportService.actualizarEstado(foco.externalReportId, { estado: 'INACTIVE' }).subscribe({
          next: (updated) => {
            const idx = this.historialIncendios.findIndex(r => r.id === foco.externalReportId);
            if (idx !== -1) this.historialIncendios[idx] = updated;
          },
          error: (err) => console.warn('Could not update status in Report Service', err)
        });
      }
    }

    this.focoSeleccionado.estado = newStatus;
    this.renderMarkers();

    if (newStatus === 'finalizado') {
      this.focoSeleccionado = null;
    }

    await this.showToast(
      `Estado actualizado: ${newStatus}`,
      newStatus === 'controlado' ? 'success' : 'medium'
    );
  }

  async createBrigade() {
    if (!this.nuevaBrigadaNombre.trim()) {
      await this.showToast('Por favor ingresa el nombre de la brigada', 'warning');
      return;
    }

    const nombre = this.nuevaBrigadaNombre.trim();

    if (!this.mainZoneId) {
      await this.showToast('No se pudo determinar la zona principal. Intenta de nuevo.', 'danger');
      return;
    }

    // Attempt to create in backend
    this.geoService.createBrigade({
      name: nombre,
      institution: 'Valle del Sol',
      status: 'AVAILABLE',
      latitude: -33.46,
      longitude: -70.65,
      zoneId: this.mainZoneId
    }).subscribe({
      next: (res) => {
        this.brigadasDisponibles.push({
          id: res.id,
          nombre: res.name,
          tiempoMinutos: 10,
          backendId: res.id
        });
        this.nuevaBrigadaNombre = '';
        this.showToast('Brigada creada', 'success');
      },
      error: (err) => {
        console.error('Error creating brigade in backend', err);
        this.showToast('Error al crear brigada. Verifica tu conexión.', 'danger');
      }
    });
  }

  async deleteBrigade(brigada: any) {
    // Attempt to delete in backend
    if (brigada.backendId) {
      this.geoService.deleteBrigade(brigada.backendId).subscribe({
        error: (err) => console.warn('Could not delete brigade in backend', err)
      });
    }

    this.brigadasDisponibles = this.brigadasDisponibles.filter(
      b => b.id !== brigada.id
    );
    await this.showToast(`${brigada.nombre} eliminada`, 'warning');
  }

  async deleteFromHistory(reporte: ReporteResponse) {
    this.reportService.eliminarReporte(reporte.id).subscribe({
      next: async () => {
        this.historialIncendios = this.historialIncendios.filter(r => r.id !== reporte.id);
        await this.showToast('Reporte eliminado del historial', 'warning');
      },
      error: async () => this.showToast('Error al eliminar el reporte', 'danger')
    });
  }

  getEmergencyDesc(emergenciaId: number): string {
    const em = this.emergencias.find(e => e.id === emergenciaId);
    return em ? em.desc : 'Emergencia desconocida';
  }

  async deleteEmergency(emergencia: any) {
    // geo-service no expone borrado de mapped-reports; se retira solo de la vista local.
    // Return brigade if one was assigned
    if (emergencia.brigada) {
      this.returnBrigade(emergencia);
    }

    // Clear selection if it is the current hotspot
    if (this.focoSeleccionado?.id === emergencia.id) {
      this.focoSeleccionado = null;
    }

    this.emergencias = this.emergencias.filter(e => e.id !== emergencia.id);
    this.renderMarkers();
    await this.showToast('Emergencia eliminada del historial', 'warning');
  }

  /* ================================================================
     GESTIÓN DE ZONAS Y RUTAS (admin) — dibujadas a mano sobre el mapa
     ================================================================ */
  private loadBackendZonesAndRoutes() {
    this.geoService.getZones().subscribe({
      next: (zones) => { this.backendZones = zones; },
      error: (err) => console.warn('No se pudieron cargar las zonas del backend', err)
    });
    this.geoService.getEvacuationRoutes().subscribe({
      next: (routes) => { this.backendRoutes = routes; },
      error: (err) => console.warn('No se pudieron cargar las rutas del backend', err)
    });
  }

  get backendOperationalZones(): ZoneResponse[] {
    return this.backendZones.filter(z => z.zoneType === 'OPERATIONAL');
  }

  /** Comunas predefinidas seleccionables como contexto para dibujar una zona (todas salvo la región). */
  get operationalComunas(): ComunaZone[] {
    return this.zones.filter(z => z.zoneType !== 'PROVINCE');
  }

  get hasMainZone(): boolean {
    return this.backendZones.some(z => z.zoneType === 'MAIN');
  }

  async startDrawZone() {
    if (!this.hasMainZone) {
      const ok = await this.ensureRegionMainZone();
      if (!ok) return;
    }
    this.drawingMode = 'zone';
    this.selectedComunaContext = null;
    this.drawingPoints = [];
    this.showZoneForm = false;
    this.clearAdminDrawing();
  }

  /**
   * Región (predefinida, p.ej. "Provincia de Santiago") — se crea una sola vez
   * como zona principal (MAIN) real en el backend, para que las zonas
   * operativas que dibuje el admin puedan validarse contra ella.
   */
  private async ensureRegionMainZone(): Promise<boolean> {
    const provincia = this.zones.find(z => z.zoneType === 'PROVINCE');
    if (!provincia) {
      await this.showToast('No se encontró la región predefinida.', 'danger');
      return false;
    }
    return new Promise<boolean>((resolve) => {
      this.geoService.createZone({
        name: provincia.name,
        description: 'Región principal (predefinida)',
        isActive: true,
        color: provincia.color && /^#([A-Fa-f0-9]{6})$/.test(provincia.color) ? provincia.color : '#3388ff',
        zoneType: 'MAIN',
        geoJson: this.simplifyGeometryForBackend(provincia.geometry)
      }).subscribe({
        next: async () => {
          await this.showToast(`Región "${provincia.name}" establecida como zona principal`, 'success');
          this.loadBackendZonesAndRoutes();
          resolve(true);
        },
        error: async (err) => {
          const msg = err.error?.message || 'No se pudo establecer la región principal.';
          await this.showToast(msg, 'danger');
          resolve(false);
        }
      });
    });
  }

  /** Comuna predefinida elegida como contexto: solo centra el mapa, no crea nada por sí sola. */
  onComunaContextSelected() {
    if (!this.selectedComunaContext || !this.map) return;
    const layer = L.geoJSON(this.selectedComunaContext.geometry as any);
    this.map.fitBounds(layer.getBounds(), { padding: [30, 30] });
  }

  async startDrawRoute() {
    if (this.backendOperationalZones.length === 0) {
      await this.showToast('Primero debes crear una zona operativa.', 'warning');
      return;
    }
    this.drawingMode = 'route';
    this.showRouteForm = true;
  }

  cancelDrawing() {
    this.drawingMode = 'none';
    this.selectedComunaContext = null;
    this.drawingPoints = [];
    this.showZoneForm = false;
    this.showRouteForm = false;
    this.newZoneName = '';
    this.newZoneDescription = '';
    this.newZoneColor = '#3388ff';
    this.newRouteName = '';
    this.newRouteDescription = '';
    this.selectedRouteZoneId = '';
    this.routeTipo = 'ESTRUCTURAL';
    this.routeSeveridad = 'BAJA';
    this.clearAdminDrawing();
  }

  private clearAdminDrawing() {
    if (this.adminDrawingLayer) { this.adminDrawingLayer.remove(); this.adminDrawingLayer = null; }
    this.adminDrawingMarkers.forEach(m => m.remove());
    this.adminDrawingMarkers = [];
  }

  /** Dibujo de la zona (clic a clic), solo activo una vez elegida la comuna de contexto. */
  private onMapClickForDrawing(e: L.LeafletMouseEvent) {
    if (this.drawingMode !== 'zone' || !this.selectedComunaContext || this.showZoneForm) return;

    this.drawingPoints.push(e.latlng);
    const marker = L.circleMarker(e.latlng, {
      radius: 5, color: '#ffffff', weight: 2, fillColor: '#ff6a00', fillOpacity: 1
    }).addTo(this.map!);
    this.adminDrawingMarkers.push(marker);
    this.redrawAdminShape();
  }

  private redrawAdminShape() {
    if (this.adminDrawingLayer) { this.adminDrawingLayer.remove(); this.adminDrawingLayer = null; }
    if (this.drawingPoints.length < 2) return;
    this.adminDrawingLayer = L.polygon(this.drawingPoints, { color: '#ff6a00', weight: 3, fillOpacity: 0.2 }).addTo(this.map!);
  }

  async finishDrawingZone() {
    if (this.drawingPoints.length < 3) {
      await this.showToast('Necesitas al menos 3 puntos para dibujar la zona.', 'warning');
      return;
    }
    this.showZoneForm = true;
  }

  private pointsToPolygonGeoJson(): string {
    const ring = this.drawingPoints.map(p => [p.lng, p.lat]);
    ring.push(ring[0]);
    return JSON.stringify({ type: 'Polygon', coordinates: [ring] });
  }

  async saveZone() {
    if (this.drawingPoints.length < 3) {
      await this.showToast('Dibuja la zona en el mapa primero.', 'warning'); return;
    }
    if (!this.newZoneName || this.newZoneName.trim().length < 4) {
      await this.showToast('El nombre debe tener al menos 4 caracteres.', 'warning'); return;
    }
    if (!this.newZoneDescription || this.newZoneDescription.trim().length < 1) {
      await this.showToast('Agrega una descripción para la zona.', 'warning'); return;
    }
    if (!/^#([A-Fa-f0-9]{6})$/.test(this.newZoneColor)) {
      await this.showToast('El color debe tener formato #RRGGBB.', 'warning'); return;
    }

    this.geoService.createZone({
      name: this.newZoneName.trim(),
      description: this.newZoneDescription.trim(),
      isActive: true,
      color: this.newZoneColor,
      zoneType: 'OPERATIONAL',
      geoJson: this.pointsToPolygonGeoJson()
    }).subscribe({
      next: async () => {
        await this.showToast('Zona creada correctamente', 'success');
        this.cancelDrawing();
        this.loadBackendZonesAndRoutes();
      },
      error: async (err) => {
        const msg = err.error?.message || 'No se pudo crear la zona. Verifica que el polígono esté dentro de la región principal.';
        await this.showToast(msg, 'danger');
      }
    });
  }

  /**
   * El backend (ZoneRequestDTO.geoJson) limita el campo a 10000 caracteres, pero
   * los límites comunales reales (OpenStreetMap) traen cientos de vértices con
   * 7 decimales de precisión y superan ese límite ampliamente. Se redondea a 5
   * decimales (~1m de precisión, de sobra para este uso) y, si aun así no entra,
   * se reduce el número de vértices manteniendo la forma general del polígono.
   */
  private simplifyGeometryForBackend(geometry: GeoJSON.Geometry, maxChars = 9500): string {
    if (geometry.type !== 'Polygon' && geometry.type !== 'MultiPolygon') {
      return JSON.stringify(geometry);
    }

    const round = (n: number) => Math.round(n * 1e5) / 1e5;

    const decimateRing = (ring: number[][], step: number): number[][] => {
      const kept = step <= 1
        ? ring.slice()
        : ring.filter((_, i) => i % step === 0 || i === ring.length - 1);
      const rounded = kept.map(([lng, lat]) => [round(lng), round(lat)]);
      const first = rounded[0], last = rounded[rounded.length - 1];
      if (first[0] !== last[0] || first[1] !== last[1]) rounded.push([first[0], first[1]]);
      return rounded;
    };

    const allRings: number[][][] = geometry.type === 'Polygon'
      ? (geometry as any).coordinates
      : (geometry as any).coordinates.flat();
    const minRingLength = Math.min(...allRings.map(r => r.length));
    const maxStep = Math.max(1, Math.floor((minRingLength - 4) / 4));

    let result: any = geometry;
    for (let step = 1; step <= maxStep; step++) {
      result = geometry.type === 'Polygon'
        ? { type: 'Polygon', coordinates: (geometry as any).coordinates.map((r: number[][]) => decimateRing(r, step)) }
        : { type: 'MultiPolygon', coordinates: (geometry as any).coordinates.map((poly: number[][][]) => poly.map(r => decimateRing(r, step))) };

      if (JSON.stringify(result).length <= maxChars) break;
    }

    return JSON.stringify(result);
  }

  /* ----------  Ruta de evacuación automática (centro de la zona → borde más cercano)  ---------- */
  /**
   * El backend exige que la ruta quede completamente dentro de la zona, así que la
   * "ruta automática" es el segmento más corto desde el centro de la zona hasta su
   * borde — no se usa una distancia en metros porque saldría de la zona y el
   * backend la rechazaría. Tipo/severidad solo quedan en la descripción, como
   * contexto para la brigada.
   */
  private computeAutoRouteGeoJson(geoJson: string): string | null {
    let geometry: any;
    try { geometry = JSON.parse(geoJson); } catch { return null; }

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

    let best: number[] = ring[0];
    let bestDist = Infinity;
    for (let i = 0; i < ring.length - 1; i++) {
      const proj = this.closestPointOnSegment([cx, cy], ring[i], ring[i + 1]);
      const d = Math.hypot(proj[0] - cx, proj[1] - cy);
      if (d < bestDist) { bestDist = d; best = proj; }
    }

    return JSON.stringify({ type: 'LineString', coordinates: [[cx, cy], best] });
  }

  private closestPointOnSegment(p: number[], a: number[], b: number[]): number[] {
    const dx = b[0] - a[0], dy = b[1] - a[1];
    const lenSq = dx * dx + dy * dy;
    if (lenSq === 0) return a;
    let t = ((p[0] - a[0]) * dx + (p[1] - a[1]) * dy) / lenSq;
    t = Math.max(0, Math.min(1, t));
    return [a[0] + t * dx, a[1] + t * dy];
  }

  async saveRoute() {
    if (!this.selectedRouteZoneId) {
      await this.showToast('Selecciona la zona operativa a la que pertenece la ruta.', 'warning'); return;
    }

    const zona = this.backendZones.find(z => z.id === this.selectedRouteZoneId);
    if (!zona) {
      await this.showToast('La zona seleccionada ya no existe.', 'danger'); return;
    }

    const geoJson = this.computeAutoRouteGeoJson(zona.geoJson);
    if (!geoJson) {
      await this.showToast('No se pudo calcular automáticamente la ruta para esta zona.', 'danger'); return;
    }

    const tipoLabel = { ESTRUCTURAL: 'estructural', FORESTAL: 'forestal', URBANO: 'urbano' }[this.routeTipo];
    const severidadLabel = { BAJA: 'baja', MEDIA: 'media', ALTA: 'alta' }[this.routeSeveridad];
    const name = this.newRouteName.trim() || `Ruta de evacuación — ${zona.name}`;
    const description = this.newRouteDescription.trim() ||
      `Ruta de evacuación generada automáticamente para incidente ${tipoLabel} de severidad ${severidadLabel} en la zona ${zona.name}.`;

    this.geoService.createEvacuationRoute({
      name,
      description,
      geoJson,
      zoneId: this.selectedRouteZoneId
    }).subscribe({
      next: async () => {
        await this.showToast('Ruta de evacuación generada correctamente', 'success');
        this.cancelDrawing();
        this.loadBackendZonesAndRoutes();
      },
      error: async (err) => {
        const msg = err.error?.message || 'No se pudo crear la ruta.';
        await this.showToast(msg, 'danger');
      }
    });
  }

  async deleteBackendZone(zone: ZoneResponse) {
    this.geoService.deleteZone(zone.id).subscribe({
      next: async () => {
        await this.showToast('Zona eliminada', 'warning');
        this.loadBackendZonesAndRoutes();
      },
      error: async () => this.showToast('No se pudo eliminar la zona.', 'danger')
    });
  }

  async deleteBackendRoute(route: EvacuationResponse) {
    this.geoService.deleteEvacuationRoute(route.id).subscribe({
      next: async () => {
        await this.showToast('Ruta eliminada', 'warning');
        this.loadBackendZonesAndRoutes();
      },
      error: async () => this.showToast('No se pudo eliminar la ruta.', 'danger')
    });
  }

  private renderMapLayers() {
  if (!this.map) return;

  this.zoneLayers.forEach((layer: L.Layer) => layer.remove());
  this.routeLayers.forEach((layer: L.Layer) => layer.remove());

  this.zoneLayers = [];
  this.routeLayers = [];

  this.zones.forEach((zone) => {
    if (!zone.geometry) return;

    // Provincia de Santiago: solo contorno de referencia, sin relleno
    const esProvincia = zone.zoneType === 'PROVINCE';
    const esPrincipal = zone.zoneType === 'MAIN';

    const layer = L.geoJSON(zone.geometry, {
      style: {
        color: zone.color || '#3388ff',
        weight: esProvincia ? 2 : (esPrincipal ? 4 : 2),
        dashArray: esProvincia ? '6 4' : undefined,
        fillOpacity: esProvincia ? 0 : (esPrincipal ? 0.06 : 0.22)
      }
    }).addTo(this.map!);

    layer.bindPopup(`${zone.name} (${zone.zoneType})`);
    this.zoneLayers.push(layer);

    if (esPrincipal) {
      this.map!.fitBounds(layer.getBounds(), {
        padding: [20, 20],
        animate: false
      });
    }
  });

  this.routes.forEach((route: any) => {
    if (!route.geoJson) return;

    const geo = JSON.parse(route.geoJson);

    const layer = L.geoJSON(geo, {
      style: {
        color: '#0066ff',
        weight: 5,
        opacity: 0.9
      }
    }).addTo(this.map!);

    layer.bindPopup(route.name);
    this.routeLayers.push(layer);

    if (geo.type === 'LineString' && geo.coordinates.length >= 2) {
      const start = geo.coordinates[0];
      const end = geo.coordinates[geo.coordinates.length - 1];

      const startMarker = L.circleMarker([start[1], start[0]], {
        radius: 7,
        color: '#00aa55',
        fillColor: '#00ff88',
        fillOpacity: 1,
        weight: 2
      }).addTo(this.map!);

      const endMarker = L.circleMarker([end[1], end[0]], {
        radius: 8,
        color: '#cc0000',
        fillColor: '#ff3333',
        fillOpacity: 1,
        weight: 2
      }).addTo(this.map!);

      this.routeLayers.push(startMarker);
      this.routeLayers.push(endMarker);
    }
  });
}
  
}

