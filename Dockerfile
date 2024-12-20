# 1. JDK 설정
FROM openjdk:17-jdk-slim AS production

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 애플리케이션 설정 파일 복사
COPY src/main/resources/application.yml.template /app/application.yml

# 4. JAR 파일 복사
COPY build/libs/*.jar /app/app.jar

# 5. 컨테이너 실행 시 JAR 파일 실행 후 복사한 설정파일로 실행 (환경변수들로 세팅해주었음)
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.config.location=file:/app/application.yml"]

# 6. 8080포트 개방
EXPOSE 8080