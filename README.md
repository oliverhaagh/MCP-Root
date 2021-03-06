[![Build Status](https://travis-ci.org/oliverhaagh/MCP-Root.svg?branch=master)](https://travis-ci.org/oliverhaagh/MCP-Root)
# MCC Root CA List Service

## Setup
To set up the service you will need to have a running instance of MariaDB or MySQL and have Java 8 installed. 

The [setup.sql](setup.sql) file can be used to set up a database and user for the application. 

## Building and Running
Building the project can either be done using an existing installation of Maven 3+ or using the included Maven wrapper.

Building using an existing Maven installation can be done using:

```
mvn clean install
``` 

Using the Maven wrapper on Windows:
```
.\mvnw.cmd clean install
```

On Linux and MacOS:
```
./mvnw clean install
```

This will generate an executable WAR file in the target folder which can then be executed using:
```
java -jar target/root-ca-list-0.0.1-SNAPSHOT.war
```

NOTE that the default configuration is using an in-memory database which will most likely not work as intended on runtime.
It is therefore recommended to actually execute the application with the 'prod' profile activated:
```
java -Dspring.profiles.active=prod -jar target/root-ca-list-0.0.1-SNAPSHOT.war
```

If using an IDE like Eclipse or IntelliJ the main() function can also be run directly. 

## API
When running the application both a Swagger 2 API and an OpenAPI 3 API will be generated. 
The Swagger 2 definition can be gotten on http://localhost:8080/v2/api-docs, and the OpenAPI 3 definition can be gotten on http://localhost:8080/v3/api-docs. 
