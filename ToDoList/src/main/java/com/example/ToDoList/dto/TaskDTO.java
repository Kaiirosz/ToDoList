package com.example.ToDoList.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskDTO {

    @NotBlank
    private String taskName;

    @NotNull
    private String taskDescription;

    @Future
    @JsonFormat(pattern = "MM-dd-yyyy HH:mm") //control JSON serialization so that the JSON given from client request is of the specified pattern.
    private LocalDateTime dueDate;

    @NotNull
    private Boolean completed;
}
