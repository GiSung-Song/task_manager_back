# 1. JDK 설정
FROM openjdk:17-jdk-slim AS production

# 2. JAR 파일 복사
COPY build/libs/*.jar app.jar

# 3. 컨테이너 실행 시 JAR 파일 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]

# 4. 8080포트 개방
EXPOSE 8080