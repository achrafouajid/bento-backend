// src/app/core/config/api-config.ts
/**
 * Centralized API configuration
 * Single source of truth for all endpoint definitions
 */
export const API_CONFIG = {
  baseUrl: '/api/v1',
  endpoints: {
    // Authentication
    auth: {
      login: '/auth/login',
      refresh: '/auth/refresh',
      logout: '/auth/logout',
      me: '/auth/me',
    },
    // Organizations
    organizations: {
      create: '/organizations',
      getMe: '/organizations/me',
      patch: '/organizations/me',
    },
    // Users
    users: {
      list: '/users',
      create: '/users',
      get: (id: string) => `/users/${id}`,
      update: (id: string) => `/users/${id}`,
      deactivate: (id: string) => `/users/${id}/deactivate`,
    },
    // Partners
    partners: {
      list: '/partners',
      create: '/partners',
      get: (id: string) => `/partners/${id}`,
      update: (id: string) => `/partners/${id}`,
      delete: (id: string) => `/partners/${id}`,
      listByType: (type: string) => `/partners/type/${type}`,
      listByStage: (stage: string) => `/partners/stage/${stage}`,
      customer360: (id: string) => `/partners/${id}/customer-360`,
      activities: (id: string) => `/partners/${id}/activities`,
      attachments: (id: string) => `/partners/${id}/attachments`,
      addresses: (id: string) => `/partners/${id}/addresses`,
      contacts: (id: string) => `/partners/${id}/contacts`,
    },
    // Deals
    deals: {
      list: '/deals',
      create: '/deals',
      get: (id: string) => `/deals/${id}`,
      update: (id: string) => `/deals/${id}`,
      delete: (id: string) => `/deals/${id}`,
      updateStage: (id: string) => `/deals/${id}/stage`,
    },
    // Proposals
    proposals: {
      list: '/proposals',
      create: '/proposals',
      get: (id: string) => `/proposals/${id}`,
      update: (id: string) => `/proposals/${id}`,
      delete: (id: string) => `/proposals/${id}`,
      send: (id: string) => `/proposals/${id}/send`,
      confirm: (id: string) => `/proposals/${id}/confirm`,
    },
    // Invoices
    invoices: {
      list: '/invoices',
      create: '/invoices',
      get: (id: string) => `/invoices/${id}`,
      update: (id: string) => `/invoices/${id}`,
      delete: (id: string) => `/invoices/${id}`,
      updateStatus: (id: string) => `/invoices/${id}/status`,
      creditNotes: (id: string) => `/invoices/${id}/credit-notes`,
      recoveryReminders: (id: string) => `/invoices/${id}/recovery-reminders`,
    },
    // Tickets
    tickets: {
      list: '/tickets',
      create: '/tickets',
      get: (id: string) => `/tickets/${id}`,
      update: (id: string) => `/tickets/${id}`,
      delete: (id: string) => `/tickets/${id}`,
      comments: (id: string) => `/tickets/${id}/comments`,
    },
    // Tasks
    tasks: {
      list: '/tasks',
      create: '/tasks',
      get: (id: string) => `/tasks/${id}`,
      update: (id: string) => `/tasks/${id}`,
      delete: (id: string) => `/tasks/${id}`,
      comments: (id: string) => `/tasks/${id}/comments`,
    },
    // Analytics
    analytics: {
      dashboard: '/analytics/dashboard',
      salesForecast: '/analytics/sales-forecast',
      topCustomers: '/analytics/top-customers',
      dealsByRegion: '/analytics/deals-by-region',
    },
    // Files
    files: {
      upload: '/files',
      download: (id: string) => `/files/${id}`,
    },
  },
} as const;

// src/app/core/config/http-config.ts
/**
 * HTTP client configuration
 * Defines timeouts, retry policies, and other HTTP settings
 */
export const HTTP_CONFIG = {
  timeout: 30000, // 30 seconds
  retryAttempts: 3,
  retryDelay: 1000, // 1 second
  retryableStatusCodes: [408, 429, 500, 502, 503, 504],
  tokenRefreshThreshold: 60000, // Refresh token 1 minute before expiry
} as const;

// src/app/core/config/error-messages.ts
/**
 * Centralized error messages
 * Easy to update and maintain across the application
 */
export const ERROR_MESSAGES = {
  network: 'Network error. Please check your connection.',
  unauthorized: 'Your session has expired. Please log in again.',
  forbidden: 'You do not have permission to perform this action.',
  notFound: 'The requested resource was not found.',
  validation: 'Please check the form and try again.',
  conflict: 'This resource already exists.',
  serverError: 'A server error occurred. Please try again later.',
  timeout: 'The request timed out. Please try again.',
  unknown: 'An unexpected error occurred.',
  fileToLarge: 'File size exceeds the maximum allowed limit (15 MB).',
  invalidFileType: 'The file type is not allowed.',
} as const;
