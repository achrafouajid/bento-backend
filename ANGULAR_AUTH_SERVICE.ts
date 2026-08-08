// src/app/core/services/auth/token.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

/**
 * Token service - manages JWT token storage and retrieval
 * SOLID: Single Responsibility - only handles token persistence
 */
@Injectable({ providedIn: 'root' })
export class TokenService {
  private readonly TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'current_user';

  private tokenSubject = new BehaviorSubject<string | null>(
    this.getToken()
  );
  public token$ = this.tokenSubject.asObservable();

  constructor() {}

  /**
   * Save tokens to local storage
   */
  saveTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(this.TOKEN_KEY, accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
    this.tokenSubject.next(accessToken);
  }

  /**
   * Get access token
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Get refresh token
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * Save current user
   */
  saveUser(user: any): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  /**
   * Get current user
   */
  getUser(): any {
    const user = localStorage.getItem(this.USER_KEY);
    return user ? JSON.parse(user) : null;
  }

  /**
   * Check if token exists
   */
  hasToken(): boolean {
    return !!this.getToken();
  }

  /**
   * Check if token is expired
   */
  isTokenExpired(): boolean {
    const token = this.getToken();
    if (!token) return true;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expiryTime = payload.exp * 1000;
      return Date.now() >= expiryTime;
    } catch {
      return true;
    }
  }

  /**
   * Clear all tokens and user data
   */
  clearTokens(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.tokenSubject.next(null);
  }

  /**
   * Decode JWT payload
   */
  decodeToken(): any {
    const token = this.getToken();
    if (!token) return null;

    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch {
      return null;
    }
  }

  /**
   * Get user role from token
   */
  getUserRole(): string | null {
    const payload = this.decodeToken();
    return payload?.role || null;
  }

  /**
   * Get user authorities from token
   */
  getUserAuthorities(): string[] {
    const payload = this.decodeToken();
    return payload?.authorities || [];
  }
}

// src/app/core/services/auth/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, of } from 'rxjs';
import {
  tap,
  catchError,
  switchMap,
  timer,
  finalize,
} from 'rxjs/operators';
import { LoginRequest, LoginResponse, UserDto, RefreshTokenRequest } from '../../models';
import { TokenService } from './token.service';
import { AuthApiService } from '../api/auth-api.service';
import { HTTP_CONFIG } from '../../config';

/**
 * Authentication service
 * SOLID: Single Responsibility - coordinates authentication flow
 * SOLID: Dependency Inversion - depends on TokenService and AuthApiService abstractions
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject = new BehaviorSubject<UserDto | null>(
    this.tokenService.getUser()
  );
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(
    this.tokenService.hasToken()
  );
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  private tokenRefreshTimer: any;

  constructor(
    private tokenService: TokenService,
    private authApiService: AuthApiService,
    private router: Router
  ) {
    this.setupAutoTokenRefresh();
  }

  /**
   * Login
   */
  login(request: LoginRequest): Observable<LoginResponse> {
    return this.authApiService.login(request).pipe(
      tap(response => this.handleLoginSuccess(response)),
      catchError(error => {
        this.handleLoginFailure();
        throw error;
      })
    );
  }

  /**
   * Logout
   */
  logout(): void {
    this.authApiService.logout().pipe(
      finalize(() => this.handleLogout())
    ).subscribe();
  }

  /**
   * Refresh token
   */
  refreshToken(): Observable<LoginResponse> {
    const refreshToken = this.tokenService.getRefreshToken();
    if (!refreshToken) {
      this.handleLogout();
      return of();
    }

    return this.authApiService.refresh({ refresh_token: refreshToken }).pipe(
      tap(response => this.handleLoginSuccess(response)),
      catchError(() => {
        this.handleLogout();
        throw new Error('Token refresh failed');
      })
    );
  }

  /**
   * Get current user
   */
  getCurrentUser(): UserDto | null {
    return this.currentUserSubject.value;
  }

  /**
   * Check if user has permission
   */
  hasPermission(permission: string): boolean {
    const authorities = this.tokenService.getUserAuthorities();
    return authorities.includes(permission);
  }

  /**
   * Check if user has role
   */
  hasRole(role: string): boolean {
    const userRole = this.tokenService.getUserRole();
    return userRole === role;
  }

  /**
   * Handle successful login
   */
  private handleLoginSuccess(response: LoginResponse): void {
    this.tokenService.saveTokens(response.access_token, response.refresh_token);
    this.tokenService.saveUser(response.user);
    this.currentUserSubject.next(response.user);
    this.isAuthenticatedSubject.next(true);
    this.setupAutoTokenRefresh();
  }

  /**
   * Handle login failure
   */
  private handleLoginFailure(): void {
    this.handleLogout();
  }

  /**
   * Handle logout
   */
  private handleLogout(): void {
    this.tokenService.clearTokens();
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    this.clearTokenRefreshTimer();
    this.router.navigate(['/login']);
  }

  /**
   * Setup automatic token refresh
   * Refreshes token 1 minute before expiry
   */
  private setupAutoTokenRefresh(): void {
    this.clearTokenRefreshTimer();

    const payload = this.tokenService.decodeToken();
    if (!payload?.exp) return;

    const expiryTime = payload.exp * 1000;
    const nowTime = Date.now();
    const refreshTime = expiryTime - HTTP_CONFIG.tokenRefreshThreshold;

    if (refreshTime > nowTime) {
      const delayMs = refreshTime - nowTime;
      this.tokenRefreshTimer = timer(delayMs).pipe(
        switchMap(() => this.refreshToken())
      ).subscribe(
        () => console.log('Token refreshed automatically'),
        error => console.error('Auto token refresh failed', error)
      );
    }
  }

  /**
   * Clear token refresh timer
   */
  private clearTokenRefreshTimer(): void {
    if (this.tokenRefreshTimer) {
      this.tokenRefreshTimer.unsubscribe();
    }
  }
}
