import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable, of } from 'rxjs';
import { UserDTO } from '../models/user-dto.model';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private baseUrl = `${environment.apiBaseUrl}/users`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<UserDTO[]> {
    return environment.useMock
      ? this.http.get<UserDTO[]>('/assets/mock/employees.json')
      : this.http.get<UserDTO[]>(this.baseUrl);
  }

  create(dto: Partial<UserDTO>): Observable<UserDTO> {
    return environment.useMock
      ? of({ ...dto, id: Date.now() } as UserDTO)
      : this.http.post<UserDTO>(this.baseUrl, dto);
  }

  delete(id: number): Observable<void> {
    return environment.useMock
      ? of(void 0)
      : this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
