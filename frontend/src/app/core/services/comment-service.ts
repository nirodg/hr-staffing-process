import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { environment } from "../../../environments/environment";
import { Observable, of } from "rxjs";
import { CommentDTO } from "../../core/models/comment-dto.model";

@Injectable({ providedIn: "root" })
export class CommentService {
  private baseUrl = `${environment.apiBaseUrl}/staffing-processes`;

  constructor(private http: HttpClient) {}

  getByProcessId(commentId: number): Observable<CommentDTO[]> {
    return environment.useMock
      ? this.http.get<CommentDTO[]>(`/assets/mock/comments-${commentId}.json`)
      : this.http.get<CommentDTO[]>(`${this.baseUrl}/comments/${commentId}`);
  }

  addComment(staffingId: number, data: Partial<CommentDTO>): Observable<CommentDTO> {
    return this.http.post<CommentDTO>(`${this.baseUrl}/${staffingId}/comments`, data);
  }
  
  getAll(): Observable<CommentDTO[]> {
    return environment.useMock
      ? this.http.get<CommentDTO[]>("/assets/mock/comments.json")
      : this.http.get<CommentDTO[]>(this.baseUrl);
  }

  getByStaffingProcessId(processId: number): Observable<CommentDTO[]> {
    return this.http.get<CommentDTO[]>(
      `${this.baseUrl}/staffing/${processId}/comments`
    );
  }
}
