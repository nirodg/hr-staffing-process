import { CommonModule } from "@angular/common";
import { Component } from "@angular/core";
import { Router, RouterModule } from "@angular/router";
import { KeycloakAuthService } from "src/app/core/services/keycloak-auth.service";

@Component({
  selector: "app-logout",
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-100 px-4">
      <div
        class="max-w-md w-full text-center bg-white shadow-xl rounded-2xl p-8"
      >
        <div class="text-5xl mb-4 text-blue-500">👋</div>
        <h1 class="text-2xl font-semibold mb-2">You've been logged out</h1>
        <p class="text-gray-600 mb-6">Thank you for using the app.</p>
        <a routerLink="/" class="text-blue-600 hover:underline text-sm">
          Log in again
        </a>
      </div>
    </div>
  `,
})
export class LogoutComponent {
  constructor(
    private auth: KeycloakAuthService,
    private router: Router
  ) {}

  async ngOnInit() {
    const isLoggedIn = await this.auth.isAuthenticated();

    if (isLoggedIn) {
      history.length > 1 ? history.back() : this.router.navigate(["/staffing"]);
    } else {
      this.router.navigate(["/login"]);
    }
  }
}
