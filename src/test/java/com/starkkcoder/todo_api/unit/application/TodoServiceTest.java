package com.starkkcoder.todo_api.unit.application;

import com.starkkcoder.todo_api.application.TodoService;
import com.starkkcoder.todo_api.application.exceptions.TodoNotFoundException;
import com.starkkcoder.todo_api.domain.model.TodoModel;
import com.starkkcoder.todo_api.domain.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoService todoService;

    private TodoModel existingTodo;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        existingTodo = new TodoModel(
                1L,
                "Tarea existente",
                "Descripción existente",
                false,
                now,
                now
        );
    }

    @Test
    void getAll_shouldReturnListOfTodos() {
        when(todoRepository.findAll()).thenReturn(List.of(existingTodo));

        List<TodoModel> result = todoService.getAll();

        assertEquals(1, result.size());
        assertEquals("Tarea existente", result.get(0).getTitle());
        verify(todoRepository).findAll();
    }

    @Test
    void getById_whenExists_shouldReturnTodo() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(existingTodo));

        TodoModel result = todoService.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Tarea existente", result.getTitle());
        verify(todoRepository).findById(1L);
    }

    @Test
    void getById_whenNotExists_shouldThrowTodoNotFoundException() {
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TodoNotFoundException.class, () -> todoService.getById(99L));
        verify(todoRepository).findById(99L);
    }

    @Test
    void create_shouldSaveTodoCorrectly() {
        ArgumentCaptor<TodoModel> captor = ArgumentCaptor.forClass(TodoModel.class);

        when(todoRepository.save(any(TodoModel.class))).thenAnswer(invocation -> {
            TodoModel todo = invocation.getArgument(0);
            todo.setId(10L);
            return todo;
        });

        TodoModel result = todoService.create("Nueva tarea", "Nueva descripción");

        verify(todoRepository).save(captor.capture());
        TodoModel saved = captor.getValue();

        assertEquals("Nueva tarea", saved.getTitle());
        assertEquals("Nueva descripción", saved.getDescription());
        assertFalse(saved.isCompleted());
        assertEquals(10L, result.getId());
    }

    @Test
    void update_shouldModifyExistingTodo() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(existingTodo));
        when(todoRepository.save(any(TodoModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TodoModel updated = todoService.update(
                1L,
                "Título actualizado",
                "Descripción actualizada",
                true
        );

        assertEquals("Título actualizado", updated.getTitle());
        assertEquals("Descripción actualizada", updated.getDescription());
        assertTrue(updated.isCompleted());

        verify(todoRepository).findById(1L);
        verify(todoRepository).save(existingTodo);
    }

    @Test
    void update_whenTodoDoesNotExist_shouldThrowException() {
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TodoNotFoundException.class, () ->
                todoService.update(99L, "x", "y", true)
        );
    }

    @Test
    void delete_shouldDeleteWhenExists() {
        when(todoRepository.existsById(1L)).thenReturn(true);

        todoService.delete(1L);

        verify(todoRepository).deleteById(1L);
    }

    @Test
    void delete_whenNotExists_shouldThrowException() {
        when(todoRepository.existsById(99L)).thenReturn(false);

        assertThrows(TodoNotFoundException.class, () -> todoService.delete(99L));
    }

    @Test
    void toggleStatus_shouldInvertCompletedValue() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(existingTodo));
        when(todoRepository.save(any(TodoModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TodoModel toggled = todoService.toggleStatus(1L);

        assertTrue(toggled.isCompleted());
        verify(todoRepository).findById(1L);
        verify(todoRepository).save(existingTodo);
    }
}
