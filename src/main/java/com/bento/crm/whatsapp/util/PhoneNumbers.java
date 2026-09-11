package com.bento.crm.whatsapp.util;

import java.util.Optional;

/**
 * Normalisation of CRM-entered phone numbers to E.164.
 *
 * <p>Partner phone numbers are free text, so the same person can be stored as
 * {@code 0661234567}, {@code 06 61 23 45 67}, {@code +212661234567} or
 * {@code 00212661234567}. All four must collapse to one value, otherwise the
 * unique constraint on (organization, phone) fails to dedupe conversations and an
 * inbound reply cannot be matched back to the contact it came from.
 */
public final class PhoneNumbers {

    private static final String DEFAULT_COUNTRY_CODE = "212";

    private PhoneNumbers() {
    }

    /**
     * @param raw           a phone number as typed into the CRM
     * @param defaultPrefix country calling code applied to local-format numbers,
     *                      without a plus (Morocco is {@code 212})
     * @return the number as {@code +<digits>}, or empty when it cannot be a mobile
     */
    public static Optional<String> toE164(String raw, String defaultPrefix) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }

        String prefix = (defaultPrefix == null || defaultPrefix.isBlank()) ? DEFAULT_COUNTRY_CODE : defaultPrefix;
        String digits = raw.replaceAll("[^0-9+]", "");

        if (digits.startsWith("+")) {
            digits = digits.substring(1);
        } else if (digits.startsWith("00")) {
            // International prefix dialled the European way.
            digits = digits.substring(2);
        } else if (digits.startsWith("0")) {
            // National format: drop the trunk zero and apply the country code.
            digits = prefix + digits.substring(1);
        } else if (!digits.startsWith(prefix)) {
            digits = prefix + digits;
        }

        digits = digits.replaceAll("\\D", "");

        // E.164 allows at most 15 digits; anything under 8 is not a reachable number.
        if (digits.length() < 8 || digits.length() > 15) {
            return Optional.empty();
        }
        return Optional.of("+" + digits);
    }

    public static Optional<String> toE164(String raw) {
        return toE164(raw, DEFAULT_COUNTRY_CODE);
    }
}
