FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
# git clone https://github.com/mangeot/jibikiREST.git
COPY src/ ./src/.
COPY lib/ ./lib/.
COPY build.xml .
RUN cp src/fr/jibiki/RestHttpServer.properties.in src/fr/jibiki/RestHttpServer.properties
RUN apk add apache-ant
RUN ant dist

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=builder /app/dist/lib/JibikiRest.jar .
COPY --from=builder /app/lib/postgresql-42.7.8.jar .
EXPOSE 80
CMD ["java", "-cp", "postgresql-42.7.8.jar:JibikiRest.jar","fr.jibiki.RestHttpServer"]