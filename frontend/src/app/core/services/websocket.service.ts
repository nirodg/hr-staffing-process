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
    const headers: { [key: string]: string } = {};

    if (this.auth.getToken()) {
      headers["Authorization"] = `Bearer ${this.auth.getToken()}`;
    }
    if (environment.backendAppToken) {
      headers["X-APP-TOKEN"] = environment.backendAppToken;
    }   
  

    this.stompClient = new Client({
      brokerURL: undefined, // disables native WebSocket, use SockJS
      webSocketFactory: () => {
        // Create SockJS with headers in connectHeaders
        return new SockJS(
          `${environment.baseUrl}/ws?jwt=${this.auth.getToken()}&X-APP-TOKEN=${environment.backendAppToken}`
        );
      },
      connectHeaders: headers, // Important for STOMP connection headers
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      // debug: (str) => console.log("STOMP:", str),
      beforeConnect: () => {
        return new Promise<void>((resolve) => {
          if (this.auth.isAuthenticated()) {
            resolve();
          } else {
            this.auth.refreshToken().then(resolve).catch(console.error);
          }
        });
      },
    });

    this.stompClient.onConnect = () => {
      this.stompClient.subscribe("/backend-updates", (message: any) => {
        this.messageSubject.next(JSON.parse(message.body));
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error("STOMP error:", frame.headers["message"]);
      this.handleConnectionError();
    };
    this.stompClient.onWebSocketError = (event) => {
      console.error("WebSocket error:", event);
      this.handleConnectionError();
    };

    this.stompClient.onDisconnect = () => {
      console.log("Disconnected");
      this.handleConnectionError();
    };
    this.stompClient.activate();
  }

  getMessages() {
    return this.messageSubject.asObservable();
  }

  private handleConnectionError() {
    // Reconnect logic
    setTimeout(() => {
      if (!this.stompClient.connected) {
        this.connect();
      }
    }, 5000);
  }
}
