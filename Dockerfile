# 빌드 스테이지
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

# 실행 스테이지
FROM openjdk:17-jdk-slim
WORKDIR /app

# 타임존 설정
RUN apt-get update && \
    apt-get install -y --no-install-recommends tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 애플리케이션 jar 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 실행 환경 변수 설정
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/auction_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
ENV SPRING_DATASOURCE_USERNAME=auction_user 
ENV SPRING_DATASOURCE_PASSWORD=auction_password
ENV SPRING_REDIS_HOST=redis
ENV SPRING_REDIS_PORT=6379

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# 컨테이너 포트 노출
EXPOSE 8080