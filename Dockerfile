# 빌드 단계: JDK 21 + Gradle Wrapper
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
# Windows 커밋 시 누락되는 실행 권한 보정
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

# 실행 단계: JRE 21 경량 이미지
FROM eclipse-temurin:21-jre
WORKDIR /app
# plain jar 제외, 실행 가능한 boot jar만 복사
COPY --from=build /app/build/libs/maple-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
# 무료 인스턴스 저메모리 환경 대응 힙 비율 제한
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
