# Angular Frontend - API Integration Layer

**Location**: Create in your Angular project at `src/app/core/`

This document provides production-grade Angular services for consuming the Bento CRM Backend API.

## Project Structure

```
src/app/
├── core/                          # Core module (singleton services)
│   ├── models/                    # TypeScript DTOs (matching backend)
│   │   ├── auth.model.ts
│   │   ├── organization.model.ts
│   │   ├── user.model.ts
│   │   ├── partner.model.ts
│   │   ├── invoice.model.ts
│   │   ├── deal.model.ts
│   │   └── common.model.ts
│   ├── services/
│   │   ├── api/
│   │   │   ├── base-api.service.ts       # Abstract base for all API services
│   │   │   ├── auth-api.service.ts
│   │   │   ├── organization-api.service.ts
│   │   │   ├── user-api.service.ts
│   │   │   ├── partner-api.service.ts
│   │   │   ├── invoice-api.service.ts
│   │   │   └── deal-api.service.ts
│   │   ├── auth/
│   │   │   ├── auth.service.ts           # High-level auth (login/logout/token)
│   │   │   └── token.service.ts          # Token storage/retrieval
│   │   └── error/
│   │       └── error-handler.service.ts  # Centralized error handling
│   ├── interceptors/
│   │   ├── jwt.interceptor.ts            # Add JWT to requests
│   │   ├── error.interceptor.ts          # Handle HTTP errors
│   │   └── loading.interceptor.ts        # Track loading state
│   ├── guards/
│   │   ├── auth.guard.ts                 # Protect routes requiring auth
│   │   └── role.guard.ts                 # Protect routes by role
│   ├── config/
│   │   └── api-config.ts                 # API endpoints configuration
│   └── core.module.ts
├── shared/
│   └── components/
│       └── error-dialog/                 # Reusable error display
└── environments/
    ├── environment.ts
    └── environment.prod.ts
```

## Implementation Files

All files below follow SOLID principles:
- **S**ingle Responsibility: Each service has one reason to change
- **O**pen/Closed: Services are open for extension via inheritance
- **L**iskov Substitution: Services can be replaced with compatible implementations
- **I**nterface Segregation: Small, focused interfaces
- **D**ependency Inversion: Depend on abstractions, not concrete classes

