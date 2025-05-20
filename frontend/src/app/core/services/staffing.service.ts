import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { map, Observable, of } from "rxjs";
import { environment } from "src/environments/environment";
import { StaffingProcess } from "../models/staffing-process.model";
import { StaffingProcessDTO } from "../models/staffing-process-dto.model";

@Injectable({ providedIn: "root" })
export class StaffingService {
  private baseUrl = `${environment.apiBaseUrl}/staffing-processes`;
  
  constructor(private http: HttpClient) {}

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
}
