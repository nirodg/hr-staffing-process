import { CommonModule, Location } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { Client } from "@stomp/stompjs";
import { Apollo, gql, QueryRef } from "apollo-angular";
import { Observable } from "rxjs";
import { map } from "rxjs/operators";
import { ClientDTO } from "src/app/core/models/client-dto.model";
import { ClientService } from "src/app/core/services/client.service";
import { StaffingService } from "src/app/core/services/staffing.service";

@Component({
  standalone: true,
  selector: "app-client-detail",
  templateUrl: "./client-detail.component.html",
  styleUrls: ["./client-detail.component.css"],
  imports: [CommonModule],
})
export class ClientDetailComponent implements OnInit {
  queryRef!: QueryRef<any>;
  staffingProcesses!: Observable<ClientDTO[]>;
  clientId!: string;
  client: ClientDTO;
  page = 0;
  size = 10;
  constructor(
    private route: ActivatedRoute,
    private staffingService: StaffingService,
    private router: Router,
    private location: Location,
    private clientService: ClientService
  ) {}

  ngOnInit(): void {
    this.fetchProcesses();
  }
  fetchProcesses(): void {
    this.clientId = this.route.snapshot.paramMap.get("clientId");
    this.clientService.getById(this.clientId).subscribe((data) => {
      this.client = data;
      this.staffingProcesses =
        this.staffingService.getStaffingProcessesByClient(
          this.clientId,
          this.page,
          this.size
        );
    });
  }

  openProcess(id: string): void {
    this.router.navigate(["/staffing", id, "comments"]);
  }

  openEmployeeProfile(username: string): void {
    this.router.navigate(["/users", username,]);
  }

  nextPage() {
    this.page++;
    this.fetchProcesses();
  }

  prevPage() {
    if (this.page > 0) this.page--;
    this.fetchProcesses();
  }
  goBack() {
    this.location.back();
  }
}
