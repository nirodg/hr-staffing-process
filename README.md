# hr-staffing-process
An Angular based front-end app with Spring boot designed for helping companies to better match their employees to current projects/clients. 


# What does the backend comes with
1. JWT Authentication
2. Handling of X-APP-TOKEN and JWT token
5. Handles TraceId and Loki
6. Handles Kafka for async events
7. Handles WebSocket for real-time updates
8. API Error Handling RFC7807
9. Integrates Flyway DB migration
10. Integrates MapStruct
11. Integrates Lombok

# How to start all services
`docker-compose up -d --build` 
- frontend is accessible via http://localhost:4200 or http://frontend.localhost
- keycloak is accessible via http://localhost:8080 (admin:admin) or http://keycloak.localhost
- backend is accessible via http://localhost:8050 or http://api.localhost (keep in mind to add x-app-token & jwt to all requests)
- portainer is accessible via http://localhost:9443 or http://portainer.localhost
- adminer is accessible via http://localhost:8099 or http://adminer.localhost
- kafka-ui is accessible via http://localhost:8888 or http://kafka.localhost
- grafana is accessible via http://localhost:3000 or http://grafana.localhost

Note: Update the /etc/hosts file (equivalent in Windows) if you want the second alternative
