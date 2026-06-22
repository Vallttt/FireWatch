import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

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
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./forgot-password/forgot-password.page')
      .then(m => m.ForgotPasswordPage)
  },
  {//ruta del dashboard
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./Dashboard/tab1.page').then( m => m.Tab1Page)
  },
  {//ruta de reportar fuego
    path: 'reportar',
    canActivate: [authGuard],
    loadComponent: () => import('./Reportar/tab2.page').then( m => m.Tab2Page)
  },
  {//ruta de alertas
    path: 'alertas',
    canActivate: [authGuard],
    loadComponent: () => import('./Alertas/tab3.page').then( m => m.Tab3Page)
  },
  {//ruta del mapa
    path: 'mapa',
    canActivate: [authGuard],
    loadComponent: () => import('./Mapa/mapa.page').then( m => m.MapaPage)
  }
];
