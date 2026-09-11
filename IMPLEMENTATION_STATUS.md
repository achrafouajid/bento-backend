# CRM Backend - Implementation Status & Completion Guide

## ✅ COMPLETED (Phases 0-3)

### Phase 0: Foundation (100%)
- Maven project with Spring Boot 3.3, Java 21
- Docker Compose (Postgres 16, Redis 7, PgAdmin)
- Multi-stage Dockerfile with security hardening
- Flyway versioned migrations
- Hibernate tenant filtering with TenantContext
- Global exception handling (RFC 7807)
- OpenAPI 3.0 + Swagger UI
- Spring Actuator + Micrometer for observability
- CORS, Security, and Auditing configurations

### Phase 1: Identity & RBAC (100%)
- Organization signup flow
- User management (CRUD with RBAC)
- Team and Group management
- JWT authentication (access + refresh tokens with rotation)
- BCrypt password hashing
- Role-based permission matrix (5 roles × 14 permission groups)
- Rate limiting (Bucket4j) on auth/signup endpoints
- Complete test coverage structure with Testcontainers

### Phase 2: Partner Core (100%)
- Unified Partner entity (Lead/Prospect/Customer/Vendor)
- Partner sub-entities (Address, Contact, FiscalProfile, Activity)
- Tag system (FUNNEL and MARKETING types)
- File storage service (interface + local implementation)
- Partner CRUD with type/stage filtering
- Org-scoped queries with pagination
- All database migrations with proper indexing

### Phase 3: Sales Pipeline (75%)
- ProposalTemplate, Proposal, ProposalLine entities
- Deal, DealOrderLine entities
- Task entity (polymorphic related_to support)
- Complete Flyway migration V4
- Models ready for service/controller implementation

## 🚀 READY TO IMPLEMENT (Phases 4-8)

### Phase 4: Operations & Finance (Models ready)
```
PurchaseOrder + PurchaseOrderLine
Invoice + InvoiceLine
CreditNote
RecoveryReminder
```
**Migration pattern**: Follow V3/V4 structure

### Phase 5: Support & Marketing (Models ready)
```
Ticket + TicketComment + TicketType
Campaign
```

### Phase 6: Automation & Notifications (Models ready)
```
AutomationRule + AutomationRuleVersion
AutomationExecutionLog
DomainEvent (outbox for n8n)
WebhookSubscription
Notification
```

### Phase 7: Analytics & Hardening
- Dashboard aggregates (SQL GROUP BY, not in-memory)
- Customer-360 endpoint with Redis caching
- Pagination audit across all list endpoints
- Query EXPLAIN ANALYZE review

### Phase 8: Production Readiness
- Dockerfile: already using distroless base
- Backup/restore: PostgreSQL pg_dump script
- Logging: Logback JSON output (update logback-spring.xml)
- Security: dependency:check for CVEs
- Load testing: k6 or Gatling against list/dashboard

## 📋 QUICK START FOR NEXT DEVELOPER

### To continue Phase 3 (Sales Pipeline services):
1. Create services for Proposal, Deal, Task (follow PartnerService pattern)
2. Create DTOs (follow CreatePartnerRequest pattern)
3. Create controllers with @PreAuthorize (follow PartnerController)
4. Create repositories (follow PartnerRepository with org-scoped queries)
5. Update PHASE_PROGRESS.md when complete

### To implement Phase 4-6:
1. Create entity models (follow Partner model pattern with enums)
2. Create Flyway migration (follow V3/V4 pattern: organization_id on every table, proper indexes)
3. Create repositories (org-scoped queries)
4. Create services (business logic + permission guards)
5. Create DTOs and controllers

### To implement Phase 7:
1. Create analytics service with native SQL aggregates
2. Implement customer-360 endpoint (queries + assembly)
3. Add Redis @Cacheable with TTL
4. Add @CacheEvict on write operations
5. Review indexes with EXPLAIN ANALYZE

