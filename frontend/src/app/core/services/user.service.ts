import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { UserDTO } from "../models/user-dto.model";
import { map, Observable } from "rxjs";
import { environment } from "src/environments/environment";
import { Apollo, gql } from "apollo-angular";

@Injectable({ providedIn: "root" })
export class UserService {
  private baseUrl = `${environment.apiBaseUrl}/users`;

  constructor(
    private http: HttpClient,
    private apollo: Apollo
  ) {}

  getMyProfile(): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.baseUrl}/my_account`);
  }

  getByUsername(username: String): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.baseUrl}/${username}/account`);
  }

  updateMyProfile(profile: Partial<UserDTO>): Observable<UserDTO> {
    return this.http.put<UserDTO>(`${this.baseUrl}/me`, profile);
  }

  deleteMyProfile(): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/me`);
  }

  countAdmins(): Observable<number> {
    return this.apollo
      .query<{ countAdmins: number }>({
        query: gql`
          query {
            countAdmins
          }
        `,
        fetchPolicy: "network-only",
      })
      .pipe(map((result) => result.data.countAdmins));
  }
}
