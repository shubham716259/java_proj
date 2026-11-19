# Multi-stage Docker build for Smart Notes
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

COPY lib/ ./lib/
COPY src/ ./src/

RUN mkdir bin && \
    javac -encoding UTF-8 -cp "lib/mysql-connector-j.jar:src" -d bin \
    src/com/notes/model/*.java \
    src/com/notes/dao/*.java \
    src/com/notes/helper/*.java \
    src/com/notes/web/*.java \
    src/com/notes/Main.java

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/bin ./bin
COPY lib/ ./lib/
COPY static/ ./static/
COPY db.properties ./db.properties

EXPOSE 8080
ENV PORT=8080

CMD ["java", "-cp", "bin:lib/mysql-connector-j.jar", "com.notes.Main"]
