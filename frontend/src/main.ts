import { enableProdMode, importProvidersFrom, inject } from "@angular/core";
import { bootstrapApplication } from "@angular/platform-browser";
import { AppComponent } from "./app/app.component";
import {
  HTTP_INTERCEPTORS,
  provideHttpClient,
  withInterceptorsFromDi,
} from "@angular/common/http";
import { provideRouter } from "@angular/router";
import { routes } from "./app/app.routes";
import { environment } from "./environments/environment";
import { provideKeycloak } from "keycloak-angular";
import { AuthInterceptor } from "./app/core/interceptors/auth.interceptor";
import { provideApollo } from "apollo-angular";
import { HttpLink } from "apollo-angular/http";
import { InMemoryCache } from "@apollo/client/core";

if (environment.production) {
  enableProdMode();
}

fetch("/assets/config.json")
  .then((res) => {
    return res.json();
  })
  .then((config) => {
    // replace at runtime envs
    environment.backendAppToken = config.backendAppToken;

    bootstrapApplication(AppComponent, {
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        {
          // Inject the Authentication Interceptopr, adds jtw token to any request
          provide: HTTP_INTERCEPTORS,
          useClass: AuthInterceptor,
          multi: true,
        },
        provideRouter(routes),
        provideKeycloak({
          config: {
            url: environment.keycloakUrl,
            realm: environment.keycloakRealm,
            clientId: environment.keycloakClientId,
          },
          initOptions: {
            onLoad: "check-sso", // or 'check-sso'
            checkLoginIframe: false, // disable 3p-cookie iframe checks
            silentCheckSsoRedirectUri:
              window.location.origin + "/assets/silent-check-sso.html",
          } as any,
        }),
        provideHttpClient(),
        provideApollo(() => {
          const httpLink = inject(HttpLink);

          return {
            link: httpLink.create({
              uri: `${environment.baseUrl}/graphql`,
            }),
            cache: new InMemoryCache(),
          };
        }),
      ],
    });
  });
