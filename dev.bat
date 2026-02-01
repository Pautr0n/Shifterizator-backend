@echo off
echo Starting Shifterizator DEV environment...

echo Starting MySQL (docker compose)...
docker-compose up -d

echo Setting environment variables...
set SPRING_PROFILES_ACTIVE=dev
set MYSQL_HOST=localhost
set MYSQL_PORT=3307
set MYSQL_DATABASE=shifterizator_dev
set MYSQL_USER=root
set MYSQL_PASSWORD=root

echo Running Spring Boot with DEV profile...
mvn spring-boot:run -Dspring-boot.run.profiles=dev