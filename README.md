
# Blog Application Setup Instructions

### Prerequisites
Before running the application, certain steps must be completed to ensure that the application runs smoothly. These include creating the necessary database and user in MySQL, configuring Flyway migrations, and setting up profiles for different environments.

# Blog Application Setup Instructions

> **Note:** If you do not have Docker installed on your system, start with [1.2 Create the Database and User].

### 1. Run the application
Go to the location where docker file exist and open in any terminal execute  `docker-compose up`.


### 1.2. Create the Database and User
You need to manually create the database and user in MySQL. Run the following SQL script in your MySQL environment:
```sql
DROP DATABASE IF EXISTS blogdb;
CREATE DATABASE IF NOT EXISTS blogdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

DROP USER IF EXISTS `bloguser`@`localhost`;
CREATE USER IF NOT EXISTS `bloguser`@`localhost` IDENTIFIED WITH mysql_native_password BY 'baturayacarturk';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, EXECUTE, CREATE VIEW, SHOW VIEW
ON blogdb.* TO `bloguser`@`localhost`;

FLUSH PRIVILEGES;
```

This script creates a `blogdb` database and a `bloguser` user with the required privileges.

### 2. Configure the Application

Ensure that your `application.properties` or `application.yml` files are configured to connect to the database with the credentials you’ve just created.

### 3. Flyway Database Migrations

Flyway is used to manage and apply database migrations. Flyway will automatically run any pending migrations on application startup, ensuring that your database schema is up to date.

To apply migrations, Flyway needs to connect to the correct database. Ensure that the `spring.flyway.enabled` property is set to true in the appropriate profile.

Flyway will automatically scan the `classpath:db/migration` directory for migration scripts and apply them in the correct order.

### 4. Profiling

The application uses Spring Profiles to manage different environments. The main profiles used in this project are:

-   **default:** Used for development environments, where the application connects to the MySQL database.
-   **test:** Used for running tests, also configured to use an H2 in-memory database.

You can switch between these profiles by setting the `spring.profiles.active` property. For example:


`./gradlew bootRun --args='--spring.profiles.active=test'` 

This will run the application using the local profile, connecting to the H2 database.

### 5. Running the Application

To start the application with the default profile (which connects to MySQL):

`./gradlew bootRun 

To start the application with the test profile (which connects to H2).

`./gradlew bootRun --args='--spring.profiles.active=test'`

### 6. Running Tests

Tests are configured to run against an in-memory H2 database using the test profile. This ensures that tests are isolated from the production and development databases. Except integration tests. 

To run the tests:

./gradlew test

Or to run specific tests:

./gradlew test --tests "com.blog.application.blog.repository.*"

Which runs every repository related tests. You can replace "repository" with package names.  

### 7. Running Flyway Migrations

To apply database migrations manually, you can use the following command:

`./gradlew flywayMigrate` 

Ensure that the database and user have been set up correctly before running this command. The migrations will be applied to the database specified in your `application.properties` or `application.yml` under the active profile.

### 8. Swagger Documentation

Once the application is running, you can access the Swagger UI to explore and test the API endpoints.

Swagger UI Endpoint: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
