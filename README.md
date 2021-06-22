# pbl-server-app-0.1
Server application for generating and managing data for machine learning

# About
pbl-server-app-0.1 is JAVA project for university PBL (Project Based Learning) of automatization computing for AI. This software is based on RabbitMQ service. 
After logging to the application user can create his own computing task for other service. He can also import calculation sets from .CSV files to fill them with data from sending form, or import calculation set with setted values from .xls or .xlsx files. User can choose target queue from which other software placed on different machines will get its own task. After sending task we can see and monitor tasks result from service.

# How to run
Download jar file from target directory and run it in your command prompt. It will run on localhost:8080. After start it will create settings.txt file to personalize work variables (you can change it but it needs restart after every change). For proper work on yours pc you need RabbitMQ service (from Docker for example use this command -> [docker run --rm -it --hostname my-rabbit -p 15672:15672 -p 5672:5672 rabbitmq:3-management guest:guest] or installed as standalone Windows service) with user (springuser, springpassword)

# Stack
- Java 11
- Spring Boot, Spring Security
- H2 Database / JPA
- Thymeleaf + HTML/CSS/JS/Bootstrap
- Javax Validation
- RabbitMQ
- Apache Commons CSV, Lombok
