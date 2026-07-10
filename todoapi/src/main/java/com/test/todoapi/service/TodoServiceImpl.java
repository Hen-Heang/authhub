package com.test.todoapi.service;

import com.henheang.securityapi.repository.UserRepository;
import com.test.todoapi.domain.TodoList;
import com.test.todoapi.payload.TodoListRequest;
import com.test.todoapi.payload.TodoListResponse;
import com.test.todoapi.repository.TodoRepository;
import com.test.todoapi.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createTodoList(TodoListRequest request) {
        // Get the user by ID
        TodoList todoList = new TodoList();
        todoList.setUser(userRepository.findById(AuthUtils.getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found")));
        todoList.setTitle(request.getTitle());
        todoList.setDescription(request.getDescription());
        todoList.setColor(request.getColor());
        todoList.setPosition(request.getPosition() != null ? request.getPosition().toString() : "0");

        TodoList savedTodoList = todoRepository.save(todoList);
        mapToResponse(savedTodoList);
    }

    private void mapToResponse(TodoList savedTodoList) {
        TodoListResponse.builder()
                .listId(String.valueOf(savedTodoList.getListId()))
                .title(savedTodoList.getTitle())
                .description(savedTodoList.getDescription())
                .color(savedTodoList.getColor())
                .position(savedTodoList.getPosition())
                .isArchived(savedTodoList.getIsArchived())
                .createdAt(savedTodoList.getCreatedAt())
                .updatedAt(savedTodoList.getUpdatedAt())
                .itemCount(savedTodoList.getTodoItems() != null ? savedTodoList.getTodoItems().size() : 0)
                .build();
    }
}