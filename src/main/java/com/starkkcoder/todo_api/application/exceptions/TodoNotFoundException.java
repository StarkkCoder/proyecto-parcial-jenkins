package com.starkkcoder.todo_api.application.exceptions;

public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(Long id) {
        super("Todo no encontrado con id: " + id);
    }
}
