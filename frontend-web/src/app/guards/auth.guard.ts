import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

/**
 * Protege las rutas que requieren sesión iniciada.
 * Permite el acceso si hay un JWT válido, o si el usuario entró por el
 * modo de emergencia (ciudadano sin cuenta reportando un incendio).
 */
export const authGuard: CanActivateFn = () => {
  const hasToken = !!localStorage.getItem('jwt_token');
  const emergencyMode = localStorage.getItem('emergencyMode') === 'true';

  if (hasToken || emergencyMode) {
    return true;
  }

  return inject(Router).createUrlTree(['/login']);
};
