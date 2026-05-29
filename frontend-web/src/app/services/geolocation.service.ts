import { Injectable } from '@angular/core';
import { Capacitor } from '@capacitor/core';
import { Geolocation } from '@capacitor/geolocation';

export interface GeoPosition {
  lat: number;
  lng: number;
  accuracy?: number;
}

/**
 * Servicio unificado de geolocalización.
 *
 * - Android/iOS: usa el plugin nativo @capacitor/geolocation.
 *   Llama siempre a requestPermissions() antes de obtener la posición;
 *   si ya está concedido, es un no-op y no muestra diálogo.
 * - Web (ng serve): usa navigator.geolocation del navegador.
 */
@Injectable({ providedIn: 'root' })
export class GeolocationService {

  async getCurrentPosition(): Promise<GeoPosition> {
    if (Capacitor.isNativePlatform()) {
      return this.nativePosition();
    }
    return this.browserPosition();
  }

  /* ------------------------------------------------------------------ */
  /*  Nativo (Android / iOS)                                             */
  /* ------------------------------------------------------------------ */
  private async nativePosition(): Promise<GeoPosition> {
    // Siempre pedir permisos explícitamente.
    // Si ya están concedidos → no-op (sin diálogo).
    // Si es primera vez o fue denegado → muestra el diálogo del sistema.
    let perm = await Geolocation.requestPermissions({ permissions: ['location', 'coarseLocation'] });

    const granted = perm.location === 'granted' || perm.coarseLocation === 'granted';
    if (!granted) {
      // El usuario rechazó el diálogo o fue denegado permanentemente
      throw new Error(
        'Permiso de ubicación denegado.\n' +
        'Ve a Ajustes → Apps → Valle del Sol → Permisos → Ubicación y actívalo.'
      );
    }

    try {
      const pos = await Geolocation.getCurrentPosition({
        enableHighAccuracy: true,
        timeout: 15000
      });
      return {
        lat: pos.coords.latitude,
        lng: pos.coords.longitude,
        accuracy: pos.coords.accuracy
      };
    } catch (err: any) {
      // GPS apagado, timeout, etc.
      const msg = err?.message || String(err);
      if (msg.includes('timeout') || msg.includes('Timeout')) {
        throw new Error('Tiempo de espera agotado. Asegúrate de tener el GPS activado.');
      }
      throw new Error('No se pudo obtener la ubicación: ' + msg);
    }
  }

  /* ------------------------------------------------------------------ */
  /*  Web (navigator.geolocation)                                        */
  /* ------------------------------------------------------------------ */
  private browserPosition(): Promise<GeoPosition> {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocalización no soportada en este navegador.'));
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (pos) => resolve({
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
          accuracy: pos.coords.accuracy
        }),
        (err) => {
          const msgs: Record<number, string> = {
            1: 'Permiso de ubicación denegado en el navegador.',
            2: 'Ubicación no disponible.',
            3: 'Tiempo de espera agotado al obtener la ubicación.'
          };
          reject(new Error(msgs[err.code] || 'No se pudo obtener la ubicación.'));
        },
        { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
      );
    });
  }
}
