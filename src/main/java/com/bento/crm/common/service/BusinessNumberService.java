package com.bento.crm.common.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.UUID;

/**
 * Sequential business numbers per organization and prefix ("FAC-2026-0007"), backed by the
 * {@code business_number_sequence} table that has been in the schema since V1 but was never used —
 * which is why invoices went out without a number.
 *
 * <p>The increment is a single atomic upsert, so concurrent creates in one organization can never
 * be handed the same value. It runs in its own transaction so a number is consumed even if the
 * caller later rolls back: gaps are acceptable, duplicates are not.</p>
 */
@Service
@RequiredArgsConstructor
public class BusinessNumberService {

    @PersistenceContext
    private EntityManager entityManager;

    /** Next number for the prefix, formatted as {@code PREFIX-YYYY-NNNN}. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String next(UUID organizationId, String prefix) {
        String yearedPrefix = prefix + "-" + Year.now().getValue();
        Number value = (Number) entityManager.createNativeQuery("""
                        INSERT INTO business_number_sequence (organization_id, prefix, next_value)
                        VALUES (:org, :prefix, 2)
                        ON CONFLICT (organization_id, prefix)
                        DO UPDATE SET next_value = business_number_sequence.next_value + 1
                        RETURNING next_value - 1
                        """)
                .setParameter("org", organizationId)
                .setParameter("prefix", yearedPrefix)
                .getSingleResult();
        return String.format("%s-%04d", yearedPrefix, value.longValue());
    }
}
