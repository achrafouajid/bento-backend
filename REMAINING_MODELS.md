# Remaining Entity Models to Implement

## Phase 4: Operations & Finance

### PurchaseOrder.java
```
- id, organization_id (BaseTenantEntity)
- deal_id FK
- vendor_partner_id FK
- status ENUM(DRAFT, SENT, CONFIRMED, DELIVERED, INVOICED)
- delivery_date
- sent_via
```

### Invoice.java
```
- id, organization_id
- type ENUM(CUSTOMER, VENDOR)
- partner_id FK
- deal_id FK
- purchase_order_id FK
- status ENUM(DRAFT, SENT, PARTIALLY_PAID, PAID, OVERDUE)
- due_date, sent_at, paid_at
- customer_account, customer_name, delivery_address
- vat_number, subtotal, tax, total
```

### CreditNote.java
```
- id, organization_id
- invoice_id FK
- partner_id FK
- reason
- amount
- issued_at
```

### RecoveryReminder.java
```
- id, organization_id
- invoice_id FK
- partner_id FK
- channel ENUM(EMAIL, SMS, WHATSAPP)
- template_id FK
- status ENUM(SCHEDULED, SENT, FAILED)
- sent_at
```

---

## Phase 5: Support & Marketing

### Ticket.java
```
- id, organization_id
- title, description
- partner_id FK
- assigned_to_user_id FK
- status ENUM(OPEN, IN_PROGRESS, RESOLVED, CLOSED)
- priority ENUM(LOW, MEDIUM, HIGH, URGENT)
- ticket_type_id FK
- deadline, resolution
```

### TicketComment.java
```
- id, organization_id
- ticket_id FK
- author_user_id FK
- content TEXT
```

### TicketType.java
```
- id, organization_id
- name
```

### Campaign.java
```
- id, organization_id
- title
- channel ENUM(WHATSAPP, SMS, EMAIL)
- status ENUM(DRAFT, SCHEDULED, SENDING, ACTIVE, COMPLETED)
- template_id FK
- target_tag_id FK (nullable)
- target_filter JSONB (complex criteria)
- scheduled_at
- sent_count
- metrics JSONB (sent, delivered, failed, opened, clicked)
```

---

## Phase 6: Automation & Notifications

### AutomationRule.java
```
- id, organization_id
- name, description
- is_active
- trigger ENUM(PARTNER_CREATED, PARTNER_UPDATED, DEAL_CREATED, DEAL_UPDATED, TICKET_CREATED, TICKET_UPDATED)
- condition_groups JSONB (OR-of-ANDs)
- actions JSONB
- priority
- stop_on_match
- version (integer)
```

### AutomationRuleVersion.java
```
- id, organization_id
- rule_id FK
- version_number
- snapshot JSONB (full rule at this version)
- created_at
```

### AutomationExecutionLog.java
```
- id, organization_id
- rule_id FK
- rule_version
- trigger
- entity_type
- entity_id
- dry_run
- conditions_trace JSONB
- actions_executed JSONB
- status ENUM(SUCCESS, PARTIAL, FAILED)
- executed_at
```

### DomainEvent.java (outbox pattern for n8n)
```
- id, organization_id
- event_type
- entity_type
- entity_id
- payload JSONB
- created_at
- published_at (nullable, marks when dispatcher sent to webhooks)
```

### WebhookSubscription.java
```
- id, organization_id
- url
- secret (for HMAC signing)
- event_types TEXT[]
- is_active
```

### Notification.java
```
- id, organization_id
- recipient_user_id FK
- type ENUM(DEAL, TASK, TICKET, SYSTEM, MENTION)
- title, message
- related_entity_type
- related_entity_id
- is_read
- created_at
```

---

## Flyway Migrations Needed

### V5__phase4_finance.sql
- PurchaseOrder, PurchaseOrderLine tables
- Invoice, InvoiceLine tables
- CreditNote, RecoveryReminder tables
- Indexes: (organization_id, status), (organization_id, partner_id), (due_date)

### V6__phase5_support_marketing.sql
- Ticket, TicketComment, TicketType tables
- Campaign table
- Indexes: (organization_id, status), (organization_id, assigned_to), (organization_id, partner_id)

### V7__phase6_automation.sql
- AutomationRule, AutomationRuleVersion tables
- AutomationExecutionLog table
- DomainEvent table (critical: no FK, designed for polling)
- WebhookSubscription table
- Notification table
- Indexes: (organization_id, is_active), (event_type, published_at), (published_at IS NULL)

---

## Services & Controllers Pattern

Each entity needs (follow existing models):

### Service Pattern
```java
@Service
@RequiredArgsConstructor
public class EntityService {
    private final EntityRepository repo;
    
    // CRUD with TenantContext.getCurrentOrganizationId()
    // Business logic + permission checks (at least one ADMIN per org, etc)
    // Transactional methods for mutations
}
```

### Controller Pattern
```java
@RestController @RequestMapping("/entities")
public class EntityController {
    private final EntityService service;
    
    @PostMapping @PreAuthorize("hasAuthority('ENTITY_CREATE')")
    @PostMapping("/{id}") @PreAuthorize("hasAuthority('ENTITY_WRITE')")
    @GetMapping @PreAuthorize("hasAuthority('ENTITY_READ')")
    // Standard REST + pagination
}
```

### DTO Pattern
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class CreateEntityRequest {
    @JsonProperty("snake_case_fields")
    @NotBlank private String field;
}

@Data @Builder
public class EntityResponseDto {
    private UUID id;
    @JsonProperty("snake_case") private String field;
}
```

---

## Quick Checklist for Each Phase

- [ ] Create entities (extend BaseTenantEntity)
- [ ] Create enums for status/type fields
- [ ] Create Flyway migration (V#__phase#_description.sql)
- [ ] Create repositories (org-scoped @Query)
- [ ] Create DTOs (Request + Response)
- [ ] Create mappers (MapStruct interfaces)
- [ ] Create services (business logic, @Transactional)
- [ ] Create controllers (@PreAuthorize per operation)
- [ ] Write integration tests (Testcontainers)
- [ ] Update PHASE_PROGRESS.md

---

## Critical Reminders

1. **Never forget organization_id** - Every tenant-scoped table must have it as leading index column
2. **Service-layer validation** - State transitions, permission checks (not just DB constraints)
3. **Pagination** - All list endpoints must use Spring Data Pageable (max 100 items)
4. **RFC 7807 errors** - GlobalExceptionHandler already handles it
5. **Audit fields** - Inherited from BaseTenantEntity via @CreatedDate, @CreatedBy, etc.
6. **Query performance** - Use @EntityGraph for detail views, projections for lists
7. **Type safety** - Use enums, not strings, for status/type columns

---

**Remaining effort**: ~12-15 hours to complete all 8 phases following established patterns.
