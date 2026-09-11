package com.bento.crm.organization.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.common.model.UserRole;
import com.bento.crm.identity.model.AppUser;
import com.bento.crm.identity.repository.AppUserRepository;
import com.bento.crm.organization.dto.CreateOrganizationRequest;
import com.bento.crm.organization.dto.UpdateOrganizationRequest;
import com.bento.crm.organization.model.Organization;
import com.bento.crm.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Organization createOrganization(CreateOrganizationRequest request) {
        // Signup is unauthenticated, so reject an address that is already registered anywhere.
        // Without this an attacker could create an organization under a customer's email and add
        // a second account resolvable by the cross-tenant login lookup.
        String adminEmail = request.getAdminEmail().trim().toLowerCase();
        if (!userRepository.findAllByEmailAcrossOrganizations(adminEmail).isEmpty()) {
            throw new IllegalStateException("An account with this email already exists");
        }

        Organization organization = Organization.builder()
                .name(request.getName())
                .industry(request.getIndustry())
                .defaultCurrency(request.getDefaultCurrency() != null ? request.getDefaultCurrency() : "USD")
                .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
                .fiscalYearStartMonth(1)
                .plan("FREE")
                .build();

        organization = organizationRepository.save(organization);

        TenantContext.setCurrentOrganizationId(organization.getId());

        AppUser adminUser = AppUser.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(request.getAdminPassword()))
                .displayName(request.getAdminName())
                .role(UserRole.ADMIN)
                .isActive(true)
                .language("en")
                .avatarColor("blue")
                .build();
        adminUser.setOrganizationId(organization.getId());

        userRepository.save(adminUser);
        TenantContext.clear();

        log.info("Organization created: {} with admin user: {}", organization.getId(), adminEmail);
        return organization;
    }

    public Organization getCurrentOrganization() {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }

    @Transactional
    public Organization updateCurrentOrganization(UpdateOrganizationRequest request) {
        Organization organization = getCurrentOrganization();

        organization.setName(request.getName());
        organization.setIndustry(request.getIndustry());
        organization.setLogoUrl(request.getLogoUrl());
        if (request.getTimezone() != null) {
            organization.setTimezone(request.getTimezone());
        }
        if (request.getDefaultCurrency() != null) {
            organization.setDefaultCurrency(request.getDefaultCurrency());
        }
        if (request.getFiscalYearStartMonth() != null) {
            organization.setFiscalYearStartMonth(request.getFiscalYearStartMonth());
        }

        organization = organizationRepository.save(organization);
        log.info("Organization updated: {}", organization.getId());
        return organization;
    }
}
