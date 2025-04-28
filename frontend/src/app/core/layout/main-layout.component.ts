import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterOutlet } from "@angular/router";
import { TopBarComponent } from "./top-bar.component";
import { NavBarComponent } from "./nav-bar.component";
import { FooterComponent } from "./footer.component";
import { NotificationService } from "src/app/core/services/notification.service";

@Component({
  selector: "app-main-layout",
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    TopBarComponent,
    NavBarComponent,
    FooterComponent,
  ],
  template: `
    <div class="min-h-screen flex flex-col bg-gray-100 text-gray-800">
      <app-top-bar></app-top-bar>
      <app-nav-bar></app-nav-bar>
      <main class="flex-1 py-6">
        <div class="max-w-4xl mx-auto px-4 py-6">
          <div class="bg-white rounded-lg shadow p-6 min-h-[300px]">
            <router-outlet></router-outlet>
          </div>
        </div>
      </main>

      <app-footer></app-footer>
    </div>
  `,
})
export class MainLayoutComponent {}
