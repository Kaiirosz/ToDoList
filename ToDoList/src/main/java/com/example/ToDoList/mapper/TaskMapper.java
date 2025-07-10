package com.example.ToDoList.mapper;

import com.example.ToDoList.dto.TaskDTO;
import com.example.ToDoList.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring") //defines the mapper interface the parenthesis generates the mapper as a spring context bean.
public interface TaskMapper {
    Task toEntity(TaskDTO dto);

    TaskDTO toDTO(Task entity);
}
