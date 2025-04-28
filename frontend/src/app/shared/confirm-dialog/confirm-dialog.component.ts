import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule],
  template: `
    <h2 class="text-lg font-bold mb-4">Are you sure?</h2>
    <p class="mb-6 text-gray-700">This action cannot be undone.</p>
    <div class="flex justify-end gap-2">
      <button mat-button (click)="dialogRef.close(false)">No</button>
      <button mat-button color="warn" (click)="dialogRef.close(true)">Yes</button>
    </div>
  `,
})
export class ConfirmDialogComponent {
  dialogRef = inject(MatDialogRef<ConfirmDialogComponent>);
}