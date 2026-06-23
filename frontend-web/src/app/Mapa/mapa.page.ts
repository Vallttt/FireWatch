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
  private mapResizeObserver: ResizeObserver | null = null;
  public isAdmin = false;
  public focoSeleccionado: any = null;

  public brigadasDisponibles: any[] = [];
  public brigadasOcupadas: { id: string; nombre: string; tiempoMinutos: number; emergenciaId: number }[] = [];
  public nuevaBrigadaNombre = '';
  /** Zona real a la que se asigna la brigada que se está creando. */
  public newBrigadaZoneId = '';

  public emergencias: any[] = [];
  public historialIncendios: ReporteResponse[] = [];
  private mainZoneId: string | null = null;

  /** Ruta de evacuación ligada al reporte que el propio ciudadano registró (no a su GPS). */
  public miReporteActivo: ReporteResponse | null = null;
  public miZonaNombre: string | null = null;
  public citizenEvacuationRoutes: EvacuationResponse[] = [];
  public loadingMyRoute = false;
  private citizenRouteLayers: L.Layer[] = [];

  /* ----------  GESTIÓN DE ZONAS Y RUTAS (admin)  ---------- */
  public backendZones: ZoneResponse[] = [];
  public backendRoutes: EvacuationResponse[] = [];

  public drawingMode: 'none' | 'zone' = 'none';
  /** Región predefinida (límite real) elegida para establecer la zona principal. */
  public selectedRegionContext: ComunaZone | null = null;
  /** Comuna predefinida (límite real) dentro de la cual el admin dibuja la zona. */
  public selectedComunaContext: ComunaZone | null = null;
  public drawingPoints: L.LatLng[] = [];
  private adminDrawingLayer: L.Layer | null = null;
  private adminDrawingMarkers: L.CircleMarker[] = [];

  public showZoneForm = false;
  public newZoneName = '';
  public newZoneDescription = '';
  public newZoneColor = '#3388ff';

  /** Paleta para asignar a cada zona nueva un color distinto y diferenciable en el mapa. */
  private readonly zonePalette = [
    '#dc2626', '#2563eb', '#16a34a', '#d97706', '#7c3aed',
    '#0891b2', '#db2777', '#65a30d', '#ea580c', '#4f46e5'
  ];

  private marcadoresLeaflet: L.Marker[] = [];
  private backendLoaded = false;

  constructor(
    private ngZone: NgZone,
    private toastController: ToastController,
    private geoService: GeoService,
    private reportService: ReportService,
    private zonesAssetService: ZonesAssetService
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
    else { this.loadMyEvacuationRoute(); }
  }

  /** Límites comunales reales (asset local — zone-service aún no tiene datos reales). */
  private loadZones() {
    this.zonesAssetService.getZones().subscribe({
      next: (zones) => {
        this.zones = zones;
        // Placeholder de zoneId para brigadas (no son zonas reales en este MVP) — se usa la región como referencia.
        const region = zones.find(z => z.zoneType === 'PROVINCE');
        this.mainZoneId = region ? region.id : null;
        if (this.map) { this.renderMapLayers(); }
      },
      error: (err) => console.warn('No se pudieron cargar los límites comunales', err)
    });
  }

  /**
   * La ruta de evacuación del ciudadano es PERSONAL del REPORTE que él mismo
   * registró (no compartida con otros reportes de la misma zona): se busca
   * su reporte más reciente y se trae la ruta generada específicamente para
   * ese reporte. Si el ciudadano no ha reportado nada, no se muestra
   * ninguna ruta — solo verá los incendios reportados en el mapa.
   */
  loadMyEvacuationRoute() {
    const userId = localStorage.getItem('userId');
    if (!userId) {
      this.miReporteActivo = null;
      this.miZonaNombre = null;
      this.citizenEvacuationRoutes = [];
      return;
    }

    this.loadingMyRoute = true;
    this.reportService.listarReportes().subscribe({
      next: (reportes) => {
        const misReportes = reportes
          .filter(r => r.usuarioId === userId)
          .sort((a, b) => new Date(b.fechaIncidente).getTime() - new Date(a.fechaIncidente).getTime());

        this.miReporteActivo = misReportes[0] || null;

        if (!this.miReporteActivo) {
          this.miZonaNombre = null;
          this.citizenEvacuationRoutes = [];
          this.loadingMyRoute = false;
          if (this.map) { this.renderCitizenRoute(); }
          return;
        }

        const miReporte = this.miReporteActivo;

        // Nombre de la zona, directo desde el zoneId del reporte (ya es una zona real).
        this.geoService.getZones().subscribe({
          next: (zones) => {
            const zona = zones.find(z => z.id === miReporte.zoneId);
            this.miZonaNombre = zona ? zona.name : null;
          },
          error: () => { this.miZonaNombre = null; }
        });

        this.geoService.getEvacuationRoutesByReport(miReporte.id).subscribe({
          next: (rutas) => {
            this.citizenEvacuationRoutes = rutas || [];
            this.loadingMyRoute = false;
            if (this.map) { this.renderCitizenRoute(); }
          },
          error: (err) => {
            console.warn('No se pudo obtener la ruta de evacuación del reporte', err);
            this.citizenEvacuationRoutes = [];
            this.loadingMyRoute = false;
          }
        });
      },
      error: (err) => {
        console.warn('No se pudieron cargar tus reportes', err);
        this.loadingMyRoute = false;
      }
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

  /** Dibuja en el mapa, resaltada, la ruta de evacuación de la zona del ciudadano. */
  private renderCitizenRoute() {
    if (!this.map) return;

    this.citizenRouteLayers.forEach((layer: L.Layer) => layer.remove());
    this.citizenRouteLayers = [];

    this.citizenEvacuationRoutes.forEach((route) => {
      if (!route.geoJson) return;
      const geo = JSON.parse(route.geoJson);

      const layer = L.geoJSON(geo, {
        style: { color: '#ff6a00', weight: 5, opacity: 0.95 }
      }).addTo(this.map!);
      layer.bindPopup(`Tu ruta de evacuación: ${route.name}`);
      this.citizenRouteLayers.push(layer);

      // Marca claramente dónde empieza la ruta (el incendio reportado) y dónde
      // termina (la zona/punto seguro), para que no sea solo una línea sin contexto.
      if (geo.type === 'LineString' && geo.coordinates.length >= 2) {
        const start = geo.coordinates[0];
        const end = geo.coordinates[geo.coordinates.length - 1];

        const startMarker = L.circleMarker([start[1], start[0]], {
          radius: 8, color: '#cc0000', fillColor: '#ff3333', fillOpacity: 1, weight: 2
        }).addTo(this.map!).bindPopup('Inicio: tu incendio reportado');

        const safeIcon = L.divIcon({
          className: 'safe-zone-marker',
          html: '🚩',
          iconSize: [28, 28],
          iconAnchor: [10, 26]
        });
        const endMarker = L.marker([end[1], end[0]], { icon: safeIcon })
          .addTo(this.map!).bindPopup('Zona segura');

        this.citizenRouteLayers.push(startMarker, endMarker);
      }

      this.map!.fitBounds(layer.getBounds(), { padding: [40, 40] });
    });
  }

  /**
   * Cada vez que se vuelve a esta página (p.ej. tras registrar un reporte en
   * la pestaña Reportar) se refrescan incendios, zonas y la ruta personal del
   * ciudadano — de lo contrario quedarían con los datos de la primera carga.
   */
  ionViewDidEnter() {
    setTimeout(() => {
      this.initSatelliteMap();
      this.loadBackendData();
      if (this.isAdmin) { this.loadBackendZonesAndRoutes(); }
      else { this.loadMyEvacuationRoute(); }
    }, 300);
  }

  /* ================================================================
    LOAD DATA FROM BACKEND
     ================================================================ */
  private loadBackendData() {
    this.geoService.getMapData().subscribe({
      next: (data) => {
        this.backendLoaded = true;

        // Map available brigades
        this.brigadasDisponibles = (data.brigades || [])
          .filter(b => b.status === 'AVAILABLE')
          .map(b => ({ id: b.id, nombre: b.name, tiempoMinutos: 10, backendId: b.id, zoneId: b.zoneId }));

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
                zoneId: brigade.zoneId,
                emergenciaId: emergencia.id
              });
            } else {
              // Brigade DEPLOYED with no active fire → reset to AVAILABLE
              this.geoService.updateBrigade(brigade.id, {
                name: brigade.name,
                institution: brigade.institution || 'Valle del Sol',
                status: 'AVAILABLE',
                latitude: -33.46,
                longitude: -70.65,
                zoneId: brigade.zoneId
              }).subscribe({
                next: () => {
                  this.brigadasDisponibles.push({
                    id: brigade.id,
                    nombre: brigade.name,
                    tiempoMinutos: 10,
                    backendId: brigade.id,
                    zoneId: brigade.zoneId
                  });
                  this.autoAssignBrigades();
                },
                error: (err) => console.warn('Could not reset orphaned brigade', err)
              });
            }
          });

        this.renderMarkers();
        this.autoAssignBrigades();
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

  // El contenedor Ionic puede cambiar de tamaño (rotación, contenido que se agrega
  // tras cargar el backend); invalidateSize() mantiene a Leaflet sincronizado con
  // el tamaño real para que los tiles se recalculen bien.
  const mapContainer = document.getElementById('globalMap');
  if (mapContainer) {
    this.mapResizeObserver = new ResizeObserver(() => this.map?.invalidateSize());
    this.mapResizeObserver.observe(mapContainer);
  }

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
    this.dispatchBrigade(selectedBrigade, this.focoSeleccionado);
    await this.showToast(`${selectedBrigade.nombre} despachada`, 'success');
  }

  /**
   * Despacha una brigada hacia un foco: marca DEPLOYED en el backend
   * (conservando la zona real de la brigada, no la región completa) y mueve
   * la brigada de "disponibles" a "ocupadas" localmente. La usan tanto el
   * despacho manual (assignBrigade) como la auto-asignación por zona.
   */
  private dispatchBrigade(brigada: any, fuego: any) {
    if (brigada.backendId) {
      this.geoService.updateBrigade(brigada.backendId, {
        name: brigada.nombre,
        institution: 'Valle del Sol',
        status: 'DEPLOYED',
        latitude: fuego.lat,
        longitude: fuego.lng,
        zoneId: brigada.zoneId || this.mainZoneId
      }).subscribe({
        error: (err) => console.warn('Could not update brigade in backend', err)
      });
    }

    fuego.brigada = brigada.nombre;
    this.brigadasOcupadas.push({ ...brigada, emergenciaId: fuego.id });
    this.brigadasDisponibles = this.brigadasDisponibles.filter(b => b.id !== brigada.id);
    this.renderMarkers();
  }

  /**
   * Despacho automático: una brigada disponible cuya zona contiene un foco
   * activo sin brigada asignada se despacha sola (prioriza una zona
   * operativa específica sobre la zona principal, que cubre toda la región).
   */
  private autoAssignBrigades() {
    if (!this.isAdmin || this.backendZones.length === 0) return;

    this.emergencias.forEach((fuego) => {
      if (fuego.brigada || fuego.estado === 'finalizado') return;

      const zonaDe = (brigada: any) => this.backendZones.find(z => z.id === brigada.zoneId);

      const candidata =
        this.brigadasDisponibles.find(b => {
          const zona = zonaDe(b);
          return zona?.zoneType === 'OPERATIONAL' && this.pointInZoneGeoJson(fuego.lng, fuego.lat, zona.geoJson);
        }) ||
        this.brigadasDisponibles.find(b => {
          const zona = zonaDe(b);
          return zona?.zoneType === 'MAIN' && this.pointInZoneGeoJson(fuego.lng, fuego.lat, zona.geoJson);
        });

      if (candidata) {
        this.dispatchBrigade(candidata, fuego);
        this.showToast(`${candidata.nombre} despachada automáticamente a un incendio en su zona`, 'success');
      }
    });
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
      const zoneId = (occupied as any).zoneId || this.mainZoneId;

      // Update in backend (conserva la zona real de la brigada, no la región completa)
      if ((occupied as any).backendId && zoneId) {
        this.geoService.updateBrigade((occupied as any).backendId, {
          name: occupied.nombre,
          institution: 'Valle del Sol',
          status: 'AVAILABLE',
          latitude: -33.46,
          longitude: -70.65,
          zoneId
        }).subscribe({
          error: (err) => console.warn('Could not update brigade in backend', err)
        });
      }

      this.brigadasDisponibles.push({
        id: occupied.id,
        nombre: occupied.nombre,
        tiempoMinutos: occupied.tiempoMinutos,
        backendId: (occupied as any).backendId,
        zoneId
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
      if (foco.externalReportId) {
        this.reportService.actualizarEstado(foco.externalReportId, { estado: 'INACTIVE' }).subscribe({
          next: (updated) => {
            const idx = this.historialIncendios.findIndex(r => r.id === foco.externalReportId);
            if (idx !== -1) this.historialIncendios[idx] = updated;
          },
          error: (err) => console.warn('Could not update status in Report Service', err)
        });
      }

      // Borrado lógico en geo-service: sin esto, el incendio queda con
      // reportStatus ACTIVE para siempre ahí, y vuelve a aparecer en el mapa
      // cada vez que se recargan los datos (p.ej. al volver a esta página).
      if (foco.backendId) {
        this.geoService.deleteMappedReport(foco.backendId).subscribe({
          error: (err) => console.warn('No se pudo quitar el incendio del mapa en el backend', err)
        });
      }

      // La ruta de evacuación es personal de este reporte — al finalizar el
      // incendio, deja de tener sentido y se elimina también.
      if (foco.externalReportId) {
        this.geoService.deleteEvacuationRoutesByReport(foco.externalReportId).subscribe({
          error: (err) => console.warn('No se pudo eliminar la ruta del reporte', err)
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

    if (!this.newBrigadaZoneId) {
      await this.showToast('Crea primero una zona para poder asignarle una brigada.', 'danger');
      return;
    }

    // Attempt to create in backend
    this.geoService.createBrigade({
      name: nombre,
      institution: 'Valle del Sol',
      status: 'AVAILABLE',
      latitude: -33.46,
      longitude: -70.65,
      zoneId: this.newBrigadaZoneId
    }).subscribe({
      next: (res) => {
        this.brigadasDisponibles.push({
          id: res.id,
          nombre: res.name,
          tiempoMinutos: 10,
          backendId: res.id,
          zoneId: res.zoneId
        });
        this.nuevaBrigadaNombre = '';
        this.showToast('Brigada creada', 'success');
        this.autoAssignBrigades();
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

        // La ruta de evacuación es personal de este reporte — al eliminarlo, se elimina también.
        this.geoService.deleteEvacuationRoutesByReport(reporte.id).subscribe({
          error: (err) => console.warn('No se pudo eliminar la ruta del reporte', err)
        });
      },
      error: async () => this.showToast('Error al eliminar el reporte', 'danger')
    });
  }

  getEmergencyDesc(emergenciaId: number): string {
    const em = this.emergencias.find(e => e.id === emergenciaId);
    return em ? em.desc : 'Emergencia desconocida';
  }

  async deleteEmergency(emergencia: any) {
    // Return brigade if one was assigned
    if (emergencia.brigada) {
      this.returnBrigade(emergencia);
    }

    // Clear selection if it is the current hotspot
    if (this.focoSeleccionado?.id === emergencia.id) {
      this.focoSeleccionado = null;
    }

    if (emergencia.backendId) {
      this.geoService.deleteMappedReport(emergencia.backendId).subscribe({
        error: (err) => console.warn('No se pudo quitar el incendio del mapa en el backend', err)
      });
    }

    // La ruta de evacuación es personal de este reporte — al eliminarlo, se elimina también.
    if (emergencia.externalReportId) {
      this.geoService.deleteEvacuationRoutesByReport(emergencia.externalReportId).subscribe({
        error: (err) => console.warn('No se pudo eliminar la ruta del reporte', err)
      });
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
      next: (zones) => {
        this.backendZones = zones;
        if (this.map) { this.renderMapLayers(); }

        // Preselecciona una zona para el formulario de "nueva brigada" (prioriza
        // una zona operativa real sobre la región completa).
        if (!this.newBrigadaZoneId || !zones.some(z => z.id === this.newBrigadaZoneId)) {
          const preferida = zones.find(z => z.zoneType === 'OPERATIONAL') || zones[0];
          this.newBrigadaZoneId = preferida ? preferida.id : '';
        }

        this.autoAssignBrigades();
      },
      error: (err) => console.warn('No se pudieron cargar las zonas del backend', err)
    });
    this.geoService.getEvacuationRoutes().subscribe({
      next: (routes) => {
        this.backendRoutes = routes;
        if (this.map) { this.renderMapLayers(); }
      },
      error: (err) => console.warn('No se pudieron cargar las rutas del backend', err)
    });
  }

  /** Regiones predefinidas (por ahora solo la Región Metropolitana). */
  get regiones(): ComunaZone[] {
    return this.zones.filter(z => z.zoneType === 'PROVINCE');
  }

  /** Comunas predefinidas seleccionables como contexto para dibujar una zona (todas salvo la región). */
  get operationalComunas(): ComunaZone[] {
    return this.zones.filter(z => z.zoneType !== 'PROVINCE');
  }

  get hasMainZone(): boolean {
    return this.backendZones.some(z => z.zoneType === 'MAIN');
  }

  startDrawZone() {
    this.drawingMode = 'zone';
    this.selectedRegionContext = this.regiones.length === 1 ? this.regiones[0] : null;
    this.selectedComunaContext = null;
    this.drawingPoints = [];
    this.showZoneForm = false;
    // Asigna automáticamente un color distinto a cada zona nueva (recorre la
    // paleta según cuántas operativas ya existan) para que se diferencien en
    // el mapa. El admin igual puede cambiarlo en el formulario.
    const operativas = this.backendZones.filter(z => z.zoneType === 'OPERATIONAL').length;
    this.newZoneColor = this.zonePalette[operativas % this.zonePalette.length];
    this.clearAdminDrawing();
  }

  /**
   * Región (predefinida, p.ej. "Región Metropolitana") — se establece una sola
   * vez como zona principal (MAIN) real en el backend, para que las zonas
   * operativas que dibuje el admin puedan validarse contra ella.
   */
  private async ensureRegionMainZone(region: ComunaZone): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this.geoService.createZone({
        name: region.name,
        description: 'Región principal (predefinida)',
        isActive: true,
        color: region.color && /^#([A-Fa-f0-9]{6})$/.test(region.color) ? region.color : '#3388ff',
        zoneType: 'MAIN',
        geoJson: this.simplifyGeometryForBackend(region.geometry)
      }).subscribe({
        next: async () => {
          await this.showToast(`Región "${region.name}" establecida como zona principal`, 'success');
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

  cancelDrawing() {
    this.drawingMode = 'none';
    this.selectedRegionContext = null;
    this.selectedComunaContext = null;
    this.drawingPoints = [];
    this.showZoneForm = false;
    this.newZoneName = '';
    this.newZoneDescription = '';
    this.newZoneColor = '#3388ff';
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

    if (!this.hasMainZone) {
      if (!this.selectedRegionContext) {
        await this.showToast('Selecciona la región primero.', 'warning'); return;
      }
      const ok = await this.ensureRegionMainZone(this.selectedRegionContext);
      if (!ok) return;
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

  navigateToZone(zone: ZoneResponse) {
    if (!this.map || !zone.geoJson) return;

    try {
      const geoJson = JSON.parse(zone.geoJson);
      const geoLayer = L.geoJSON(geoJson);
      const bounds = geoLayer.getBounds();

      if (bounds.isValid()) {
        this.map.fitBounds(bounds, { padding: [50, 50] });

        // Mostrar el nombre de la zona como popup en el centro
        const center = bounds.getCenter();
        const popup = L.popup()
          .setLatLng(center)
          .setContent(`<strong>${zone.name}</strong>`)
          .openOn(this.map);
      }
    } catch (error) {
      console.error('Error al navegar a la zona:', error);
    }
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

    // Comunas predefinidas: solo borde coloreado de referencia, sin relleno
    // (no son zonas operativas reales, son solo contexto geográfico).
    this.zones.forEach((zone) => {
      if (!zone.geometry) return;
      const esRegion = zone.zoneType === 'PROVINCE';

      const layer = L.geoJSON(zone.geometry, {
        style: {
          color: zone.color || '#3388ff',
          weight: 2,
          dashArray: esRegion ? '6 4' : undefined,
          fillOpacity: 0
        }
      }).addTo(this.map!);

      this.zoneLayers.push(layer);
      // No se hace fitBounds a la Región completa: su territorio real (~150km)
      // es mucho más alto que el contenedor del mapa, así que encajarlo entero
      // fuerza un zoom muy alejado y las comunas urbanas (lo relevante acá)
      // terminan viéndose como un punto ilegible. El zoom inicial centrado en
      // Santiago ya es el adecuado para este mapa.
    });

    // Zonas operativas reales (dibujadas por el admin): visión exclusiva del
    // admin. Las rutas de evacuación NUNCA se muestran al admin en el mapa —
    // son personales de cada ciudadano (ver renderCitizenRoute).
    if (!this.isAdmin) return;

    // Color por zona: se respeta el que el admin haya elegido, pero las zonas
    // antiguas que quedaron con el azul por defecto (#3388ff) se diferencian
    // asignándoles un color de la paleta por su posición, así no se ven todas
    // iguales aunque hayan sido creadas antes de tener color automático.
    let defaultColorIndex = 0;
    this.backendZones.forEach((zone) => {
      if (!zone.geoJson) return;
      let geo: any;
      try { geo = JSON.parse(zone.geoJson); } catch { return; }

      let color = zone.color;
      if (!color || color.toLowerCase() === '#3388ff') {
        color = this.zonePalette[defaultColorIndex % this.zonePalette.length];
        defaultColorIndex++;
      }

      const layer = L.geoJSON(geo, {
        style: {
          color,
          weight: 2,
          fillOpacity: 0.15
        }
      }).addTo(this.map!);
      // Sin popup/etiqueta al hacer clic en la zona — el usuario pidió
      // explícitamente que nunca aparezca ninguna etiqueta sobre el mapa.
      this.zoneLayers.push(layer);
    });
  }
}

