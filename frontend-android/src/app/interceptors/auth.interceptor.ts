import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError, BehaviorSubject, filter, take } from 'rxjs';

const publicUrls = [
  '/auth/login',
  '/api/users/register',
  '/api/auth/password/forgot',
  '/api/auth/password/reset',
  '/api/auth/emergency-mode',
  '/api/zones',
  '/api/evacuation-routes'
];

let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (req, next): any => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // ✅ Permitir URLs públicas sin token
  if (publicUrls.some(url => req.url.includes(url))) {
    return next(req);
  }

  const token = authService.getToken();

  // ✅ Si no hay token, ir sin autenticación
  if (!token) {
    return next(req);
  }

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
