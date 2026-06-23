import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonButton, IonIcon, IonCard, IonCardContent
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { alertCircleOutline, arrowBackOutline } from 'ionicons/icons';

@Component({
  selector: 'app-unauthorized',
  templateUrl: './unauthorized.page.html',
  styleUrls: ['./unauthorized.page.scss'],
  standalone: true,
  imports: [
    CommonModule,
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonButton,
    IonIcon,
    IonCard,
    IonCardContent
  ]
})
export class UnauthorizedPage {

  constructor(private router: Router) {
    addIcons({ alertCircleOutline, arrowBackOutline });
  }

  goBack(): void {
    this.router.navigate(['/login']);
  }
}
