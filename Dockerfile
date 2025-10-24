FROM bellsoft/liberica-runtime-container:jdk-24-glibc AS builder
WORKDIR /opt/app

COPY gradlew gradlew
COPY gradle/ gradle/
COPY build.gradle settings.gradle gradle.properties ./

RUN ./gradlew dependencies --no-daemon --stacktrace

COPY buildSrc/ buildSrc/
COPY adapter/ adapter/
COPY gateway/ gateway/
COPY core/ core/
COPY application/ application/

RUN ./gradlew bootJar --no-daemon --stacktrace

FROM bellsoft/liberica-runtime-container:jre-24-glibc
WORKDIR /opt/app
EXPOSE 8080

RUN apk add --no-cache libstdc++

COPY --from=builder /opt/app/application/build/libs/*.jar /opt/app/app.jar
ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]