# mod-reading-room

Copyright (C) 2022-2023 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Table of Contents

- [Introduction](#introduction)
- [API information](#api-information)
- [Installing and deployment](#installing-and-deployment)
  - [Compiling](#compiling)
  - [Running it](#running-it)
  - [Docker](#docker)
  - [Module descriptor](#module-descriptor)
  - [Environment variables](#environment-variables)
- [Additional information](#Additional-information)
  - [Issue tracker](#issue-tracker)
  - [API documentation](#api-documentation)
  - [Code analysis](#code-analysis)
  - [Other documentation](#other-documentation)

## Introduction

APIs for managing reading room and its related access.

## API information

Reading Room Access API provides the following URLs:

| Method | URL                                             | Permissions | Description                                                                                                                                                                                                                                    |
|--------|-------------------------------------------------|-------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GET    | /reading-room                                   |   reading-room.collection.get          | Get list of reading rooms based on Cql query. By default, the api only return the records with isDeleted = false. To get both deleted and non deleted records, the query param includeDeleted with value true should be passed in the request. |
| POST   | /reading-room                                   | reading-room.item.post            | create a new reading room                                                                                                                                                                                                                      |
| PUT    | /reading-room/{readingRoomId}                   |  reading-room.item.put           | Update reading room details                                                                                                                                                                                                                    |
| DELETE | /reading-room/{readingRoomId}                   |   reading-room.item.delete          | Delete a reading room. This api will do soft deletion. Update the isDeleted flag to true and delete the associated servicePoints for a reading room                                                                                            |
| POST   | /reading-room/{readingRoomId}/access-log        |    reading-room.access-log.post         | create a access log for reading room                                                                                                                                                                                                           |
| GET    | /reading-room-patron-permission/{patronId}      |   reading-room.patron-permission.item.get          | Get list of patron permissions based on userId                                                                                                                                                                                                 |
| PUT    | /reading-room-patron-permission/{patronId}      |     reading-room.patron-permission.item.put        | update list of patron permissions based on userId                                                                                                                                                                                              |
| GET    | /reading-room/access-log        |   reading-room.access-log.collection.get          | Get access log entries based on Cql query.                                                                                                                                                                                                     |

## Installing and deployment

### Compiling

Compile with
```shell
mvn clean install
```

### Running it

Run locally on listening port 8081 (default listening port):

Using Docker to run the local stand-alone instance:

```shell
DB_HOST=localhost DB_PORT=5432 DB_DATABASE=okapi_modules DB_USERNAME=folio_admin DB_PASSWORD=folio_admin \
   java -Dserver.port=8081 -jar target/mod-reading-room-*.jar
```

### Docker

Build the docker container with:

```shell
docker build -t dev.folio/mod-reading-room .
```

### Module Descriptor

See the built `target/ModuleDescriptor.json` for the interfaces that this module
requires and provides, the permissions, and the additional module metadata.

### Environment variables

| Name                   |    Default value    | Description                                                                                                                                                                |
|:-----------------------|:-------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DB_HOST                |      postgres       | Postgres hostname                                                                                                                                                          |
| DB_PORT                |        5432         | Postgres port                                                                                                                                                              |
| DB_USERNAME            |     folio_admin     | Postgres username                                                                                                                                                          |
| DB_PASSWORD            |          -          | Postgres username password                                                                                                                                                 |
| DB_DATABASE            |    okapi_modules    | Postgres database name                                                                                                                                                     |
| ACTUATOR\_EXPOSURE     | health,info,loggers | Back End Module Health Check Protocol                                                                                                                                      |
## Additional information

### Issue tracker

See project [MODRR](https://folio-org.atlassian.net/browse/UXPROD-4663)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### ModuleDescriptor

See the built `target/ModuleDescriptor.json` for the interfaces that this module
requires and provides, the permissions, and the additional module metadata.

### API documentation

This module's [API documentation](https://dev.folio.org/reference/api/#mod-reading-room).

### Code analysis

[SonarQube analysis](https://sonarcloud.io/project/overview?id=org.folio:mod-reading-room).

## Other documentation

The built artifacts for this module are available.
See [configuration](https://dev.folio.org/download/artifacts) for repository access,
and the [Docker image](https://hub.docker.com/r/folioci/mod-reading-room). Look at contribution guidelines [Contributing](https://dev.folio.org/guidelines/contributing).
