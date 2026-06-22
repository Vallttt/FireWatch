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
  ZoneResponse, ZoneType
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
  public isAdmin = false;
  public focoSeleccionado: any = null;
  public zones: ComunaZone[] = [];
  public zoneLayers: L.Layer[] = [];

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
  public drawingPoints: L.LatLng[] = [];
  private adminDrawingLayer: L.Layer | null = null;
  private adminDrawingMarkers: L.CircleMarker[] = [];

  public showZoneForm = false;
  public newZoneName = '';
  public newZoneDescription = '';
  public newZoneType: ZoneType = 'OPERATIONAL';
  public newZoneColor = '#3388ff';

  public showRouteForm = false;
  public newRouteName = '';
  public newRouteDescription = '';
  public selectedRouteZoneId = '';

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

  async startDrawZone() {
    this.drawingMode = 'zone';
    this.drawingPoints = [];
    this.showZoneForm = false;
    this.clearAdminDrawing();
    await this.showToast('Haz clic en el mapa para marcar los vértices de la zona (mínimo 3).', 'primary');
  }

  async startDrawRoute() {
    if (this.backendOperationalZones.length === 0) {
      await this.showToast('Primero debes crear una zona operativa.', 'warning');
      return;
    }
    this.drawingMode = 'route';
    this.drawingPoints = [];
    this.showRouteForm = false;
    this.clearAdminDrawing();
    await this.showToast('Haz clic en el mapa para marcar el recorrido de la ruta (mínimo 2 puntos).', 'primary');
  }

  cancelDrawing() {
    this.drawingMode = 'none';
    this.drawingPoints = [];
    this.showZoneForm = false;
    this.showRouteForm = false;
    this.newZoneName = '';
    this.newZoneDescription = '';
    this.newZoneType = 'OPERATIONAL';
    this.newZoneColor = '#3388ff';
    this.newRouteName = '';
    this.newRouteDescription = '';
    this.selectedRouteZoneId = '';
    this.clearAdminDrawing();
  }

  private clearAdminDrawing() {
    if (this.adminDrawingLayer) { this.adminDrawingLayer.remove(); this.adminDrawingLayer = null; }
    this.adminDrawingMarkers.forEach(m => m.remove());
    this.adminDrawingMarkers = [];
  }

  private onMapClickForDrawing(e: L.LeafletMouseEvent) {
    if (this.drawingMode === 'none' || this.showZoneForm || this.showRouteForm) return;

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

    this.adminDrawingLayer = this.drawingMode === 'zone'
      ? L.polygon(this.drawingPoints, { color: '#ff6a00', weight: 3, fillOpacity: 0.2 }).addTo(this.map!)
      : L.polyline(this.drawingPoints, { color: '#ff6a00', weight: 4 }).addTo(this.map!);
  }

  async finishDrawingZone() {
    if (this.drawingPoints.length < 3) {
      await this.showToast('Necesitas al menos 3 puntos para dibujar una zona.', 'warning');
      return;
    }
    this.showZoneForm = true;
  }

  async finishDrawingRoute() {
    if (this.drawingPoints.length < 2) {
      await this.showToast('Necesitas al menos 2 puntos para dibujar una ruta.', 'warning');
      return;
    }
    this.showRouteForm = true;
  }

  private pointsToPolygonGeoJson(): string {
    const ring = this.drawingPoints.map(p => [p.lng, p.lat]);
    ring.push(ring[0]);
    return JSON.stringify({ type: 'Polygon', coordinates: [ring] });
  }

  private pointsToLineGeoJson(): string {
    const coords = this.drawingPoints.map(p => [p.lng, p.lat]);
    return JSON.stringify({ type: 'LineString', coordinates: coords });
  }

  async saveZone() {
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
      zoneType: this.newZoneType,
      geoJson: this.pointsToPolygonGeoJson()
    }).subscribe({
      next: async () => {
        await this.showToast('Zona creada correctamente', 'success');
        this.cancelDrawing();
        this.loadBackendZonesAndRoutes();
      },
      error: async (err) => {
        const msg = err.error?.message || 'No se pudo crear la zona. Verifica que el polígono esté dentro de la zona principal (si es operativa).';
        await this.showToast(msg, 'danger');
      }
    });
  }

  async saveRoute() {
    if (!this.selectedRouteZoneId) {
      await this.showToast('Selecciona la zona operativa a la que pertenece la ruta.', 'warning'); return;
    }
    if (!this.newRouteName || this.newRouteName.trim().length < 4) {
      await this.showToast('El nombre debe tener al menos 4 caracteres.', 'warning'); return;
    }
    if (!this.newRouteDescription || this.newRouteDescription.trim().length < 20) {
      await this.showToast('La descripción debe tener al menos 20 caracteres.', 'warning'); return;
    }

    this.geoService.createEvacuationRoute({
      name: this.newRouteName.trim(),
      description: this.newRouteDescription.trim(),
      geoJson: this.pointsToLineGeoJson(),
      zoneId: this.selectedRouteZoneId
    }).subscribe({
      next: async () => {
        await this.showToast('Ruta de evacuación creada correctamente', 'success');
        this.cancelDrawing();
        this.loadBackendZonesAndRoutes();
      },
      error: async (err) => {
        const msg = err.error?.message || 'No se pudo crear la ruta. Verifica que esté dentro de la zona seleccionada.';
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
    this.zoneLayers = [];

    this.zones.forEach((zone) => {
      if (!zone.geometry) return;

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
        this.map!.fitBounds(layer.getBounds(), { padding: [20, 20], animate: false });
      }
    });
  }

  private loadBackendData() {
    this.geoService.getMapData().subscribe({
      next: (data) => {
        this.backendLoaded = true;

        const main = (data.zones || []).find((z: any) => z.zoneType === 'MAIN');
        this.mainZoneId = main ? main.id : null;

        this.brigadasDisponibles = (data.brigades || [])
          .filter(b => b.status === 'AVAILABLE')
          .map(b => ({ id: b.id, nombre: b.name, tiempoMinutos: 10, backendId: b.id }));

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

    if (this.zones.length > 0) { this.renderMapLayers(); }
    this.renderMarkers();
    this.renderCitizenRoute();
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
        this.ngZone.run(() => { this.focoSeleccionado = fuego; });
      });
    });
  }

  async assignBrigade(selectedBrigade: any) {
    if (!this.focoSeleccionado || this.focoSeleccionado.brigada) return;

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
    this.brigadasOcupadas.push({ ...selectedBrigade, emergenciaId: this.focoSeleccionado.id });
    this.brigadasDisponibles = this.brigadasDisponibles.filter(b => b.id !== selectedBrigade.id);
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
      this.brigadasOcupadas = this.brigadasOcupadas.filter(b => b.emergenciaId !== emergencia.id);
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

      // geo-service no expone borrado de mapped-reports; el marcador se
      // retira solo de la vista local (ver más abajo).
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
    if (brigada.backendId) {
      this.geoService.deleteBrigade(brigada.backendId).subscribe({
        error: (err) => console.warn('Could not delete brigade in backend', err)
      });
    }

    this.brigadasDisponibles = this.brigadasDisponibles.filter(b => b.id !== brigada.id);
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
    if (emergencia.brigada) { this.returnBrigade(emergencia); }

    if (this.focoSeleccionado?.id === emergencia.id) { this.focoSeleccionado = null; }

    this.emergencias = this.emergencias.filter(e => e.id !== emergencia.id);
    this.renderMarkers();
    await this.showToast('Emergencia eliminada del historial', 'warning');
  }
}
