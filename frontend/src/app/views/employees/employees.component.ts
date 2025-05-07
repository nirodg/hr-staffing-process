import { CommonModule } from "@angular/common";
import { HttpClient } from "@angular/common/http";
import { Component, Inject, inject } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { MatTableModule } from "@angular/material/table";
import { data } from "cypress/types/jquery";
import { UserDTO } from "src/app/core/models/user-dto.model";
import { EmployeeService } from "src/app/core/services/employee.service";
import { UserFormComponent } from "src/app/shared/user-form/user-form.component";
import { StatusBadgeComponent } from "../../shared/status-badge-component/status-badge-component.component";
import { RefreshService } from "src/app/core/services/refresh.service";
import {getRoleFromEnum} from "../../core/constants/role-map"

@Component({
  selector: "app-employees",
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatDialogModule,
    MatDialogModule,
  ],
  template: `
    <div class="bg-gray-100 rounded-xl shadow p-6 font-jakarta text-gray-800">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-xl font-semibold">Employees</h2>
        <span class="text-sm text-gray-500">
          <ng-container *ngIf="employees?.length">
            Results 1 - {{ employees.length }} of {{ employees.length }}
          </ng-container></span
        >
      </div>

      <div class="overflow-x-auto">
        <table class="w-full table-auto border-collapse">
          <thead
            class="bg-gray-50 border-b text-sm font-semibold text-gray-600"
          >
            <tr>
              <th class="px-4 py-3 text-left">Name</th>
              <th class="px-4 py-3 text-left">Email</th>
              <th class="px-4 py-3 text-left">Role</th>
              <th class="px-4 py-3 text-left">Position</th>
              <th class="px-4 py-3 text-left">Status</th>
            </tr>
          </thead>
          <tbody class="text-sm font-normal">
            <tr *ngFor="let e of employees" class="hover:bg-gray-100 border-b">
              <td class="px-4 py-3 flex items-center gap-4">
                <img
                  [src]="e.avatar || defaultAvatar"
                  class="w-10 h-10 rounded-md object-cover"
                  alt="avatar"
                />
                <div>
                  <div class="font-semibold">
                    {{ e.lastName }} {{ e.firstName }}
                  </div>
                  <div class="text-xs text-blue-600">{{ e.role }}</div>
                </div>
              </td>
              <td class="px-4 py-3">{{ e.email }}</td>
              <td class="px-4 py-3">{{ this.getRole(e.roles) }}</td>
              <td class="px-4 py-3">{{ e.position }}</td>
              <td class="px-4 py-3">
                <div class="flex items-center">
                  <span
                    class="w-2 h-2 rounded-full mr-2"
                    [ngClass]="{
                      'bg-green-500': e.available === true,
                      'bg-red-500': e.available === false,
                    }"
                  ></span>

                  {{ e.available === true ? "Active" : "Disabled" }}
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class EmployeesComponent {
  private http = inject(HttpClient);

  // employees: { id: number; name: string }[] = [];
  employees: UserDTO[] = [];
  defaultAvatar = "https://placehold.co/40x40";
  columns = ["id", "name", "role", "email"];

  constructor(
    private employeeService: EmployeeService,
    private refreshService: RefreshService
  ) {}

  ngOnInit() {
    this.loadData();

    this.refreshService.employees$.subscribe(() => {
      this.loadData();
    });
  }

  loadData(): void {
    this.employeeService.getAll().subscribe((data) => {
      this.employees = data;
    });
  }

  getRole(role: string): string {
    return getRoleFromEnum(role);
  }
}
