import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  IonContent, IonCard, IonCardHeader, IonCardTitle, IonCardContent,
  IonGrid, IonRow, IonCol, IonIcon, IonHeader,
  IonToolbar, IonButtons, IonMenuButton, IonTitle,
  IonList, IonItem, IonLabel, IonBadge
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  flameOutline, warningOutline, timeOutline, trendingUpOutline,
  flame, mapOutline, notificationsOutline, ellipse,
  navigateOutline, chevronForwardOutline
} from 'ionicons/icons';

import { DashboardService, DashboardStats } from '../services/dashboard.service';
import { ReportService, ReporteResponse } from '../services/report.service';
import { AlertService, Notificacion } from '../services/alert.service';

@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: ['tab1.page.scss'],
  standalone: true,
  imports: [
    CommonModule,
    IonContent, IonCard, IonCardHeader, IonCardTitle, IonCardContent,
    IonGrid, IonRow, IonCol, IonIcon, IonHeader,
    IonToolbar, IonButtons, IonMenuButton, IonTitle,
    IonList, IonItem, IonLabel, IonBadge
  ]
})
export class Tab1Page implements OnInit {
  headerHidden: boolean = false;
  private lastScroll: number = 0;

  // Stats dinámicos desde BFF
  stats: DashboardStats = {
    totalIncendios: 0,
    alertasEmitidas: 0,
    brigadasActivas: 0,
    estadoGlobal: 'Cargando...'
  };

  actividadReciente: any[] = [];

  constructor(
    private router: Router,
    private dashboardService: DashboardService,
    private reportService: ReportService,
    private alertService: AlertService
  ) {
    addIcons({
      flameOutline, warningOutline, timeOutline, trendingUpOutline,
      flame, mapOutline, notificationsOutline, ellipse,
      navigateOutline, chevronForwardOutline
    });
  }

  ngOnInit() {
    this.cargarStats();
  }

  ionViewDidEnter() {
    this.cargarStats();
    this.cargarActividad();
  }

  private cargarStats() {
    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
      },
      error: (err) => {
        console.warn('No se pudo conectar con el BFF.', err);
        this.stats = {
          totalIncendios: 0,
          alertasEmitidas: 0,
          brigadasActivas: 0,
          estadoGlobal: 'Sin conexión'
        };
      }
    });
  }

  // EVENTOS DE SALTO INDESTRUCTIBLES
  saltarAlMapa() { this.router.navigate(['/mapa']); }
  saltarAReportar() { this.router.navigate(['/reportar']); }
  saltarAAlertas() { this.router.navigate(['/alertas']); }

  onContentScroll(e: any) {
    const current = e.detail.scrollTop;
    if (current > 80 && current > this.lastScroll) {
      this.headerHidden = true;
    } else if (current < this.lastScroll - 4 || current < 40) {
      this.headerHidden = false;
    }
    this.lastScroll = current;
  }

  private cargarActividad() {
    this.reportService.listarReportes().subscribe({
      next: (reportes) => {
        const reporteItems = reportes.slice(0, 5).map(r => ({
          tipo: 'reporte',
          icono: 'flame-outline',
          descripcion: r.descripcion,
          fecha: r.fechaIncidente,
          badge: r.severity,
          badgeColor: r.severity === 'HIGH' || r.severity === 'CRITICAL' ? 'danger' : 'warning'
        }));
        const alertItems = this.actividadReciente.filter(a => a.tipo === 'alerta');
        this.actividadReciente = [...reporteItems, ...alertItems]
          .sort((a, b) => new Date(b.fecha).getTime() - new Date(a.fecha).getTime())
          .slice(0, 6);
      }
    });

    this.alertService.historial().subscribe({
      next: (alertas) => {
        const alertItems = alertas.slice(0, 5).map(a => ({
          tipo: 'alerta',
          icono: 'notifications-outline',
          descripcion: a.mensaje.substring(0, 60),
          fecha: a.fechaEnvio,
          badge: a.tipoAlerta,
          badgeColor: 'primary'
        }));
        const reporteItems = this.actividadReciente.filter(a => a.tipo === 'reporte');
        this.actividadReciente = [...reporteItems, ...alertItems]
          .sort((a, b) => new Date(b.fecha).getTime() - new Date(a.fecha).getTime())
          .slice(0, 6);
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
}
