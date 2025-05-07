import { Component, OnInit, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { HttpClient, HttpClientModule } from "@angular/common/http";
import { MatTableModule } from "@angular/material/table";
import { MatIcon, MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";
import { MatDialog } from "@angular/material/dialog";

import { StaffingProcessDialogComponent } from "../../staffing-process-dialog-component/staffing-process-dialog-component.component";

import { getStatusFromActive } from "../../core/constants/status-map";
import { KeycloakAuthService } from "src/app/core/services/keycloak-auth.service";

import { ClientService } from "../../core/services/client.service";
import { EmployeeService } from "../../core/services/employee.service";
import { Route, Router } from "@angular/router";
import { StaffingService } from "src/app/core/services/staffing.service";
import { StaffingProcessDTO } from "src/app/core/models/staffing-process-dto.model";
import { StatusBadgeComponent } from "../../shared/status-badge-component/status-badge-component.component";
import { RefreshService } from "src/app/core/services/refresh.service";

@Component({
  selector: "app-list-staffing-process",
  standalone: true,
  imports: [
    CommonModule,
    HttpClientModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    StatusBadgeComponent
],
  template: `
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-xl font-bold mb-4">Staffing Process List</h2>
      <button
        mat-raised-button
        color="primary"
        *ngIf="isAdmin()"
        (click)="openAddDialog()"
      >
        + Add new process
      </button>
    </div>

    <div class="bg-gray-100 rounded-xl shadow p-6 font-jakarta text-gray-800">

  <div class="overflow-x-auto">
    <table class="w-full table-auto border-collapse">
      <thead class="bg-gray-50 border-b text-sm font-semibold text-gray-600">
        <tr>
          <th class="px-4 py-3 text-left">Title</th>
          <th class="px-4 py-3 text-left">Client</th>
          <th class="px-4 py-3 text-left">Employee</th>
          <th class="px-4 py-3 text-left">Status</th>
          <th class="px-4 py-3 text-left">Action</th>
        </tr>
      </thead>
      <tbody class="text-sm font-normal">
        <tr *ngFor="let row of dataSource" class="hover:bg-gray-100 border-b">
          <td class="px-4 py-3">{{ row.title }}</td>
          <td class="px-4 py-3">{{ row.client.clientName }}</td>
          <td class="px-4 py-3">{{ row.employee.lastName }} {{ row.employee.firstName }}</td>
          <td>
            <app-status-badge [isActive]="!row.active" />
          </td>
          <td class="px-4 py-3 text-right">
            <button mat-button (click)="seeComments(row)">
                <mat-icon>info</mat-icon>
                Details
            </button>            
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</div>


  `,
})
export class ListStaffingProcessComponent implements OnInit {
  private http = inject(HttpClient);
  private dialog = inject(MatDialog);
  private auth = inject(KeycloakAuthService);
  
  constructor(
    private clientService: ClientService,
    private employeeService: EmployeeService,
    private staffingService: StaffingService,
    private refreshService: RefreshService,
    private router : Router
  ) {}

  dataSource: StaffingProcessDTO[] = [];

  ngOnInit(): void {
    this.loadData();

    this.refreshService.staffing$.subscribe(() => {
      this.loadData();
    });
  }

  loadData(): void {

    this.staffingService.getAll().subscribe(processes => {
      this.dataSource = processes;
    });

  }

  // edit(row: StaffingProcess) {
  // const ref = this.dialog.open(FormDialogComponent, {
  //   data: { title: "Edit Staffing", initialValue: row.client },
  // });

  // ref.componentInstance.title = "Edit Staffing";
  // ref.componentInstance.initialValue = row.client;

  // ref.componentInstance.save.subscribe((newValue: string) => {
  //   console.log('✅ Edited:', newValue);
  //   this.dataSource[index].client = newValue;
  //   this.dataSource = [...this.dataSource]; // refresh table
  // });
  // }

  // confirmDelete(index: number) {
  //   const ref = this.dialog.open(ConfirmDialogComponent);

  //   ref.afterClosed().subscribe((result) => {
  //     if (result) {
  //       this.dataSource.splice(index, 1);
  //       this.dataSource = [...this.dataSource];
  //       console.log("🗑️ Deleted entry at index", index);
  //     }
  //   });
  // }

  addEntry(newItem: StaffingProcessDTO) {
    this.dataSource = [...this.dataSource, newItem];
  }

  async openAddDialog(): Promise<void> {
    const { AddStaffingProcessComponent } = await import('../../components/add-staffing-process-component/add-staffing-process-component.component');
    const dialogRef = this.dialog.open(AddStaffingProcessComponent, { width: '600px' });

    dialogRef.afterClosed().subscribe((refreshNeeded) => {
      if (refreshNeeded) {
        this.loadData(); // reload from backend
      }
    });
  }  

  getStatus(isActive: boolean) {
    return getStatusFromActive(isActive);
  }

  isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  markAsDone(row: any) {
    // Assuming PATCH or PUT to backend; here we mock it
    row.isActive = true;
    // TODO: Call backend update + reload list if needed
  }

  seeComments(row: any) {
    this.router.navigate(["/staffing", row.id, "comments"]);
  }

  onMarkComplete(id: number) {
    this.staffingService.markAsCompleted(id).subscribe(() => {
      this.dataSource = this.dataSource.map(p =>
        p.id === id ? { ...p, isActive: true } : p
      );
    });
  }
}
