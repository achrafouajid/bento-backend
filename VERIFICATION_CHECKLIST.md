# Implementation Verification Checklist

## Pre-Flight Checks

- [ ] Clone/navigate to crm-backend directory
- [ ] Read PROJECT_SUMMARY.md (5 min overview)
- [ ] Read IMPLEMENTATION_STATUS.md (patterns guide)
- [ ] `cp .env.example .env` (create environment file)
- [ ] Check `docker --version` (Docker installed)

## Phase 0-1 Verification (Infrastructure & Auth)

### Database & Configuration
- [ ] File exists: `src/main/resources/db/migration/V1__baseline.sql`
- [ ] File exists: `src/main/resources/db/migration/V2__phase1_identity_rbac.sql`
- [ ] File exists: `src/main/resources/application.yml` (Spring config)
- [ ] JwtProperties loaded from JWT_SECRET env var
- [ ] TenantContext properly ThreadLocal-scoped

### Security Configuration
- [ ] SecurityConfig has @PreAuthorize integration
- [ ] JwtAuthFilter extracts token and authorities
- [ ] TenantFilterInterceptor enables Hibernate @Filter
- [ ] RateLimitFilter applied before auth endpoints
- [ ] CORS explicitly configured (not wildcard)

### Classes to Verify Exist
```
✓ Permission enum (with role matrix)
✓ UserRole enum (ADMIN, MANAGER, SALESPERSON, SUPPORT, VIEWER)
✓ TenantContext (ThreadLocal holder)
✓ BaseTenantEntity (all entities extend this)
✓ Organization, AppUser, RefreshToken, Team, CrmGroup entities
✓ AuthService, JwtService, OrganizationService, UserService
✓ AuthController, OrganizationController, UserController
✓ GlobalExceptionHandler (RFC 7807)
✓ OpenApiConfig (Swagger wiring)
✓ SecurityConfig + JwtAuthFilter + RateLimitFilter
```

## Phase 2 Verification (Partner Core)

### Database
- [ ] File exists: `src/main/resources/db/migration/V3__phase2_partner_core.sql`
- [ ] Partner, PartnerAddress, PartnerContact, PartnerFiscalProfile exist
- [ ] PartnerActivity, Tag, StoredFile entities created
- [ ] All tables have organization_id as leading index column
- [ ] Foreign key relationships properly defined

### Services & Storage
- [ ] PartnerRepository has org-scoped @Query methods
- [ ] PartnerService follows CRUD pattern
- [ ] FileStorageService interface exists
- [ ] LocalFileStorageService validates content-type and size
- [ ] File operations check TenantContext for org-scoped paths

### Controllers
- [ ] PartnerController exists with standard REST endpoints
- [ ] All endpoints have @PreAuthorize annotations
- [ ] POST /partners returns 201 Created
- [ ] GET /partners supports pagination
- [ ] DELETE /partners/{id} returns 204 No Content

## Phase 3 Verification (Sales Pipeline)

### Entities Exist
- [ ] ProposalTemplate entity defined
- [ ] Proposal + ProposalLine entities defined
- [ ] Deal + DealOrderLine entities defined
- [ ] Task entity with polymorphic related_to defined
- [ ] Database migration V4__phase3_sales_pipeline.sql created

### Pending Implementation
- [ ] ProposalService (create, update, list, send)
- [ ] DealService (create, update, list, stage transition)
- [ ] TaskService (create, update, list, comment)
- [ ] ProposalController + DealController + TaskController
- [ ] ProposalRepository, DealRepository, TaskRepository

## Docker & Deployment Verification

### Docker Setup
- [ ] Dockerfile exists with multi-stage build
- [ ] Runtime image is eclipse-temurin:21-jre-alpine
- [ ] App runs as non-root user
- [ ] Health check configured
- [ ] docker-compose.yml defines: postgres, redis, app, pgadmin

### Environment Configuration
- [ ] .env file exists (created from .env.example)
- [ ] Database credentials set in .env
- [ ] JWT_SECRET set to strong value in .env
- [ ] REDIS_PASSWORD configured
- [ ] CORS_ALLOWED_ORIGINS set to your frontend origins

### Running Locally
```bash
# Start containers
docker-compose up -d

# Check services health
docker-compose ps

# Access endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8080/swagger-ui.html

# Check logs
docker-compose logs -f app
```

## API Testing Checklist

### Authentication Flow
```bash
# 1. Create organization
curl -X POST http://localhost:8080/organizations \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Org",
    "admin_email": "admin@test.com",
    "admin_name": "Admin User",
    "admin_password": "SecurePassword123!",
    "default_currency": "USD",
    "timezone": "UTC"
  }'

# 2. Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@test.com",
    "password": "SecurePassword123!"
  }'

# 3. Copy access_token from response

# 4. Test protected endpoint
curl -X GET http://localhost:8080/users \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

# 5. Refresh token
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refresh_token": "<REFRESH_TOKEN>"}'
```

