import { Routes } from '@angular/router';
import { authGuard, adminGuard, citizenGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },

  // ✅ RUTAS PÚBLICAS (sin autenticación)
  {
    path: 'login',
    loadComponent: () => import('./login/login.page').then(m => m.LoginPage)
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./forgot-password/forgot-password.page')
        .then(m => m.ForgotPasswordPage)
  },
  {
    path: 'unauthorized',
    loadComponent: () =>
      import('./pages/unauthorized.page')
        .then(m => m.UnauthorizedPage)
  },

  // ✅ RUTAS PROTEGIDAS: Solo ADMIN
  {
    path: 'dashboard',
    canActivate: [adminGuard],
    loadComponent: () => import('./Dashboard/tab1.page').then(m => m.Tab1Page),
    data: { role: 'admin' }
  },

  // ✅ RUTAS PROTEGIDAS: USER o CIUDADANO
  {
    path: 'reportar',
    canActivate: [citizenGuard],
    loadComponent: () => import('./Reportar/tab2.page').then(m => m.Tab2Page),
    data: { role: 'citizen' }
  },
  {
    path: 'alertas',
    canActivate: [authGuard],
    loadComponent: () => import('./Alertas/tab3.page').then(m => m.Tab3Page)
  },
  {
    path: 'mapa',
    canActivate: [authGuard],
    loadComponent: () => import('./Mapa/mapa.page').then(m => m.MapaPage)
  },

  // ✅ Catch-all: redirigir a login
  {
    path: '**',
    redirectTo: 'login'
  }
];
