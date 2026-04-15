import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  IonContent, IonCard, IonCardHeader, IonCardTitle, IonCardContent,
  IonGrid, IonRow, IonCol, IonIcon, IonHeader,
  IonToolbar, IonButtons, IonMenuButton, IonTitle
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  flameOutline, warningOutline, timeOutline, trendingUpOutline,
  flame, mapOutline, notificationsOutline, ellipse,
  navigateOutline, chevronForwardOutline
} from 'ionicons/icons';

import { DashboardService, DashboardStats } from '../services/dashboard.service';

@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: ['tab1.page.scss'],
  standalone: true,
  imports: [
    CommonModule,
    IonContent, IonCard, IonCardHeader, IonCardTitle, IonCardContent,
    IonGrid, IonRow, IonCol, IonIcon, IonHeader,
    IonToolbar, IonButtons, IonMenuButton, IonTitle
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

  constructor(
    private router: Router,
    private dashboardService: DashboardService
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
}
