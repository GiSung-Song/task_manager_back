# 1. JDK 설정
FROM openjdk:17-jdk-slim AS production

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. JAR 파일 복사
COPY build/libs/*.jar app.jar

# 4. 컨테이너 실행 시 JAR 파일 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# 5. 8080포트 개방
EXPOSE 8080