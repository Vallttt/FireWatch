import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  Router,
  NavigationEnd,
  RouterLink,
  RouterLinkActive
} from '@angular/router';
import { filter } from 'rxjs/operators';
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
export class AppComponent {

  public isLoginPage = false;
  public isAdmin = false;
  public isEmergencyMode = false;

  constructor(private router: Router, private authService: AuthService) {
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
