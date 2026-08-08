# Angular Frontend - Production-Grade Implementation Guide

## Overview

This guide provides a complete, production-ready Angular implementation for consuming the Bento CRM Backend API. All code follows SOLID principles and best practices used by senior software engineers.

## SOLID Principles Applied

### S - Single Responsibility Principle
Each service has ONE reason to change:
- `TokenService`: Only manages token storage/retrieval
- `AuthService`: Only coordinates auth flow
- `ErrorHandlerService`: Only handles error formatting
- `BaseApiService`: Only manages HTTP communication
- Components: Only handle presentation logic

### O - Open/Closed Principle
Services are OPEN for extension, CLOSED for modification:
- `BaseApiService`: Abstract class extended by specific API services
- `AuthGuard`, `RoleGuard`, `PermissionGuard`: Implement `CanActivate` interface
- Services use dependency injection, not hard-coded dependencies

### L - Liskov Substitution Principle
Implementations can be swapped without breaking:
- All API services extend `BaseApiService`
- All guards implement Angular's `CanActivate` interface
- Facades abstract service implementation details

### I - Interface Segregation Principle
Use small, focused interfaces:
- `LoginRequest`, `LoginResponse` are separate
- `CreatePartnerRequest` is different from `UpdatePartnerRequest`
- Each service has a minimal public API

### D - Dependency Inversion Principle
Depend on abstractions, not concrete classes:
- Services injected via constructor, not `new()` keyword
- Facades coordinate services without components knowing implementation
- HttpClient injected into services, not hardcoded

## Project Structure

```
src/app/
├── core/                              # Singleton services (import once in AppModule)
│   ├── config/
│   │   ├── api-config.ts             # Endpoint definitions
│   │   ├── http-config.ts            # HTTP settings
│   │   └── error-messages.ts         # Centralized error text
│   ├── models/
│   │   ├── auth.model.ts             # Auth DTOs
│   │   ├── common.model.ts           # Shared interfaces
│   │   ├── partner.model.ts          # Partner DTOs
│   │   └── [other models]
│   ├── services/
│   │   ├── api/
│   │   │   ├── base-api.service.ts   # Abstract base (retry logic, error handling)
│   │   │   ├── auth-api.service.ts
│   │   │   ├── partner-api.service.ts
│   │   │   └── [other API services]
│   │   ├── auth/
│   │   │   ├── auth.service.ts       # High-level auth coordination
│   │   │   └── token.service.ts      # Token persistence
│   │   ├── error/
│   │   │   └── error-handler.service.ts  # Centralized error handling
│   │   ├── loading/
│   │   │   └── loading.service.ts    # Loading state management
│   │   └── facade/
│   │       ├── partner-facade.service.ts  # Simplifies partner operations
│   │       └── [other facades]
│   ├── interceptors/
│   │   ├── jwt.interceptor.ts        # Add JWT to requests
│   │   ├── error.interceptor.ts      # Handle HTTP errors globally
│   │   └── loading.interceptor.ts    # Track loading state
│   ├── guards/
│   │   ├── auth.guard.ts             # Require authentication
│   │   ├── role.guard.ts             # Require specific role
│   │   └── permission.guard.ts       # Require specific permission
│   └── core.module.ts                # Imports all core services/guards
├── features/                          # Feature modules (lazy-loaded)
│   ├── auth/
│   │   ├── login/
│   │   ├── signup/
│   │   └── auth.module.ts
│   ├── dashboard/
│   ├── partners/
│   │   ├── partners-list/
│   │   ├── partner-detail/
│   │   ├── partner-form/
│   │   └── partners.module.ts
│   ├── deals/
│   ├── invoices/
│   ├── admin/
│   └── error/
├── shared/                            # Shared UI components (not singletons)
│   ├── components/
│   │   ├── loading-spinner/
│   │   ├── error-dialog/
│   │   ├── navigation/
│   │   └── footer/
│   └── shared.module.ts
├── app-routing.module.ts             # Route definitions with guards
├── app.module.ts                     # Root module (imports CoreModule once)
└── app.component.ts                  # Root component
```

## Key Patterns

### 1. Service Hierarchy

```
BaseApiService (abstract)
    ↓
┌─────────────────────────────────┐
├─ AuthApiService
├─ PartnerApiService
├─ InvoiceApiService
└─ [Other API services]

Facade Services (coordinate multiple services)
    ↓
┌─────────────────────────────────┐
├─ PartnerFacadeService
├─ InvoiceFacadeService
└─ [Other facades]
```

### 2. Dependency Injection Chain

```
Component (needs to do something)
    ↓ injects
PartnerFacadeService (coordinates operations)
    ↓ injects
PartnerApiService (talks to HTTP)
ErrorHandlerService (formats errors)
LoadingService (tracks loading state)
    ↓ injects
HttpClient (makes requests)
```

### 3. Request Lifecycle

