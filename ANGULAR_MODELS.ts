// src/app/core/models/common.model.ts
export interface ApiResponse<T> {
  content: T[];
  page: number;
  size: number;
  total_elements: number;
  total_pages: number;
}

export interface ApiError {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  timestamp: string;
  validation_errors?: FieldError[];
}

export interface FieldError {
  field: string;
  message: string;
}

export interface PaginationOptions {
  page?: number;
  size?: number;
  sort?: string;
}

// src/app/core/models/auth.model.ts
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  access_token: string;
  refresh_token: string;
  token_type: string;
  expires_in: number;
  user: UserDto;
}

export interface RefreshTokenRequest {
  refresh_token: string;
}

export interface UserDto {
  id: string;
  organization_id: string;
  email: string;
  display_name: string;
  initials: string;
  avatar_color: string;
  role: UserRole;
  team_id?: string;
  is_active: boolean;
  phone?: string;
  job_title?: string;
  language: string;
  last_active_at?: string;
}

export type UserRole = 'ADMIN' | 'MANAGER' | 'SALESPERSON' | 'SUPPORT' | 'VIEWER';

export const ROLE_PERMISSIONS: Record<UserRole, string[]> = {
  ADMIN: ['PARTNERS_READ', 'PARTNERS_WRITE', 'DEALS_READ', 'DEALS_WRITE', 'INVOICES_READ', 'INVOICES_WRITE', 'USERS_WRITE', 'ADMIN_ACCESS'],
  MANAGER: ['PARTNERS_READ', 'PARTNERS_WRITE', 'DEALS_READ', 'DEALS_WRITE', 'INVOICES_READ', 'INVOICES_WRITE', 'USERS_READ'],
  SALESPERSON: ['PARTNERS_READ', 'PARTNERS_WRITE', 'DEALS_READ', 'DEALS_WRITE', 'PROPOSALS_READ', 'PROPOSALS_WRITE'],
  SUPPORT: ['PARTNERS_READ', 'TICKETS_READ', 'TICKETS_WRITE', 'TASKS_READ', 'TASKS_WRITE'],
  VIEWER: ['PARTNERS_READ', 'DEALS_READ', 'PROPOSALS_READ', 'TICKETS_READ', 'ANALYTICS_READ'],
};

// src/app/core/models/organization.model.ts
export interface CreateOrganizationRequest {
  name: string;
  industry?: string;
  default_currency?: string;
  timezone?: string;
  admin_email: string;
  admin_name: string;
  admin_password: string;
}

export interface OrganizationDto {
  id: string;
  name: string;
  logo_url?: string;
  industry?: string;
  timezone: string;
  fiscal_year_start_month: number;
  default_currency: string;
  plan: string;
  created_at: string;
}

// src/app/core/models/user.model.ts
export interface CreateUserRequest {
  email: string;
  display_name: string;
  password: string;
  role?: UserRole;
  team_id?: string;
  phone?: string;
  job_title?: string;
  language?: string;
}

export interface UpdateUserRequest {
  display_name: string;
  phone?: string;
  job_title?: string;
  language?: string;
}

// src/app/core/models/partner.model.ts
export type PartnerType = 'LEAD' | 'PROSPECT' | 'CUSTOMER' | 'VENDOR';
export type PartnerStage = 'NEW' | 'CONTACTED' | 'ATTEMPTED_CONTACT' | 'MEETING_SCHEDULED' | 'QUALIFIED' | 'PROPOSAL_SENT' | 'CONFIRMED' | 'CUSTOMER' | 'LOST' | 'DISQUALIFIED';
export type PartnerSource = 'WEBSITE' | 'TRADE_SHOW' | 'LINKEDIN' | 'CAMPAIGN' | 'REFERRAL' | 'COLD_CALL' | 'INBOUND' | 'OTHER';
export type Temperature = 'COLD' | 'WARM' | 'HOT';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface CreatePartnerRequest {
  type: PartnerType;
  name: string;
  company_name?: string;
  email?: string;
  phone?: string;
  city?: string;
  country?: string;
  source?: PartnerSource;
  score?: number;
  temperature?: Temperature;
  priority?: Priority;
  qualification?: string;
  stage?: PartnerStage;
  assigned_to_user_id?: string;
  owner_id?: string;
  estimated_deal_value?: number;
  probability?: number;
  comments?: string;
}

