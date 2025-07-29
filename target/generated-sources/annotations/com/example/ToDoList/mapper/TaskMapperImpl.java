package com.example.ToDoList.mapper;

import com.example.ToDoList.dto.TaskDTO;
import com.example.ToDoList.dto.TaskPatchDTO;
import com.example.ToDoList.model.Task;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-28T23:51:47+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23 (Oracle Corporation)"
)
@Component
public class TaskMapperImpl implements TaskMapper {

    @Override
    public Task toEntity(TaskDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Task.TaskBuilder task = Task.builder();

        task.taskName( dto.getTaskName() );
        task.taskDescription( dto.getTaskDescription() );
        task.dueDate( dto.getDueDate() );
        task.completed( dto.getCompleted() );

        return task.build();
    }

    @Override
    public TaskDTO toDTO(Task task) {
        if ( task == null ) {
            return null;
        }

        TaskDTO.TaskDTOBuilder taskDTO = TaskDTO.builder();

        taskDTO.taskName( task.getTaskName() );
        taskDTO.taskDescription( task.getTaskDescription() );
        taskDTO.dueDate( task.getDueDate() );
        taskDTO.completed( task.getCompleted() );

        return taskDTO.build();
    }

    @Override
    public void editTaskFromDTO(TaskDTO dto, Task task) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getTaskName() != null ) {
            task.setTaskName( dto.getTaskName() );
        }
        if ( dto.getTaskDescription() != null ) {
            task.setTaskDescription( dto.getTaskDescription() );
        }
        if ( dto.getDueDate() != null ) {
            task.setDueDate( dto.getDueDate() );
        }
        if ( dto.getCompleted() != null ) {
            task.setCompleted( dto.getCompleted() );
        }
    }

    @Override
    public void patchTaskFromDTO(TaskPatchDTO dto, Task task) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getTaskName() != null ) {
            task.setTaskName( dto.getTaskName() );
        }
        if ( dto.getTaskDescription() != null ) {
            task.setTaskDescription( dto.getTaskDescription() );
        }
        if ( dto.getDueDate() != null ) {
            task.setDueDate( dto.getDueDate() );
        }
        if ( dto.getCompleted() != null ) {
            task.setCompleted( dto.getCompleted() );
        }
    }

    @Override
    public List<TaskDTO> toDTOList(Iterable<Task> taskList) {
        if ( taskList == null ) {
            return null;
        }

        List<TaskDTO> list = new ArrayList<TaskDTO>();
        for ( Task task : taskList ) {
            list.add( toDTO( task ) );
        }

        return list;
    }
}
