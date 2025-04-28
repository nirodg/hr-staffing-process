import { inject, Injectable } from "@angular/core";
import { Subject, Observable } from "rxjs";
import { KafkaTopic } from "../constants/kafka-topic";
import { RefreshService } from "./refresh.service";
import { WebSocketService } from "./websocket.service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { KafkaPayload } from "../models/kafka-payload";
import { KafkaAction } from "../constants/kafka-actions";
import { KeycloakAuthService } from "./keycloak-auth.service";
import { Router } from "@angular/router";


@Injectable({ providedIn: "root" })
export class NotificationService {
  private subject = new Subject<string>();
  private snack = inject(MatSnackBar);
  private refreshService = inject(RefreshService);
  private auth = inject(KeycloakAuthService);
  private router = inject(Router);

  constructor(private ws: WebSocketService) {
    this.ws.connect();

    this.ws.getMessages().subscribe((payload: KafkaPayload) => {
      const message = this.mapPayloadToMessage(payload);

      if (payload.userId === this.auth.getUsername()) return;

      this.subject.next(message);
      this.snack.open(message, "Close", {
        duration: 10000,
        panelClass: ["bg-blue-600", "text-white"],
      });
      this.handleDataRefresh(payload);
    });
  }

  getMessages(): Observable<string> {
    return this.subject.asObservable();
  }

  private mapPayloadToMessage(payload: KafkaPayload): string {
    let action: String = "";

    switch (payload.action) {
      case KafkaAction.CREATE:
        action = "was created";
        break;
      case KafkaAction.DELETE:
        action = "was deleted";
        break;
      case KafkaAction.UPDATE:
        action = "was updated";
        break;
    }

    switch (payload.topic) {
      case KafkaTopic.CLIENTS:
        return `🔔 A client ${action}`;
      case KafkaTopic.EMPLOYEES:
        return `👤 An Employee ${action}`;
      case KafkaTopic.STAFFING_PROCESS:
        return `📋 A Staffing Process ${action}`;
      case KafkaTopic.COMMENTS:
        return `💬 A Comment ${action}`;
      default:
        return `📢 An object was modified`;
    }
  }

  private handleDataRefresh(payload: KafkaPayload) {
    switch (payload.topic) {
      case KafkaTopic.CLIENTS:
        this.refreshService.refreshClients();
        break;
      case KafkaTopic.EMPLOYEES:
        this.refreshService.refreshEmployees();
        break;
      case KafkaTopic.STAFFING_PROCESS:
        this.refreshService.refreshStaffing();
        break;
      case KafkaTopic.COMMENTS:
        this.refreshService.refreshComments();
        break;
    }
  }
}
