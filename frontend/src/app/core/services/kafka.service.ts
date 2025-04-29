import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, interval, switchMap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class KafkaRestService {
    private baseUrl = '/kafka-rest';

    constructor(private http: HttpClient) {}

    httpOptions = {
        headers: new HttpHeaders({
            'Access-Control-Allow-Methods':'DELETE, POST, GET, OPTIONS',
            'Access-Control-Allow-Headers':'Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With',
            'Access-Control-Allow-Origin':'*'
        })
      };
  
    listenToTopic(topic: string): Observable<any> {
      return interval(3000).pipe( // every 3s poll
        switchMap(() => this.http.get(`${this.baseUrl}/topics/${topic}`))
      );
    }
}