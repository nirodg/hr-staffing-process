import { Injectable } from "@angular/core";
import { TopicMessages } from "kafkajs";
import { Subject, Observable } from "rxjs";
import { KafkaPayload } from "../models/kafka-payload";

@Injectable({ providedIn: "root" })
export class RefreshService {
  private clientsSubject = new Subject<void>();
  private employeesSubject = new Subject<void>();
  private staffingSubject = new Subject<void>();
  private commentsSubject = new Subject<void>();
  private editLockSubject = new Subject<Object>();

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

  get editLock$(): Observable<KafkaPayload> {
    return this.editLockSubject.asObservable();
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

  refreshEditLock(object: any) {
    this.editLockSubject.next(object);
  }
}
