import { CommonModule } from "@angular/common";
import { HttpClient } from "@angular/common/http";
import { Component, inject } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { MatTableModule } from "@angular/material/table";
import { Router } from "@angular/router";
import { ClientDTO } from "src/app/core/models/client-dto.model";
import { ClientService } from "src/app/core/services/client.service";
import { RefreshService } from "src/app/core/services/refresh.service";
import { FormDialogComponent } from "src/app/shared/form-dialog/form-dialog.component";

@Component({
  selector: "app-client",
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatDialogModule],
  template: `
    <div class="bg-gray-100 rounded-xl shadow p-6 font-jakarta text-gray-800">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-xl font-semibold">Clients</h2>
        <ng-container *ngIf="clients?.length">
          <span class="text-sm text-gray-500">Total: {{ clients.length }}</span>
        </ng-container>
        <button
          mat-raised-button
          color="primary"
          (click)="openAddClientDialog()"
        >
          + Add Client
        </button>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full table-auto border-collapse">
          <thead
            class="bg-gray-50 border-b text-sm font-semibold text-gray-600"
          >
            <tr>
              <th class="px-4 py-3 text-left">Client</th>
              <th class="px-4 py-3 text-left">Email</th>
              <th class="px-4 py-3 text-left">Contact Person</th>
            </tr>
          </thead>
          <tbody class="text-sm font-normal">
            <tr
              *ngFor="let client of clients"
              class="hover:bg-gray-100 border-b"
            >
              <td class="px-4 py-3 flex items-center gap-4">
                <img
                  [src]="client.avatar || defaultAvatar"
                  class="w-10 h-10 rounded-md object-cover"
                  alt="avatar"
                />
                <span class="font-medium">
                  <a
                    (click)="openEmployeeProfile(client.id)"
                    style="cursor: pointer"
                    class="font-medium text-blue-600 dark:text-blue-500 hover:underline"
                    >{{ client.clientName }}</a
                  >
                </span>
              </td>
              <td class="px-4 py-3 text-sm text-gray-700">
                {{ client.clientEmail }}
              </td>
              <td class="px-4 py-3 text-sm text-gray-700">
                <div class="font-medium">{{ client.contactPersonName }}</div>
                <div class="text-xs text-gray-500">
                  {{ client.contactPersonEmail }}
                </div>
                <div class="text-xs text-gray-500">
                  {{ client.contactPersonPhone }}
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class ClientsComponent {
  private http = inject(HttpClient);

  // clients: { id: number; name: string }[] = [];
  clients: ClientDTO[] = [];
  defaultAvatar = "https://placehold.co/40x40";

  constructor(
    private dialog: MatDialog,
    private clientService: ClientService,
    private router: Router,
    private refreshService: RefreshService
  ) {}

  ngOnInit() {
    this.loadClients();
    this.refreshService.clients$.subscribe(() => {
      this.loadClients();
    });
  }

  loadClients() {
    this.clientService.getAll().subscribe((data) => {
      this.clients = data;
    });
  }

  async openAddClientDialog(): Promise<void> {
    const { AddClientComponent } = await import(
      "../../components/add-client-component/add-client-component.component"
    );

    const dialogRef = this.dialog.open(AddClientComponent, {
      width: "500px",
    });

    dialogRef.afterClosed().subscribe((shouldRefresh) => {
      if (shouldRefresh) {
        this.loadClients();
      }
    });
  }
  openEmployeeProfile(clientId: number) {
    this.router.navigate(["/clients", clientId]);
  }
}
