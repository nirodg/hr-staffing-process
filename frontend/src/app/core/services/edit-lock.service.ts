import { Injectable } from "@angular/core";
import { HttpClient, HttpErrorResponse, HttpResponse } from "@angular/common/http";
import { environment } from "src/environments/environment";
import { catchError, map, Observable, of, throwError } from "rxjs";


export interface EditLockResult {
  acquired: boolean;
  editingBy?: string | null;
}

@Injectable({ providedIn: "root" })
export class EditLockService {
  private baseUrl = `${environment.apiBaseUrl}/editing`;

  constructor(private http: HttpClient) {}

  startEditing(entity: string, id: number): Observable<EditLockResult> {
    // return this.http.post<String>(`${this.baseUrl}/${entity}/${id}/start`, {});
     return this.http
      .post<void>(`${this.baseUrl}/${entity}/${id}/start`, {}, { observe: "response" })
      .pipe(
        map((res: HttpResponse<void>) => ({ acquired: res.status === 200 })),
        catchError((err: HttpErrorResponse) => {
          if (err.status === 409) {
            return of<EditLockResult>({
              acquired: false,
              editingBy: (err.error as string) || null,
            });
          }
          return throwError(() => err);
        })
      );
  }

  stopEditing(entity: string, id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${entity}/${id}/stop`);
  }

  getCurrentEditor(entity: string, id: number): Observable<string | null> {
    return this.http.get(`${this.baseUrl}/${entity}/${id}`, {
      responseType: "text",
    });
  }
}