export interface PartnerDto extends CreatePartnerRequest {
  id: string;
  organization_id: string;
  created_at: string;
  updated_at: string;
  created_by: string;
  updated_by: string;
}

export interface PartnerAddressDto {
  id: string;
  partner_id: string;
  address_type: 'REGISTERED_OFFICE' | 'DELIVERY' | 'WAREHOUSE' | 'BILLING';
  street_address: string;
  postal_code: string;
  city: string;
  country: string;
  is_primary: boolean;
}

export interface PartnerContactDto {
  id: string;
  partner_id: string;
  full_name: string;
  job_title: string;
  mobile: string;
  email: string;
  is_primary: boolean;
  is_decision_maker: boolean;
}

// src/app/core/models/deal.model.ts
export type DealStage = 'OPEN' | 'PO_SENT' | 'AWAITING_DELIVERY' | 'AWAITING_INVOICING' | 'INVOICED' | 'PAID' | 'OVERDUE' | 'CLOSED_WON' | 'CLOSED_LOST';

export interface CreateDealRequest {
  partner_id: string;
  proposal_id?: string;
  title: string;
  stage?: DealStage;
  amount?: number;
  discount?: number;
  comments?: string;
  order_number?: string;
  order_date?: string;
  sales_person_user_id?: string;
  currency?: string;
  payment_terms?: string;
}

export interface DealDto extends CreateDealRequest {
  id: string;
  organization_id: string;
  created_at: string;
  updated_at: string;
}

// src/app/core/models/invoice.model.ts
export type InvoiceType = 'CUSTOMER' | 'VENDOR';
export type InvoiceStatus = 'DRAFT' | 'SENT' | 'PARTIALLY_PAID' | 'PAID' | 'OVERDUE';

export interface CreateInvoiceRequest {
  type: InvoiceType;
  partner_id: string;
  deal_id?: string;
  purchase_order_id?: string;
  status?: InvoiceStatus;
  due_date?: string;
  customer_account?: string;
  customer_name?: string;
  delivery_address?: string;
  vat_number?: string;
  subtotal?: number;
  tax?: number;
  total?: number;
}

export interface InvoiceDto extends CreateInvoiceRequest {
  id: string;
  organization_id: string;
  sent_at?: string;
  paid_at?: string;
  created_at: string;
  updated_at: string;
}

export interface CreditNoteRequest {
  reason: string;
  amount: number;
}

// src/app/core/models/proposal.model.ts
export type ProposalStatus = 'DRAFT' | 'SENT' | 'CONFIRMED' | 'REJECTED' | 'EXPIRED';

export interface CreateProposalRequest {
  partner_id: string;
  template_id?: string;
  title: string;
  status?: ProposalStatus;
  delivery_method?: string;
  opportunity_value?: number;
  closing_probability?: number;
  expected_closing_date?: string;
  competitors?: string[];
}

export interface ProposalDto extends CreateProposalRequest {
  id: string;
  organization_id: string;
  sent_at?: string;
  confirmed_at?: string;
  created_at: string;
  updated_at: string;
}

// src/app/core/models/ticket.model.ts
export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface CreateTicketRequest {
  title: string;
  description?: string;
  partner_id: string;
  assigned_to_user_id?: string;
  status?: TicketStatus;
  priority?: TicketPriority;
  ticket_type_id?: string;
  deadline?: string;
}

export interface TicketDto extends CreateTicketRequest {
  id: string;
  organization_id: string;
  resolution?: string;
  created_at: string;
  updated_at: string;
}
