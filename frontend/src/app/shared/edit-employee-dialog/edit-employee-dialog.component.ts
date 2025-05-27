import { Component, Inject, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, ReactiveFormsModule } from "@angular/forms";
import {
  MatDialogRef,
  MAT_DIALOG_DATA,
  MatDialogModule,
} from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatButtonModule } from "@angular/material/button";
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar";
import { CommonModule } from "@angular/common";
import { UserDTO } from "src/app/core/models/user-dto.model";
import { KeycloakAuthService } from "src/app/core/services/keycloak-auth.service";
import { UserService } from "src/app/core/services/user.service";

function isEqual(a: any, b: any): boolean {
  const aKeys = Object.keys(a);
  const bKeys = Object.keys(b);
  if (aKeys.length !== bKeys.length) return false;
  for (const key of aKeys) {
    if (a[key] !== b[key]) return false;
  }
  return true;
}

@Component({
  selector: "app-edit-employee-dialog",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSnackBarModule,
  ],
  template: `
    <h2 mat-dialog-title>Edit Employee</h2>
    <form [formGroup]="form" (ngSubmit)="save()" class="p-4 space-y-4">
      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Email</mat-label>
        <input matInput formControlName="email" />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>First Name</mat-label>
        <input matInput formControlName="firstName" />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Last Name</mat-label>
        <input matInput formControlName="lastName" />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Position</mat-label>
        <input matInput formControlName="position" />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Role</mat-label>
        <mat-select
          formControlName="roles"
          [disabled]="editingSelf && isLastAdmin"
        >
          <mat-option *ngFor="let role of roles" [value]="role.value">
            {{ role.label }}
          </mat-option>
        </mat-select>
      </mat-form-field>

      <div *ngIf="editingSelf && isLastAdmin" class="text-red-600 text-sm">
        ❗ You are the only ADMIN. You cannot change your role.
      </div>

      <div class="flex justify-end gap-2 mt-4">
        <button mat-button (click)="close()">Cancel</button>
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
export class EditEmployeeDialogComponent implements OnInit {
  form: FormGroup;
  initialData: UserDTO;
  editingSelf = false;
  isLastAdmin = false;
  roles = [
    { value: "CLIENT_PUBLIC_ADMIN", label: "Admin" },
    { value: "CLIENT_PUBLIC_USER", label: "User" },
  ];
  constructor(
    @Inject(MAT_DIALOG_DATA) public data: UserDTO,
    public dialogRef: MatDialogRef<EditEmployeeDialogComponent>,
    private fb: FormBuilder,
    private auth: KeycloakAuthService,
    private userService: UserService
  ) {
    this.initialData = structuredClone(data);
    this.form = this.fb.group({
      available: [data.available],
      roles: [data.roles?.[0] || "CLIENT_PUBLIC_USER"],
      email: [data.email],
      firstName: [data.firstName],
      lastName: [data.lastName],
      position: [data.position],
    });

    this.form.valueChanges.subscribe(() => this.form.markAsTouched());
  }

  ngOnInit(): void {
    this.editingSelf = this.auth.getUsername() === this.data.username;
    this.userService.countAdmins().subscribe((count) => {
      this.isLastAdmin = count <= 1;
    });
  }

  get hasChanged(): boolean {
    const current = this.form.getRawValue();
    const original = {
      available: this.initialData.available,
      roles: this.initialData.roles?.[0] ?? "CLIENT_PUBLIC_USER",
      email: this.initialData.email,
      firstName: this.initialData.firstName,
      lastName: this.initialData.lastName,
      position: this.initialData.position,
    };

    return !isEqual(current, original);
  }

  close(): void {
    console.log(this.hasChanged);
    this.dialogRef.close();
  }

  save(): void {
    if (this.form.valid && this.hasChanged) {
      this.dialogRef.close(this.form.value);
    }
  }
}
