import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { KeycloakAuthService } from 'src/app/core/services/keycloak-auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-100 px-4">
      <div class="max-w-md w-full text-center bg-white shadow-xl rounded-2xl p-8">
        <div class="text-5xl mb-4 text-blue-600">🔐</div>
        <h1 class="text-2xl font-semibold mb-2">Authentication Required</h1>
        <p class="text-gray-600 mb-6">You must log in to access this app.</p>
        <button
          (click)="login()"
          class="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700 transition"
        >
          Login with Keycloak
        </button>
      </div>
    </div>
  `
})
export class LoginComponent {
  constructor(
    private auth: KeycloakAuthService,
    private router: Router
  ) {}

  async ngOnInit() {
    const loggedIn = await this.auth.isAuthenticated();
    if (loggedIn) {
      this.router.navigate(['/staffing']);
    }
  }

  login() {
    this.auth.login();
  }
}
