import { Component, Inject } from "@angular/core";
import { FormBuilder, FormGroup, ReactiveFormsModule } from "@angular/forms";
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialogModule,
} from "@angular/material/dialog";
import { CommonModule } from "@angular/common";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { ClientDTO } from "src/app/core/models/client-dto.model";
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar";

@Component({
  standalone: true,
  selector: "app-edit-client-dialog",
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
  ],
  template: `
    <h2 mat-dialog-title>Edit Client Info</h2>
    <form [formGroup]="form" (ngSubmit)="save()" class="space-y-4 p-4">
      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Client Name</mat-label>
        <input matInput formControlName="clientName" />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Client Email</mat-label>
        <input matInput formControlName="clientEmail" />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Contact Person Name</mat-label>
        <input matInput formControlName="contactPersonName" />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Contact Person Email</mat-label>
        <input matInput formControlName="contactPersonEmail" />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Contact Person Phone</mat-label>
        <input matInput formControlName="contactPersonPhone" />
      </mat-form-field>

      <div class="flex justify-end gap-2 mt-4">
        <button mat-button type="button" (click)="dialogRef.close()">
          Cancel
        </button>
        <button
          mat-flat-button
          color="primary"
          type="submit"
          [disabled]="!hasChanged"
        >
          💾 Save
        </button>
      </div>
    </form>
  `,
})
export class EditClientDialogComponent {
  form: FormGroup;
  initialData: ClientDTO;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: ClientDTO,
    public dialogRef: MatDialogRef<EditClientDialogComponent>,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.initialData = structuredClone(data);

    this.form = this.fb.group({
      clientName: [data.clientName],
      clientEmail: [data.clientEmail],
      contactPersonName: [data.contactPersonName],
      contactPersonEmail: [data.contactPersonEmail],
      contactPersonPhone: [data.contactPersonPhone],
    });

    this.form.valueChanges.subscribe(() => {
      this.form.markAsTouched(); // ensures re-eval of button state
    });
  }

  get hasChanged(): boolean {
    return !this.isEqual(this.initialData, this.form.value);
  }

  isEqual(a: any, b: any): boolean {
    const aKeys = Object.keys(a);
    const bKeys = Object.keys(b);

    if (aKeys.length !== bKeys.length) return false;

    for (const key of aKeys) {
      if (a[key] !== b[key]) return false;
    }

    return true;
  }

  save(): void {
    if (this.form.valid && this.hasChanged) {
      this.dialogRef.close(this.form.value);
      this.snackBar.open("Client info updated successfully ✅", "Close", {
        duration: 3000,
        panelClass: ["bg-green-600", "text-white"],
      });
    }
  }
}
