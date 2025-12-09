package com.starkkcoder.todo_api.infrastructure.ports;

import com.starkkcoder.todo_api.application.exceptions.TodoNotFoundException;
import com.starkkcoder.todo_api.application.TodoService;
import com.starkkcoder.todo_api.application.dto.CreateTodo;
import com.starkkcoder.todo_api.application.dto.TodoResponse;
import com.starkkcoder.todo_api.application.dto.UpdateTodo;
import com.starkkcoder.todo_api.domain.model.TodoModel;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/todos")
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TodoResponse> getAll() {
        return service.getAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TodoResponse getById(@PathVariable Long id) {
        TodoModel todo = service.getById(id);
        return toResponse(todo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TodoResponse create(@Valid @RequestBody CreateTodo request) {
        TodoModel created = service.create(request.getTitle(), request.getDescription());
        return toResponse(created);
    }

    @PutMapping("/{id}")
    public TodoResponse update(@PathVariable Long id,
                               @Valid @RequestBody UpdateTodo request) {
        TodoModel updated = service.update(
                id,
                request.getTitle(),
                request.getDescription(),
                request.getCompleted()
        );
        return toResponse(updated);
    }

    @PatchMapping("/{id}/toggle")
    public TodoResponse toggle(@PathVariable Long id) {
        TodoModel updated = service.toggleStatus(id);
        return toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private TodoResponse toResponse(TodoModel todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }
}
