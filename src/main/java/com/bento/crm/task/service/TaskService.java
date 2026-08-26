package com.bento.crm.task.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.common.model.RelatedEntityType;
import com.bento.crm.common.repository.EntityLinkSpecifications;
import com.bento.crm.task.dto.CreateTaskRequest;
import com.bento.crm.task.model.Task;
import com.bento.crm.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional
    public Task createTask(CreateTaskRequest request) {
        Task task = new Task();
        applyRequest(task, request);
        task.setOrganizationId(TenantContext.getCurrentOrganizationId());
        return taskRepository.save(task);
    }

    public Task getTask(UUID id) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return taskRepository.findByOrganizationIdAndId(orgId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    public Page<Task> listTasks(Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return taskRepository.findByOrganizationId(orgId, pageable);
    }

    /**
     * Lists the tasks attached to a given record, e.g. everything raised for one customer.
     * Both filter arguments are optional; with neither set this is equivalent to
     * {@link #listTasks(Pageable)}.
     */
    public Page<Task> listTasks(RelatedEntityType relatedEntityType, UUID relatedEntityId, Pageable pageable) {
        if (relatedEntityType == null && relatedEntityId == null) {
            return listTasks(pageable);
        }
        UUID orgId = TenantContext.getCurrentOrganizationId();
        Specification<Task> spec = EntityLinkSpecifications.<Task>inOrganization(orgId)
                .and(EntityLinkSpecifications.relatedTo(relatedEntityType, relatedEntityId));
        return taskRepository.findAll(spec, pageable);
    }

    @Transactional
    public Task updateTask(UUID id, CreateTaskRequest request) {
        Task task = getTask(id);
        applyRequest(task, request);
        return taskRepository.save(task);
    }

    private void applyRequest(Task task, CreateTaskRequest request) {
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssignedTeamId(request.getAssignedTeamId());
        task.setAssignedToUserId(request.getAssignedToUserId());
        task.setAssignedByUserId(request.getAssignedByUserId());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setRelatedEntity(request.toEntityLink());
    }

    @Transactional
    public void deleteTask(UUID id) {
        Task task = getTask(id);
        taskRepository.delete(task);
    }
}
