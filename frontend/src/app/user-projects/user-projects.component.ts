import { Component, Input, OnInit } from "@angular/core";
import { Router, RouterLink } from "@angular/router";
import { Observable, map } from "rxjs";
import { StaffingProcess } from "../core/models/staffing-process.model";
import { KeycloakAuthService } from "../core/services/keycloak-auth.service";
import { Apollo, gql } from "apollo-angular";
import { CommonModule, Location } from "@angular/common";
import { StaffingService } from "../core/services/staffing.service";

@Component({
  selector: "app-user-projects",
  standalone: true,
  templateUrl: "./user-projects.component.html",
  styleUrl: "./user-projects.component.css",
  imports: [CommonModule, RouterLink],
})
export class UserProjectsComponent implements OnInit {
  staffingProcesses: Observable<StaffingProcess[]>;
  page = 0;
  size = 10;
  requiredUsername!: string;
  @Input() username: string;

  constructor(
    private auth: KeycloakAuthService,
    private router: Router,
    private staffingSerice: StaffingService
  ) {}

  ngOnInit(): void {
    this.requiredUsername = this.username != "" ? this.username : this.auth.getUsername();
    this.fetchProcesses();
  }

  fetchProcesses(): void {
    this.staffingProcesses = this.staffingSerice.getStaffingProcessesByEmployee(
      this.requiredUsername,
      this.page,
      this.size
    );
  }

  openProcess(id: string): void {
    this.router.navigate(["/staffing", id, "comments"]);
  }

  nextPage() {
    this.page++;
    this.fetchProcesses();
  }

  prevPage() {
    if (this.page > 0) this.page--;
    this.fetchProcesses();
  }
}
