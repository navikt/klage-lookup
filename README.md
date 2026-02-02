# klage-lookup

A Spring Boot microservice for verifying access to persons via Tilgangsmaskinen.

## Overview

This service provides an API endpoint for verifying whether a NAV employee (identified by `navIdent`) has access to a specific user (`brukerId`). It integrates with Tilgangsmaskinen to perform population-based access control checks. When a case (`sak`) is provided, the service first retrieves all related aktør IDs from FPSAK and validates access against each of them.

## Features

- **Access Verification**: Validates access rights via Tilgangsmaskinen
- **Case-based Access**: Retrieves aktør IDs from FPSAK when a case is provided, checking access for all related persons
- **Caching**: Results are cached in Redis/Valkey for 10 minutes to reduce load
- **Resilience**: Built-in retry mechanism using Spring Boot 4's resilience annotations
- **Metrics**: Prometheus metrics for monitoring Tilgangsmaskinen and FPSAK response times

## API Documentation

Swagger UI is available at `/swagger-ui.html`.

## Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────────────┐
│  Client App     │────▶│  klage-lookup   │────▶│  Tilgangsmaskinen       │
│  (kabal-api,    │     │                 │     │  (populasjonstilgangs-  │
│  kabal-         │     │  - Caching      │     │   kontroll)             │
│  innstillinger) │     │  - Retry        │     │                         │
└─────────────────┘     │  - Metrics      │     └─────────────────────────┘
                        │                 │
                        │        │        │     ┌─────────────────────────┐
                        │        │        │────▶│  FPSAK                  │
                        │        ▼        │     │  (aktør lookup for sak) │
                        │  ┌──────────┐   │     └─────────────────────────┘
                        │  │ Valkey   │   │
                        │  │ (Redis)  │   │
                        │  └──────────┘   │
                        └─────────────────┘
```