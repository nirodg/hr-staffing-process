import { Injectable } from "@angular/core";
import { Subject, Observable } from "rxjs";

@Injectable({ providedIn: 'root' })
export class RefreshService {
  private clientsSubject = new Subject<void>();
  private employeesSubject = new Subject<void>();
  private staffingSubject = new Subject<void>();
  private commentsSubject = new Subject<void>();

  get clients$(): Observable<void> {
    return this.clientsSubject.asObservable();
  }

  get employees$(): Observable<void> {
    return this.employeesSubject.asObservable();
  }

  get staffing$(): Observable<void> {
    return this.staffingSubject.asObservable();
  }

  get comments$(): Observable<void> {
    return this.commentsSubject.asObservable();
  }

  refreshClients() {
    this.clientsSubject.next();
  }

  refreshEmployees() {
    this.employeesSubject.next();
  }

  refreshStaffing() {
    this.staffingSubject.next();
  }

  refreshComments() {
    this.commentsSubject.next();
  }
}
