import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { ClientDTO } from '../core/models/client-dto.model';
import { UserDTO } from '../core/models/user-dto.model';

@Component({
  selector: 'app-staffing-process-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule
  ],
  template: `
   <div class="w-full max-w-lg bg-white p-6 rounded-2xl shadow-2xl">
  <h2 class="text-xl font-semibold mb-4">Add Staffing Process</h2>

  <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-4">
    <mat-form-field appearance="fill" class="w-full">
      <mat-label>Title</mat-label>
      <input matInput formControlName="title" />
    </mat-form-field>

    <mat-form-field appearance="fill" class="w-full">
      <mat-label>Client</mat-label>
      <mat-select formControlName="clientId" required>
        <mat-option *ngFor="let client of data.clients" [value]="client.id">
          {{ client.clientName }}
        </mat-option>
      </mat-select>
    </mat-form-field>

    <mat-form-field appearance="fill" class="w-full">
      <mat-label>Employee</mat-label>
      <mat-select formControlName="employeeId" required>
        <mat-option *ngFor="let emp of data.employees" [value]="emp.id">
          {{ emp.name }}
        </mat-option>
      </mat-select>
    </mat-form-field>

    <div class="flex justify-end gap-2 pt-2">
      <button mat-button type="button" (click)="close()">Cancel</button>
      <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid">Save</button>
    </div>
  </form>
</div>

  `
})
export class StaffingProcessDialogComponent {
  form = this.fb.group({
    title: ['', Validators.required],
    clientId: [null, Validators.required],
    employeeId: [null, Validators.required]
  });

  constructor(
    private fb: FormBuilder,
    private ref: MatDialogRef<StaffingProcessDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { clients: ClientDTO[]; employees: UserDTO[] }
  ) {}

  close() {
    this.ref.close();
  }

  submit() {
    if (this.form.valid) {
      this.ref.close(this.form.value);
    }
  }
}
