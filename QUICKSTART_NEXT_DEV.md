# Quick Start for Next Developer

**Welcome!** This is a production-ready multi-tenant CRM backend built in 3 phases. Here's how to get oriented fast.

## First 10 Minutes

1. **Read this file** (you're reading it!)
2. **Read PROJECT_SUMMARY.md** (5 min - full context)
3. **Read IMPLEMENTATION_STATUS.md** (5 min - how to implement next)
4. **Run verification checklist** (20 min - confirm everything works)

## Architecture in 30 Seconds

- **Frontend**: Angular 21, "varyocrm" (not your concern)
- **Backend**: Java 21 + Spring Boot 3.3
- **Database**: PostgreSQL 16, Postgres Docker container runs migrations via Flyway
- **Caching/Rate-limit**: Redis 7
- **Security**: JWT tokens (15min access, 30day refresh) + BCrypt passwords + Spring Security
- **Multi-tenant**: Every table has `organization_id`, enforced at ORM level via Hibernate @Filter

## What's Already Working

✅ User signup (create organization + admin user)  
✅ User authentication (login/refresh/logout with token rotation)  
✅ User management (CRUD with role-based permissions)  
✅ Partner management (Lead/Prospect/Customer/Vendor with addresses, contacts, attachments)  
✅ File storage (local filesystem, swappable to S3)  
✅ Rate limiting (auth endpoints strictly limited)  
✅ API documentation (OpenAPI/Swagger)  
✅ Docker deployment (multi-stage, security hardened)  

## What You Need to Implement Next

**Phase 3 (Sales Pipeline)** - 2-3 hours:
- [ ] ProposalService + ProposalController
- [ ] DealService + DealController
- [ ] TaskService + TaskController
- [ ] Create DTOs and repositories

**Then Phases 4-6** follow the same pattern.

## Directory Guide (what's important)

```
crm-backend/
├── src/main/java/com/bento/crm/
│   ├── common/              # Security, configs, exceptions - READ don't modify
│   ├── auth/                # Login/JWT - READ don't modify
│   ├── organization/        # Org signup - READ don't modify
│   ├── identity/            # Users/teams/groups - READ don't modify
│   ├── partner/             # Partners - READ this to understand pattern
│   ├── proposal/            # ← Implement services + controller
│   ├── deal/                # ← Implement services + controller
│   ├── task/                # ← Implement services + controller
│   └── [file, audit, etc.]  # Implement as needed
├── src/main/resources/
│   └── db/migration/
│       ├── V1, V2, V3, V4   # ← Flyway creates schema
│       └── V5 (yours)       # ← You'll create this for Phase 4
└── docker-compose.yml       # One command: docker-compose up -d
```

## Copy-Paste Workflow (Implementing Phase 3 as Example)

### 1. Create Entity
```java
// File: src/main/java/com/bento/crm/proposal/model/ProposalService.java
// Copy from: src/main/java/com/bento/crm/partner/model/Partner.java
// Change: class name + field names
```

### 2. Create Repository
```java
// File: src/main/java/com/bento/crm/proposal/repository/ProposalRepository.java
// Copy from: src/main/java/com/bento/crm/partner/repository/PartnerRepository.java
// Change: replace "Partner" with "Proposal"
```

### 3. Create DTO
```java
// File: src/main/java/com/bento/crm/proposal/dto/CreateProposalRequest.java
// Copy from: src/main/java/com/bento/crm/partner/dto/CreatePartnerRequest.java
// Change: add proposal-specific fields
```

### 4. Create Service
```java
// File: src/main/java/com/bento/crm/proposal/service/ProposalService.java
// Copy from: src/main/java/com/bento/crm/partner/service/PartnerService.java
// Change: method names + business logic
```

### 5. Create Controller
```java
// File: src/main/java/com/bento/crm/proposal/controller/ProposalController.java
// Copy from: src/main/java/com/bento/crm/partner/controller/PartnerController.java
// Change: endpoints + request/response types
```

## How to Test Locally

```bash
# Start everything
docker-compose up -d

# Verify it's running
curl http://localhost:8080/actuator/health

# See API docs
open http://localhost:8080/swagger-ui.html

# Test: create org
curl -X POST http://localhost:8080/organizations \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Test","admin_email":"admin@test.com",
    "admin_name":"Admin","admin_password":"TestPass123!"
  }'

# Test: login (from response above, find org_id)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"TestPass123!"}'

# Save ACCESS_TOKEN from response

# Test: protected endpoint
curl -X GET "http://localhost:8080/partners?page=0&size=20" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

## Key Patterns (copy these exactly)

### Every Service Needs
```java
UUID orgId = TenantContext.getCurrentOrganizationId();
// Use this in services to namespace operations (file paths, business logic)
```

### Every Controller Endpoint Needs
```java
@PreAuthorize("hasAuthority('ENTITY_READ')")
// Permissions: ENTITY_READ, ENTITY_CREATE, ENTITY_WRITE, ENTITY_DELETE
```

### Every Repository Query Needs
```java
@Query("SELECT e FROM Entity e WHERE e.organizationId = :orgId AND ...")
// Always filter by organization_id - no exceptions
```

### Every Entity Needs to Extend
```java
public class MyEntity extends BaseTenantEntity {
    // This gives you: id, organization_id, created_at, updated_at, created_by, updated_by
}
```

## Common Mistakes (Don't Do These)

❌ Hardcoding secrets (use .env)  
❌ Forgetting @PreAuthorize on mutations  
❌ Querying without organization_id filter  
❌ Committing .env file (it's .gitignore'd for good reason)  
❌ Modifying existing auth/security code (you'll break multi-tenancy)  
❌ Creating entities that don't extend BaseTenantEntity  
❌ Putting business logic in controllers (it belongs in services)  

## Command Reference

```bash
# See what's running
docker-compose ps

# View logs
docker-compose logs app

# Stop everything
docker-compose down

# Clean restart
docker-compose down && docker-compose up -d

# Run tests
mvn test

# Build jar
mvn clean package

# Rebuild Docker image
docker build -t crm-backend:latest .
```

## When You Get Stuck

1. **"404 endpoint not found"** → Check @RequestMapping on controller class + @PostMapping/@GetMapping on method
2. **"403 Forbidden"** → Check @PreAuthorize + your JWT has correct role
3. **"No entity with id X found"** → Check you're querying org-scoped: `findByOrganizationIdAndId(orgId, id)`
4. **"Hibernate filter not initialized"** → Check TenantContext is set in interceptor before repository call
5. **"Port 8080 already in use"** → `docker-compose down` or kill whatever else is using 8080

## Documentation Map

| Document | Read When |
|----------|-----------|
| PROJECT_SUMMARY.md | Getting oriented (30 min) |
| IMPLEMENTATION_STATUS.md | Ready to code (patterns guide) |
| REMAINING_MODELS.md | Implementing Phase 4-6 (entity reference) |
| VERIFICATION_CHECKLIST.md | Testing your changes |
| PHASE_PROGRESS.md | Tracking what's done |

## Success Criteria for Phase 3

- [ ] ProposalService passes integration tests
- [ ] DealService passes integration tests
- [ ] TaskService passes integration tests
- [ ] All endpoints have @PreAuthorize
- [ ] All queries org-scoped
- [ ] Swagger docs show new endpoints
- [ ] docker-compose down && docker-compose up -d still works
- [ ] New entities in fresh org can CRUD without errors

## Before Committing

```bash
# 1. Make sure tests pass
mvn test

# 2. Check no secrets in code
grep -r "JWT_SECRET\|password\|secret" src/main/java | grep -v "@Value\|env\|getSecret\|Properties"

# 3. Verify .env not committed
git status .env  # should say "deleted: .env"

# 4. Update PHASE_PROGRESS.md (mark Phase 3 complete)

# 5. Commit with clear message
git add -A
git commit -m "Phase 3: Implement Proposal, Deal, Task services + controllers"
```

## Phone Numbers (Internal Reference Only)

- **Frontend repo**: https://github.com/your-org/crm-frontend (Angular)
- **API Docs**: http://localhost:8080/swagger-ui.html (auto-generated)
- **Database GUI**: http://localhost:5050 (PgAdmin)
- **Metrics**: http://localhost:8080/actuator/prometheus (Prometheus format)

## One More Thing

If you're reading this and thinking "why so much documentation?", you're right to notice. This was built with the assumption that **you'll maintain/extend it without the original author**. Every pattern is documented, every decision explained, every next step planned.

The codebase isn't "clever" - it's boring. Boring is good. Boring scales. Boring is maintainable.

**Now go build Phase 3.** You've got everything you need.

---

**Questions?** Check IMPLEMENTATION_STATUS.md section "Key Patterns to Follow"  
**Stuck?** Check "When You Get Stuck" section above  
**Done with Phase 3?** Update PHASE_PROGRESS.md and move to Phase 4 using same patterns
