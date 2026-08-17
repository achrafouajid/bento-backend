package com.bento.crm.whatsapp.repository;

import com.bento.crm.whatsapp.model.WaAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaAccountRepository extends JpaRepository<WaAccount, UUID> {

    Optional<WaAccount> findByOrganizationId(UUID organizationId);

    /**
     * Resolves an inbound Meta webhook to a tenant. This is the only tenant key the
     * callback carries, which is why it is unique across organizations.
     */
    Optional<WaAccount> findByPhoneNumberId(String phoneNumberId);
}
