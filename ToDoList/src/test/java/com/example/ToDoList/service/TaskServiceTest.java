package com.example.ToDoList.service;

import com.example.ToDoList.dto.TaskDTO;
import com.example.ToDoList.exception.TaskNotFoundException;
import com.example.ToDoList.mapper.TaskMapper;
import com.example.ToDoList.model.Task;
import com.example.ToDoList.repository.TaskRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    TaskRepository taskRepository;

    @Mock
    TaskMapper taskMapper;

    @InjectMocks
    TaskService taskService;

    @Test
    public void givenTask_whenCreateTask_thenReturnTaskDTO() {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName("Do The Dishes").taskDescription("Description").build();
        Task task = Task.builder().id(1).taskName("Do The Dishes").taskDescription("Description").build();
        when(taskMapper.toEntity(taskDTO)).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(taskDTO);
        when(taskRepository.save(task)).thenReturn(task);
        TaskDTO savedTask = taskService.createTask(taskDTO);
        Assertions.assertNotNull(savedTask);
        Assertions.assertEquals("Do The Dishes", savedTask.getTaskName());
        Assertions.assertEquals("Description", savedTask.getTaskDescription());
        verify(taskMapper).toEntity(taskDTO);
        verify((taskMapper)).toDTO(task);
        verify(taskRepository).save(task);
    }

    @Test
    public void givenId_whenDeleteTask_thenDeleteTaskInRepository() {
        Long id = 1L;
        when(taskRepository.existsById(id)).thenReturn(true);
        taskService.deleteTask(id);
        verify(taskRepository).existsById(id);
        verify(taskRepository).deleteById(id);
    }

    @Test
    public void givenInvalidId_whenDeleteTask_thenThrowTaskNotFoundException() {
        Long id = 5L;
        when(taskRepository.existsById(id)).thenReturn(false);
        Assertions.assertThrows(TaskNotFoundException.class, () -> {
            taskService.deleteTask(id);
        });
        verify(taskRepository).existsById(id);
        verify(taskRepository,never()).deleteById(anyLong());
    }

    @Test
    public void givenIdAndTaskDTO_whenEditTask_thenReturnUpdatedTaskDTO(){
        Long id = 1L;
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName("Clean your room")
                .taskDescription("Description")
                .build();
        Task task = Task.builder()
                .taskName("Clean your room")
                .taskDescription("Description")
                .build();
        Task updatedTask = Task.builder()
                .taskName("Wipe the floor")
                .taskDescription("Updated Description")
                .build();
        TaskDTO updatedTaskDTO = TaskDTO.builder()
                .taskName("Wipe the floor")
                .taskDescription("Updated Description")
                .build();
        when(taskRepository.findById(id)).thenReturn(Optional.of(task));
        doNothing().when(taskMapper).editTaskFromDTO(taskDTO,task);
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskMapper.toDTO(updatedTask)).thenReturn(updatedTaskDTO);
        TaskDTO result = taskService.editTask(id, taskDTO);
        Assertions.assertEquals(result,updatedTaskDTO);
        verify(taskRepository).findById(id);
        verify(taskMapper).editTaskFromDTO(taskDTO,task);
        verify(taskRepository).save(any(Task.class));
        verify(taskMapper).toDTO(updatedTask);
    }

    @Test
    public void givenInvalidId_whenEditTask_thenThrowTaskNotFoundException() {
        Long id = 5L;
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName("Clean your room")
                .taskDescription("Description")
                .build();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());
        Assertions.assertThrows(TaskNotFoundException.class, () -> {
            taskService.editTask(id, taskDTO);
        });
        verify(taskRepository).findById(id);
        verify(taskMapper, never()).editTaskFromDTO(any(TaskDTO.class), any(Task.class));
        verify(taskRepository, never()).save(any(Task.class));
        verify(taskMapper, never()).toDTO(any(Task.class));
    }

    @Test
    public void givenTasksExist_whenGetAllTasks_thenReturnTaskDTOList(){
        List<Task> taskList = List.of(
                Task.builder().taskName("Task 1").taskDescription("Desc 1").build(),
                Task.builder().taskName("Task 2").taskDescription("Desc 2").build()
        );

        List<TaskDTO> taskDTOList = List.of(
                TaskDTO.builder().taskName("Task 1").taskDescription("Desc 1").build(),
                TaskDTO.builder().taskName("Task 2").taskDescription("Desc 2").build()
        );
        when(taskRepository.findAll()).thenReturn(taskList);
        when(taskMapper.toDTOList(taskList)).thenReturn(taskDTOList);
        List<TaskDTO> result = taskService.getAllTasks();
        Assertions.assertEquals(result, taskDTOList);
        verify(taskRepository).findAll();
        verify(taskMapper).toDTOList(taskList);
    }

}