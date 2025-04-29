import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { UserDTO } from "../models/user-dto.model";
import { Observable } from "rxjs";
import { environment } from "src/environments/environment";

@Injectable({ providedIn: "root" })
export class UserService {
  private baseUrl = `${environment.apiBaseUrl}/users`;

  constructor(private http: HttpClient) {}

  getMyProfile(): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.baseUrl}/my_account`);
  }

  updateMyProfile(profile: Partial<UserDTO>): Observable<UserDTO> {
    return this.http.put<UserDTO>(`${this.baseUrl}/me`, profile);
  }

  deleteMyProfile(): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/me`);
  }
}
