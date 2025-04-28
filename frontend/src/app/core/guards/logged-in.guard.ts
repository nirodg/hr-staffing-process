import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { KeycloakAuthService } from '../services/keycloak-auth.service';
import { Router } from '@angular/router';

export const LoggedInGuard: CanActivateFn = async (route, state) => {
  const auth = inject(KeycloakAuthService);
  const router = inject(Router);

  const isLoggedIn = await auth.isAuthenticated();
  if (!isLoggedIn) {
    router.navigate(['/login']);
    return false;
  }

  return true;
};
