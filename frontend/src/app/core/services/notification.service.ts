import { inject, Injectable } from "@angular/core";
import { Subject, Observable, interval, switchMap } from "rxjs";
import { KafkaTopic } from "../constants/kafka-topic";
import { RefreshService } from "./refresh.service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { KafkaPayload } from "../models/kafka-payload";
import { KafkaAction } from "../constants/kafka-actions";
import { KeycloakAuthService } from "./keycloak-auth.service";
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
      const message = this.mapPayloadToMessage(payload);

      if (payload.userId === this.auth.getUsername()) return;

      this.handleDataRefresh(payload, message);
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

  private handleDataRefresh(payload: KafkaPayload, snackMessage: string) {
    const currentUrl = this.router.url;
    
    if(currentUrl){
      console.log(currentUrl)
    }


    switch (payload.topic) {
      case KafkaTopic.CLIENTS:
        if(currentUrl === "/clients"){
          this.displaySnack(snackMessage);
          this.refreshService.refreshClients();
          break;
        }
      case KafkaTopic.EMPLOYEES:
        this.refreshService.refreshEmployees();
        break;
      case KafkaTopic.STAFFING_PROCESS:
        if (currentUrl == "/staffing") this.displaySnack(snackMessage);this.refreshService.refreshStaffing();
        break;
      case KafkaTopic.COMMENTS:
        // ✅ only reload if user is viewing a comment thread
        if (/^\/staffing\/\d+\/comments$/.test(currentUrl)) {
          this.refreshService.refreshComments();
          this.displaySnack(snackMessage);
        }
        break;
    }
  }

  private displaySnack(snackMessage: string) {
    this.subject.next(snackMessage);
    this.snack.open(snackMessage, "Close", {
      duration: 10000,
      panelClass: ["bg-blue-600", "text-white"],
    });
  }
}