```
1. Component calls facade.getPartners()
   ↓
2. Facade calls loadingService.show()
   ↓
3. Facade calls errorHandler.clearError()
   ↓
4. Facade calls apiService.listPartners()
   ↓
5. ApiService calls http.get()
   ↓
6. JwtInterceptor adds authorization header
   ↓
7. LoadingInterceptor increments request counter
   ↓
8. Request sent to backend
   ↓
9. Response received (or error)
   ↓
10. ErrorInterceptor handles errors (if any)
   ↓
11. LoadingInterceptor decrements counter, hides spinner
   ↓
12. Component receives data via Observable
   ↓
13. Component updates UI
```

## Implementation Checklist

### Step 1: Set Up Core Module
- [ ] Create `core/` directory structure
- [ ] Create `TokenService` for token management
- [ ] Create `AuthService` for authentication flow
- [ ] Create `ErrorHandlerService` for error handling
- [ ] Create `LoadingService` for loading states
- [ ] Create `core.module.ts` with all providers
- [ ] Import `CoreModule` in `AppModule` (only once!)

### Step 2: Create API Services
- [ ] Create `api-config.ts` with all endpoint definitions
- [ ] Create `BaseApiService` with HTTP logic
- [ ] Create `AuthApiService` extending `BaseApiService`
- [ ] Create service for each entity (Partner, Invoice, etc.)
- [ ] Implement pagination support
- [ ] Implement retry logic with exponential backoff

### Step 3: Set Up Interceptors
- [ ] Create `JwtInterceptor` to add authorization header
- [ ] Create `ErrorInterceptor` for global error handling
- [ ] Create `LoadingInterceptor` for request tracking
- [ ] Register interceptors in `CoreModule`
- [ ] Test interceptor order (JWT → Error → Loading)

### Step 4: Implement Guards
- [ ] Create `AuthGuard` for authentication
- [ ] Create `RoleGuard` for role-based access
- [ ] Create `PermissionGuard` for permission-based access
- [ ] Test guards on protected routes

### Step 5: Create Models & DTOs
- [ ] Define all request/response interfaces
- [ ] Create `ApiResponse<T>` for paginated responses
- [ ] Create request/response DTOs for each endpoint
- [ ] Keep models in sync with backend DTOs

### Step 6: Create Feature Modules
- [ ] Create lazy-loaded feature modules
- [ ] Use guards on route configurations
- [ ] Import `SharedModule` for common components
- [ ] Create facade services for each feature

### Step 7: Create Components
- [ ] Use reactive forms with validation
- [ ] Inject facades, not API services directly
- [ ] Subscribe with `takeUntil` pattern for cleanup
- [ ] Handle loading states via observables
- [ ] Display errors from `ErrorHandlerService`

### Step 8: Test
- [ ] Unit tests for services
- [ ] Integration tests for components
- [ ] E2E tests for user flows
- [ ] Verify JWT refresh works
- [ ] Verify error handling works
- [ ] Verify rate limiting errors

## Usage Examples

### Login Flow
```typescript
// Component
this.authService.login(credentials).subscribe(
  () => this.router.navigate(['/dashboard']),
  error => console.error('Login failed', error)
);

// AuthService automatically:
// 1. Saves tokens to storage
// 2. Saves user to storage
// 3. Updates currentUser$ observable
// 4. Sets up auto token refresh
```

### Get Paginated Data
```typescript
// Component
this.partnerFacade.getPartners(page, size).subscribe(
  response => {
    this.partners = response.content;
    this.totalElements = response.total_elements;
  }
);

// Facade automatically:
// 1. Shows loading spinner
// 2. Clears previous errors
// 3. Calls API service
// 4. Handles errors if any
// 5. Hides loading spinner
```

### Create Resource
```typescript
// Component
this.partnerFacade.createPartner(formData).subscribe(
  () => this.router.navigate(['/partners']),
  error => {} // ErrorHandlerService shows error globally
);

// Facade:
// 1. Shows loading spinner
// 2. Validates data (in component)
// 3. Calls API service
// 4. Shows success message
// 5. Handles errors globally
```

### Protected Routes
```typescript
// Routing module
const routes: Routes = [
  {
    path: 'partners',
    canActivate: [AuthGuard, PermissionGuard],
    data: { permissions: ['PARTNERS_READ'] },
    loadChildren: () => import('./partners/partners.module').then(m => m.PartnersModule)
  }
];

// Route is only accessible if:
// 1. User is authenticated (AuthGuard)
// 2. User has PARTNERS_READ permission (PermissionGuard)
```

## Best Practices

### 1. Always Use Facades in Components
❌ Bad:
```typescript
this.partnerApiService.listPartners().subscribe(...);
```

✅ Good:
```typescript
this.partnerFacade.getPartners().subscribe(...);
```

### 2. Use takeUntil Pattern for Subscriptions
❌ Bad:
```typescript
this.service.getData().subscribe(data => {
  this.data = data;
}); // Memory leak!
```

