import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { ConfigService } from './core/services/config.service';
import { KeycloakAuthService } from './core/services/keycloak-auth.service';
import { NotificationService } from './core/services/notification.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet></router-outlet>`,
})
export class AppComponent implements OnInit {
  private titleService = inject(Title);
  private config = inject(ConfigService);
  private auth = inject(KeycloakAuthService);
  private notificationService = inject(NotificationService);

  ngOnInit(): void {
    this.titleService.setTitle(this.config.siteName);
    this.auth.startAutoTokenRefresh();

    let lastHandled = 0;
    window.addEventListener('storage', (event) => {
      if (event.key === 'logout-event') {
        const now = Date.now();
        if (now - lastHandled > 1000) {
          console.log('🛑 Detected logout in another tab, logging out...');
          lastHandled = now;
          this.auth.logout(); // triggers Keycloak logout
        }
      }
    });

  }
}