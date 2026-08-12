package com.bento.crm.identity.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.identity.dto.CreateGroupMeetingRequest;
import com.bento.crm.identity.dto.CreateGroupMessageRequest;
import com.bento.crm.identity.dto.CreateTeamRequest;
import com.bento.crm.identity.model.CrmGroup;
import com.bento.crm.identity.model.GroupMeeting;
import com.bento.crm.identity.model.GroupMessage;
import com.bento.crm.identity.repository.CrmGroupRepository;
import com.bento.crm.identity.repository.GroupMeetingRepository;
import com.bento.crm.identity.repository.GroupMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CrmGroupService {

    private final CrmGroupRepository crmGroupRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final GroupMeetingRepository groupMeetingRepository;

    public CrmGroupService(CrmGroupRepository crmGroupRepository,
                            GroupMessageRepository groupMessageRepository,
                            GroupMeetingRepository groupMeetingRepository) {
        this.crmGroupRepository = crmGroupRepository;
        this.groupMessageRepository = groupMessageRepository;
        this.groupMeetingRepository = groupMeetingRepository;
    }

    private static UUID getCurrentUserId() {
        return UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Transactional
    public CrmGroup createTeam(CreateTeamRequest request) {
        UUID orgId = TenantContext.getCurrentOrganizationId();

        CrmGroup group = CrmGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        group.setOrganizationId(orgId);
        return crmGroupRepository.save(group);
    }

    public CrmGroup getTeam(UUID teamId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return crmGroupRepository.findByOrganizationIdAndId(orgId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
    }

    public Page<CrmGroup> listTeams(Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return crmGroupRepository.findByOrganizationId(orgId, pageable);
    }

    @Transactional
    public CrmGroup updateTeam(UUID teamId, CreateTeamRequest request) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        CrmGroup group = crmGroupRepository.findByOrganizationIdAndId(orgId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        group.setName(request.getName());
        group.setDescription(request.getDescription());

        return crmGroupRepository.save(group);
    }

    @Transactional
    public void deleteTeam(UUID teamId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        CrmGroup group = crmGroupRepository.findByOrganizationIdAndId(orgId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        crmGroupRepository.delete(group);
    }

    public Page<GroupMessage> listMessages(UUID groupId, Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        getTeam(groupId);
        return groupMessageRepository.findByGroupId(orgId, groupId, pageable);
    }

    @Transactional
    public GroupMessage createMessage(UUID groupId, CreateGroupMessageRequest request) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        getTeam(groupId);

        UUID authorId = getCurrentUserId();
        GroupMessage toSave = GroupMessage.builder()
                .groupId(groupId)
                .authorUserId(authorId)
                .content(request.getContent())
                .readByUserIds(List.of(authorId))
                .build();
        toSave.setOrganizationId(orgId);
        return groupMessageRepository.save(toSave);
    }

    public Page<GroupMeeting> listMeetings(UUID groupId, Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        getTeam(groupId);
        return groupMeetingRepository.findByGroupId(orgId, groupId, pageable);
    }

    @Transactional
    public GroupMeeting createMeeting(UUID groupId, CreateGroupMeetingRequest request) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        getTeam(groupId);

        GroupMeeting toSave = GroupMeeting.builder()
                .groupId(groupId)
                .title(request.getTitle())
                .description(request.getDescription())
                .scheduledAt(request.getScheduledAt())
                .createdByUserId(getCurrentUserId())
                .attendeeUserIds(request.getAttendeeUserIds())
                .meetingLink(request.getMeetingLink())
                .build();
        toSave.setOrganizationId(orgId);
        return groupMeetingRepository.save(toSave);
    }
}
