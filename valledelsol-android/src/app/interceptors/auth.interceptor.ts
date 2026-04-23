import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Interceptor funcional (Angular 20 standalone).
 * Adjunta el JWT almacenado en localStorage a cada request que lo necesite.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('jwt_token');

  if (token) {
    const cloned = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(cloned);
  }

  return next(req);
};
