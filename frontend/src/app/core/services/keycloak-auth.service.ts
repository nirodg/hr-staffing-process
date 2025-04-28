import { inject, Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { KeycloakUserInfo } from '../models/keycloak-user-info.model';
import { PERMISOS } from '../permissions/permissions';


@Injectable({ providedIn: 'root' })
export class KeycloakAuthService {
  private keycloak = inject(Keycloak);

  logout(): void {
    // Broadcast logout across tabs
    localStorage.setItem('logout-event', Date.now().toString());
  
    // Redirect to Keycloak logout
    this.keycloak.logout(`${window.location.origin}/logout`);
  }  

  login(): void {
    this.keycloak.login();
  }

  getUsername(): string {
    return this.getParsedToken().preferred_username ?? this.getFullName();
  }

  getEmail(): string {
    const parsed = this.keycloak.tokenParsed as KeycloakUserInfo | undefined;
    return parsed?.email ?? "undefined";
  }

  getFullName(): string {
    const parsed = this.keycloak.tokenParsed as KeycloakUserInfo | undefined;
    return parsed?.name ?? `${parsed?.given_name ?? ''} ${parsed?.family_name ?? ''}`.trim();
  }

  getParsedToken(): KeycloakUserInfo | undefined {
    return this.keycloak.tokenParsed as KeycloakUserInfo | undefined;
  }

  getToken(): string | undefined {
    return this.keycloak.token;
  }

  getRoles(): string[] {
    return this.keycloak.realmAccess?.roles ?? [];
  }

  hasRole(role: string): boolean {
    return this.getRoles().includes(role);
  }

  isAdmin(): boolean {
    return this.hasRole('client_public_admin');
  }

  isReadOnly(): boolean {
    return this.hasRole('client_public_user');
  }

  isAuthenticated(): boolean {
    return !!this.keycloak.token;
  }

  startAutoTokenRefresh(intervalMs = 60000) {
    setInterval(() => {
      this.keycloak.updateToken(70).catch(() => {
        console.warn('🔐 Token refresh failed, logging out');
        this.logout();
      });
    }, intervalMs);
  }

  // Example
  // <button [disabled]="!auth.hasPermission('CAN_EDIT')">Save</button>
  hasPermission(permissionKey: keyof typeof PERMISOS): boolean {
    const roles = this.getRoles();
    const allowed = PERMISOS[permissionKey];
    if (Array.isArray(allowed)) return allowed.some(role => roles.includes(role));
    return roles.includes(allowed);
  }
}
