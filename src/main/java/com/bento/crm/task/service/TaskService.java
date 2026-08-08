package com.bento.crm.task.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.task.model.Task;
import com.bento.crm.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional
    public Task createTask(Task task) {
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

    @Transactional
    public Task updateTask(UUID id, Task updates) {
        Task task = getTask(id);
        task.setTitle(updates.getTitle());
        task.setStatus(updates.getStatus());
        task.setDescription(updates.getDescription());
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(UUID id) {
        Task task = getTask(id);
        taskRepository.delete(task);
    }
}
