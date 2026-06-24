import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError, BehaviorSubject, filter, take } from 'rxjs';

// URLs de autenticación: nunca llevan token (ni se intenta refrescar sobre ellas).
// OJO: aquí NO van /api/zones ni /api/evacuation-routes — esas son públicas SOLO
// para lectura anónima (GET). Si se incluyeran, las operaciones de admin
// (POST/PUT/DELETE /api/zones) irían sin token y el gateway respondería 401.
const authUrls = [
  '/auth/login',
  '/api/users/register',
  '/api/users/password/forgot',
  '/api/users/password/reset',
  '/api/auth/emergency-mode'
];

// Endpoints de LECTURA pública (GET): el ciudadano anónimo los usa sin login.
// NO se les debe adjuntar token: si el token está expirado/ inválido, el gateway
// rechaza con 401 incluso una ruta pública (valida el bearer antes del permitAll).
// Las ESCRITURAS (POST/PUT/DELETE) sí necesitan token y se manejan abajo.
const publicReadPrefixes = [
  '/api/zones',
  '/api/evacuation-routes'
];

let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (req, next): any => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // ✅ Endpoints de autenticación: nunca llevan token
  if (authUrls.some(url => req.url.includes(url))) {
    return next(req);
  }

  // ✅ Lecturas públicas (GET zonas / rutas): nunca llevan token, así un token
  //    expirado no rompe la carga pública con un 401.
  const isPublicRead = req.method === 'GET'
    && publicReadPrefixes.some(url => req.url.includes(url));
  if (isPublicRead) {
    return next(req);
  }

  const token = authService.getToken();

  // ✅ Sin token (ciudadano anónimo): pasa sin auth. Las rutas públicas de
  //    lectura (GET /api/zones, /api/evacuation-routes) las permite el gateway.
  if (!token) {
    return next(req);
  }

  // ✅ Con token: se adjunta SIEMPRE, para que las operaciones de admin
  //    (POST/PUT/DELETE /api/zones, etc.) lleguen autenticadas al gateway.

  // ✅ Validar si token está expirado
  if (authService.isTokenExpired(token)) {
    // ✅ Token expirado, intentar refrescar
    return authService.refreshToken().pipe(
      switchMap((response) => {
        return next(addTokenToRequest(req, response.token));
      }),
      catchError((refreshError) => {
        authService.logout();
        router.navigate(['/login']);
        return throwError(() => refreshError);
      })
    );
  }

  // ✅ Token válido, adjuntar al request
  return next(addTokenToRequest(req, token)).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        return handle401Error(req, next, authService, router);
      }
      return throwError(() => error);
    })
  );
};

/**
 * ✅ Manejo de error 401: refrescar token e intentar de nuevo
 */
function handle401Error(
  req: any,
  next: any,
  authService: AuthService,
  router: Router
): any {
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    return authService.refreshToken().pipe(
      switchMap((response) => {
        isRefreshing = false;
        refreshTokenSubject.next(response.token);
        return next(addTokenToRequest(req, response.token));
      }),
      catchError((refreshError) => {
        isRefreshing = false;
        authService.logout();
        router.navigate(['/login']);
        return throwError(() => refreshError);
      })
    );
  } else {
    return refreshTokenSubject.pipe(
      filter((token) => token != null),
      take(1),
      switchMap((token) => {
        return next(addTokenToRequest(req, token!));
      })
    );
  }
}

/**
 * ✅ Agregar token al header del request
 */
function addTokenToRequest(req: any, token: string) {
  return req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
      'X-Requested-With': 'XMLHttpRequest'
    }
  });
}
