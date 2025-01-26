# Library
## Overview
RESTful web service with Books, Categories and Customers.
## Technical details
* Java 21
* Maven
## Usage
run `mvn clean install` to build the application and execute all tests.

run `mvn spring-boot:run` to run the application.
The application will start on http://localhost:8081/,
the port kan be changed in the [application.properties](./src/main/resources/application.properties)

The Application provides CRUD interfaces for Customer, Books, and Categories.
Everyone can read Books and Categories.
The other interfaces can only be accessed with a token.
