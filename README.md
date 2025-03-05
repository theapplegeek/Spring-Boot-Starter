# Spring Starter Pack

## Table of Contents
- [Introduction](#introduction)
- [Features](#features)
- [Structure](#structure)
- [Dependencies](#dependencies)
- [Documentation](DOCUMENTATION.md)
- [Docker Compose Integration](#docker-compose-integration)
- [Usage](#usage)
- [Post Creation Setup](#post-creation-setup)
- [License](#license)

## Introduction
This is a starter pack for Spring Boot projects, which includes common configurations, utils and APIs.

## Features
- Authentication: JWT tokens flow.
- Role-Based Access Control (RBAC).
- Send emails + Thymeleaf template + RabbitMQ.
- HTTP Error Exception + Global Exception Handler.
- Utils: Pagination, Annotations, etc.
- Docker compose integration.
- Feature based structure.

## Structure
Feature based structure is used in this project. Each feature has its own package, which usually includes `controllers`, `services`, `repositories`, `models`, `dto`, `mappers`, etc.

```text
spring_starter_pack
    ├── auth
    ├── common => common configurations, utils, etc; can be used by any feature.
    │   ├── annotation => custom annotations
    │   ├── configuration
    │   ├── exception => custom HTTP exceptions and GlobalExceptionHandler
    │   ├── payload => common HTTP request and response payloads
    │   └── util => common utility classes
    ├── email
    ├── permission
    |-- quartz
    ├── role
    ├── security
    ├── token
    └── user
        ├── controller
        ├── dto
        ├── error => list of error messages for frontend
        ├── mapper
        ├── model
        ├── repository
        └── service
```

## Dependencies
- Maven
- Java Development Kit (JDK) 21
- Spring Boot
- Spring Web
- Spring Validation
- Spring Data JPA
- Spring Security (JWT)
- Spring Mail
- Spring Thymeleaf
- Spring Boot DevTools
- Spring Docker Compose
- Spring Boot Test
- JDBC PostgreSQL Driver
- Lombok
- Mapstruct
- RabbitMQ
- Spotless
- Quartz

## Docker Compose Integration
This project includes Docker Compose integration to run the `compose.yaml` file automatically when start the application.

## Usage
- Use [Spring Boot CLI](https://github.com/theapplegeek/Spring-Boot-CLI) to generate a new project based on the starter pack.
- Or, Clone the repository and change project `name`, `artifactId`, `groupId`, etc.

## Post Creation Setup
- Update `application.yml` file with your configurations.
- Start the application. 
  - If you have Docker running, it will start the PostgreSQL and RabbitMQ containers automatically.
  - If you don't have Docker running, you need install PostgreSQL and RabbitMQ manually.
- Run `resources/db/data.sql` file to insert initial data into the database.
- Enjoy!

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.