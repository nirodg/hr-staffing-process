import {
  Component,
  Inject,
  OnInit
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { StaffingService } from '../../core/services/staffing.service';
import { ClientService } from '../../core/services/client.service';
import { EmployeeService } from '../../core/services/employee.service';
import { Router } from '@angular/router';
import { StaffingProcessDTO } from 'src/app/core/models/staffing-process-dto.model';

@Component({
  selector: 'app-add-staffing-process',
  standalone: true,
  template: `
    <div class="p-4 min-w-[500px] bg-white dark:bg-gray-100 rounded-xl shadow-lg" *ngIf="form">
      <h2 class="text-lg font-semibold mb-4">Add New Staffing Process</h2>

      <form [formGroup]="form" (ngSubmit)="save()">
        <mat-form-field appearance="fill" class="w-full mb-4">
          <mat-label>Title</mat-label>
          <input matInput formControlName="title" required />
        </mat-form-field>

        <mat-form-field appearance="fill" class="w-full mb-4">
          <mat-label>Select Client</mat-label>
          <mat-select formControlName="clientId" required>
            <mat-option *ngFor="let client of clients" [value]="client.id">
              {{ client.clientName }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="fill" class="w-full mb-4">
          <mat-label>Select Employee</mat-label>
          <mat-select formControlName="employeeId" required>
            <mat-option *ngFor="let employee of employees" [value]="employee.id">
            {{ employee.lastName }} {{ employee.firstName }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <div class="flex justify-end mt-6 gap-2">
          <button mat-button type="button" (click)="dialogRef.close()">Cancel</button>
          <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">
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
    MatSelectModule,
    MatButtonModule
  ]
})
export class AddStaffingProcessComponent implements OnInit {
  form!: FormGroup;
  clients: any[] = [];
  employees: any[] = [];

  constructor(
    private fb: FormBuilder,
    private clientService: ClientService,
    private employeeService: EmployeeService,
    private staffingService: StaffingService,
    private router: Router,
    public dialogRef: MatDialogRef<AddStaffingProcessComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      title: [''],
      clientId: [null],
      employeeId: [null]
    });

    this.clientService.getAll().subscribe(res => this.clients = res);
    this.employeeService.getAll().subscribe(res => this.employees = res);
  }

  save(): void {
    const formValue = this.form.value;
    const payload: StaffingProcessDTO = {
      title: formValue.title,
      client:   { id: formValue.clientId },
      employee: { id: formValue.employeeId }
    };



    this.staffingService.create(payload).subscribe(() => {
      this.dialogRef.close("true");
      this.router.navigate(['/staffing']);
    });
  }
}
