## Project Overview
Project based on [Akka HTTP quickstart example](https://github.com/akka/akka-http-quickstart-scala.g8) from Akka Github.

# Running the Application

To set up the database, run the following command:
```shell
docker compose up -d
```
This will start the database container in the background.

To start the backend, use SBT:
```shell
sbt run
```

If you prefer using your IDE, you can also run the application by executing the `QuickstartApp` class.

# API

Use the following commands to interact with the API:

### Add a new record:
```shell
curl --location 'http://localhost:8080/records' \
--header 'Content-Type: application/json' \
--data '{
    "name": "John",
    "phoneNum": "+48123456789",
    "amount": 1000.0
}'
```
A record is considered valid only if the following conditions are met:
- "name" cannot be empty or have digits in it
- "phoneNum" has to be in proper format: `^(\+?[0-9]{1,3})?[0-9]{9}$`
  * Optional country code prefix (+ followed by 1-3 digits).
  * A valid phone number consisting of exactly 9 digits.
- "amount" cannot be less than 0

### Get record for processing
```shell
curl --location 'http://localhost:8080/records'
```
Fetches a single record for processing and marks it as processed.

### Get a report
```shell
curl --location 'http://localhost:8080/report?processedOnly=false'
```
Generates a report based on the current state of the records. 
- processedOnly flag:
  * When set to true, the report includes only records that have been processed.
  * When set to false, the report includes all records, both processed and unprocessed.

# Running integration tests
This project includes integration tests for both routes and repositories. Use the following commands to run the respective test suites:

- Run all integration tests (Routes and Repository):
```shell
sbt itTestAll
```
- Run only Routes tests:
```shell
sbt itTestRoutes
```
- Run only Repository tests:
```shell
sbt itTestRepo
```
