package com.starkkcoder.todo_api.application.dto;

import jakarta.validation.constraints.Size;

public class UpdateTodo {

    @Size(max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    private Boolean completed;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
