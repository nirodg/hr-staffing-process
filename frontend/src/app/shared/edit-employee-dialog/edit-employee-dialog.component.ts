import {
  AfterViewInit,
  Component,
  Inject,
  OnDestroy,
  OnInit,
} from "@angular/core";
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
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { CommonModule } from "@angular/common";
import { UserDTO } from "src/app/core/models/user-dto.model";
import { KeycloakAuthService } from "src/app/core/services/keycloak-auth.service";
import { UserService } from "src/app/core/services/user.service";
import {
  EditLockService
} from "src/app/core/services/edit-lock.service";
import { RefreshService } from "src/app/core/services/refresh.service";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { EditLockDialogBase } from "src/app/core/directives/edit-lock-dialog-base";

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
    MatProgressBarModule,
  ],
  template: `
    <h2 mat-dialog-title>Edit Employee</h2>

    <mat-progress-bar
      *ngIf="waitingForLock"
      mode="indeterminate"
      class="mb-2"
    ></mat-progress-bar>

    <form
      [formGroup]="form"
      (ngSubmit)="save()"
      class="p-4 pt-0 space-y-4"
      novalidate
    >
      <div *ngIf="isLockedByOther" class="text-red-600 text-sm">
        ⚠️ This entity is being edited by {{ editingBy }}. <b>READ ONLY</b> mode
        activated 📖
      </div>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Email</mat-label>
        <input matInput formControlName="email" [readonly]="isLockedByOther" />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>First Name</mat-label>
        <input
          matInput
          formControlName="firstName"
          [readonly]="isLockedByOther"
        />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Last Name</mat-label>
        <input
          matInput
          formControlName="lastName"
          [readonly]="isLockedByOther"
        />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Position</mat-label>
        <input
          matInput
          formControlName="position"
          [readonly]="isLockedByOther"
        />
      </mat-form-field>

      <mat-form-field appearance="fill" class="w-full">
        <mat-label>Role</mat-label>
        <mat-select
          formControlName="roles"
          [disabled]="isLockedByOther || (editingSelf && isLastAdmin)"
        >
          <mat-option *ngFor="let role of roles" [value]="role.value">{{
            role.label
          }}</mat-option>
        </mat-select>
      </mat-form-field>

      <div *ngIf="editingSelf && isLastAdmin" class="text-red-600 text-sm">
        ❗ You are the only ADMIN. You cannot change your role.
      </div>

      <div class="flex justify-end gap-2">
        <button mat-button (click)="close()">Cancel</button>
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
export class EditEmployeeDialogComponent
  extends EditLockDialogBase<UserDTO>
  implements OnInit, AfterViewInit, OnDestroy
{
  protected hasChanged(): boolean {
    const current = this.form.getRawValue();
    const original = {
      available: this.initialData.available,
      roles: this.initialData.roles?.[0] ?? "CLIENT_PUBLIC_USER",
      email: this.initialData.email,
      firstName: this.initialData.firstName,
      lastName: this.initialData.lastName,
      position: this.initialData.position,
    };
    return !this.isEqual(current, original);
  }
  protected onLockAcquired(): void {
    this.form.enable();
  }
  protected onLockLost(): void {
    this.form.disable();
  }

  entity: string = "users";
  entityId: number = this.data.id;
  currentUsername = this.auth.getUsername();

  form: FormGroup;
  initialData: UserDTO;

  editingSelf = false;
  isLastAdmin = false;

  waitingForLock = false;
  editingBy: string | null = null;

  roles = [
    { value: "CLIENT_PUBLIC_ADMIN", label: "Admin" },
    { value: "CLIENT_PUBLIC_USER", label: "User" },
  ];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: UserDTO,
    private fb: FormBuilder,
    private auth: KeycloakAuthService,
    private userService: UserService,
    editLockService: EditLockService,
    refreshService: RefreshService,
    public dialogRef: MatDialogRef<EditEmployeeDialogComponent>
  ) {
    super(data, dialogRef, editLockService, refreshService);
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

  get isLockedByOther(): boolean {
    return !!this.editingBy && this.editingBy !== this.currentUsername;
  }

  ngOnInit(): void {
    this.editingSelf = this.auth.getUsername() === this.data.username;
    this.userService
      .countAdmins()
      .subscribe((c) => (this.isLastAdmin = c <= 1));

    this.acquireLock();  
  }

  save(): void {
    if (this.form.valid && this.hasChanged) {
      const raw = this.form.getRawValue();
      this.dialogRef.close({ ...raw, roles: [raw.roles] });
    }
  }
}
