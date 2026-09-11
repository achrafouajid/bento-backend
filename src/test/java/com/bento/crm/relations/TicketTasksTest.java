package com.bento.crm.relations;

import com.bento.crm.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers raising tasks from inside a ticket (Jira-style sub-tasks): the {@code /tickets/{id}/tasks}
 * sub-resource, the defaults a sub-task inherits from its ticket, and the task progress counts
 * every ticket read carries so a list can show "2/5 tasks" per row.
 */
class TicketTasksTest extends IntegrationTestBase {

    private String currentUserId(String token) throws Exception {
        String response = mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private String createTicket(String token, String body) throws Exception {
        String response = mockMvc.perform(post("/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private String createTicketTask(String token, String ticketId, String body) throws Exception {
        String response = mockMvc.perform(post("/tickets/" + ticketId + "/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void ticketTask_isLinkedBackToTicketAndInheritsItsDefaults() throws Exception {
        String token = signUpAndLogin();
        String userId = currentUserId(token);
        String ticketId = createTicket(token, """
                {"title": "Portal login broken", "status": "OPEN", "priority": "HIGH",
                 "deadline": "2030-01-15", "assignedToUserId": "%s"}
                """.formatted(userId));

        mockMvc.perform(post("/tickets/" + ticketId + "/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Reproduce on staging"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Reproduce on staging"))
                .andExpect(jsonPath("$.relatedEntityType").value("TICKET"))
                .andExpect(jsonPath("$.relatedEntityId").value(ticketId))
                .andExpect(jsonPath("$.status").value("TODO"))
                // Inherited from the ticket: assignee, HIGH -> URGENT priority, deadline as due date.
                .andExpect(jsonPath("$.assignedToUserId").value(userId))
                .andExpect(jsonPath("$.assignedByUserId").value(userId))
                .andExpect(jsonPath("$.priority").value("URGENT"))
                .andExpect(jsonPath("$.dueDate").value("2030-01-15"));
    }

    @Test
    void ticketTask_explicitValuesWinOverInheritedOnes() throws Exception {
        String token = signUpAndLogin();
        String ticketId = createTicket(token, """
                {"title": "Slow dashboard", "status": "OPEN", "priority": "URGENT", "deadline": "2030-01-15"}
                """);

        mockMvc.perform(post("/tickets/" + ticketId + "/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Profile the query", "priority": "LOW", "status": "IN_PROGRESS",
                                 "dueDate": "2030-02-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.priority").value("LOW"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.dueDate").value("2030-02-01"))
                .andExpect(jsonPath("$.assignedToUserId").doesNotExist());
    }

    @Test
    void ticketTask_requiresTitle() throws Exception {
        String token = signUpAndLogin();
        String ticketId = createTicket(token, """
                {"title": "Needs a task", "status": "OPEN"}
                """);

        mockMvc.perform(post("/tickets/" + ticketId + "/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ticketTask_unknownTicketIs404() throws Exception {
        String token = signUpAndLogin();

        mockMvc.perform(post("/tickets/00000000-0000-0000-0000-000000000000/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Orphan\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/tickets/00000000-0000-0000-0000-000000000000/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void ticketReads_carryTaskProgress() throws Exception {
        String token = signUpAndLogin();
        String userId = currentUserId(token);
        String ticketId = createTicket(token, """
                {"title": "Export fails", "status": "OPEN"}
                """);
        String otherTicketId = createTicket(token, """
                {"title": "No tasks here", "status": "OPEN"}
                """);

        // A ticket with no tasks reports 0/0 rather than omitting the fields.
        mockMvc.perform(get("/tickets/" + ticketId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskCount").value(0))
                .andExpect(jsonPath("$.taskDoneCount").value(0));

        String doneTaskId = createTicketTask(token, ticketId, "{\"title\": \"Check logs\"}");
        createTicketTask(token, ticketId, "{\"title\": \"Patch exporter\"}");
        createTicketTask(token, ticketId, "{\"title\": \"Notify customer\"}");

        // Completing one task through the normal task API is reflected in the ticket's counts.
        mockMvc.perform(patch("/tasks/" + doneTaskId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Check logs", "status": "DONE", "assignedByUserId": "%s",
                                 "relatedEntityType": "TICKET", "relatedEntityId": "%s"}
                                """.formatted(userId, ticketId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/tickets/" + ticketId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskCount").value(3))
                .andExpect(jsonPath("$.taskDoneCount").value(1));

        mockMvc.perform(get("/tickets/" + ticketId + "/tasks").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].relatedEntityId").value(
                        org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(ticketId))));

        // The list carries per-row counts too, computed in one grouped query for the page.
        mockMvc.perform(get("/tickets").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '" + ticketId + "')].taskCount").value(3))
                .andExpect(jsonPath("$.content[?(@.id == '" + ticketId + "')].taskDoneCount").value(1))
                .andExpect(jsonPath("$.content[?(@.id == '" + otherTicketId + "')].taskCount").value(0));
    }

    @Test
    void ticketTasks_areInvisibleToOtherOrganizations() throws Exception {
        String token = signUpAndLogin();
        String ticketId = createTicket(token, """
                {"title": "Private ticket", "status": "OPEN"}
                """);
        createTicketTask(token, ticketId, "{\"title\": \"Private task\"}");

        String outsider = signUpAndLogin();
        mockMvc.perform(get("/tickets/" + ticketId + "/tasks").header("Authorization", "Bearer " + outsider))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/tickets/" + ticketId + "/tasks")
                        .header("Authorization", "Bearer " + outsider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Sneaky\"}"))
                .andExpect(status().isNotFound());
    }
}
