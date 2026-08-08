package com.bento.crm.campaign.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.campaign.model.Campaign;
import com.bento.crm.campaign.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;

    @Transactional
    public Campaign createCampaign(Campaign campaign) {
        campaign.setOrganizationId(TenantContext.getCurrentOrganizationId());
        return campaignRepository.save(campaign);
    }

    public Campaign getCampaign(UUID id) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return campaignRepository.findByOrganizationIdAndId(orgId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
    }

    public Page<Campaign> listCampaigns(Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return campaignRepository.findByOrganizationId(orgId, pageable);
    }

    @Transactional
    public Campaign updateCampaign(UUID id, Campaign updates) {
        Campaign campaign = getCampaign(id);
        campaign.setTitle(updates.getTitle());
        campaign.setStatus(updates.getStatus());
        campaign.setChannel(updates.getChannel());
        return campaignRepository.save(campaign);
    }

    @Transactional
    public void deleteCampaign(UUID id) {
        Campaign campaign = getCampaign(id);
        campaignRepository.delete(campaign);
    }
}
