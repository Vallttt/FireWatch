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

  public brigadasDisponibles: any[] = [];
  public brigadasOcupadas: { id: string; nombre: string; tiempoMinutos: number; emergenciaId: number }[] = [];
  public nuevaBrigadaNombre = '';

  public emergencias: any[] = [];

  private marcadoresLeaflet: L.Marker[] = [];
  private backendLoaded = false;

  constructor(
    private ngZone: NgZone,
    private toastController: ToastController,
    private geoService: GeoService
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
    setTimeout(() => { this.iniciarMapaSatelital(); }, 200);
    this.cargarDatosBackend();
  }

  /* ================================================================
     CARGA DE DATOS DESDE BACKEND
     ================================================================ */
  private cargarDatosBackend() {
    this.geoService.getMapData().subscribe({
      next: (data) => {
        this.backendLoaded = true;

        // Mapear brigadas
        if (data.brigades) {
          this.brigadasDisponibles = data.brigades
            .filter(b => b.status === 'AVAILABLE')
            .map(b => ({
              id: b.id,
              nombre: b.name,
              tiempoMinutos: 10,
              backendId: b.id
            }));

          const deployed = data.brigades.filter(b => b.status === 'DEPLOYED');
          // Las brigadas desplegadas se vinculan a emergencias más abajo
        }

        // Mapear reportes del mapa como emergencias
        if (data.reports) {
          this.emergencias = data.reports
            .filter(r => r.reportStatus === 'ACTIVE')
            .map((r, i) => ({
              id: i + 1,
              backendId: r.id,
              lat: r.latitude,
              lng: r.longitude,
              desc: `Incendio ${r.severity} (${r.externalReportId?.substring(0, 8) || 'N/A'})`,
              brigada: null,
              estado: 'activo',
              severity: r.severity
            }));
        }

        this.desplegarRadares();
      },
      error: (err) => {
        console.warn('No se pudo conectar con Geo Service.', err);
        this.showToast('No se pudo cargar datos del mapa', 'danger');
      }
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
     MAPA
     ================================================================ */
  iniciarMapaSatelital() {
    this.map = L.map('globalMap', {
      zoomControl: false,
      attributionControl: false
    }).setView([-33.4600, -70.6500], 12);

    L.control.attribution({ prefix: false }).addTo(this.map);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap · FireWatch',
      maxZoom: 18
    }).addTo(this.map);

    this.desplegarRadares();
  }

  desplegarRadares() {
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
     BRIGADAS
     ================================================================ */
  async asignarBrigada(brigadaSeleccionada: any) {
    if (!this.focoSeleccionado || this.focoSeleccionado.brigada) return;

    // Actualizar estado en backend si está conectado
    if (brigadaSeleccionada.backendId) {
      this.geoService.updateBrigade(brigadaSeleccionada.backendId, {
        name: brigadaSeleccionada.nombre,
        institution: 'Valle del Sol',
        status: 'DEPLOYED',
        latitude: this.focoSeleccionado.lat,
        longitude: this.focoSeleccionado.lng
      }).subscribe({
        error: (err) => console.warn('No se pudo actualizar brigada en backend', err)
      });
    }

    this.focoSeleccionado.brigada = brigadaSeleccionada.nombre;
    this.brigadasOcupadas.push({
      ...brigadaSeleccionada,
      emergenciaId: this.focoSeleccionado.id
    });
    this.brigadasDisponibles = this.brigadasDisponibles.filter(
      b => b.id !== brigadaSeleccionada.id
    );
    this.desplegarRadares();
    await this.showToast(`${brigadaSeleccionada.nombre} despachada`, 'success');
  }

  async desasignarBrigada() {
    if (!this.focoSeleccionado || !this.focoSeleccionado.brigada) return;
    const brigadaNombre = this.focoSeleccionado.brigada;
    this.devolverBrigada(this.focoSeleccionado);
    this.desplegarRadares();
    await this.showToast(`${brigadaNombre} retirada del foco`, 'warning');
  }

  private devolverBrigada(emergencia: any) {
    const ocupada = this.brigadasOcupadas.find(b => b.emergenciaId === emergencia.id);
    if (ocupada) {
      // Actualizar en backend
      if ((ocupada as any).backendId) {
        this.geoService.updateBrigade((ocupada as any).backendId, {
          name: ocupada.nombre,
          institution: 'Valle del Sol',
          status: 'AVAILABLE',
          latitude: -33.46,
          longitude: -70.65
        }).subscribe({
          error: (err) => console.warn('No se pudo actualizar brigada en backend', err)
        });
      }

      this.brigadasDisponibles.push({
        id: ocupada.id,
        nombre: ocupada.nombre,
        tiempoMinutos: ocupada.tiempoMinutos
      });
      this.brigadasOcupadas = this.brigadasOcupadas.filter(
        b => b.emergenciaId !== emergencia.id
      );
    }
    emergencia.brigada = null;
  }

  async cambiarEstado(nuevoEstado: string) {
    if (!this.focoSeleccionado) return;

    if (nuevoEstado === 'finalizado' && this.focoSeleccionado.brigada) {
      this.devolverBrigada(this.focoSeleccionado);
    }

    this.focoSeleccionado.estado = nuevoEstado;
    this.desplegarRadares();

    if (nuevoEstado === 'finalizado') {
      this.focoSeleccionado = null;
    }

    await this.showToast(
      `Estado actualizado: ${nuevoEstado}`,
      nuevoEstado === 'controlado' ? 'success' : 'medium'
    );
  }

  async crearBrigada() {
    if (!this.nuevaBrigadaNombre.trim()) {
      await this.showToast('Ingresa el nombre de la brigada', 'warning');
      return;
    }

    const nombre = this.nuevaBrigadaNombre.trim();

    // Intentar crear en backend
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
        console.error('Error al crear brigada en backend', err);
        this.showToast('Error al crear brigada. Verifica la conexión.', 'danger');
      }
    });
  }

  async eliminarBrigada(brigada: any) {
    // Intentar eliminar en backend
    if (brigada.backendId) {
      this.geoService.deleteBrigade(brigada.backendId).subscribe({
        error: (err) => console.warn('No se pudo eliminar brigada en backend', err)
      });
    }

    this.brigadasDisponibles = this.brigadasDisponibles.filter(
      b => b.id !== brigada.id
    );
    await this.showToast(`${brigada.nombre} eliminada`, 'warning');
  }

  getEmergenciaDesc(emergenciaId: number): string {
    const em = this.emergencias.find(e => e.id === emergenciaId);
    return em ? em.desc : 'Emergencia desconocida';
  }

  async eliminarEmergencia(emergencia: any) {
    // Eliminar del backend si tiene ID
    if (emergencia.backendId) {
      this.geoService.deleteMappedReport(emergencia.backendId).subscribe({
        error: (err) => console.warn('No se pudo eliminar emergencia en backend', err)
      });
    }

    // Devolver brigada si estaba asignada
    if (emergencia.brigada) {
      this.devolverBrigada(emergencia);
    }

    // Limpiar selección si es el foco actual
    if (this.focoSeleccionado?.id === emergencia.id) {
      this.focoSeleccionado = null;
    }

    this.emergencias = this.emergencias.filter(e => e.id !== emergencia.id);
    this.desplegarRadares();
    await this.showToast('Emergencia eliminada del historial', 'warning');
  }
}
