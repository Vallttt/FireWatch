import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  Router,
  NavigationEnd,
  RouterLink,
  RouterLinkActive
} from '@angular/router';
import { filter, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import {
  IonApp, IonRouterOutlet, IonSplitPane, IonMenu,
  IonHeader, IonToolbar, IonContent, IonList,
  IonItem, IonIcon, IonLabel, IonMenuToggle,
  IonFooter, IonToggle, IonButton, ToastController, AlertController
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
import { TokenExpiryService } from './services/token-expiry.service';

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

  private destroy$ = new Subject<void>();
  private sessionExpiringAlertShown = false;

  constructor(
    private router: Router,
    private authService: AuthService,
    private tokenExpiryService: TokenExpiryService,
    private toastController: ToastController,
    private alertController: AlertController
  ) {
    addIcons({
      flame,
      gridOutline,
      documentTextOutline,
      mapOutline,
      notificationsOutline,
      moonOutline,
      logOutOutline,
      logOut
    });

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.isLoginPage = event.url === '/login' || event.url === '/' || event.url === '/forgot-password';

        const role = localStorage.getItem('userRole');
        this.isAdmin = role === 'admin';
        this.isEmergencyMode = localStorage.getItem('emergencyMode') === 'true';
      });

    this.toggleDarkTheme(false);
  }

  ngOnInit(): void {
    // ✅ Iniciar monitoreo de expiración de tokens
    this.tokenExpiryService.startMonitoring();

    // ✅ Escuchar notificación de token expirando (faltan 5 minutos)
    this.tokenExpiryService.tokenExpiring$
      .pipe(takeUntil(this.destroy$))
      .subscribe((data) => {
        if (!this.sessionExpiringAlertShown) {
          this.sessionExpiringAlertShown = true;
          this.showSessionExpiringAlert(data.timeRemaining);
        }
      });

    // ✅ Escuchar notificación de sesión expirada
    this.tokenExpiryService.sessionExpired$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.showSessionExpiredAndLogout();
      });

    // ✅ Reiniciar alerta cuando se inicia sesión nueva
    this.authService.isAuthenticated$
      .pipe(takeUntil(this.destroy$))
      .subscribe((isAuth) => {
        if (isAuth) {
          this.sessionExpiringAlertShown = false;
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.tokenExpiryService.stopMonitoring();
  }

  /**
   * ✅ Mostrar alerta de sesión expirando en 5 minutos
   */
  async showSessionExpiringAlert(timeRemaining: number): Promise<void> {
    const alert = await this.alertController.create({
      header: '⏰ Sesión por expirar',
      message: `Tu sesión expirará en ${this.tokenExpiryService.formatTimeRemaining(timeRemaining)}. ¿Deseas renovarla?`,
      buttons: [
        {
          text: 'Logout',
          role: 'cancel',
          handler: () => {
            this.logout();
          }
        },
        {
          text: 'Renovar sesión',
          handler: () => {
            this.tokenExpiryService.renewSession();
          }
        }
      ],
      backdropDismiss: false // ✅ No permitir cerrar sin elegir
    });

    await alert.present();
  }

  /**
   * ✅ Mostrar alerta de sesión expirada y logout
   */
  async showSessionExpiredAndLogout(): Promise<void> {
    const toast = await this.toastController.create({
      message: '❌ Tu sesión ha expirado. Por favor, inicia sesión nuevamente.',
      duration: 5000,
      position: 'top',
      color: 'danger'
    });

    await toast.present();
    this.logout();
  }

  toggleDarkMode(event: any): void {
    this.toggleDarkTheme(event.detail.checked);
  }

  toggleDarkTheme(shouldAdd: boolean): void {
    document.body.classList.toggle('dark', shouldAdd);
  }

  logout(): void {
    this.authService.logout();
    this.tokenExpiryService.stopMonitoring();
    window.location.href = '/login';
  }
}
