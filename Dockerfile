# Stage 1: build
FROM maven:eclipse-temurin AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

#Stage 2: extract jar
FROM eclipse-temurin:17-jre-jammy AS extract
WORKDIR /build
COPY --from=build /build/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract


FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN mkdir -p /app/storage

COPY --from=extract /build/dependencies/ ./
COPY --from=extract /build/spring-boot-loader/ ./
COPY --from=extract /build/snapshot-dependencies/ ./
COPY --from=extract /build/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]