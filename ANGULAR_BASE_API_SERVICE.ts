// src/app/core/services/api/base-api.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, timer } from 'rxjs';
import { catchError, retry, retryWhen, mergeMap, finalize } from 'rxjs/operators';
import { API_CONFIG, HTTP_CONFIG, ERROR_MESSAGES } from '../../config';
import { ApiResponse, PaginationOptions } from '../../models';
import { ErrorHandlerService } from '../error/error-handler.service';

/**
 * Abstract base service for all API services
 * Implements common HTTP operations and retry logic
 * SOLID: Single Responsibility - handles only HTTP communication
 */
@Injectable({ providedIn: 'root' })
export abstract class BaseApiService {
  protected readonly baseUrl = API_CONFIG.baseUrl;
  private isLoading = false;

  constructor(
    protected http: HttpClient,
    protected errorHandler: ErrorHandlerService
  ) {}

  /**
   * GET request with retry logic
   * SOLID: Open/Closed - can be extended by subclasses
   */
  protected get<T>(url: string, params?: PaginationOptions): Observable<T> {
    return this.http.get<T>(this.buildUrl(url), {
      params: this.buildHttpParams(params),
    }).pipe(
      this.retryPolicy(),
      catchError(error => this.handleError(error))
    );
  }

  /**
   * GET request returning paginated response
   */
  protected getList<T>(
    url: string,
    pagination?: PaginationOptions
  ): Observable<ApiResponse<T>> {
    return this.get<ApiResponse<T>>(url, pagination);
  }

  /**
   * POST request
   */
  protected post<T>(url: string, body: any): Observable<T> {
    return this.http.post<T>(this.buildUrl(url), body).pipe(
      catchError(error => this.handleError(error))
    );
  }

  /**
   * PATCH request
   */
  protected patch<T>(url: string, body: any): Observable<T> {
    return this.http.patch<T>(this.buildUrl(url), body).pipe(
      catchError(error => this.handleError(error))
    );
  }

  /**
   * DELETE request
   */
  protected delete<T>(url: string): Observable<T> {
    return this.http.delete<T>(this.buildUrl(url)).pipe(
      catchError(error => this.handleError(error))
    );
  }

  /**
   * File upload with progress tracking
   */
  protected uploadFile(
    url: string,
    file: File
  ): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post(this.buildUrl(url), formData).pipe(
      catchError(error => this.handleError(error))
    );
  }

  /**
   * Retry logic with exponential backoff
   * SOLID: Strategy pattern - can be replaced with different retry strategies
   */
  private retryPolicy() {
    return retryWhen(errors =>
      errors.pipe(
        mergeMap((error, index) => {
          if (this.shouldRetry(error, index)) {
            const delayMs = HTTP_CONFIG.retryDelay * Math.pow(2, index);
            console.warn(`Retrying request (attempt ${index + 1}/${HTTP_CONFIG.retryAttempts})...`);
            return timer(delayMs);
          }
          return throwError(() => error);
        })
      )
    );
  }

  /**
   * Determine if request should be retried
   */
  private shouldRetry(error: any, attemptNumber: number): boolean {
    if (attemptNumber >= HTTP_CONFIG.retryAttempts) {
      return false;
    }

    if (error instanceof HttpErrorResponse) {
      return HTTP_CONFIG.retryableStatusCodes.includes(error.status);
    }

    return true;
  }

  /**
   * Build full URL
   */
  protected buildUrl(endpoint: string): string {
    return `${this.baseUrl}${endpoint}`;
  }

  /**
   * Build HTTP params from pagination options
   * SOLID: Dependency Inversion - depends on interface, not implementation
   */
  protected buildHttpParams(options?: PaginationOptions): HttpParams {
    let params = new HttpParams();

    if (options) {
      if (options.page !== undefined) {
        params = params.set('page', options.page.toString());
      }
      if (options.size !== undefined) {
        params = params.set('size', options.size.toString());
      }
      if (options.sort) {
        params = params.set('sort', options.sort);
      }
    }

    return params;
  }

  /**
   * Centralized error handling
   * SOLID: Single Responsibility - delegates to ErrorHandlerService
   */
  protected handleError(error: HttpErrorResponse | any): Observable<never> {
    this.errorHandler.handle(error);
    return throwError(() => error);
  }

  /**
   * Track loading state for UI spinners
   */
  setLoading(loading: boolean): void {
    this.isLoading = loading;
  }

  getLoading(): boolean {
    return this.isLoading;
  }
}

