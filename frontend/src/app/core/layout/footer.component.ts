import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfigService } from '../services/config.service';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <footer class="w-full bg-white border-t text-center py-4 text-sm text-gray-500">
      © 2025 {{ companyName }}. All rights reserved.
    </footer>
  `,
})
export class FooterComponent {
      private config = inject(ConfigService);
      companyName = this.config.companyName;
}
