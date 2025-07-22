package com.example.ToDoList.mapper;

import com.example.ToDoList.dto.TaskDTO;
import com.example.ToDoList.dto.TaskPatchDTO;
import com.example.ToDoList.model.Task;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring") //defines the mapper interface the parenthesis generates the mapper as a spring context bean.
public interface TaskMapper {
    Task toEntity(TaskDTO dto);

    TaskDTO toDTO(Task task);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)//this is PATCH/UPDATE
    void editTaskFromDTO(TaskDTO dto, @MappingTarget Task task);//The annotation makes it so only the attributes in
    // dto given by the client are modifies and the ones which are not given will be null which is then ignored

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchTaskFromDTO(TaskPatchDTO dto, @MappingTarget Task task);

    List<TaskDTO> toDTOList(Iterable<Task> taskList);
}
