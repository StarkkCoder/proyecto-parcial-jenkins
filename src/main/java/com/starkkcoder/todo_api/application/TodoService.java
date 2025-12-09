package com.starkkcoder.todo_api.application;

import com.starkkcoder.todo_api.application.exceptions.TodoNotFoundException;
import com.starkkcoder.todo_api.domain.model.TodoModel;
import com.starkkcoder.todo_api.domain.repository.TodoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoService {

    private final TodoRepository repository;

    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    public List<TodoModel> getAll() {
        return repository.findAll();
    }

    public TodoModel getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }

    public TodoModel create(String title, String description) {
        TodoModel todo = new TodoModel(title, description);
        return repository.save(todo);
    }

    public TodoModel update(Long id, String title, String description, Boolean completed) {
        TodoModel existing = getById(id);

        if (title != null && !title.isBlank()) {
            existing.setTitle(title);
        }
        if (description != null) {
            existing.setDescription(description);
        }
        if (completed != null) {
            existing.setCompleted(completed);
        }

        return repository.save(existing);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new TodoNotFoundException(id);
        }
        repository.deleteById(id);
    }

    public TodoModel toggleStatus(Long id) {
        TodoModel todo = getById(id);
        todo.setCompleted(!todo.isCompleted());
        return repository.save(todo);
    }
}
