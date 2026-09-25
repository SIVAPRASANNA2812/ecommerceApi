# ==========================================
# Stage 1: Build the Application
# ==========================================
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# 1. Copy Maven wrapper and pom.xml first
COPY mvnw pom.xml ./
COPY .mvn .mvn

# 2. Ensure the maven wrapper has execution permissions in Linux
RUN chmod +x ./mvnw

# 3. Download dependencies (this layer gets cached by Docker)
RUN ./mvnw dependency:go-offline -B

# 4. Copy source code and build the production .jar file
COPY src src
RUN ./mvnw clean package -DskipTests

# ==========================================
# Stage 2: Minimal Runtime Image
# ==========================================
FROM eclipse-temurin:25-jdk
WORKDIR /app

# 1. Create a non-root system user for security (Industry standard)
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# 2. Copy only the built .jar from Stage 1
COPY --from=build /app/target/*.jar app.jar

# 3. Expose port 8080 (where Tomcat listens)
EXPOSE 8080

# 4. Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]