// src/app/core/services/api/auth-api.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  LoginRequest,
  LoginResponse,
  RefreshTokenRequest,
} from '../../models';
import { API_CONFIG } from '../../config';
import { ErrorHandlerService } from '../error/error-handler.service';

/**
 * Authentication API service
 * SOLID: Single Responsibility - handles only auth-related API calls
 */
@Injectable({ providedIn: 'root' })
export class AuthApiService extends BaseApiService {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandlerService
  ) {
    super(http, errorHandler);
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.post<LoginResponse>(API_CONFIG.endpoints.auth.login, request);
  }

  refresh(request: RefreshTokenRequest): Observable<LoginResponse> {
    return this.post<LoginResponse>(API_CONFIG.endpoints.auth.refresh, request);
  }

  logout(): Observable<void> {
    return this.post<void>(API_CONFIG.endpoints.auth.logout, {});
  }

  getCurrentUser(): Observable<any> {
    return this.get(API_CONFIG.endpoints.auth.me);
  }
}

// src/app/core/services/api/partner-api.service.ts
/**
 * Partner API service
 * SOLID: Interface Segregation - focused only on partner operations
 */
@Injectable({ providedIn: 'root' })
export class PartnerApiService extends BaseApiService {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandlerService
  ) {
    super(http, errorHandler);
  }

  listPartners(pagination?: PaginationOptions): Observable<any> {
    return this.getList(API_CONFIG.endpoints.partners.list, pagination);
  }

  getPartner(id: string): Observable<any> {
    return this.get(API_CONFIG.endpoints.partners.get(id));
  }

  createPartner(partner: any): Observable<any> {
    return this.post(API_CONFIG.endpoints.partners.create, partner);
  }

  updatePartner(id: string, partner: any): Observable<any> {
    return this.patch(API_CONFIG.endpoints.partners.update(id), partner);
  }

  deletePartner(id: string): Observable<void> {
    return this.delete(API_CONFIG.endpoints.partners.delete(id));
  }

  listByType(type: string, pagination?: PaginationOptions): Observable<any> {
    return this.getList(API_CONFIG.endpoints.partners.listByType(type), pagination);
  }

  listByStage(stage: string, pagination?: PaginationOptions): Observable<any> {
    return this.getList(API_CONFIG.endpoints.partners.listByStage(stage), pagination);
  }

  getCustomer360(id: string): Observable<any> {
    return this.get(API_CONFIG.endpoints.partners.customer360(id));
  }

  addActivity(partnerId: string, activity: any): Observable<any> {
    return this.post(
      API_CONFIG.endpoints.partners.activities(partnerId),
      activity
    );
  }

  addAttachment(partnerId: string, file: File): Observable<any> {
    return this.uploadFile(
      API_CONFIG.endpoints.partners.attachments(partnerId),
      file
    );
  }
}

// src/app/core/services/api/invoice-api.service.ts
/**
 * Invoice API service
 * SOLID: Liskov Substitution - can be used anywhere BaseApiService is expected
 */
@Injectable({ providedIn: 'root' })
export class InvoiceApiService extends BaseApiService {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandlerService
  ) {
    super(http, errorHandler);
  }

  listInvoices(pagination?: PaginationOptions): Observable<any> {
    return this.getList(API_CONFIG.endpoints.invoices.list, pagination);
  }

  getInvoice(id: string): Observable<any> {
    return this.get(API_CONFIG.endpoints.invoices.get(id));
  }

  createInvoice(invoice: any): Observable<any> {
    return this.post(API_CONFIG.endpoints.invoices.create, invoice);
  }

  updateInvoice(id: string, invoice: any): Observable<any> {
    return this.patch(API_CONFIG.endpoints.invoices.update(id), invoice);
  }

  updateStatus(id: string, status: string): Observable<any> {
    return this.patch(API_CONFIG.endpoints.invoices.updateStatus(id), { status });
  }

  deleteInvoice(id: string): Observable<void> {
    return this.delete(API_CONFIG.endpoints.invoices.delete(id));
  }

  addCreditNote(invoiceId: string, creditNote: any): Observable<any> {
    return this.post(
      API_CONFIG.endpoints.invoices.creditNotes(invoiceId),
      creditNote
    );
  }

  addRecoveryReminder(invoiceId: string, reminder: any): Observable<any> {
    return this.post(
      API_CONFIG.endpoints.invoices.recoveryReminders(invoiceId),
      reminder
    );
  }
}

// Similar services for other entities (Deal, Proposal, Ticket, etc.)
// Follow the same pattern as above
