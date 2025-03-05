# Documentation

## Table of Contents
- [Database](#database)
    - [Database Structure](#database-structure)
- [REST APIs](#rest-apis)
- [Authentication](#authentication)
    - [Token Types](#token-types)
    - [Flow](#flow)
    - [Forgot Password - Reset Password](#forgot-password---reset-password)
    - [Auth Configuration](#auth-configuration)
- [RBAC](#rbac)
    - [Default Roles](#default-roles)
    - [Default Users](#default-users)
- [Email + Thymeleaf + RabbitMQ](#email--thymeleaf--rabbitmq)
    - [How to send an email?](#how-to-send-an-email)
    - [How add a new email template?](#how-add-a-new-email-template)
    - [RabbitMQ Queue](#rabbitmq-queue)
        - [Default Queues Structure](#default-queues-structure)
    - [SMTP - RabbitMQ Configuration](#smtp---rabbitmq-configuration)
- [HTTP Error Exception](#http-error-exception)
    - [Global Exception Handler](#global-exception-handler)
    - [Managed Exceptions](#managed-exceptions)
    - [How to add a new exception to handle?](#how-to-add-a-new-exception-to-handle)
- [Utils](#utils)
- [Annotations](#annotations)

## Database
PostgreSQL is used as the database. You can change the database adding corresponding JDBC dependency in `pom.xml` and updating configurations in `application.yml` file.

### Database Structure
```text
+-------------------------+       +--------------------------+        +--------------------------+
|        user_role        |       |      user_profile        |        |          token           |
+-------------------------+       +--------------------------+        +--------------------------+
| user_id (FK, bigint) ---+------>| id (PK, bigint)          |<---+   | id (PK, bigint)          |
| role_id (FK, bigint) ---+--+    | email (varchar(255))     |    |   | expiration (timestamp(6))|
+-------------------------+  |    | enabled (boolean)        |    |   | revoked (boolean)        |
                             |    | name (varchar(255))      |    |   | token (varchar(10240))   |
                             |    | password (varchar(255))  |    |   | token_type (varchar(255))|
                             |    | surname (varchar(255))   |    +---| user_id (FK, bigint) ----+
                             |    | username (varchar(255))  |        +--------------------------+
+-------------------------+  |    +--------------------------+
|         role            |  |
+-------------------------+  |
| id (PK, bigint)         |<-+----+
| name (varchar(255))     |       |
+-------------------------+       |
                                  |
+----------------------------+    |      +-------------------------+
|      role_permission       |    |      |      permission         |
+----------------------------+    |      +-------------------------+
| role_id (FK, bigint) ------+----+  +-->| id (PK, bigint)         |
| permission_id (FK, bigint) +-------+   | name (varchar(255))     |
+----------------------------+           +-------------------------+
```

Tables:
- `role` table stores the roles of the users.
- `permission` table stores the permissions of the roles.
- `user_profile` table stores the user details.
- `user_role` table stores the relationship between `user` and `role`.
- `role_permission` table stores the relationship between `role` and `permission`.
- `token` table stores the tokens generated for JWT access token and reset password.

Relationships:
- `user_role`: `user` can have multiple `roles`; `role` belongs to multiple `users`.
- `role_permission`: `role` can have multiple `permissions`; `permission` belongs to multiple `roles`.
- `token.user_id`: `user` can have multiple `tokens`; `token` belongs to only one `user`.

NOTE: 
- Use `data.sql` file to insert the default data in the database.
- There are tables with `qrtz_` prefix in the database created and used by Quartz.

## REST APIs
- `/api/auth**`: Authentication APIs.
- `/api/user**`: Users Management APIs.
- `/api/role**`: Roles Management APIs.
- `/api/permission**`: Permissions Management APIs.
- `/api/quartz**`: Quartz Jobs Management APIs.

You can use Postman to view and test the APIs. Import the collection from `API.postman_collection.json`.

### How import Postman collection?
- Open Postman.
- Click on `Import` button on the top left sidebar.
- Click on `Choose Files` and select the `API.postman_collection.json` file.
- Click on `Import` button.
- You will see the collection on the left sidebar.

## Authentication
JWT tokens flow is used for authentication.

### Token Types
- `Access Token`: Used to access the protected resources.
- `Refresh Token`: Used to get a new access token when the access token is expired.

### Flow
1. User logs in with email and password.
2. Server validates the credentials and generates an access token and a refresh token.
3. User sends the access token in the header (Authorization) to access the protected resources.
4. If the access token is expired, user sends the refresh token to get a new access token and a new refresh token.
5. If the refresh token is expired, user need login again to get a new access token and a new refresh token.
6. User can log out to revoke the access token.

### Forgot Password - Reset Password
1. User request to reset the password by providing the email.
2. Server sends an email with a reset password link.
3. User clicks on the link and provides the new password.
4. Server validates the token and updates the password.
5. User can log in with the new password.
6. User can request to reset the password again if the token is expired.

### Auth Configuration
You can change the configurations in `application.yml` file.
- `application.security.jwt.secret-key`: JWT secret key.
- `application.security.jwt.expiration`: Access token expiration time in milliseconds.
- `application.security.jwt.refresh-token.expiration`: Refresh token expiration time in milliseconds.
- `application.security.jwt.reset-password.expiration`: Reset password token expiration time in milliseconds.

NOTE: CHANGE JWT SECRET KEY in `application.yml` file.

## RBAC
Role-Based Access Control (RBAC) is used to manage the access control of the users.
Each user has a roles, which defines the access level of the user.
Each role has permissions, which defines the access level of the role.

### APIs Access Control
- `/api/auth**`: Everyone can access.
- `/api/user**`: Only `USER_**` permission can access.
- `/api/role**`: Only `ROLE_**` permission can access.
- `/api/permission**`: Only `PERMISSION_**` permission can access.

### Default Permissions
- `USER_READ`: Can access the resources `POST /api/user/list`.
- `USER_CREATE`: Can access the resources `POST /api/user`.
- `USER_UPDATE`: Can access the resources `PUT /api/user/:userId`.
- `USER_CHANGE_PASSWORD`: Can access the resources `PUT /api/user/change-password`.
- `USER_DELETE`: Can access the resources `DELETE /api/user/:userId`.
- `ROLE_READ`: Can access the resources `GET /api/role`.
- `PERMISSION_READ`: Can access the resources `GET /api/permission` and `GET /api/permission/role/:roleId`.
- `QUARTZ_JOB_READ`: Can access the resources `GET /api/quartz/job`.
- `QUARTZ_JOB_UPDATE`: Can access the resources `POST /api/quartz/job/reschedule`.

### Default Roles
- `ADMIN`: <br>
    Permissions:
    - `USER_READ`
    - `USER_CREATE`
    - `USER_UPDATE`
    - `USER_CHANGE_PASSWORD`
    - `USER_DELETE`
    - `ROLE_READ`
    - `PERMISSION_READ`
    - `QUARTZ_JOB_READ`
    - `QUARTZ_JOB_UPDATE`
- `USER`: <br>
    Permissions:
    - `USER_CHANGE_PASSWORD`

### Default Users
- Admin user:
    - username: `admin`
    - password: `Password`
- User:
    - username: `user`
    - password: `Password`

NOTE: CHANGE THE DEFAULT PASSWORDS in production.

## Email + Thymeleaf + RabbitMQ
This project sends emails using:
- `Thymeleaf` template engine to generate the email content.
- `RabbitMQ` to send the email asynchronously.

### How to send an email?
Use `EmailService` to send emails.
You can use the `EmailService.sendSimpleEmail` method to send an simple email.

### How add a new email template?
1. Create a new HTML file in `resources/email-templates` directory.
2. Add the template content.
3. Create a new method in `EmailService` to send the email using the new template.
4. Call the new method to send the email.

### RabbitMQ Queue
For each email type, there are separate queue and delay queue in RabbitMQ:
- `Main Queue`: The main queue to send the email and handle the email sending process.
- `Delay Queue`: When the sending of the email is failed, the email is sent to the delay queue to retry sending the email after a delay.

#### Default Queues Structure
```text
                       +----------------------+
                       |   EMAIL EXCHANGE     |
                       |       x.email        |
                       +----------+-----------+
                                  |
            +---------------------+---------------------+
            |                                           |
 +----------v--------------+                 +-----------v------------+
 | SIMPLE EMAIL QUEUE      |<-----+          | RESET PASSWORD QUEUE   |<----------+
 | q.email.simple-email    |      |          | q.email.reset-password |           |
 +----------+--------------+      |          +-----------+------------+           |
            |                     |                     |                         |
            |                     |                     |                         |
      IF send failure             |              IF send failure                  |
            |                     |                     |                         |
 +----------v----------------+    |            +--------v--------------------+    |
 | SIMPLE EMAIL DELAY QUEUE  |    |            |RESET PASSWORD DELAY QUEUE   |    |
 | q.email.simple-email.delay|    |            |q.email.reset-password.delay |    |
 +----------+----------------+    |            +--------+--------------------+    |
            |                     |                     |                         |
  After a delay, return           |             After a delay, return             |
  to main queue for retry         |             to main queue for retry           |
            |                     |                     |                         |
            +---------------------+                     +-------------------------+
```

### SMTP - RabbitMQ Configuration
You can change SMTP and RabbitMQ credentials configurations in `application.yml` file.

#### Thymeleaf Template Configuration
You can change the Thymeleaf template configurations in `email/configuration/EmailTemplateConfig.java` file.

#### RabbitMQ Configuration
You can change the RabbitMQ configurations in `common/configuration/RabbitMqConfig.java` file.

## Quartz Jobs
Quartz is used to schedule the jobs.

### How to use Quartz?
1. Create a new job class implementing `Job` interface.
2. Annotate the job class with `@QuartzJob` annotation.

NOTE: For more info about annotation `@QuartzJob`, go to [Annotations](#annotations) section.

## HTTP Error Exception
Custom HTTP exceptions are used to return the error messages in the response.

You can find the list of error messages classes in `common/exception` package.

### Global Exception Handler
`GlobalExceptionHandler` is used to handle the exceptions and return the right HTTP error in the response.

### Managed Exceptions
- `BadRequestException`: 400 Bad Request.
- `ValidationException`: 400 Bad Request with validation errors.
- `HttpMessageNotReadableException`: 400 Bad Request with message not readable.
- `BindException`: 400 Bad Request with @Valid binding errors.
- `UnauthorizedException`: 401 Unauthorized.
- `AuthenticationException`: 401 Unauthorized with authentication error.
- `ExpiredJwtException`: 401 Unauthorized with expired JWT token.
- `ForbiddenException`: 403 Forbidden.
- `AccessDeniedException`: 403 Forbidden with access denied.
- `NotFoundException`: 404 Not Found.
- `InternalServerErrorException`: 500 Internal Server Error.
- `RuntimeException`: 500 Internal Server Error with runtime exception.

### How to add a new exception to handle?
1. Create a new exception class in `common/exception` package.
2. Add new method in `GlobalExceptionHandler` to handle the new exception.

## Utils
- `FileManager`: Utility class to manage the file operations.
- `JsonUtils`: Utility class to manage the JSON conversion operations.
- `Pagination`: Utility to manage the pagination with mapper.

### How to use the Pagination?
1. Extend the `IMapper` interface in the mapper.
    ```java
    public interface MyEntityMapper extends IMapper<MyDto, MyEntity> {
        MyDto toDto(MyEntity entity);
    }
    ```
2. Create a new method in the service to return the paginated data.
    ```java
    private final PagedListMapper<MyDto, MyEntity> myEntityPagedMapper;
    private final MyEntityMapper myEntityMapper;
    
    public PagedListDto<MyDto> myPaginationMethod(
          int page, int size, String sort, String direction) {
        Page<MyEntity> Entities =
            myEntityRepository.findAll(
                PagedRequestParams.builder()
                    .page(page)
                    .size(size)
                    .sort(sort)
                    .direction(direction)
                    .build()
                    .asPageable());
        return myEntityPagedMapper.toDto(Entities, myEntityMapper);
      }
    ```

## Annotations
- `@ProvideUserLogged`: Annotation to get the logged user details from JWT token. Can be used in the controller method.
  ```java
  public void myControllerMethod(@ProvideUserLogged UserLogged userLogged) {
    // Code
  }
  ```
- `@RequestPartParsed`: Annotation to get the request part from the request and convert to specified class. Can be used in the controller method.
  ```java
  public void myControllerMethod(@RequestPartParsed("name") MyDto myDto) {
    // Code
  }
  ```
- `@ValidFileType`: Annotation to validate the file/s type. Can be used in the controller with request part.
  ```java
  public void myControllerMethod(@RequestPart("file") @Valid @ValidFileType(types = {"application/pdf"}) MultipartFile file) {
    // Code
  }
  ```
- `@QuartzJob`: Annotation to schedule the quartz job. <br>
  Annotation parameters:
  - `name`: Name of the quartz job.
  - `group`: Group of the quartz job.
  - `description`: Description of the quartz job.
  - `jobType`: Type of the quartz job.
  - `cronConfigKey`: Key of the cron expression in the application.yml file.
  - `defaultCronExpression`: Default cron expression. <br>
  
  JobType:
  - `APPLICATION_CONFIG`: This job use the application.yml file to get the cron expression, when the application start the job will be scheduled or rescheduled according to the cron expression automatically.
    ```java
    @QuartzJob(
        name = "my-job",
        group = "my-job-group",
        description = "My Job Description",
        jobType = JobType.APPLICATION_CONFIG,
        cronConfigKey = "application.cron-job.my-job")
    public class MyJob implements Job {
      // Code
    }
    ```
  - `USER_CONFIG`: This job use the API `/api/quartz/job/reschedule` to reschedule the job according to the cron expression provided by the user. For the first time, the job will be scheduled according to the cron expression provided in `defaultCronExpression` parameter.
    ```java
    @QuartzJob(
        name = "my-job",
        group = "my-job-group",
        description = "My Job Description",
        jobType = JobType.USER_CONFIG,
        defaultCronExpression = "0 0 0 * * ?")
    public class MyJob implements Job {
      // Code
    }
    ```
