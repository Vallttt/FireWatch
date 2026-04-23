import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  Router,
  NavigationEnd,
  RouterLink,
  RouterLinkActive
} from '@angular/router';
import { filter } from 'rxjs/operators';
import { Subscription, interval } from 'rxjs';
import {
  IonApp, IonRouterOutlet, IonSplitPane, IonMenu,
  IonHeader, IonToolbar, IonContent, IonList,
  IonItem, IonIcon, IonLabel, IonMenuToggle,
  IonFooter, IonToggle, IonButton
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  flame,
  gridOutline,
  documentTextOutline,
  mapOutline,
  notificationsOutline,
  moonOutline, logOut, logOutOutline } from 'ionicons/icons';
import { AuthService } from './services/auth.service';
import { AlertService } from './services/alert.service';
import { LocalNotifications } from '@capacitor/local-notifications';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    IonApp,
    IonRouterOutlet,
    IonSplitPane,
    IonMenu,
    IonHeader,
    IonToolbar,
    IonContent,
    IonList,
    IonItem,
    IonIcon,
    IonLabel,
    IonMenuToggle,
    IonFooter,
    IonToggle,
    IonButton
  ],
})
export class AppComponent implements OnInit, OnDestroy {

  public isLoginPage = false;
  public isAdmin = false;
  public isEmergencyMode = false;

  private pollSubscription?: Subscription;
  private lastAlertTimestamp: number = 0;

  constructor(
    private router: Router,
    private authService: AuthService,
    private alertService: AlertService
  ) {
    addIcons({flame,gridOutline,documentTextOutline,mapOutline,notificationsOutline,moonOutline,logOutOutline,logOut});

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.isLoginPage =
          event.url === '/login' || event.url === '/';

        const role = localStorage.getItem('userRole');
        this.isAdmin = role === 'admin';
        this.isEmergencyMode = localStorage.getItem('emergencyMode') === 'true';
      });

    this.toggleDarkTheme(false);
  }

  async ngOnInit() {
    await this.solicitarPermisoNotificaciones();
    this.iniciarPollingAlertas();
  }

  ngOnDestroy() {
    this.pollSubscription?.unsubscribe();
  }

  // ─── Notificaciones Push Locales ─────────────────────────────────────────

  private async solicitarPermisoNotificaciones() {
    try {
      const perm = await LocalNotifications.requestPermissions();
      if (perm.display === 'granted') {
        console.log('[Push] Permiso de notificaciones concedido');
      }
    } catch (e) {
      console.warn('[Push] No se pudo solicitar permiso:', e);
    }
  }

  /**
   * Consulta el historial de alertas cada 30 segundos.
   * Si llega una alerta nueva (más reciente que la última vista),
   * muestra una notificación push local.
   */
  private iniciarPollingAlertas() {
    // Inicializar timestamp con el momento actual para no notificar alertas viejas
    this.lastAlertTimestamp = Date.now();

    this.pollSubscription = interval(30_000).subscribe(() => {
      if (!this.authService.isLoggedIn()) return;

      this.alertService.historial().subscribe({
        next: (alertas) => {
          if (!alertas || alertas.length === 0) return;

          const latest = alertas.reduce((prev, curr) =>
            new Date(curr.fechaEnvio).getTime() > new Date(prev.fechaEnvio).getTime() ? curr : prev
          );

          const latestTime = new Date(latest.fechaEnvio).getTime();

          if (latestTime > this.lastAlertTimestamp) {
            this.lastAlertTimestamp = latestTime;
            this.mostrarNotificacionPush(latest.tipoAlerta, latest.mensaje);
          }
        },
        error: () => { /* silencioso en background */ }
      });
    });
  }

  private async mostrarNotificacionPush(titulo: string, cuerpo: string) {
    try {
      await LocalNotifications.schedule({
        notifications: [
          {
            id: Date.now(),
            title: `⚠️ Alerta Valle del Sol: ${titulo}`,
            body: cuerpo.length > 100 ? cuerpo.substring(0, 100) + '…' : cuerpo,
            smallIcon: 'ic_notification',
            sound: 'beep.wav',
            actionTypeId: '',
            extra: null
          }
        ]
      });
    } catch (e) {
      console.warn('[Push] Error al mostrar notificación:', e);
    }
  }

  // ─── General ─────────────────────────────────────────────────────────────

  toggleDarkMode(event: any) {
    this.toggleDarkTheme(event.detail.checked);
  }

  toggleDarkTheme(shouldAdd: boolean) {
    document.body.classList.toggle('dark', shouldAdd);
  }

  cerrarSesion() {
    this.authService.logout();
    window.location.href = '/login';
  }
}
