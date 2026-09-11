package com.bento.crm.task.dto;

import java.util.UUID;

/**
 * How far the tasks raised for one record have got: {@code done} out of {@code total}.
 *
 * <p>Read straight off a grouped count query so a list of tickets can show "2/5 tasks" per row
 * without a query per ticket. Records with no tasks simply have no row — callers treat a missing
 * entry as {@link #none(UUID)}.</p>
 */
public record TaskProgress(UUID relatedEntityId, long total, long done) {

    public static TaskProgress none(UUID relatedEntityId) {
        return new TaskProgress(relatedEntityId, 0, 0);
    }
}
