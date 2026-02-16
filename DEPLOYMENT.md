# Shifterizator Backend - Docker Deployment Guide

This guide explains how to deploy the Shifterizator backend application using Docker Compose.

## Prerequisites

- Docker Desktop (Windows/Mac) or Docker Engine (Linux)
- Docker Compose (included with Docker Desktop)
- At least 2GB of free disk space
- At least 2GB of available RAM

## Quick Start

### 1. Clone and Navigate

**Full stack (backend + frontend):** Clone both repos as siblings, then:

```bash
cd shifterizator-backend
```

**Backend only:** If you only need the API, clone just the backend repo and use `docker-compose up -d --build shifterizator-mysql shifterizator-backend` to skip the frontend.

### 2. Create Environment File

Copy the example environment file and fill in your values:

```bash
cp .env.example .env
```

**Important:** Edit `.env` and set:
- `JWT_SECRET` - Generate with: `openssl rand -base64 64`
- `MYSQL_PASSWORD` - Choose a secure password
- `MYSQLPASSWORD` - Must match `MYSQL_PASSWORD`

### 3. Build and Start

```bash
docker-compose up -d --build
```

This will:
- Build the Spring Boot application Docker image
- Build the React frontend (requires `shifterizator-frontend` repo as sibling)
- Start MySQL container
- Start the backend container
- Start the frontend container (served on port 3000 by default)
- Wait for MySQL to be healthy before starting the app

### 4. Verify Deployment

Check container status:
```bash
docker-compose ps
```

Check application logs:
```bash
docker-compose logs -f shifterizator-backend
```

Test the health endpoint:
```bash
curl http://localhost:8080/api/health
```

**If frontend is running:** Open http://localhost:3000 in your browser

## Environment Variables

All configuration is done via the `.env` file. See `.env.example` for all available variables.

### Required Variables

- `JWT_SECRET` - JWT signing secret (generate with `openssl rand -base64 64`)
- `MYSQL_PASSWORD` / `MYSQLPASSWORD` - Database password (must match)
- `MYSQL_DATABASE` / `MYSQLDATABASE` - Database name (must match)
- `MYSQL_USER` / `MYSQLUSER` - Database user (must match)

### Optional Variables

- `APP_PORT` - External port for the application (default: 8080)
- `FRONTEND_PORT` - External port for the frontend (default: 3000)
- `VITE_API_BASE_URL` - API base URL for frontend (must be reachable from browser; default: `http://localhost:8080/api`)
- `MYSQL_PORT` - External port for MySQL (default: 3306)
- `CORS_ALLOWED_ORIGINS` - Comma-separated list of allowed frontend origins
- `DDL_AUTO` - JPA schema mode: `validate` (recommended) or `update`

## Common Commands

### Start Services
```bash
docker-compose up -d
```

### Stop Services
```bash
docker-compose stop
```

### Stop and Remove Containers
```bash
docker-compose down
```

### Stop and Remove Containers + Volumes (⚠️ Deletes Database)
```bash
docker-compose down -v
```

### View Logs
```bash
# All services
docker-compose logs -f

# Application only
docker-compose logs -f shifterizator-backend

# MySQL only
docker-compose logs -f shifterizator-mysql
```

### Rebuild Application
```bash
docker-compose up -d --build shifterizator-backend
```

### Access MySQL CLI
```bash
docker-compose exec shifterizator-mysql mysql -u shifterizator -p shifterizator_prod
```

## Architecture

```
┌─────────────────────────────────────┐
│     docker-compose.yml              │
│  ┌──────────────┐  ┌─────────────┐ │
│  │  MySQL       │  │  Spring     │ │
│  │  Container   │◄─┤  Boot App   │ │
│  │  (port 3306) │  │  Container  │ │
│  │              │  │  (port 8080)│ │
│  └──────────────┘  └─────────────┘ │
│       │                  │         │
│       └──────────────────┘         │
│         Docker Network              │
└─────────────────────────────────────┘
```

**Note:** The frontend service builds from `../shifterizator-frontend`. Clone both repos as siblings for full-stack Docker.

## Health Checks

Both containers have health checks configured:

- **MySQL**: Checks if MySQL is accepting connections
- **Application**: Checks `/api/health` endpoint

The application waits for MySQL to be healthy before starting.

## Data Persistence

MySQL data is stored in a Docker volume: `shifterizator_mysql_data`

To backup:
```bash
docker run --rm -v shifterizator-backend_shifterizator_mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-backup.tar.gz /data
```

To restore:
```bash
docker run --rm -v shifterizator-backend_shifterizator_mysql_data:/data -v $(pwd):/backup alpine tar xzf /backup/mysql-backup.tar.gz -C /
```

## Troubleshooting

### Application won't start

1. Check logs: `docker-compose logs shifterizator-backend`
2. Verify MySQL is healthy: `docker-compose ps`
3. Check environment variables: `docker-compose config`

### Database connection errors

1. Verify MySQL container is running: `docker-compose ps shifterizator-mysql`
2. Check MySQL logs: `docker-compose logs shifterizator-mysql`
3. Verify credentials in `.env` match between `MYSQL_USER`/`MYSQLUSER` and `MYSQL_PASSWORD`/`MYSQLPASSWORD`

### Port already in use

Change `APP_PORT`, `FRONTEND_PORT`, or `MYSQL_PORT` in `.env` to use different ports.

### Frontend build fails (missing shifterizator-frontend)

The frontend service expects `../shifterizator-frontend` to exist. If you only need the backend, run:
```bash
docker-compose up -d --build shifterizator-mysql shifterizator-backend
```

### JWT Secret error

Ensure `JWT_SECRET` is set in `.env` and is at least 64 bytes (base64 encoded).

## Production Considerations

For production deployment:

1. **Change default passwords** - Update `MYSQL_ROOT_PASSWORD` and `MYSQL_PASSWORD`
2. **Use strong JWT secret** - Generate with `openssl rand -base64 64`
3. **Enable SSL** - Set `MYSQL_USE_SSL=true` if using remote database
4. **Set DDL_AUTO=validate** - Prevents automatic schema changes
5. **Configure CORS** - Set `CORS_ALLOWED_ORIGINS` to your frontend domain(s)
6. **Review logging** - Adjust log levels in `application-prod.yml` if needed
7. **Resource limits** - Add `deploy.resources` limits in docker-compose.yml for production

## Support

For issues or questions, check the logs first:
```bash
docker-compose logs -f
```
