import { inject, Injectable } from "@angular/core";
import { Subject, Observable } from "rxjs";
import { KafkaTopic } from "../constants/kafka-topic";
import { KafkaAction } from "../constants/kafka-actions";
import { KafkaPayload } from "../models/kafka-payload";
import { RefreshService } from "./refresh.service";
import { KeycloakAuthService } from "./keycloak-auth.service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Router } from "@angular/router";
import { WebSocketService } from "./websocket.service";

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
      if (payload.userId === this.auth.getUsername()) return;

      const message = this.mapPayloadToMessage(payload);
      this.handleDataRefresh(payload, message);
    });
  }

  getMessages(): Observable<string> {
    return this.subject.asObservable();
  }

  private mapPayloadToMessage(payload: KafkaPayload): string {
    let action = "";

    switch (payload.action) {
      case KafkaAction.CREATE:
        action = "was created";
        break;
      case KafkaAction.UPDATE:
        action = "was updated";
        break;
      case KafkaAction.DELETE:
        action = "was deleted";
        break;
    }

    switch (payload.topic) {
      case KafkaTopic.CLIENTS:
        return `🔔 A client ${action}`;
      case KafkaTopic.EMPLOYEES:
        return `👤 An employee ${action}`;
      case KafkaTopic.STAFFING_PROCESS:
        return `📋 A staffing process ${action}`;
      case KafkaTopic.COMMENTS:
        return `💬 A comment ${action}`;
      default:
        return `📢 An object ${action}`;
    }
  }

  private handleDataRefresh(payload: KafkaPayload, snackMessage: string) {
    const currentUrl = this.router.url;

    switch (payload.topic) {
      case KafkaTopic.CLIENTS:
        if (currentUrl === "/clients") {
          console.log("ok")
          this.displaySnack(snackMessage);
          this.refreshService.refreshClients();
        }
        break;

      case KafkaTopic.EMPLOYEES:
        if (currentUrl === "/employees") {
          this.displaySnack(snackMessage);
          this.refreshService.refreshEmployees();
        }
        break;

      case KafkaTopic.STAFFING_PROCESS:
        // the user can be either in the comments of a process or in the main process page
        const isUserInCommentsView = this.router.url.match(/staffing\/(\d+)\/comments/);
        
        if (currentUrl === "/staffing" || isUserInCommentsView) {
          this.refreshService.refreshStaffing();
          this.refreshService.refreshComments();
          this.displaySnack(snackMessage);
        }
        break;

      case KafkaTopic.COMMENTS: {
        const match = this.router.url.match(/staffing\/(\d+)\/comments/);
        const staffingId = match ? Number(match[1]) : null;

        if (staffingId === payload.entityId) {
          this.refreshService.refreshComments();
          this.displaySnack(snackMessage);
        }
        break;
      }

      default:
        break;
    }
  }

  private displaySnack(snackMessage: string) {
    this.subject.next(snackMessage);
    this.snack.open(snackMessage, "Close", {
      duration: 10000,
      panelClass: ["snackbar-custom"], // You should define this class in your styles
      horizontalPosition: "right",
      verticalPosition: "top",
    });
  }
}
