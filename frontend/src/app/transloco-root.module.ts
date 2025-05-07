import { HttpClient } from '@angular/common/http';
import {
  TRANSLOCO_LOADER,
  Translation,
  TranslocoLoader,
  TRANSLOCO_CONFIG,
  translocoConfig,
  TranslocoModule,
} from '@jsverse/transloco';
import { Injectable, NgModule } from '@angular/core';
import { environment } from '../environments/environment';
import { Observable } from 'rxjs';
import { ListStaffingProcessComponent } from './views/list-stuffing-process/list-stuffing-process.component';
import { NotFoundComponent } from './views/not-found/not-found.component';
import { FormDialogComponent } from './shared/form-dialog/form-dialog.component';
import { ConfirmDialogComponent } from './shared/confirm-dialog/confirm-dialog.component';
import { StatusBadgeComponentComponent } from './shared/status-badge-component/status-badge-component.component';
import { ClientsComponent } from './views/clients/clients.component';
import { EmployeesComponent } from './views/employees/employees.component';
import { CommentsComponent } from './views/comments/comments.component';
import { LoginComponent } from './views/login/login.component';
import { LogoutComponent } from './views/logout/logout.component';
import { StaffingProcessDialogComponentComponent } from './staffing-process-dialog-component/staffing-process-dialog-component.component';
import { ProfileComponent } from './shared/user-form/user-form.component';
import { AddStaffingProcessComponentComponent } from './components/add-staffing-process-component/add-staffing-process-component.component';
import { AddClientComponentComponent } from './components/add-client-component/add-client-component.component';

@Injectable({ providedIn: 'root' })
export class TranslocoHttpLoader implements TranslocoLoader {
  constructor(private http: HttpClient) {}

  getTranslation(lang: string): Observable<Translation> {
    return this.http.get<Translation>(`/assets/i18n/${lang}.json`);
  }
}

@NgModule({
  exports: [TranslocoModule],
  providers: [
    {
      provide: TRANSLOCO_CONFIG,
      useValue: translocoConfig({
        availableLangs: ['de', 'en'],
        defaultLang: 'de',
        // Remove this option if your application doesn't support changing language in runtime.
        reRenderOnLangChange: true,
        prodMode: environment.production,
      }),
    },
    { provide: TRANSLOCO_LOADER, useClass: TranslocoHttpLoader },
  ],
  declarations: [
    ListStuffingProcessComponent,
    NotFoundComponent,
    FormDialogComponent,
    ConfirmDialogComponent,
    StatusBadgeComponentComponent,
    ClientsComponent,
    EmployeesComponent,
    CommentsComponent,
    LoginComponent,
    LogoutComponent,
    StaffingProcessDialogComponentComponent,
    ProfileComponent,
    AddStaffingProcessComponentComponent,
    AddClientComponentComponent
  ],
})
export class TranslocoRootModule {}
