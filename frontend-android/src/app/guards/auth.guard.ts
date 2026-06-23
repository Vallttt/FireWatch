import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

/**
 * ✅ AUTH GUARD: Protege rutas que requieren autenticación
 * - Valida que el token no esté expirado
 * - Intenta refrescar si está próximo a expirar
 * - Permite modo emergencia (ciudadano sin cuenta)
 * - Verifica autorización por rol (si es necesario)
 */
export const authGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // ✅ Permitir acceso si está en modo emergencia
  const emergencyMode = localStorage.getItem('emergencyMode') === 'true';
  if (emergencyMode) {
    return true;
  }

  // ✅ Obtener token
  const token = authService.getToken();

  // ✅ Si no hay token, ir al login
  if (!token) {
    return router.createUrlTree(['/login']);
  }

  // ✅ Validar que el token no está expirado
  if (authService.isTokenExpired(token)) {
    // ✅ Intentar refrescar token
    return authService.refreshToken().pipe(
      map(() => {
        // ✅ Token refrescado exitosamente
        const requiredRole = route.data['role'];

        if (requiredRole) {
          // ✅ Si la ruta requiere un rol específico, validar
          return authService.getUserRole() === requiredRole
            ? true
            : router.createUrlTree(['/unauthorized']);
        }
        return true;
      }),
      catchError(() => {
        // ✅ No se pudo refrescar, ir al login
        authService.logout();
        return of(router.createUrlTree(['/login']));
      })
    );
  }

  // ✅ Token válido, verificar rol si es necesario
  const requiredRole = route.data['role'];
  if (requiredRole) {
    const userRole = authService.getUserRole();
    if (userRole !== requiredRole) {
      return router.createUrlTree(['/unauthorized']);
    }
  }

  return true;
};

/**
 * ✅ ADMIN GUARD: Protege rutas que requieren rol ADMIN
 */
export const adminGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getToken();

  if (!token || authService.isTokenExpired(token)) {
    return router.createUrlTree(['/login']);
  }

  if (authService.getUserRole() !== 'admin') {
    return router.createUrlTree(['/unauthorized']);
  }

  return true;
};

/**
 * ✅ CITIZEN GUARD: Protege rutas que requieren rol USER/CITIZEN
 */
export const citizenGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getToken();
  const emergencyMode = localStorage.getItem('emergencyMode') === 'true';

  if (!token && !emergencyMode) {
    return router.createUrlTree(['/login']);
  }

  if (token && authService.isTokenExpired(token)) {
    return authService.refreshToken().pipe(
      map(() => true),
      catchError(() => {
        authService.logout();
        return of(router.createUrlTree(['/login']));
      })
    );
  }

  return true;
};
