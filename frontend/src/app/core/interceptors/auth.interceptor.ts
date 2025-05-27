import { Injectable } from "@angular/core";
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from "@angular/common/http";
import { Observable } from "rxjs";
import { KeycloakAuthService } from "../services/keycloak-auth.service";
import { environment } from "../../../environments/environment";
import { UserService } from "../services/user.service";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private auth: KeycloakAuthService) {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    const token = this.auth.getToken();
    const headers: { [key: string]: string } = {};

    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    if (environment.backendAppToken) {
      headers["X-APP-TOKEN"] = environment.backendAppToken;
    }

    return next.handle(req.clone({ setHeaders: headers }));
  }
}
