import { Component, Inject, OnInit } from "@angular/core";
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
import { EditLockAwareComponent } from "src/app/components/edit-lock-aware/edit-lock-aware.component";
import { KeycloakAuthService } from "src/app/core/services/keycloak-auth.service";
import {
  EditLockDialogBase,
  WithId,
} from "src/app/core/directives/edit-lock-dialog-base";
import { EditLockService } from "src/app/core/services/edit-lock.service";
import { RefreshService } from "src/app/core/services/refresh.service";
import { AbstractEntity } from "src/app/core/models/abstract-dto.model";

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
      <div *ngIf="isLockedByOther" class="text-red-600 text-sm">
        ⚠️ This entity is being edited by {{ editingBy }}. <b>READ ONLY</b> mode
        activated 📖
      </div>
      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Client Name</mat-label>
        <input
          matInput
          formControlName="clientName"
          [readonly]="isLockedByOther"
        />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Client Email</mat-label>
        <input
          matInput
          formControlName="clientEmail"
          [readonly]="isLockedByOther"
        />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Contact Person Name</mat-label>
        <input
          matInput
          formControlName="contactPersonName"
          [readonly]="isLockedByOther"
        />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Contact Person Email</mat-label>
        <input
          matInput
          formControlName="contactPersonEmail"
          [readonly]="isLockedByOther"
        />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Contact Person Phone</mat-label>
        <input
          matInput
          formControlName="contactPersonPhone"
          [readonly]="isLockedByOther"
        />
      </mat-form-field>

      <div class="flex justify-end gap-2 mt-4">
        <button mat-button type="button" (click)="close()">
          Cancel
        </button>
        <button
          mat-flat-button
          color="primary"
          type="submit"
          [disabled]="!hasChanged() || isLockedByOther"
        >
          💾 Save
        </button>
      </div>
    </form>
  `,
})
export class EditClientDialogComponent
  extends EditLockDialogBase<ClientDTO>
  implements OnInit
{
  entity: string = "client";
  entityId: number = this.data.id;
  currentUsername: string = this.auth.getUsername();

  protected onLockAcquired(): void {
    this.form.enable();
  }
  protected onLockLost(): void {
    this.form.disable();
  }

  protected hasChanged(): boolean {
    const current = this.form.getRawValue();
    const original = {
      clientName: this.initialData.clientName,
      clientEmail: this.initialData.clientEmail,
      contactPersonName: this.initialData.contactPersonName,
      contactPersonEmail: this.initialData.contactPersonEmail,
      contactPersonPhone: this.initialData.contactPersonPhone,
    };
    return !this.isEqual(current, original);
  }

  form: FormGroup;
  initialData: ClientDTO;

  ngOnInit(): void {
    this.form.disable(); // initially disable until lock is acquired
    this.acquireLock();
    this.entityId = this.initialData.id;
  }

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: ClientDTO,
    public dialogRef: MatDialogRef<EditClientDialogComponent>,
    fb: FormBuilder,
    private auth: KeycloakAuthService,
    editLock: EditLockService,
    refresh: RefreshService
  ) {
    super(data as Required<ClientDTO>, dialogRef, editLock, refresh);
    this.initialData = structuredClone(data);
    this.form = fb.group({
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

  save(): void {
    if (this.form.valid && this.hasChanged) {
      this.dialogRef.close(this.form.value);
      this.snack.open("Client info updated successfully ✅", "Close", {
        duration: 3000,
        panelClass: ["bg-green-600", "text-white"],
      });
    }
  }
}
