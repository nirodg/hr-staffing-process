import { Routes } from "@angular/router";
import { MainLayoutComponent } from "./core/layout/main-layout.component";
import { RoleGuard } from "./core/guards/role.guard";
import { LoggedInGuard } from "./core/guards/logged-in.guard";

export const routes: Routes = [
  {
    path: "",
    component: MainLayoutComponent,
    children: [
      {
        path: "",
        redirectTo: "staffing",
        pathMatch: "full",
      },
      {
        path: "staffing",
        loadComponent: () =>
          import(
            "./views/list-stuffing-process/list-stuffing-process.component"
          ).then((m) => m.ListStaffingProcessComponent),
      },
      {
        path: "staffing/:id/comments",
        loadComponent: () =>
          import("./views/comments/comments.component").then(
            (m) => m.CommentsViewComponent
          ),
      },
      {
        path: "clients",
        loadComponent: () =>
          import("./views/clients/clients.component").then(
            (m) => m.ClientsComponent
          ),
        canActivate: [RoleGuard],
        data: { requiredRole: "client_public_admin" },
      },
      {
        path: "clients/:clientId",
        loadComponent: () =>
          import("./views/client-detail/client-detail.component").then(
            (m) => m.ClientDetailComponent
          ),
        canActivate: [RoleGuard],
        data: { requiredRole: "client_public_admin" },
      },
      {
        path: "employees",
        loadComponent: () =>
          import("./views/employees/employees.component").then(
            (m) => m.EmployeesComponent
          ),
        canActivate: [RoleGuard],
        data: { requiredRole: "client_public_admin" },
      },
      {
        path: "profile",
        loadComponent: () =>
          import("./shared/user-form/user-form.component").then(
            (m) => m.UserFormComponent
          ),
      },
      {
        path: "users/:username",
        loadComponent: () =>
          import("./shared/user-form/user-form.component").then(
            (m) => m.UserFormComponent
          ),
      },
    ],
    canActivate: [LoggedInGuard],
  },
  {
    path: "login",
    loadComponent: () =>
      import("./views/login/login.component").then((m) => m.LoginComponent),
  },
  {
    path: "logout",
    loadComponent: () =>
      import("./views/logout/logout.component").then((m) => m.LogoutComponent),
  },
  {
    path: "**",
    loadComponent: () =>
      import("./views/not-found/not-found.component").then(
        (m) => m.NotFoundComponent
      ),
  },
];
