import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { map, Observable, of } from "rxjs";
import { environment } from "src/environments/environment";
import { StaffingProcess } from "../models/staffing-process.model";
import { StaffingProcessDTO } from "../models/staffing-process-dto.model";
import { Apollo, gql } from "apollo-angular";
import { ClientDTO } from "../models/client-dto.model";

@Injectable({ providedIn: "root" })
export class StaffingService {
  private baseUrl = `${environment.apiBaseUrl}/staffing-processes`;

  constructor(
    private http: HttpClient,
    private gql: Apollo
  ) {}

  getAll(): Observable<StaffingProcessDTO[]> {
    if (environment.useMock) {
      return this.http.get<StaffingProcessDTO[]>(
        "/assets/mock/staffing-process.json"
      );
    } else {
      return this.http.get<StaffingProcessDTO[]>(this.baseUrl);
    }
  }

  getById(id: number): Observable<StaffingProcessDTO> {
    if (environment.useMock) {
      return this.http
        .get<StaffingProcessDTO[]>("/assets/mock/staffing-process.json")
        .pipe(map((all) => all.find((p) => p.id === id)!));
    } else {
      return this.http.get<StaffingProcessDTO>(`${this.baseUrl}/${id}`);
    }
  }

  create(dto: Partial<StaffingProcessDTO>): Observable<StaffingProcessDTO> {
    if (environment.useMock) {
      return of({
        ...dto,
        id: Date.now(),
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      } as StaffingProcessDTO);
    } else {
      return this.http.post<StaffingProcessDTO>(this.baseUrl, dto);
    }
  }

  markAsCompleted(id: number): Observable<void> {
    if (environment.useMock) {
      console.warn(`[MOCK] Marking process ${id} as completed`);
      return of(void 0);
    } else {
      return this.http.patch<void>(`${this.baseUrl}/${id}/complete`, {});
    }
  }
  delete(id: number): Observable<void> {
    if (environment.useMock) {
      console.warn(`[MOCK] Deleting process ${id}`);
      return of(void 0);
    } else {
      return this.http.delete<void>(`${this.baseUrl}/${id}`);
    }
  }

  getStaffingProcessesByEmployee(
    username: string,
    page: number,
    size: number
  ): Observable<StaffingProcess[]> {
    return this.gql
      .watchQuery<{ staffingProcessesByEmployee: StaffingProcess[] }>({
        query: gql`
          query GetStaffingProcesses(
            $username: String!
            $page: Int
            $size: Int
          ) {
            staffingProcessesByEmployee(
              username: $username
              page: $page
              size: $size
            ) {
              id
              title
              createdAt
              isActive
              client {
                id
                clientName
              }
            }
          }
        `,
        variables: {
          username: username,
          page: page,
          size: size,
        },
        fetchPolicy: "cache-and-network",
      })
      .valueChanges.pipe(map((r) => r.data.staffingProcessesByEmployee));
  }

  getStaffingProcessesByClient(
    clientId: any,
    page: any,
    size: any
  ): Observable<import("../models/client-dto.model").ClientDTO[]> {
    return this.gql
      .watchQuery<{ staffingProcessesByClient: ClientDTO[] }>({
        query: gql`
          query ($id: Int!, $page: Int, $size: Int) {
            staffingProcessesByClient(clientId: $id, page: $page, size: $size) {
              id
              title
              createdAt
              isActive
              employee {
                id
                username
                firstName
                lastName
                email
              }
            }
          }
        `,
        variables: {
          id: parseInt(clientId),
          page: page,
          size: size,
        },
        fetchPolicy: "cache-and-network",
      })
      .valueChanges.pipe(map((r) => r.data.staffingProcessesByClient));
  }

  updateTitle(id: number, newTitle: string) {
    return this.gql.mutate({
      mutation: gql`
        mutation updateProcessTitle($id: Int!, $newTitle: String!) {
          updateProcessTitle(id: $id, newTitle: $newTitle) {
            id
            title
          }
        }
      `,
      variables: { id, newTitle },
    });
  }
}
