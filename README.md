# Lost and Found System

This repository is now organized around Docker as the only way to build and run the application. Use `docker compose` from the project root to start all services.

## Project layout

- `backend/`: Spring Boot service packaged via its own `Dockerfile` and Maven `pom.xml`
- `frontend/`: Web client built with Node/Vite and served through Nginx using its `Dockerfile`
- `docker-compose.yml`: Wires together MySQL, the backend API, and the frontend UI

## Prerequisites

- Docker Engine
- Docker Compose V2 (typically available as the `docker compose` subcommand)

## Build and run

From the repository root, run:

```sh
docker compose up --build
```

The command builds the backend and frontend images, starts MySQL with seed data, then exposes the API on port `8080` and the web UI on port `80`.
