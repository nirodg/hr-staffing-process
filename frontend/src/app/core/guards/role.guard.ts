import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { KeycloakAuthService } from '../services/keycloak-auth.service';
import { Router } from '@angular/router';

export const RoleGuard: CanActivateFn = (route, state) => {
  const auth = inject(KeycloakAuthService);
  const router = inject(Router);
  const requiredRole = route.data?.['requiredRole'] as string;

  if (!requiredRole || auth.hasRole(requiredRole)) {
    return true;
  }

  // Optional: Redirect to /staffing or /not-authorized
  router.navigate(['/staffing']);
  return false;
};
