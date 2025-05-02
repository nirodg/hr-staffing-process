import { CommonModule } from "@angular/common";
import { MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";
import { MatMenuModule } from "@angular/material/menu";
import { Component, inject } from "@angular/core";
import { ConfigService } from "../services/config.service";
import { KeycloakAuthService } from "../services/keycloak-auth.service";
import { RouterModule } from "@angular/router";

@Component({
  selector: "app-top-bar",
  standalone: true,
  imports: [CommonModule, MatMenuModule, MatIconModule, MatButtonModule, RouterModule],
  template: `
    <header class="w-full bg-white shadow-md px-6 py-4">
      <div class="flex justify-between items-center">
        <!-- Centered logo + site name (flex-1 center via absolute) -->
        <div
          class="absolute left-1/2 transform -translate-x-1/2 flex items-center gap-2 text-xl font-bold text-black-600"
        >
          <span><img src="assets/logo.png" width="40" /></span>
          <span>{{ siteName }}</span>
        </div>

        <!-- User info (right side) -->
        <div class="ml-auto">
          <button
            mat-button
            [matMenuTriggerFor]="menu"
            class="flex items-center gap-1 hover:text-blue-600"
          >
            <mat-icon>account_circle</mat-icon> {{ username }}
          </button>
          <mat-menu #menu="matMenu">
            <a
              routerLink="/profile"
              class="button block w-full px-4 py-2 text-left text-sm hover:bg-gray-100"
            >
              👤 Profile
            </a>

            <button mat-menu-item (click)="logout()">Logout</button>
          </mat-menu>
        </div>
      </div>
    </header>
  `,
})
export class TopBarComponent {
  private config = inject(ConfigService);
  private kc = inject(KeycloakAuthService);

  siteName = this.config.siteName;

  get username(): string {
    return this.kc.getFullName();
  }

  logout(): void {
    console.log("1");
    this.kc.logout();
  }
}
