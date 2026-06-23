import { Injectable } from '@angular/core';
import { LocalNotifications } from '@capacitor/local-notifications';
import { Capacitor } from '@capacitor/core';

/**
 * Servicio de notificaciones locales (Capacitor).
 *
 * Muestra una notificación nativa en el dispositivo cuando llega una alerta.
 * No requiere Firebase ni servidor de push: la notificación se dispara en el
 * propio dispositivo. Funciona en Android (emulador/dispositivo). En web cae
 * de forma silenciosa (Capacitor.isNativePlatform() == false).
 */
@Injectable({ providedIn: 'root' })
export class LocalNotifyService {

  private permissionGranted = false;

  /** Pide permiso de notificaciones una sola vez (llamar tras el login). */
  async init(): Promise<void> {
    if (!Capacitor.isNativePlatform()) return;
    try {
      const perm = await LocalNotifications.requestPermissions();
      this.permissionGranted = perm.display === 'granted';
    } catch (e) {
      console.warn('No se pudo pedir permiso de notificaciones', e);
    }
  }

  /** Dispara una notificación local inmediata. */
  async show(title: string, body: string): Promise<void> {
    if (!Capacitor.isNativePlatform()) {
      console.log('[LocalNotify] (solo nativo) =>', title, '-', body);
      return;
    }

    if (!this.permissionGranted) {
      await this.init();
      if (!this.permissionGranted) return;
    }

    try {
      await LocalNotifications.schedule({
        notifications: [{
          id: Math.floor(Math.random() * 2_000_000_000),
          title,
          body,
          schedule: { at: new Date(Date.now() + 300) },
          smallIcon: 'ic_stat_icon_config_sample',
          largeIcon: 'ic_launcher',
        }]
      });
    } catch (e) {
      console.warn('No se pudo mostrar la notificación local', e);
    }
  }
}
