# Bento CRM Backend

A multi-tenant CRM backend built with Java 21, Spring Boot 3.3, PostgreSQL, and Redis.

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 21 (for local development without Docker)
- Maven 3.9+

### Local Development with Docker Compose

1. Copy environment configuration:
```bash
cp .env.example .env
```

2. Start services:
```bash
docker-compose up -d
```

3. Access the application:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- PgAdmin: http://localhost:5050

### Project Structure

```
com.bento.crm
├── common/           # Shared infrastructure
├── auth/             # Authentication & authorization
├── organization/     # Tenant management
├── identity/         # Users, teams, groups
├── partner/          # Lead, prospect, customer, vendor management
├── task/             # Task management
├── proposal/         # Proposals & templates
├── deal/             # Deals & pipeline
├── purchaseorder/    # Purchase orders
├── finance/          # Invoices, credit notes
├── ticket/           # Support tickets
├── campaign/         # Marketing campaigns
├── automation/       # Automation rules & webhooks
├── notification/     # Notifications
├── analytics/        # Reporting & analytics
├── file/             # File storage
└── audit/            # Audit trail
```

## API Documentation

OpenAPI 3.0 documentation is available at:
- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/openapi`

## Testing

### Run Tests
```bash
mvn test
```

### Integration Tests with Testcontainers
Tests automatically spin up PostgreSQL and Redis containers.

## Phases

- **Phase 0**: Foundation (✓ Complete)
- **Phase 1**: Identity & RBAC (in progress)
- **Phase 2**: Partner Core
- **Phase 3**: Sales Pipeline
- **Phase 4**: Operations & Finance
- **Phase 5**: Support & Marketing
- **Phase 6**: Automation & Notifications
- **Phase 7**: Analytics & Hardening
- **Phase 8**: Production Readiness
