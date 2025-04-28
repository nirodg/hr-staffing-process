import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export type StatusMeta = {
  label: string;
  color: string;
  icon: string;
  animate?: boolean;
};

const STATUS_BY_ACTIVE: Record<'true' | 'false', StatusMeta> = {
  true: {
    label: 'Completed',
    color: 'bg-green-500',
    icon: '✅',
  },
  false: {
    label: 'In Progress',
    color: 'bg-yellow-400',
    icon: '⏳',
    animate: true,
  },
};
@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="inline-flex items-center gap-2 text-sm">
      <span
        class="h-3 w-3 rounded-full"
        [ngClass]="[
          meta.color,
          meta.animate ? 'animate-pulse' : ''
        ]"
      ></span>
      <!-- <span>{{ meta.icon }} {{ meta.label }}</span> -->
      <span>{{ meta.label }}</span>
    </span>
  `,
})
export class StatusBadgeComponent {
  @Input() isActive = false;

  get meta(): StatusMeta {
    return STATUS_BY_ACTIVE[String(this.isActive) as 'true' | 'false'];
  }
}