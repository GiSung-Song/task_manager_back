# 1. JDK 설정
FROM openjdk:17-jdk-slim AS production

# 2. 변수 설정
ARG JAR_PATH=build/libs/*.jar

# 3. JAR 파일 복사
COPY ${JAR_PATH} app.jar

# 4. 컨테이너 실행 시 JAR 파일 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]

# 5. 8080포트 개방
EXPOSE 8080