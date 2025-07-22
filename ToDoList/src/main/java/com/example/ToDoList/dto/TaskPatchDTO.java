package com.example.ToDoList.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskPatchDTO {
    private String taskName;
    private String taskDescription;
    private LocalDateTime dueDate;
    private Boolean completed;
}
