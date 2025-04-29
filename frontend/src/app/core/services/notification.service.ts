import { inject, Injectable } from "@angular/core";
import { Subject, Observable } from "rxjs";
import { KafkaTopic } from "../constants/kafka-topic";
import { RefreshService } from "./refresh.service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { KafkaPayload } from "../models/kafka-payload";
import { KafkaAction } from "../constants/kafka-actions";
import { KeycloakAuthService } from "./keycloak-auth.service";
import { Router } from "@angular/router";
import {KafkaRestService} from "./kafka.service"


@Injectable({ providedIn: "root" })
export class NotificationService {
  private subject = new Subject<string>();
  private snack = inject(MatSnackBar);
  private refreshService = inject(RefreshService);
  private auth = inject(KeycloakAuthService);
  private router = inject(Router);
  constructor(private kafkaRest: KafkaRestService) {

    this.kafkaRest.listenToTopic('EMPLOYEES').subscribe((messages) => {
      console.log('Kafka REST message:', messages);
      // this.handleDataRefresh('COMMENTS');
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
