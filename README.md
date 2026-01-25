# klage-lookup

A Spring Boot microservice for verifying access to persons via Tilgangsmaskinen.

## Overview

This service provides an API endpoint for verifying whether a NAV employee (identified by `navIdent`) has access to a specific user (`brukerId`). It integrates with Tilgangsmaskinen to perform population-based access control checks.

## Features

- **Access Verification**: Validates access rights via Tilgangsmaskinen
- **Caching**: Results are cached in Redis/Valkey for 10 minutes to reduce load
- **Resilience**: Built-in retry mechanism using Spring Boot 4's resilience annotations
- **Metrics**: Prometheus metrics for monitoring Tilgangsmaskinen response times

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
                        │        ▼        │
                        │  ┌──────────┐   │
                        │  │ Valkey   │   │
                        │  │ (Redis)  │   │
                        │  └──────────┘   │
                        └─────────────────┘
```