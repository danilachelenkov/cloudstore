#Задаем базовый образ для контейнер
FROM openjdk:22-ea-21-slim-bullseye

#Внутренний порт REST-приложения
EXPOSE 8081

COPY target/diplom-cloudstore-0.0.1.jar cloudstore.jar

CMD ["java", "-jar", "cloudstore.jar"]
#ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "/cloudstore.jar"]