import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { environment } from "../../../environments/environment";
import { Observable, of } from "rxjs";
import { ClientDTO } from "../models/client-dto.model";

@Injectable({ providedIn: "root" })
export class ClientService {
  private baseUrl = `${environment.apiBaseUrl}/clients`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ClientDTO[]> {
    return environment.useMock
      ? this.http.get<ClientDTO[]>("/assets/mock/clients.json")
      : this.http.get<ClientDTO[]>(this.baseUrl);
  }

  create(dto: Partial<ClientDTO>): Observable<ClientDTO> {
    return environment.useMock
      ? of({ ...dto, id: Date.now() } as ClientDTO)
      : this.http.post<ClientDTO>(this.baseUrl, dto);
  }

  delete(id: number): Observable<void> {
    return environment.useMock
      ? of(void 0)
      : this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  getById(clientId: string): Observable<ClientDTO> {
    return this.http.get<ClientDTO>(`${this.baseUrl}/${clientId}`);
  }
}