✅ Good:
```typescript
private destroy$ = new Subject<void>();

this.service.getData()
  .pipe(takeUntil(this.destroy$))
  .subscribe(data => this.data = data);

ngOnDestroy() {
  this.destroy$.next();
  this.destroy$.complete();
}
```

### 3. Never Hardcode API URLs
❌ Bad:
```typescript
this.http.get('/api/v1/partners')
```

✅ Good:
```typescript
this.http.get(API_CONFIG.endpoints.partners.list)
```

### 4. Validate Forms Before Submission
✅ Good:
```typescript
onSubmit() {
  if (this.form.invalid) {
    this.markFormGroupTouched(this.form);
    return;
  }
  // Submit...
}
```

### 5. Handle All Observable Subscriptions
✅ Use `| async` pipe in templates:
```html
<div *ngIf="(isLoading$ | async)">Loading...</div>
```

Or manually unsubscribe:
```typescript
ngOnDestroy() {
  this.destroy$.next();
  this.destroy$.complete();
}
```

## Error Handling

Errors are handled at multiple levels:

1. **Global Error Interceptor**: Catches HTTP errors, updates `ErrorHandlerService`
2. **Service Error Handling**: Services use `catchError` to handle specific errors
3. **Component Error Handling**: Components can subscribe to `errorMessage$` observable
4. **Error Dialog**: Global error dialog displays errors from `ErrorHandlerService`

Example:
```typescript
// Backend returns 400 Bad Request
↓
ErrorInterceptor catches it
↓
ErrorHandlerService.handle() is called
↓
errorMessage$ observable emits error text
↓
Error dialog displays message to user
```

## Testing Strategy

### Unit Tests (Services)
```typescript
describe('AuthService', () => {
  it('should save tokens on login', () => {
    // Test without making real HTTP calls
  });
});
```

### Integration Tests (Components + Services)
```typescript
describe('PartnersListComponent', () => {
  it('should load partners on init', () => {
    // Test component + facade + service integration
  });
});
```

### E2E Tests (Full User Flow)
```typescript
describe('Partner workflow', () => {
  it('should create, read, update, delete partner', () => {
    // Test complete user flow
  });
});
```

## Performance Optimization

1. **Lazy Loading**: Feature modules are lazy-loaded
2. **Change Detection OnPush**: Use `ChangeDetectionStrategy.OnPush` on components
3. **Unsubscribe Pattern**: Always unsubscribe to prevent memory leaks
4. **Retry with Backoff**: Automatic retry prevents overloading backend
5. **JWT Caching**: Token stored in memory and localStorage
6. **Request Deduplication**: Can be added to interceptor if needed

## Environment Configuration

```typescript
// environment.ts (development)
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api/v1',
};

// environment.prod.ts (production)
export const environment = {
  production: true,
  apiBaseUrl: '/api/v1', // Relative URL to avoid CORS issues
};
```

Update `api-config.ts`:
```typescript
import { environment } from '../../../environments/environment';

export const API_CONFIG = {
  baseUrl: environment.apiBaseUrl,
  endpoints: { ... }
};
```

## Security Considerations

✅ Token stored in localStorage (with HttpOnly flag via Set-Cookie if possible)  
✅ JWT interceptor adds token to every request  
✅ Error interceptor handles 401 (unauthorized) responses  
✅ Guard prevents unauthorized access to routes  
✅ CORS configured on backend  
✅ HTTPS enforced in production  
✅ Token refresh happens automatically before expiry  
✅ No sensitive data in localStorage except tokens  

## Common Issues & Solutions

### Issue: Memory Leaks from Subscriptions
**Solution**: Use `takeUntil` pattern with `ngOnDestroy`

### Issue: Multiple Token Refreshes
**Solution**: `AuthService` manages one refresh timer, prevents race conditions

### Issue: 401 Errors After Token Refresh
**Solution**: `ErrorInterceptor` catches 401, calls `AuthService.logout()`

### Issue: Interceptors Not Applied
**Solution**: Check `CoreModule` is imported in `AppModule`, not in features

### Issue: Route Guards Not Working
**Solution**: Ensure guards are provided in `CoreModule`, routes use `canActivate`

## Deployment Checklist

- [ ] Environment variables configured for production
- [ ] API base URL points to production backend
- [ ] JWT secret matches backend
- [ ] CORS origin whitelist includes frontend URL
- [ ] Build optimization enabled (`ng build --prod`)
- [ ] Service worker enabled for offline capability
- [ ] Error logging configured (Sentry, etc.)
- [ ] Performance monitoring enabled
- [ ] Security headers configured
- [ ] Tested on multiple browsers

## Conclusion

This architecture provides:
✅ Production-grade scalability  
✅ SOLID principles compliance  
✅ Easy to test and maintain  
✅ Clear separation of concerns  
✅ Reusable services and facades  
✅ Centralized error handling  
✅ Type-safe API integration  
✅ Enterprise-ready security  

Follow these patterns, and your Angular app will be professional, maintainable, and ready for production.
