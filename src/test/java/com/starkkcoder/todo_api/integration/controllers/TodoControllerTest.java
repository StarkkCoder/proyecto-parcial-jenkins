package com.starkkcoder.todo_api.integration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starkkcoder.todo_api.application.TodoService;
import com.starkkcoder.todo_api.application.exceptions.TodoNotFoundException;
import com.starkkcoder.todo_api.domain.model.TodoModel;
import com.starkkcoder.todo_api.infrastructure.ports.ApiExceptionHandler;
import com.starkkcoder.todo_api.infrastructure.ports.TodoController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TodoController.class)
@Import(ApiExceptionHandler.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TodoService todoService;

    private TodoModel buildTodo(Long id, String title, String description, boolean completed) {
        LocalDateTime now = LocalDateTime.now();
        return new TodoModel(id, title, description, completed, now, now);
    }

    @Test
    void getAll_shouldReturnListOfTodos() throws Exception {
        var todo1 = buildTodo(1L, "Tarea 1", "Desc 1", false);
        var todo2 = buildTodo(2L, "Tarea 2", "Desc 2", true);

        Mockito.when(todoService.getAll()).thenReturn(List.of(todo1, todo2));

        mockMvc.perform(get("/api/v1/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Tarea 1"))
                .andExpect(jsonPath("$[1].completed").value(true));
    }

    @Test
    void getById_whenExists_shouldReturnTodo() throws Exception {
        var todo = buildTodo(1L, "Tarea 1", "Desc 1", false);

        Mockito.when(todoService.getById(1L)).thenReturn(todo);

        mockMvc.perform(get("/api/v1/todos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Tarea 1"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void getById_whenNotExists_shouldReturnProblemDetails404() throws Exception {
        Mockito.when(todoService.getById(99L))
                .thenThrow(new TodoNotFoundException(99L));

        mockMvc.perform(get("/api/v1/todos/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Todo no encontrada"))
                .andExpect(jsonPath("$.detail", containsString("99")))
                .andExpect(jsonPath("$.type").value("https://example.com/problems/todo-not-found"))
                .andExpect(jsonPath("$.errorCode").value("TODO_NOT_FOUND"));
    }

    @Test
    void create_whenValidRequest_shouldReturnCreatedTodo() throws Exception {
        var requestJson = """
                {
                  "title": "Nueva tarea",
                  "description": "Descripción nueva"
                }
                """;

        var savedTodo = buildTodo(10L, "Nueva tarea", "Descripción nueva", false);
        Mockito.when(todoService.create(eq("Nueva tarea"), eq("Descripción nueva"))).thenReturn(savedTodo);

        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("Nueva tarea"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void create_whenInvalidRequest_shouldReturnProblemDetails400() throws Exception {
        var invalidJson = """
                {
                  "title": "",
                  "description": "algo"
                }
                """;

        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Datos de entrada no válidos"))
                .andExpect(jsonPath("$.type").value("https://example.com/problems/validation-error"))
                .andExpect(jsonPath("$.errors", not(empty())))
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }

    @Test
    void update_shouldReturnUpdatedTodo() throws Exception {
        var requestJson = """
                {
                  "title": "Actualizada",
                  "description": "Descripción actualizada",
                  "completed": true
                }
                """;

        var updated = buildTodo(1L, "Actualizada", "Descripción actualizada", true);

        Mockito.when(todoService.update(eq(1L), eq("Actualizada"),
                        eq("Descripción actualizada"), eq(true)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/todos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Actualizada"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void toggle_shouldInvertCompleted() throws Exception {
        var toggled = buildTodo(1L, "Tarea 1", "Desc 1", true);

        Mockito.when(todoService.toggleStatus(1L)).thenReturn(toggled);

        mockMvc.perform(patch("/api/v1/todos/{id}/toggle", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/todos/{id}", 1L))
                .andExpect(status().isNoContent());

        Mockito.verify(todoService).delete(1L);
    }
}
