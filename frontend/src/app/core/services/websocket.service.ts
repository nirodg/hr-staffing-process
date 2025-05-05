import { inject, Injectable } from "@angular/core";
import { Client, IMessage, Stomp } from "@stomp/stompjs";
import SockJS from "sockjs-client/dist/sockjs";
import { Subject } from "rxjs";
import { environment } from "../../../environments/environment";
import { KeycloakAuthService } from "./keycloak-auth.service";

@Injectable({ providedIn: "root" })
export class WebSocketService {
  private stompClient!: Client;
  private messageSubject = new Subject<any>();
  private auth = inject(KeycloakAuthService);

  connect(): void {
    // Adding a Bearer token to headers
    const socketOptions = {
      headers: { Authorization: `Bearer ${this.auth.getToken()}` },
    };

    console.log(socketOptions)
    this.stompClient = new Client({
      brokerURL: undefined, // disables native WebSocket, use SockJS
      webSocketFactory: () => new SockJS(`${environment.baseUrl}/ws`, null, socketOptions),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = () => {
      this.stompClient.subscribe("/backend-updates", (message: any) => {
        this.messageSubject.next(JSON.parse(message.body));
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error("Broker error:", frame);
    };

    this.stompClient.activate();
  }

  getMessages() {
    return this.messageSubject.asObservable();
  }
}
