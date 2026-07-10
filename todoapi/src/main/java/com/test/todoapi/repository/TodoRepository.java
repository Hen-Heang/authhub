package com.test.todoapi.repository;

import com.test.todoapi.domain.TodoList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<TodoList, Long> {




}
