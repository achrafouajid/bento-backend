// src/app/core/services/error/error-handler.service.ts
import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { ApiError, ERROR_MESSAGES } from '../../models';
import { ERROR_MESSAGES as CONFIG_ERRORS } from '../../config';

/**
 * Error Handler Service
 * SOLID: Single Responsibility - handles error formatting and reporting
 * SOLID: Dependency Inversion - injected into services, not vice versa
 */
@Injectable({ providedIn: 'root' })
export class ErrorHandlerService {
  private errorSubject = new BehaviorSubject<ApiError | null>(null);
  public error$ = this.errorSubject.asObservable();

  private errorMessageSubject = new BehaviorSubject<string>('');
  public errorMessage$ = this.errorMessageSubject.asObservable();

  constructor() {}

  /**
   * Handle HTTP error
   */
  handle(error: HttpErrorResponse | any): void {
    const errorResponse = this.parseError(error);
    this.errorSubject.next(errorResponse);
    this.errorMessageSubject.next(this.getErrorMessage(error));
  }

  /**
   * Show error message
   */
  showError(message: string): void {
    this.errorMessageSubject.next(message);
  }

  /**
   * Clear error
   */
  clearError(): void {
    this.errorSubject.next(null);
    this.errorMessageSubject.next('');
  }

  /**
   * Parse HTTP error response
   */
  private parseError(error: HttpErrorResponse | any): ApiError {
    if (error instanceof HttpErrorResponse) {
      // API returned an error response
      if (error.error && typeof error.error === 'object') {
        return error.error as ApiError;
      }

      // Generic HTTP error
      return {
        status: error.status,
        title: this.getHttpErrorTitle(error.status),
        detail: error.message || 'An error occurred',
        type: 'http-error',
        instance: error.url || '',
        timestamp: new Date().toISOString(),
      };
    }

    // Network or other error
    return {
      status: 0,
      title: 'Error',
      detail: error.message || 'An unexpected error occurred',
      type: 'error',
      instance: '',
      timestamp: new Date().toISOString(),
    };
  }

  /**
   * Get user-friendly error message
   */
  private getErrorMessage(error: HttpErrorResponse | any): string {
    if (error instanceof HttpErrorResponse) {
      switch (error.status) {
        case 0:
          return CONFIG_ERRORS.network;
        case 400:
          return CONFIG_ERRORS.validation;
        case 401:
          return CONFIG_ERRORS.unauthorized;
        case 403:
          return CONFIG_ERRORS.forbidden;
        case 404:
          return CONFIG_ERRORS.notFound;
        case 408:
          return CONFIG_ERRORS.timeout;
        case 409:
          return CONFIG_ERRORS.conflict;
        case 429:
          return CONFIG_ERRORS.timeout; // Rate limit
        case 500:
        case 502:
        case 503:
        case 504:
          return CONFIG_ERRORS.serverError;
        default:
          return CONFIG_ERRORS.unknown;
      }
    }

    return CONFIG_ERRORS.unknown;
  }

  /**
   * Get HTTP status title
   */
  private getHttpErrorTitle(status: number): string {
    const titles: Record<number, string> = {
      400: 'Bad Request',
      401: 'Unauthorized',
      403: 'Forbidden',
      404: 'Not Found',
      408: 'Request Timeout',
      409: 'Conflict',
      429: 'Too Many Requests',
      500: 'Internal Server Error',
      502: 'Bad Gateway',
      503: 'Service Unavailable',
      504: 'Gateway Timeout',
    };

    return titles[status] || 'Error';
  }
}

// src/app/core/services/loading/loading.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

/**
 * Loading Service - manages global loading state
 * SOLID: Single Responsibility - only tracks loading state
 */
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  private requestCountSubject = new BehaviorSubject<number>(0);
  public requestCount$ = this.requestCountSubject.asObservable();

  constructor() {}

  /**
   * Show loading indicator
   */
  show(): void {
    const count = this.requestCountSubject.value + 1;
    this.requestCountSubject.next(count);
    this.loadingSubject.next(true);
  }

  /**
   * Hide loading indicator
   */
  hide(): void {
    const count = Math.max(0, this.requestCountSubject.value - 1);
    this.requestCountSubject.next(count);

    if (count === 0) {
      this.loadingSubject.next(false);
    }
  }

  /**
   * Check if loading
   */
  isLoading(): Observable<boolean> {
    return this.loading$;
  }

  /**
   * Force stop loading
   */
  stop(): void {
    this.requestCountSubject.next(0);
    this.loadingSubject.next(false);
  }
}

// src/app/core/services/facade/partner-facade.service.ts
/**
 * Partner Facade Service
 * SOLID: Facade Pattern - simplifies complex interactions
 * Coordinates between multiple services (Partner API, Error Handler, Loading Service)
 */
@Injectable({ providedIn: 'root' })
export class PartnerFacadeService {
  constructor(
    private partnerApiService: any, // PartnerApiService
    private errorHandler: ErrorHandlerService,
    private loadingService: LoadingService
  ) {}

  /**
   * Get partners with error handling and loading state
   */
  getPartners(page: number = 0, size: number = 20): Observable<any> {
    this.loadingService.show();
    this.errorHandler.clearError();

    return this.partnerApiService.listPartners({ page, size }).pipe(
      finalize(() => this.loadingService.hide())
    );
  }

  /**
   * Create partner with validation and error handling
   */
  createPartner(partner: any): Observable<any> {
    this.loadingService.show();
    this.errorHandler.clearError();

    return this.partnerApiService.createPartner(partner).pipe(
      tap(() => this.showSuccess('Partner created successfully')),
      catchError(error => {
        this.errorHandler.handle(error);
        throw error;
      }),
      finalize(() => this.loadingService.hide())
    );
  }

  /**
   * Update partner
   */
  updatePartner(id: string, partner: any): Observable<any> {
    this.loadingService.show();
    this.errorHandler.clearError();

    return this.partnerApiService.updatePartner(id, partner).pipe(
      tap(() => this.showSuccess('Partner updated successfully')),
      catchError(error => {
        this.errorHandler.handle(error);
        throw error;
      }),
      finalize(() => this.loadingService.hide())
    );
  }

  /**
   * Delete partner
   */
  deletePartner(id: string): Observable<any> {
    this.loadingService.show();
    this.errorHandler.clearError();

    return this.partnerApiService.deletePartner(id).pipe(
      tap(() => this.showSuccess('Partner deleted successfully')),
      catchError(error => {
        this.errorHandler.handle(error);
        throw error;
      }),
      finalize(() => this.loadingService.hide())
    );
  }

  /**
   * Get partner customer 360
   */
  getCustomer360(id: string): Observable<any> {
    this.loadingService.show();
    this.errorHandler.clearError();

    return this.partnerApiService.getCustomer360(id).pipe(
      catchError(error => {
        this.errorHandler.handle(error);
        throw error;
      }),
      finalize(() => this.loadingService.hide())
    );
  }

  private showSuccess(message: string): void {
    // Implement toast/snackbar notification
    console.log('Success:', message);
  }
}
