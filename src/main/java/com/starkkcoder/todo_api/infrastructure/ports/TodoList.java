package com.starkkcoder.todo_api.infrastructure.ports;

import com.starkkcoder.todo_api.domain.model.TodoModel;
import com.starkkcoder.todo_api.domain.repository.TodoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TodoList implements TodoRepository {

    private final List<TodoModel> storage = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public synchronized List<TodoModel> findAll() {
        return new ArrayList<>(storage);
    }

    @Override
    public synchronized Optional<TodoModel> findById(Long id) {
        return storage.stream()
                .filter(todo -> todo.getId().equals(id))
                .findFirst();
    }

    @Override
    public synchronized TodoModel save(TodoModel todo) {
        if (todo.getId() == null) {
            todo.setId(idGenerator.getAndIncrement());
            storage.add(todo);
        } else {
            deleteById(todo.getId());
            storage.add(todo);
        }
        return todo;
    }

    @Override
    public synchronized void deleteById(Long id) {
        storage.removeIf(todo -> todo.getId().equals(id));
    }

    @Override
    public synchronized boolean existsById(Long id) {
        return storage.stream().anyMatch(todo -> todo.getId().equals(id));
    }
}
