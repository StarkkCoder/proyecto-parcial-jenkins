package com.starkkcoder.todo_api.domain.repository;

import com.starkkcoder.todo_api.domain.model.TodoModel;

import java.util.List;
import java.util.Optional;

public interface TodoRepository {

    List<TodoModel> findAll();

    Optional<TodoModel> findById(Long id);

    TodoModel save(TodoModel todo);

    void deleteById(Long id);

    boolean existsById(Long id);
}
