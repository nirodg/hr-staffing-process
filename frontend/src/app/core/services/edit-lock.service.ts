import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Observable, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class EditLockService {

  private baseUrl = `${environment.apiBaseUrl}/editing`;

  constructor(private http: HttpClient) {}

  startEditing(entity: string, id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${entity}/${id}/start`, {});
  }

  stopEditing(entity: string, id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${entity}/${id}/stop`);
  }

  getCurrentEditor(entity: string, id: number): Observable<string | null> {
    return this.http.get(`${this.baseUrl}/${entity}/${id}`, {
      responseType: 'text'
    });
  }
}
