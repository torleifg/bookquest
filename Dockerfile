FROM bellsoft/liberica-runtime-container:jdk-21-glibc as builder
WORKDIR /opt/app

COPY gradlew gradlew
COPY gradle/wrapper gradle/wrapper
COPY build.gradle settings.gradle ./

RUN ./gradlew dependencies --no-daemon --stacktrace

COPY adapter/ adapter/
COPY gateway/ gateway/
COPY core/ core/
COPY application/ application/

RUN ./gradlew bootJar --no-daemon --stacktrace

FROM bellsoft/liberica-runtime-container:jre-21-glibc
WORKDIR /opt/app
EXPOSE 8080

RUN apk add --no-cache libstdc++

COPY --from=builder /opt/app/application/build/libs/*.jar /opt/app/app.jar
ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]