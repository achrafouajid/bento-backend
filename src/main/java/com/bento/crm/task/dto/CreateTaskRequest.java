package com.bento.crm.task.dto;

import com.bento.crm.task.model.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank
    private String title;

    private String description;

    private UUID assignedTeamId;

    private UUID assignedToUserId;

    @NotNull
    private UUID assignedByUserId;

    @NotNull
    private Task.TaskStatus status;

    private Task.Priority priority;

    private LocalDate dueDate;

    @NotNull
    private Task.RelatedEntityType relatedEntityType;

    private UUID relatedEntityId;
}
