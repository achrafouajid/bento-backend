// src/app/core/core.module.ts
import { NgModule, Optional, SkipSelf } from '@angular/core';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

// Services
import { AuthService } from './services/auth/auth.service';
import { TokenService } from './services/auth/token.service';
import { ErrorHandlerService } from './services/error/error-handler.service';
import { LoadingService } from './services/loading/loading.service';

// API Services
import { AuthApiService } from './services/api/auth-api.service';
import { PartnerApiService } from './services/api/partner-api.service';
import { InvoiceApiService } from './services/api/invoice-api.service';

// Interceptors
import { JwtInterceptor } from './interceptors/jwt.interceptor';
import { ErrorInterceptor } from './interceptors/error.interceptor';
import { LoadingInterceptor } from './interceptors/loading.interceptor';

// Guards
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';
import { PermissionGuard } from './guards/permission.guard';

/**
 * Core Module
 * Contains singleton services, guards, and interceptors
 * Should only be imported once in AppModule
 *
 * SOLID: Module organization - all core dependencies in one place
 */
@NgModule({
  imports: [HttpClientModule],
  providers: [
    // Services
    TokenService,
    AuthService,
    ErrorHandlerService,
    LoadingService,

    // API Services
    AuthApiService,
    PartnerApiService,
    InvoiceApiService,

    // Interceptors (order matters - JWT first, then error, then loading)
    {
      provide: HTTP_INTERCEPTORS,
      useClass: JwtInterceptor,
      multi: true,
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ErrorInterceptor,
      multi: true,
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: LoadingInterceptor,
      multi: true,
    },

    // Guards
    AuthGuard,
    RoleGuard,
    PermissionGuard,
  ],
})
export class CoreModule {
  /**
   * Ensure CoreModule is only imported once
   */
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error('CoreModule is already loaded. Import only once in AppModule.');
    }
  }
}

// src/app/app-routing.module.ts
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';
import { PermissionGuard } from './core/guards/permission.guard';

/**
 * Application routing
 * Uses guards to protect routes based on authentication and authorization
 */
const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full',
  },
  {
    path: 'login',
    loadChildren: () =>
      import('./features/auth/auth.module').then(m => m.AuthModule),
  },
  {
    path: 'dashboard',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./features/dashboard/dashboard.module').then(
        m => m.DashboardModule
      ),
  },
  {
    path: 'partners',
    canActivate: [AuthGuard, PermissionGuard],
    data: { permissions: ['PARTNERS_READ'] },
    loadChildren: () =>
      import('./features/partners/partners.module').then(
        m => m.PartnersModule
      ),
  },
  {
    path: 'deals',
    canActivate: [AuthGuard, PermissionGuard],
    data: { permissions: ['DEALS_READ'] },
    loadChildren: () =>
      import('./features/deals/deals.module').then(m => m.DealsModule),
  },
  {
    path: 'invoices',
    canActivate: [AuthGuard, PermissionGuard],
    data: { permissions: ['INVOICES_READ'] },
    loadChildren: () =>
      import('./features/invoices/invoices.module').then(
        m => m.InvoicesModule
      ),
  },
  {
    path: 'admin',
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ADMIN'] },
    loadChildren: () =>
      import('./features/admin/admin.module').then(m => m.AdminModule),
  },
  {
    path: 'unauthorized',
    loadChildren: () =>
      import('./features/error/error.module').then(m => m.ErrorModule),
  },
  {
    path: 'forbidden',
    loadChildren: () =>
      import('./features/error/error.module').then(m => m.ErrorModule),
  },
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}

// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { CoreModule } from './core/core.module';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

/**
 * Root Application Module
 * Only imports CoreModule (once), not individual services
 */
@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    CoreModule, // Import CoreModule only here
    AppRoutingModule,
  ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}

// src/app/app.component.ts
import { Component, OnInit } from '@angular/core';
import { AuthService } from './core/services/auth/auth.service';
import { ErrorHandlerService } from './core/services/error/error-handler.service';
import { LoadingService } from './core/services/loading/loading.service';

/**
 * Root Application Component
 */
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  isLoading$ = this.loadingService.loading$;
  errorMessage$ = this.errorHandler.errorMessage$;
  isAuthenticated$ = this.authService.isAuthenticated$;
  currentUser$ = this.authService.currentUser$;

  constructor(
    private authService: AuthService,
    private errorHandler: ErrorHandlerService,
    private loadingService: LoadingService
  ) {}

  ngOnInit(): void {
    // Check if user is already authenticated (token exists in storage)
    // This handles page refreshes
  }
}

// src/app/app.component.html
<div class="app-container">
  <!-- Top Navigation -->
  <app-navigation *ngIf="(isAuthenticated$ | async) as authenticated">
  </app-navigation>

  <!-- Loading Spinner -->
  <app-loading-spinner *ngIf="(isLoading$ | async)"></app-loading-spinner>

  <!-- Error Dialog -->
  <app-error-dialog
    [message]="(errorMessage$ | async) || ''"
  ></app-error-dialog>

  <!-- Main Content -->
  <main class="app-content">
    <router-outlet></router-outlet>
  </main>

  <!-- Footer -->
  <app-footer></app-footer>
</div>
