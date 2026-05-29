import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  
  {//ruta de login
    path: 'login',
    loadComponent: () => import('./login/login.page').then( m => m.LoginPage)
  },
  {//ruta del dashboard
    path: 'dashboard',
    loadComponent: () => import('./Dashboard/tab1.page').then( m => m.Tab1Page)
  },
  {//ruta de reportar fuego
    path: 'reportar',
    loadComponent: () => import('./Reportar/tab2.page').then( m => m.Tab2Page)
  },
  {//ruta de alertas
    path: 'alertas',
    loadComponent: () => import('./Alertas/tab3.page').then( m => m.Tab3Page)
  },
  {//ruta del mapa
    path: 'mapa',
    loadComponent: () => import('./Mapa/mapa.page').then( m => m.MapaPage)
  }
];
