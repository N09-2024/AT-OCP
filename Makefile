.PHONY: help build up down restart logs logs-backend logs-nginx clean ps

# Colors
GREEN  := \033[0;32m
YELLOW := \033[0;33m
NC     := \033[0m

help: ## Show this help message
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "$(GREEN)%-15s$(NC) %s\n", $$1, $$2}'

# Build all Docker images
build: ## Build Docker images
	@echo "$(YELLOW)Building Docker images...$(NC)"
	docker-compose build

# Start all services
up: ## Start all services
	@echo "$(YELLOW)Starting services...$(NC)"
	docker-compose up -d
	@echo "$(GREEN)Services started!$(NC)"
	@echo "Backend API: http://localhost:8080"
	@echo "PostgreSQL: localhost:5432"

# Stop all services
down: ## Stop all services
	@echo "$(YELLOW)Stopping services...$(NC)"
	docker-compose down

# Restart all services
restart: down up ## Restart all services

# View all logs
logs: ## View all logs
	docker-compose logs -f

# View backend logs only
logs-backend: ## View backend logs
	docker-compose logs -f backend

# View nginx logs only
logs-nginx: ## View nginx logs
	docker-compose logs -f nginx

# View PostgreSQL logs only
logs-postgres: ## View PostgreSQL logs
	docker-compose logs -f postgres

# Clean up volumes and stopped containers
clean: ## Clean up Docker resources
	@echo "$(YELLOW)Cleaning up...$(NC)"
	docker-compose down -v --remove-orphans
	docker system prune -f

# Show running containers
ps: ## Show running containers
	docker-compose ps

# Build and run tests
test-backend: ## Run backend tests
	docker-compose run --rm backend mvn test

# Generate JaCoCo report
jacoco-report: ## Generate JaCoCo coverage report
	docker-compose run --rm backend mvn jacoco:report

# Enter backend container shell
shell-backend: ## Shell into backend container
	docker-compose exec backend sh

# Enter PostgreSQL container shell
shell-postgres: ## Shell into PostgreSQL container
	docker-compose exec postgres psql -U at_ocp_user -d at_ocp_db

# Rebuild without cache
rebuild: ## Rebuild images without cache
	docker-compose build --no-cache
