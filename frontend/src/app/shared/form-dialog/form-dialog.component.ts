import { Component, Input, Output, EventEmitter, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { HttpClientModule, HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    HttpClientModule
  ],
  template: `
    <div class="w-[400px] rounded-2xl shadow-xl bg-white p-6">
      <h2 class="text-xl font-semibold mb-4">{{ title }}</h2>

      <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-4">
      <mat-form-field *ngIf="selectConfig" appearance="fill" class="w-full">
        <mat-label>{{ selectConfig.label }}</mat-label>
        <mat-select [formControlName]="selectConfig.formControlName" required>
          <mat-option
            *ngFor="let option of selectConfig.options"
            [value]="option.value"
          >
            {{ option.label }}
          </mat-option>
        </mat-select>
      </mat-form-field>


        <div class="flex justify-end gap-2">
          <button mat-button type="button" (click)="dialogRef.close()">Cancel</button>
          <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">
            Save
          </button>
        </div>
      </form>
    </div>
  `,
})
export class FormDialogComponent implements OnInit {
  @Input() title = 'Add';
  @Input() initialValue: string = 'Select an intem';

  @Input() selectConfig?: {
    label: string;
    formControlName: string;
    options: { label: string; value: any }[];
  };
  

  public dialogRef = inject(MatDialogRef<FormDialogComponent>);
  private fb = inject(FormBuilder);

  data: { id: number; name: string }[] = [];

  form: FormGroup = this.fb.group({
    name: ['', Validators.required],
    // Add this dynamically in ngOnInit
  });

  ngOnInit() {
    if (this.selectConfig) {
      this.form.addControl(
        this.selectConfig.formControlName,
        this.fb.control('', Validators.required)
      );
    }
  
    if (this.initialValue) {
      this.form.patchValue({ name: this.initialValue });
    }
  }

  submit() {
    if (this.form.valid) {
      // this.save.emit(this.form.value);
      this.dialogRef.close();
    }
  }
}
