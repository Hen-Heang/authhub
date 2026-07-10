package com.test.todoapi.payload;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TodoListResponse {

    @JsonProperty("list_id")
    private String listId;

    private String title;

    private String description;

    private String color;

    private String position;

    @JsonProperty("is_archived")
    private Boolean isArchived;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("item_count")
    private Integer itemCount;


}
