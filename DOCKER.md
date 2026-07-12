# Docker Deployment Guide

## Prerequisites

- Docker 20.10+
- Docker Compose 2.0+
- Make (optional, for convenience commands)

## Quick Start

1. **Clone and configure**:
   ```bash
   cd AT-OCP
   cp .env.example .env
   # Edit .env with your values
   ```

2. **Start services**:
   ```bash
   # Using Make
   make up
   
   # Or using Docker Compose directly
   docker-compose up -d
   ```

3. **Verify deployment**:
   ```bash
   make ps           # Check container status
   make logs-backend # View backend logs
   ```

4. **Access the application**:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Actuator: http://localhost:8080/actuator/health

## Services

| Service | Port | Description |
|---------|------|-------------|
| Backend | 8080 | Spring Boot API |
| PostgreSQL | 5432 | Database |
| Nginx | 80, 443 | Reverse Proxy |

## Common Commands

```bash
# Start services
make up

# Stop services
make down

# View logs
make logs
make logs-backend
make logs-nginx

# Restart services
make restart

# Rebuild images
make rebuild

# Clean up everything
make clean

# Run tests
make test-backend

# Generate coverage report
make jacoco-report

# Shell into containers
make shell-backend
make shell-postgres
```

## Production Deployment

### SSL Configuration

1. Place your SSL certificates in `nginx/ssl/`:
   - `server.crt` - Certificate
   - `server.key` - Private key

2. Uncomment the HTTPS server block in `nginx/conf.d/backend.conf`

3. Update the docker-compose.yml to mount SSL certificates

### Environment Variables

For production, set these environment variables:

```bash
export DB_PASSWORD=your-secure-db-password
export JWT_SECRET=your-very-long-random-jwt-secret
export SPRING_PROFILES=prod
```

### Database Backups

```bash
# Create backup
docker-compose exec postgres pg_dump -U at_ocp_user at_ocp_db > backup.sql

# Restore backup
docker-compose exec -T postgres psql -U at_ocp_user at_ocp_db < backup.sql
```

### Health Checks

```bash
# Check container health
docker-compose ps

# Manual health check
curl http://localhost:8080/actuator/health
```

## Troubleshooting

### Container won't start

```bash
# Check logs
docker-compose logs backend
docker-compose logs postgres

# Check if ports are in use
netstat -tulpn | grep 8080
```

### Database connection issues

```bash
# Verify database is ready
docker-compose exec postgres pg_isready -U at_ocp_user

# Check database logs
docker-compose logs postgres
```

### Out of memory

```bash
# Check container resource usage
docker stats

# Increase memory in docker-compose.yml if needed
```
