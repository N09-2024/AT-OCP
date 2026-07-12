-- Database initialization script
-- This script runs automatically on first container start

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create application user (if not exists)
-- Note: The user is already created by Docker via POSTGRES_USER env var

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE at_ocp_db TO at_ocp_user;

-- Create schema for Flyway migrations
CREATE SCHEMA IF NOT EXISTS flyway_schema_history;

-- Log initialization
DO $$
BEGIN
    RAISE NOTICE 'AT-OCP Database initialized successfully';
END $$;
