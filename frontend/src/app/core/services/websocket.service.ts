import { Injectable } from '@angular/core';
import { Client, IMessage, Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';
import { Subject } from 'rxjs';
import {environment} from "../../../environments/environment"

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private stompClient!: Client;
  private messageSubject = new Subject<any>();

  connect(): void {
    this.stompClient = new Client({
      brokerURL: undefined, // disables native WebSocket, use SockJS
      webSocketFactory: () => new SockJS(`${environment.baseUrl}/ws`),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });
    
    this.stompClient.onConnect = () => {
      this.stompClient.subscribe('/backend-updates', (message: any) => {
        this.messageSubject.next(JSON.parse(message.body));
      });
    };
    
    this.stompClient.onStompError = (frame) => {
      console.error('Broker error:', frame);
    };

    this.stompClient.activate();
  }

  getMessages() {
    return this.messageSubject.asObservable();
  }
}