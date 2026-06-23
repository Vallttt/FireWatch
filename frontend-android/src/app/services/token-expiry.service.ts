import { Injectable, inject } from '@angular/core';
import { AuthService } from './auth.service';
import { Subject, interval, takeUntil, filter } from 'rxjs';

/**
 * ✅ TOKEN EXPIRY SERVICE
 * Monitorea la expiración del token y notifica cuando está por vencer
 * Permite al usuario renovar sesión antes de que expire
 */
@Injectable({ providedIn: 'root' })
export class TokenExpiryService {

  private authService = inject(AuthService);

  // ✅ Emite cuando faltan 5 minutos para expirar
  private tokenExpiringSubject = new Subject<{
    timeRemaining: number;
    percentageRemaining: number;
  }>();
  public tokenExpiring$ = this.tokenExpiringSubject.asObservable();

  // ✅ Emite cuando la sesión ha expirado
  private sessionExpiredSubject = new Subject<void>();
  public sessionExpired$ = this.sessionExpiredSubject.asObservable();

  private destroy$ = new Subject<void>();
  private monitoringInterval = 30000; // Check cada 30 segundos

  /**
   * ✅ Iniciar monitoreo de expiración
   */
  startMonitoring(): void {
    this.stopMonitoring();

    interval(this.monitoringInterval)
      .pipe(
        takeUntil(this.destroy$),
        filter(() => this.authService.isLoggedIn())
      )
      .subscribe(() => {
        this.checkTokenExpiry();
      });

    // Chequeo inmediato
    this.checkTokenExpiry();
  }

  /**
   * ✅ Detener monitoreo
   */
  stopMonitoring(): void {
    this.destroy$.next();
  }

  /**
   * ✅ Verificar estado del token
   */
  private checkTokenExpiry(): void {
    const token = this.authService.getToken();

    if (!token) {
      this.sessionExpiredSubject.next();
      return;
    }

    const timeRemaining = this.authService.getTimeUntilExpiry(token);
    const totalExpiry = this.getTotalTokenExpiry(token);
    const percentageRemaining = (timeRemaining / totalExpiry) * 100;

    // ✅ Si faltan menos de 5 minutos (300 segundos)
    if (timeRemaining > 0 && timeRemaining < 300) {
      this.tokenExpiringSubject.next({
        timeRemaining,
        percentageRemaining
      });
    }

    // ✅ Si ya expiró
    if (timeRemaining <= 0) {
      this.sessionExpiredSubject.next();
      this.authService.logout();
    }
  }

  /**
   * ✅ Obtener tiempo total de expiración (desde emisión)
   */
  private getTotalTokenExpiry(token: string): number {
    const payload = this.authService.decodeToken(token);
    if (!payload) return 3600; // Default 1 hora

    // Asumir que el token fue emitido con validez de 15 minutos antes
    return 900; // 15 minutos
  }

  /**
   * ✅ Renovar sesión
   */
  renewSession(): void {
    this.authService.refreshToken().subscribe({
      next: () => {
        console.log('✅ Sesión renovada');
        this.checkTokenExpiry(); // Re-chequear después de renovar
      },
      error: (err) => {
        console.error('❌ Error renovando sesión:', err);
        this.sessionExpiredSubject.next();
      }
    });
  }

  /**
   * ✅ Obtener tiempo formateado para mostrar
   */
  formatTimeRemaining(seconds: number): string {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;

    if (minutes > 0) {
      return `${minutes}m ${secs}s`;
    }
    return `${secs}s`;
  }
}