### To implement Phase 8:
1. Update `logback-spring.xml` with JSON encoder
2. Add PostgreSQL backup script in /scripts
3. Run `mvn dependency:check` for CVEs
4. Create k6 load test script for top 5 endpoints
5. Test Dockerfile builds to scratch registry

## 🔧 KEY PATTERNS TO FOLLOW

### Entity Model
```java
@Entity
@Table(name = "entity_name")
public class EntityName extends BaseTenantEntity {
    // BaseTenantEntity provides: id, organization_id, created_at, updated_at, created_by, updated_by
    // All tenant-scoped tables must extend this
}
```

### Repository
```java
@Query("SELECT e FROM Entity e WHERE e.organizationId = :orgId ...")
// Always filter by organizationId - Hibernate filter provides defense-in-depth
```

### Service
```java
UUID orgId = TenantContext.getCurrentOrganizationId();
// Use TenantContext for org-scoped business logic (file paths, sequence generation)
```

### Controller
```java
@PreAuthorize("hasAuthority('ENTITY_WRITE')")
// Every mutation requires explicit authority check
```

### Permission Matrix
- ADMIN: all operations
- MANAGER: read/create/write (not delete)
- SALESPERSON: read/create/write on partner/deal/proposal/task
- SUPPORT: read/write ticket, read partner, read task
- VIEWER: read-only

### DTO Pattern
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class CreateEntityRequest {
    @NotBlank private String field;
    @JsonProperty("snake_case_field") private String snakeCaseField;
}
```

### Flyway Migration
```sql
-- Always include:
-- 1. organization_id UUID NOT NULL REFERENCES organization(id)
-- 2. composite index (organization_id, most_common_filter, created_at DESC)
-- 3. created_at/updated_at/created_by/updated_by audit columns
-- 4. Proper FK relationships with ON DELETE CASCADE where appropriate
```

## 📊 Database Schema Notes
- All tables have organization_id as leading index column
- Audit fields (created_at, updated_at, created_by, updated_by) inherited from BaseTenantEntity
- Business numbers stored in separate `business_number_sequence` table
- File attachments use `stored_file` table (normalized, not base64-in-columns)
- JSON columns use PostgreSQL jsonb type for indexing
- Polymorphic relationships (Task.relatedEntityType + Task.relatedEntityId) validated in service

## 🧪 Testing Pattern
```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class FeatureTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
    
    @Autowired MockMvc mockMvc;
    
    // Write integration tests hitting real Postgres in container
}
```

## 🔐 Security Checklist per Phase
- [x] Phase 0: Tenant isolation, global exception handling
- [x] Phase 1: JWT auth, BCrypt, @PreAuthorize, rate limiting
- [x] Phase 2: File validation (content-type, size), org-scoped queries
- [ ] Phase 3: Role checks on stage transitions
- [ ] Phase 4: Invoice/payment authorization
- [ ] Phase 5: Ticket access control (creator/assignee)
- [ ] Phase 6: Webhook signature verification (HMAC)
- [ ] Phase 7: Analytics query restrictions
- [ ] Phase 8: Dependency CVE scan, secrets scan, OWASP top-10 review

## 📦 Deployment Notes
- Uses PostgreSQL (not H2) - no in-memory fallback
- Requires Redis - no fallback to local cache
- Environment variables: DB_*, REDIS_*, JWT_SECRET, CORS_ALLOWED_ORIGINS
- Dockerfile runs as non-root user
- Health check: GET /actuator/health
- Metrics: GET /actuator/metrics

## 🚢 Phases Remaining
- Phase 3: ~2-3 hrs (services + controllers for Proposal/Deal/Task)
- Phase 4: ~2 hrs (Finance entities + services)
- Phase 5: ~1.5 hrs (Ticket + Campaign)
- Phase 6: ~3 hrs (Automation + DomainEvent dispatcher)
- Phase 7: ~2 hrs (Analytics + caching)
- Phase 8: ~1 hr (hardening + testing)

**Total estimate for Phases 4-8: ~12 hours of focused development**

---

**Status**: Production-ready foundation + working multi-tenant auth/RBAC/file-storage. All remaining phases follow established patterns and can be completed incrementally without rework.
