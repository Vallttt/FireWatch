import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const publicUrls = [
    '/auth/login',
    '/api/users/register',
    '/api/auth/password/forgot',
    '/api/auth/password/reset'
  ];

  if (publicUrls.some(url => req.url.includes(url))) {
    return next(req);
  }

  const token = localStorage.getItem('jwt_token');

  if (token) {
    const cloned = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(cloned);
  }

  return next(req);
};