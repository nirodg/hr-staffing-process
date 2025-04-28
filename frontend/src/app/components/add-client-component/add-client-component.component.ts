import { Component } from "@angular/core";
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from "@angular/forms";
import { MatDialogRef } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { CommonModule } from "@angular/common";

import { ClientService } from "../../core/services/client.service";

@Component({
  selector: "app-add-client",
  standalone: true,
  template: `
    <div
      class="p-6 min-w-[400px] bg-white dark:bg-gray-100 rounded-xl shadow-xl"
      *ngIf="form"
    >
      <h2 class="text-lg font-semibold mb-4">Add New Client</h2>

      <form [formGroup]="form" (ngSubmit)="save()" class="space-y-4">
        <mat-form-field appearance="fill" class="w-full">
          <mat-label>Client Name</mat-label>
          <input matInput formControlName="clientName" required />
        </mat-form-field>

        <!-- Client Email -->
        <mat-form-field appearance="fill" class="w-full">
          <mat-label>Company Email</mat-label>
          <input matInput formControlName="clientEmail" type="email" required />
        </mat-form-field>

        <!-- Contact Person Name -->
        <mat-form-field appearance="fill" class="w-full">
          <mat-label>Contact Name</mat-label>
          <input matInput formControlName="contactPersonName" required />
        </mat-form-field>

        <!-- Contact Email -->
        <mat-form-field appearance="fill" class="w-full">
          <mat-label>Contact Email</mat-label>
          <input
            matInput
            formControlName="contactPersonEmail"
            type="email"
            required
          />
        </mat-form-field>

        <!-- Contact Phone -->
        <mat-form-field appearance="fill" class="w-full">
          <mat-label>Contact Phone</mat-label>
          <input matInput formControlName="contactPersonPhone" type="tel" required/>
        </mat-form-field>

        <div class="flex justify-end pt-2 gap-2">
          <button mat-button type="button" (click)="dialogRef.close()">
            Cancel
          </button>
          <button
            mat-raised-button
            color="primary"
            type="submit"
            [disabled]="form.invalid"
          >
            Save
          </button>
        </div>
      </form>
    </div>
  `,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
})
export class AddClientComponent {
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private clientService: ClientService,
    public dialogRef: MatDialogRef<AddClientComponent>
  ) {
    this.form = this.fb.group({
      clientName: ['', Validators.required],
      clientEmail: ['', [Validators.required, Validators.email]],
      contactPersonName: ['', Validators.required],
      contactPersonEmail: ['', [Validators.required, Validators.email]],
      contactPersonPhone: ['', [Validators.required]]
    });
  }

  save(): void {
    if (this.form.invalid) return;

    this.clientService.create(this.form.value).subscribe(() => {
      this.dialogRef.close(true);
    });
  }
}
