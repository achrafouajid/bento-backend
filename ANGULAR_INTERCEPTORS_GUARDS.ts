// src/app/core/interceptors/jwt.interceptor.ts
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { TokenService } from '../services/auth/token.service';

/**
 * JWT Interceptor - adds authorization header to requests
 * SOLID: Single Responsibility - only handles JWT attachment
 */
@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private tokenService: TokenService) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    const token = this.tokenService.getToken();

    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      });
    }

    return next.handle(request);
  }
}

// src/app/core/interceptors/error.interceptor.ts
import { Injectable, Injector } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth/auth.service';
import { ErrorHandlerService } from '../services/error/error-handler.service';

/**
 * Error Interceptor - handles HTTP errors globally
 * SOLID: Single Responsibility - centralized error handling
 */
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(
    private injector: Injector,
    private errorHandler: ErrorHandlerService
  ) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Handle 401 Unauthorized - refresh token
        if (error.status === 401) {
          const authService = this.injector.get(AuthService);
          authService.logout();
        }

        // Handle 403 Forbidden - user doesn't have permission
        if (error.status === 403) {
          this.errorHandler.showError(
            'You do not have permission to perform this action.'
          );
        }

        // Handle 404 Not Found
        if (error.status === 404) {
          this.errorHandler.showError('The requested resource was not found.');
        }

        // Handle 429 Too Many Requests - rate limited
        if (error.status === 429) {
          const retryAfter = error.headers.get('Retry-After');
          this.errorHandler.showError(
            `Rate limited. Please try again in ${retryAfter || '60'} seconds.`
          );
        }

        // Handle 500+ Server Errors
        if (error.status >= 500) {
          this.errorHandler.showError(
            'A server error occurred. Please try again later.'
          );
        }

        return throwError(() => error);
      })
    );
  }
}

// src/app/core/interceptors/loading.interceptor.ts
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpResponse,
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap, finalize } from 'rxjs/operators';
import { LoadingService } from '../services/loading/loading.service';

/**
 * Loading Interceptor - tracks HTTP request state
 * SOLID: Single Responsibility - manages loading state only
 */
@Injectable()
export class LoadingInterceptor implements HttpInterceptor {
  private requestsInProgress = 0;

  constructor(private loadingService: LoadingService) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    this.requestsInProgress++;
    this.loadingService.show();

    return next.handle(request).pipe(
      tap(event => {
        if (event instanceof HttpResponse) {
          // Optional: Log successful response
        }
      }),
      finalize(() => {
        this.requestsInProgress--;
        if (this.requestsInProgress === 0) {
          this.loadingService.hide();
        }
      })
    );
  }
}

// src/app/core/guards/auth.guard.ts
import { Injectable } from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  UrlTree,
  Router,
  CanActivateChild,
  CanLoad,
  Route,
  UrlSegment,
} from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from '../services/auth/auth.service';

/**
 * Auth Guard - protects routes that require authentication
 * SOLID: Single Responsibility - only checks authentication
 */
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate, CanActivateChild, CanLoad {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> {
    return this.checkAuth(state.url);
  }

  canActivateChild(
    childRoute: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> {
    return this.checkAuth(state.url);
  }

  canLoad(
    route: Route,
    segments: UrlSegment[]
  ): Observable<boolean | UrlTree> {
    const url = `/${route.path}`;
    return this.checkAuth(url);
  }

  private checkAuth(url: string): Observable<boolean | UrlTree> {
    return this.authService.isAuthenticated$.pipe(
      map(isAuthenticated => {
        if (isAuthenticated) {
          return true;
        }

        // Redirect to login
        this.router.navigate(['/login'], { queryParams: { returnUrl: url } });
        return false;
      })
    );
  }
}

// src/app/core/guards/role.guard.ts
import { Injectable } from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  UrlTree,
  Router,
} from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from '../services/auth/auth.service';

/**
 * Role Guard - protects routes based on user role
 * SOLID: Interface Segregation - focused on role checking
 */
@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> {
    const requiredRoles = route.data['roles'] as string[];

    if (!requiredRoles || requiredRoles.length === 0) {
      return new Observable(obs => {
        obs.next(true);
        obs.complete();
      });
    }

    const userRole = this.authService.getCurrentUser()?.role;

    if (userRole && requiredRoles.includes(userRole)) {
      return new Observable(obs => {
        obs.next(true);
        obs.complete();
      });
    }

    this.router.navigate(['/unauthorized']);
    return new Observable(obs => {
      obs.next(false);
      obs.complete();
    });
  }
}

// src/app/core/guards/permission.guard.ts
import { Injectable } from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  UrlTree,
  Router,
} from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth/auth.service';

/**
 * Permission Guard - protects routes based on user permissions
 * SOLID: Single Responsibility - only checks permissions
 */
@Injectable({ providedIn: 'root' })
export class PermissionGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> {
    const requiredPermissions = route.data['permissions'] as string[];

    if (!requiredPermissions || requiredPermissions.length === 0) {
      return new Observable(obs => {
        obs.next(true);
        obs.complete();
      });
    }

    const hasPermission = requiredPermissions.some(permission =>
      this.authService.hasPermission(permission)
    );

    if (hasPermission) {
      return new Observable(obs => {
        obs.next(true);
        obs.complete();
      });
    }

    this.router.navigate(['/forbidden']);
    return new Observable(obs => {
      obs.next(false);
      obs.complete();
    });
  }
}
