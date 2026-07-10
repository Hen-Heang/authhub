package com.test.todoapi.service;

import com.test.todoapi.payload.TodoListRequest;
import jakarta.validation.Valid;

public interface TodoService {

    void createTodoList(@Valid TodoListRequest request);
}
