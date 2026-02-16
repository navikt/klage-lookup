# klage-lookup

A Spring Boot microservice for performing various registry lookup operations for use in Team Klage applications.

## Overview

This service provides an API endpoint for verifying whether a NAV employee (identified by `navIdent`) has access to a specific user (`brukerId`). It integrates with Tilgangsmaskinen to perform population-based access control checks. When a case (`sak`) is provided, the service first retrieves all related aktør IDs from FPSAK and validates access against each of them.

It also provides functionality for getting various info from Azure AD, via EntraProxy. This includes checking groups for a given user, and for the currently logged in user (via token). Also provides info about users in a given enhet.

## Features

- **Access Verification**: Validates access rights via Tilgangsmaskinen
- **Case-based Access**: Retrieves aktør IDs from FPSAK when a case is provided, checking access for all related persons
- **Group verification**: Checks whether a given user belongs to a specific group in Azure AD via EntraProxy. When checking the currently logged in user, this check is performed on the given token
- **Enhet info**: Retrieves information about users currently in a given enhet from EntraProxy
- **User info**: Retrieves information about a given user from EntraProxy
- **Caching**: Results are cached in Redis/Valkey for 10 minutes to reduce load
- **Resilience**: Built-in retry mechanism using Spring Boot 4's resilience annotations
- **Metrics**: Prometheus metrics for monitoring Tilgangsmaskinen and FPSAK response times

## API Documentation

Swagger UI is available at `/swagger-ui.html`.

## Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────────────┐
│  Client App     │────▶│  klage-lookup   │────▶│  EntraProxy             │
│  (kabal-api,    │     │                 │     │  - Groups               │
│  kabal-         │     │  - Caching      │     │  - User info            │
│  innstillinger) │     │  - Retry        │     │  - Enhet users          │
└─────────────────┘     │  - Metrics      │     └─────────────────────────┘
                        │                 │     
                        │        │        │
                        │        │        │     ┌─────────────────────────┐
                        │        │        │────▶│  Tilgangsmaskinen       │
                        │        │        │     │  (populasjonstilgangs-  │
                        │        │        │     │   kontroll)             │
                        │        │        │     └─────────────────────────┘
                        │        │        │
                        │        │        │     ┌─────────────────────────┐
                        │        │        │────▶│  FPSAK                  │
                        │        ▼        │     │  (aktør lookup for sak) │
                        │  ┌──────────┐   │     └─────────────────────────┘
                        │  │ Valkey   │   │
                        │  │ (Redis)  │   │
                        │  └──────────┘   │
                        └─────────────────┘
```