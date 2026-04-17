## 2.0.0 2026-04-17

### Breaking changes
* Upgrade to Spring Boot 4 ([MODRR-38](https://folio-org.atlassian.net/browse/MODRR-38))

### Features
* Use GitHub Workflows for Maven ([MODRR-40](https://folio-org.atlassian.net/browse/MODRR-40))

### Bug fixes
* Fix querying by enum values ([MODRR-32](https://folio-org.atlassian.net/browse/MODRR-32))

### Tech Debt
* Sensitive data in logs cleanup ([MODRR-30](https://folio-org.atlassian.net/browse/MODRR-30))

### Dependencies
* Bump `spring-boot` from `3.4.3` to `4.0.5`
* Bump `folio-spring-support` from `9.0.0` to `10.0.0`
* Bump `folio-backend-common` from `2.0.0` to `3.0.9` (replaces `folio-spring-system-user`)
* Bump `openapi-generator` from `7.9.0` to `7.21.0`
* Bump `checkstyle` from `10.15.0` to `13.4.0`

## 1.2.0 2025-08-17
* re-released with new version for app-reading-room release

## 1.1.0 2025-03-13
* [FOLIO-4218](https://folio-org.atlassian.net/browse/FOLIO-4218) - Update mod-reading-room to Java 21

## 1.0.0 2024-10-30

* [MODRR-5](https://folio-org.atlassian.net/browse/MODRR-5) - Maintain access logs for the Reading Room
* [MODRR-6](https://folio-org.atlassian.net/browse/MODRR-6) - Deployment liability for reading-room with schema creation.
* [MODRR-8](https://folio-org.atlassian.net/browse/MODRR-8) - Implement GET API to display reading room access details for a particular User.
* [MODRR-9](https://folio-org.atlassian.net/browse/MODRR-9) - Implement PUT API to update RRA for a particular user in the User detail screen.
* [MODRR-11](https://folio-org.atlassian.net/browse/MODRR-11) - Create POST API for RRA in Settings screen
* [MODRR-12](https://folio-org.atlassian.net/browse/MODRR-12) - Create PUT API for RRA in Settings screen
* [MODRR-13](https://folio-org.atlassian.net/browse/MODRR-13) - Create DELETE API for RRA in Settings screen
* [MODRR-14](https://folio-org.atlassian.net/browse/MODRR-14) - Delete the associated SP when RR is deleted and remove the name unique constraint
* [MODRR-17](https://folio-org.atlassian.net/browse/MODRR-17) - Add validation for PUT patron permission API
* [MODRR-18](https://folio-org.atlassian.net/browse/MODRR-18) - Patron permission get API is not returning all the reading rooms.
* [MODRR-19](https://folio-org.atlassian.net/browse/MODRR-19) - Adding test cases for missing scenarios
* [MODRR-20](https://folio-org.atlassian.net/browse/MODRR-20) - apk upgrade fixing vulns
* [MODRR-23](https://folio-org.atlassian.net/browse/MODRR-23) - Create GET API for reading room access log
* [MODRR-24](https://folio-org.atlassian.net/browse/MODRR-24) - Update Spring support version for Ramsons
* [MODRR-26](https://folio-org.atlassian.net/browse/MODRR-26) - Update pom.xml and interface dependencies for Ramsons
* [TCR-41](https://folio-org.atlassian.net/browse/TCR-41) - changing the logger statement as per TCR review comments
