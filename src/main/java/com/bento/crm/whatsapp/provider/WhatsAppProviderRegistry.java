package com.bento.crm.whatsapp.provider;

import com.bento.crm.whatsapp.model.WaAccount;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the transport for a tenant's account, so switching an organization from
 * the mock to live Meta is a single column change with no redeploy.
 */
@Component
public class WhatsAppProviderRegistry {

    private final Map<WaAccount.Provider, WhatsAppProvider> providers =
            new EnumMap<>(WaAccount.Provider.class);

    public WhatsAppProviderRegistry(List<WhatsAppProvider> discovered) {
        discovered.forEach(p -> providers.put(p.kind(), p));
    }

    public WhatsAppProvider forAccount(WaAccount account) {
        WhatsAppProvider provider = providers.get(account.getProvider());
        if (provider == null) {
            throw new IllegalStateException("No WhatsApp provider registered for " + account.getProvider());
        }
        return provider;
    }
}
