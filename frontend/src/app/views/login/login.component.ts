import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { KeycloakAuthService } from 'src/app/core/services/keycloak-auth.service';
import { environment } from 'src/environments/environment'

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="min-h-screen bg-gray-400 flex items-center justify-center">
      <div class="bg-white p-10 rounded-2xl shadow-md w-full max-w-md text-center">
        <img src="assets/logo.png" alt="Logo" class="mx-auto mb-6" width="120" height="auto">
        <h1 class="text-2xl font-bold mb-2">{{ this.appName }}</h1>
        <p class="mb-6 text-gray-600">Secure access to your account</p>
        <button
          (click)="login()"
          class="w-full py-3 px-6 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition"
        >
          
          Log in with Keycloak
        </button>
        <a href="#" class="block mt-4 text-sm text-gray-500 hover:text-blue-600">Need help?</a>
      </div>
    </div>
  `,
})
export class LoginComponent {

  public appName = environment.appName;

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
