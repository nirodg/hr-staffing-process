import { Component, inject } from "@angular/core";
import { NavigationEnd, Router, RouterModule } from "@angular/router";
import { MatDialog } from "@angular/material/dialog";
import { FormDialogComponent } from "../../shared/form-dialog/form-dialog.component"; // adjust path as needed
import { KeycloakAuthService } from "../services/keycloak-auth.service";
import { NgIf } from "@angular/common";

@Component({
  selector: "app-nav-bar",
  standalone: true,
  imports: [RouterModule, NgIf],
  template: `
    <ng-container *ngIf="isAdmin()">
      <nav class="flex gap-4 px-6 py-3 bg-[#0A2540] shadow-md">
        <div class="max-w-3xl w-full mx-auto px-4 flex gap-4">
          <a
            routerLink="/staffing"
            routerLinkActive="text-white bg-[#1E3A8A]"
            [routerLinkActiveOptions]="{ exact: true }"
            class="text-[#C0D3DF] hover:bg-[#1B3B5F] px-4 py-2 rounded-md text-sm font-medium transition-all"
          >
            Staffing
          </a>
          <a
            routerLink="/clients"
            routerLinkActive="text-white bg-[#1E3A8A]"
            class="text-[#C0D3DF] hover:bg-[#1B3B5F] px-4 py-2 rounded-md text-sm font-medium transition-all"
          >
            Clients
          </a>
          <a
            routerLink="/employees"
            routerLinkActive="text-white bg-[#1E3A8A]"
            class="text-[#C0D3DF] hover:bg-[#1B3B5F] px-4 py-2 rounded-md text-sm font-medium transition-all"
          >
            Employees
          </a>
          <!-- <a *ngIf="auth.isAdmin()" routerLink="/clients" class="px-4 py-2 hover:underline">Clients</a>
            <a *ngIf="auth.isAdmin()" routerLink="/employees" class="px-4 py-2 hover:underline">Employees</a>

            <a *ngIf="auth.isAdmin()" routerLink="/staffing" class="px-4 py-2 hover:underline">Staffing</a> -->
        </div>
      </nav>
    </ng-container>
  `,
})
export class NavBarComponent {
  private auth = inject(KeycloakAuthService);
  isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  isReadOnly(): boolean {
    return this.auth.isReadOnly();
  }
}
