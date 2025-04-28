import { Component, Inject, Input, OnInit, Optional } from "@angular/core";
import { FormBuilder, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { CommonModule } from "@angular/common";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatButtonModule } from "@angular/material/button";
import {
  MatDialogRef,
  MatDialogModule,
  MAT_DIALOG_DATA,
} from "@angular/material/dialog";
import { Router } from "@angular/router";

import { UserService } from "../../core/services/user.service";
import { EmployeeService } from "../../core/services/employee.service";
import { UserDTO } from "../../core/models/user-dto.model";

@Component({
  selector: "app-user-form",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
  ],
  template: `
    <div
      *ngIf="form"
      class="max-w-2xl mx-auto mt-6 bg-white shadow rounded-xl p-6 space-y-6"
    >
      <h2 class="text-xl font-semibold text-gray-800 dark:text-black">
        {{ title }}
      </h2>

      <form [formGroup]="form" (ngSubmit)="save()" class="space-y-4">
        <mat-form-field class="w-full" appearance="fill">
          <mat-label>Username</mat-label>
          <input matInput formControlName="username" />
        </mat-form-field>

        <mat-form-field class="w-full" appearance="fill">
          <mat-label>Email</mat-label>
          <input matInput type="email" formControlName="email" />
        </mat-form-field>

        <mat-form-field class="w-full" appearance="fill">
          <mat-label>First Name</mat-label>
          <input matInput formControlName="firstName" />
        </mat-form-field>

        <mat-form-field class="w-full" appearance="fill">
          <mat-label>Last Name</mat-label>
          <input matInput formControlName="lastName" />
        </mat-form-field>

        <mat-form-field class="w-full" appearance="fill">
          <mat-label>Position</mat-label>
          <input matInput formControlName="position" />
        </mat-form-field>

        <mat-checkbox formControlName="available">
          Available for new projects
        </mat-checkbox>

        <div class="flex justify-between pt-4">
          <!-- <button mat-raised-button color="primary" type="submit">Save</button>
        <button mat-button color="warn" type="button" (click)="delete()">Delete Profile</button> -->
        </div>
      </form>
    </div>
  `,
})
export class UserFormComponent implements OnInit {
  @Input() mode: "profile" | "create" = "profile";
  form!: FormGroup;
  title = "";

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private employeeService: EmployeeService,
    private router: Router,
    @Optional() private dialogRef?: MatDialogRef<UserFormComponent>,
    @Optional()
    @Inject(MAT_DIALOG_DATA)
    private data?: { mode: "profile" | "create" }
  ) {}

  ngOnInit(): void {
    this.mode = this.data?.mode ?? "profile";
    this.title =
      this.mode === "profile" ? "Your Profile" : "Add a New Employee";

    if (this.mode === "profile") {
      this.userService.getMyProfile().subscribe((user) => this.buildForm(user));
    } else {
      this.buildForm(); // empty form for creation
    }
  }

  private buildForm(user?: Partial<UserDTO>) {
    this.form = this.fb.group({
      username: [
        { value: user?.username ?? "", disabled: this.mode === "profile" },
      ],
      firstName: [{ value: user?.firstName ?? "", disabled: this.mode === "profile" }],
      lastName: [{ value: user?.lastName ?? "", disabled: this.mode === "profile" }],
      email: [{ value: user?.email ?? "", disabled: this.mode === "profile" }],
      position: [{ value: user?.position ?? "", disabled: this.mode === "profile" }],
      available: [{value: user?.available ?? "", disabled: this.mode === "profile"}],
    });
  }

  save(): void {
    const payload = this.form.getRawValue();

    if (this.mode === "profile") {
      this.userService.updateMyProfile(payload).subscribe(() => {
        alert("Profile updated!");
        this.dialogRef.close();
      });
    } else {
      this.employeeService.create(payload).subscribe(() => {
        alert("Employee added!");
        this.dialogRef.close();
        this.router.navigate(["/employees"]);
      });
    }
  }
}
