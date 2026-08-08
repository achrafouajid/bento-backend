// src/app/features/partners/partners-list/partners-list.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { PartnerFacadeService } from '../../../core/services/facade/partner-facade.service';
import { PartnerDto, PaginationOptions, PartnerType, PartnerStage } from '../../../core/models';

/**
 * Partners List Component
 * Demonstrates:
 * - Using Facade service (hides complexity)
 * - RxJS subscriptions with takeUntil pattern
 * - Pagination
 * - Error handling
 * - Loading states
 *
 * SOLID: Single Responsibility - only handles presentation logic
 */
@Component({
  selector: 'app-partners-list',
  templateUrl: './partners-list.component.html',
  styleUrls: ['./partners-list.component.scss'],
})
export class PartnersListComponent implements OnInit, OnDestroy {
  partners: PartnerDto[] = [];
  totalElements = 0;
  pageSize = 20;
  currentPage = 0;
  isLoading = false;

  // Filters
  filterType: PartnerType | null = null;
  filterStage: PartnerStage | null = null;

  private destroy$ = new Subject<void>();

  constructor(private partnerFacade: PartnerFacadeService) {}

  ngOnInit(): void {
    this.loadPartners();

    // Subscribe to loading state
    // In real app, use facade's loading$ observable
  }

  /**
   * Load partners
   */
  loadPartners(): void {
    const pagination: PaginationOptions = {
      page: this.currentPage,
      size: this.pageSize,
    };

    this.partnerFacade
      .getPartners(this.currentPage, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          this.partners = response.content;
          this.totalElements = response.total_elements;
        },
        error: (error) => {
          console.error('Failed to load partners', error);
          // Error is handled by ErrorHandlerService globally
        },
      });
  }

  /**
   * Handle page change
   */
  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadPartners();
  }

  /**
   * Handle partner create
   */
  onCreatePartner(): void {
    // Navigate to create partner form
  }

  /**
   * Handle partner edit
   */
  onEditPartner(partnerId: string): void {
    // Navigate to edit partner form
  }

  /**
   * Handle partner delete
   */
  onDeletePartner(partnerId: string): void {
    if (confirm('Are you sure you want to delete this partner?')) {
      this.partnerFacade
        .deletePartner(partnerId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.loadPartners(); // Reload list
          },
          error: (error) => {
            console.error('Failed to delete partner', error);
          },
        });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

// src/app/features/partners/partner-form/partner-form.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { PartnerFacadeService } from '../../../core/services/facade/partner-facade.service';
import { CreatePartnerRequest, PartnerType, PartnerSource } from '../../../core/models';

/**
 * Partner Form Component
 * Demonstrates:
 * - Reactive Forms
 * - Form validation
 * - Service integration
 * - Loading states
 * - Error handling
 *
 * SOLID: Single Responsibility - handles partner form only
 */
@Component({
  selector: 'app-partner-form',
  templateUrl: './partner-form.component.html',
  styleUrls: ['./partner-form.component.scss'],
})
export class PartnerFormComponent implements OnInit {
  form!: FormGroup;
  isLoading = false;
  isEditMode = false;
  partnerId: string | null = null;

  partnerTypes: PartnerType[] = ['LEAD', 'PROSPECT', 'CUSTOMER', 'VENDOR'];
  partnerSources: PartnerSource[] = ['WEBSITE', 'TRADE_SHOW', 'LINKEDIN', 'CAMPAIGN', 'REFERRAL', 'COLD_CALL', 'INBOUND', 'OTHER'];

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private partnerFacade: PartnerFacadeService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.partnerId = this.route.snapshot.paramMap.get('id');
    if (this.partnerId) {
      this.isEditMode = true;
      this.loadPartnerData(this.partnerId);
    }
  }

  /**
   * Initialize form with validation
   */
  private initializeForm(): void {
    this.form = this.fb.group({
      type: ['LEAD', Validators.required],
      name: ['', [Validators.required, Validators.minLength(2)]],
      company_name: [''],
      email: ['', Validators.email],
      phone: [''],
      city: [''],
      country: [''],
      source: [''],
      score: [0, [Validators.min(0), Validators.max(100)]],
      temperature: ['WARM'],
      priority: ['MEDIUM'],
      comments: [''],
    });
  }

  /**
   * Load partner data for edit mode
   */
  private loadPartnerData(partnerId: string): void {
    // In real app, you'd have a getPartner method in facade
    // this.partnerFacade.getPartner(partnerId)
    //   .pipe(takeUntil(this.destroy$))
    //   .subscribe(partner => {
    //     this.form.patchValue(partner);
    //   });
  }

  /**
   * Submit form
   */
  onSubmit(): void {
    if (this.form.invalid) {
      this.markFormGroupTouched(this.form);
      return;
    }

    const payload: CreatePartnerRequest = this.form.value;
    this.isLoading = true;

    const request$ = this.isEditMode && this.partnerId
      ? this.partnerFacade.updatePartner(this.partnerId, payload)
      : this.partnerFacade.createPartner(payload);

    request$
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.router.navigate(['/partners']);
        },
        error: (error) => {
          console.error('Failed to save partner', error);
          this.isLoading = false;
        },
      });
  }

  /**
   * Mark all form fields as touched to show validation errors
   */
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();

      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

// src/app/features/auth/login/login.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth/auth.service';
import { LoginRequest } from '../../../core/models';

/**
 * Login Component
 * Demonstrates:
 * - Authentication service integration
 * - Form handling
 * - Route navigation
 * - Error handling
 *
 * SOLID: Single Responsibility - handles login only
 */
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {
  form!: FormGroup;
  isLoading = false;
  returnUrl: string = '/dashboard';

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.initializeForm();
  }

  ngOnInit(): void {
    // If already authenticated, redirect to dashboard
    this.authService.isAuthenticated$
      .pipe(takeUntil(this.destroy$))
      .subscribe(isAuthenticated => {
        if (isAuthenticated) {
          this.router.navigate([this.returnUrl]);
        }
      });

    // Get return URL from route parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
  }

  /**
   * Initialize login form
   */
  private initializeForm(): void {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
    });
  }

  /**
   * Handle login
   */
  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    this.isLoading = true;
    const payload: LoginRequest = this.form.value;

    this.authService
      .login(payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.router.navigate([this.returnUrl]);
        },
        error: (error) => {
          console.error('Login failed', error);
          this.isLoading = false;
        },
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
