# StaffingApp 🚀

> A modern, real-time staffing platform built with Angular 19 🅰️ & Spring Boot 3 🌱

---

## 📚 Table of Contents

1. [Features 🎯](#features-🎯)
2. [Tech Stack 🛠️](#tech-stack-🛠️)
3. [Architecture Overview 🧱](#architecture-overview-🧱)
4. [Getting Started 🏁](#getting-started-🏁)
5. [Development Workflows 🤖](#development-workflows-🤖)
6. [Contributing ❤️](#contributing-❤️)
7. [License 📄](#license-📄)

---

## Features 🎯

Our platform offers comprehensive staffing functionalities:

* **User Management**: Secure OAuth2 login via Keycloak, with role-based access (`client_public_admin`, `client_public_user`).
* **Profile Page**: Interactive `/profile` view for managing user details in real time.
* **Staffing Processes**: Create, list, mark complete, and comment on processes. Includes nested threads, inline replies, infinite scroll, and real-time updates.
* **Clients & Employees**: Unified CRUD operations with reusable `UserFormComponent` and conditional form validation.
* **Optimistic Locking**: Version checks on save with UI disable/spinner and conflict messaging.
* **Pagination & Search**: Built-in Angular Material paginator

---

## Tech Stack 🛠️

### Frontend

* **Angular 19**: Standalone components, lazy-loaded feature modules, Tailwind CSS & Angular Material, dark mode prepared.
* **State Management**: RxJS-based `RefreshService` for cross-component update propagation.
* **Forms & Validation**: Template-driven + Reactive forms, conditional rules, auto-reset logic.
* **Testing**: Unit tests via Jest & Angular Testing Library; e2e tests with Cypress.
* **Tooling**: Angular CLI, ESLint, Stylelint, Husky pre-commit hooks.

### Backend

* **Spring Boot 3**: Layered MVC architecture (Controllers → Services → Repositories).
* **Security**: JWT authentication, `X-APP-TOKEN` header support, Helmet-inspired HTTP security headers.
* **Messaging & Real-Time**: Apache Kafka for async events; STOMP+SockJS WebSocket endpoints.
* **Persistence**: MySQL with Flyway-managed migrations; MapStruct mappers; Lombok annotations.
* **Tracing & Logging**: Jaeger trace IDs passed through logs; Loki-compatible log format; RFC7807 API error payloads.

---

## Architecture Overview 🧱

```text
├── frontend/               # Angular application
│   ├── src/app/
│   │   ├── core/           # Singletons (services, guards, interceptors)
│   │   ├── shared/         # Reusable components, pipes, directives
│   │   └── views/          # Lazy-loaded feature modules
├── backend/                # Spring Boot service
│   ├── src/main/java/...   # Controllers, services, repositories, mappers
│   ├── resources/
│   │   ├── db/migration/   # Flyway scripts
│   │   └── application.yml # Configuration
├── deployment/             # Deployment manifests & scripts
│   ├── deploy-local.sh     # Kubernetes deployment script
│   └── k8s/                # YAMLs for Kind/minikube
└── docker-compose.yml
```

* **Lazy Loading**: `loadChildren` routes reduce initial bundle size.
* **Modular Design**: Clear separation of concerns and layers for testability and maintainability.
* **Containerized**: Docker Compose for local dev; Kubernetes manifests for cluster deployment.

---

## Getting Started 🏁

### Run Locally with Docker 🐳

Bring up all services:

```bash
docker-compose up -d --build
```

| Service     | URL                                                                                                      |
| ----------- | -------------------------------------------------------------------------------------------------------- |
| Frontend    | [http://localhost:4200](http://localhost:4200) or [http://frontend.localhost](http://frontend.localhost) |
| Keycloak    | [http://localhost:8080](http://localhost:8080) or [http://keycloak.localhost](http://keycloak.localhost) |
| Backend API | [http://localhost:8050](http://localhost:8050) or [http://api.localhost](http://api.localhost)           |
| Adminer     | [http://localhost:8099](http://localhost:8099) or [http://adminer.localhost](http://adminer.localhost)   |
| Kafka UI    | [http://localhost:8888](http://localhost:8888) or [http://kafka.localhost](http://kafka.localhost)       |
| Grafana     | [http://localhost:3000](http://localhost:3000) or [http://grafana.localhost](http://grafana.localhost)   |

> ℹ️ Update `/etc/hosts` (or OS equivalent) to map `*.localhost` domains.

### Deploy to Kubernetes ☸️

Use the `deploy-local.sh` script for local Kind/Minikube:

```bash
./deploy-local.sh [options]
```

**Options**:

* `-n, --namespace <ns>`  Target namespace (default: `default`)
* `-build-all`          Build all images and exit
* `-build [context]`    Build one image (frontend/backend)
* `-down [context]`     Remove resources (all or specific)
* `-routes`             Show service ingress URLs
* `-h, --help`          Display help

After successful deploy, access via ingress:

```
https://frontend.localhost
https://api.localhost/v1
https://keycloak.localhost
```


---

## Contributing ❤️

1. Fork & clone the repo
2. Create a branch: `git checkout -b feat/your-feature`
3. Follow Conventional Commits: `feat: add new job assignment page`
4. Push & open a PR against `main`

We welcome issues and PRs—please tag relevant team members for reviews.

---

## License 📄

Distributed under the MIT License. See [LICENSE](LICENSE) for details.
