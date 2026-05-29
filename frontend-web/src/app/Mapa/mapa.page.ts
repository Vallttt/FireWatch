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

import { GeoService, BrigadeResponse, MappedReportResponse } from '../services/geo.service';
import { ReportService, ReporteResponse } from '../services/report.service';

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
  public zones: any[] = [];
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

  private marcadoresLeaflet: L.Marker[] = [];
  private backendLoaded = false;

  constructor(
    private ngZone: NgZone,
    private toastController: ToastController,
    private geoService: GeoService,
    private reportService: ReportService
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
    this.loadBackendData();
  }

  ionViewDidEnter() {
    setTimeout(() => {
      this.initSatelliteMap();

      if (this.backendLoaded) {
        this.renderMapLayers();
      }
    }, 300);
  }

  /* ================================================================
     LOAD DATA FROM BACKEND
     ================================================================ */
  private loadBackendData() {
    this.geoService.getMapData().subscribe({
      next: (data) => {
        this.zones = data.zones || [];
        this.routes = data.routes || [];

        console.log('ZONAS MAPA PRINCIPAL:', this.zones);
        console.log('RUTAS MAPA PRINCIPAL:', this.routes);

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
            } else {
              // Brigade DEPLOYED with no active fire → reset to AVAILABLE
              this.geoService.updateBrigade(brigade.id, {
                name: brigade.name,
                institution: brigade.institution || 'Valle del Sol',
                status: 'AVAILABLE',
                latitude: -33.46,
                longitude: -70.65
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
  this.map = L.map('globalMap', {
    zoomControl: false,
    attributionControl: false
  }).setView([-33.4600, -70.6500], 12);

  L.control.attribution({ prefix: false }).addTo(this.map);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap · FireWatch',
    maxZoom: 18
  }).addTo(this.map);

  setTimeout(() => {
    this.map?.invalidateSize();

    if (this.backendLoaded) {
      this.renderMapLayers();
    }

    this.renderMarkers();
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
    if (selectedBrigade.backendId) {
      this.geoService.updateBrigade(selectedBrigade.backendId, {
        name: selectedBrigade.nombre,
        institution: 'Valle del Sol',
        status: 'DEPLOYED',
        latitude: this.focoSeleccionado.lat,
        longitude: this.focoSeleccionado.lng
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
      if ((occupied as any).backendId) {
        this.geoService.updateBrigade((occupied as any).backendId, {
          name: occupied.nombre,
          institution: 'Valle del Sol',
          status: 'AVAILABLE',
          latitude: -33.46,
          longitude: -70.65
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

      // Delete from Geo Service → removes from map
      if (foco.backendId) {
        this.geoService.deleteMappedReport(foco.backendId).subscribe({
          error: (err) => console.warn('Could not delete mapped_report in Geo Service', err)
        });
      }

      // Mark INACTIVE in Report Service → updates dashboard
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

    // Attempt to create in backend
    this.geoService.createBrigade({
      name: nombre,
      institution: 'Valle del Sol',
      status: 'AVAILABLE',
      latitude: -33.46,
      longitude: -70.65
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
    // Delete from backend if it has an ID
    if (emergencia.backendId) {
      this.geoService.deleteMappedReport(emergencia.backendId).subscribe({
        error: (err) => console.warn('Could not delete emergency in backend', err)
      });
    }

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

  private renderMapLayers() {
  if (!this.map) return;

  this.zoneLayers.forEach((layer: L.Layer) => layer.remove());
  this.routeLayers.forEach((layer: L.Layer) => layer.remove());

  this.zoneLayers = [];
  this.routeLayers = [];

  this.zones.forEach((zone: any) => {
    if (!zone.geoJson) return;

    const geo = JSON.parse(zone.geoJson);

    const layer = L.geoJSON(geo, {
      style: {
        color: zone.color || '#3388ff',
        weight: zone.type === 'MAIN' ? 4 : 2,
        fillOpacity: zone.type === 'MAIN' ? 0.08 : 0.28
      }
    }).addTo(this.map!);

    layer.bindPopup(`${zone.name} (${zone.type})`);
    this.zoneLayers.push(layer);

    if (zone.type === 'MAIN') {
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

