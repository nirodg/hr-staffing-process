// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
  useMock: false,
  appName: "Staffing Process",
  baseUrl: 'http://localhost:8050',
  apiBaseUrl: 'http://localhost:8050/api',
  keycloakUrl: 'http://keycloak:8080',
  keycloakRealm: 'staffing-process-ui-realm',
  keycloakClientId: 'angular-app',
  backendAppToken: ''
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/plugins/zone-error';  // Included with Angular CLI.