### Partner Management
```bash
# List partners
curl -X GET "http://localhost:8080/partners?page=0&size=20" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

# Create partner
curl -X POST http://localhost:8080/partners \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -d '{
    "type": "LEAD",
    "name": "Acme Corporation",
    "email": "contact@acme.com",
    "stage": "NEW"
  }'

# Get partner details
curl -X GET http://localhost:8080/partners/{id} \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

## Database Verification

### Connect to PostgreSQL
```bash
# Via pgadmin (GUI)
# Open http://localhost:5050
# Login: admin@example.com / admin

# Via psql (CLI)
psql -h localhost -U postgres -d crm_db

# Check tables
\dt

# Verify organization_id column on all tables
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public';

SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'app_user';
```

### Verify Migrations
```sql
-- Connect to crm_db
SELECT * FROM flyway_schema_history;

-- Should show:
-- V1__baseline.sql
-- V2__phase1_identity_rbac.sql
-- V3__phase2_partner_core.sql
-- V4__phase3_sales_pipeline.sql
```

## Code Quality Checks

### No Hardcoded Secrets
- [ ] Run: `grep -r "JWT_SECRET" src/` → only in JwtProperties (from env)
- [ ] Run: `grep -r "password" src/main/java` → no hardcoded values
- [ ] .env file in .gitignore
- [ ] No credentials in application.yml

### Tenant Isolation
- [ ] All @Entity classes extend BaseTenantEntity
- [ ] All repository queries use `WHERE e.organizationId = :orgId`
- [ ] TenantContext.getCurrentOrganizationId() used in services
- [ ] No queries without organization_id filter

### Authorization
- [ ] Every mutation endpoint has @PreAuthorize
- [ ] Permission.forRole() provides complete matrix
- [ ] At least one ADMIN check in OrganizationService.createOrganization
- [ ] DeactivateUser prevents removing last admin

## Logging & Monitoring

### Actuator Endpoints
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/prometheus
```

### Expected Responses
- [ ] /actuator/health → 200 {"status":"UP"}
- [ ] /actuator/metrics → 200 {"names":[...]}
- [ ] /actuator/prometheus → 200 (Prometheus format)

## Performance Baseline

### Query Optimization
- [ ] No N+1 queries (check logs for multiple queries per request)
- [ ] Pagination default size=20, max size=100
- [ ] Indexes on (organization_id, status, created_at DESC)
- [ ] List endpoints use projections, not full entities

### Rate Limiting
```bash
# Auth endpoint: 10 per minute per IP
for i in {1..12}; do
  curl -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@test.com","password":"wrong"}'
done
# 11th request should return 429 Too Many Requests

# Check X-Rate-Limit-Remaining header
curl -v http://localhost:8080/auth/login ... 2>&1 | grep X-Rate-Limit
```

## Documentation Verification

- [ ] README.md explains quick start
- [ ] OpenAPI available at /openapi
- [ ] Swagger UI at /swagger-ui.html
- [ ] PHASE_PROGRESS.md lists what's done
- [ ] IMPLEMENTATION_STATUS.md explains patterns
- [ ] PROJECT_SUMMARY.md provides overview

## Integration Test Verification

```bash
# Run tests
mvn test

# Expected output:
# SmokeTest.java should pass with testcontainers
# - testApplicationContextLoads
# - testHealthCheckEndpoint
# - testMetricsEndpoint
```

## Cleanup & Final Check

- [ ] Delete test data created during verification
- [ ] Restart docker-compose to verify clean state: `docker-compose down && docker-compose up -d`
- [ ] Confirm fresh organization signup flow works
- [ ] Document any deviations from IMPLEMENTATION_PLAN.md
- [ ] Create ticket/issue for Phase 3 implementation if needed

## ✅ Sign-Off

- [ ] All infrastructure checks pass
- [ ] Authentication flow works end-to-end
- [ ] Partner CRUD operations functional
- [ ] Database migrations applied correctly
- [ ] Rate limiting operative
- [ ] Documentation complete
- [ ] Ready to proceed with Phase 3

---

**Estimated Time**: 30-45 minutes for full verification

**If any checks fail**: 
1. Review IMPLEMENTATION_STATUS.md for the relevant component
2. Check docker-compose logs: `docker-compose logs -f app`
3. Verify .env file has all required variables
4. Ensure Docker daemon is running
5. Check port availability (8080 for app, 5432 for postgres, 6379 for redis)

**Proceed to Phase 3** once all checks pass.
