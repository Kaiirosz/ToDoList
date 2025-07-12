package com.example.ToDoList.dto;

import lombok.Data;

import java.util.Date;

@Data
public class TaskDTO {
    private String taskName;
    private String taskDescription;
    private Date dueDate;
    private Boolean completed;
}
