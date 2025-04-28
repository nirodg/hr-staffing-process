FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn .mvn
COPY mvnw pom.xml ./

# Copy the source code (src) directory
COPY src ./src

# Install dependencies and build the app using Maven Wrapper
RUN ./mvnw clean package -DskipTests=true

# Copy the jar file to the Docker image
RUN cp target/*.jar app.jar

# Expose necessary ports
EXPOSE 8080
EXPOSE 5005

# Run the application with remote debugging enabled
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "app.jar"]
