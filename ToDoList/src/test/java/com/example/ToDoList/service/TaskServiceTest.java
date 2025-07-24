package com.example.ToDoList.service;

import com.example.ToDoList.dto.TaskDTO;
import com.example.ToDoList.dto.TaskPatchDTO;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.parse("07-19-2026 23:30",FORMATTER);
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 5L;

    @Test
    public void givenValidId_whenGetTask_thenReturnTaskDTO(){
        Task task = createSampleTask();
        TaskDTO taskDTO = createSampleTaskDTO();
        when(taskRepository.findById(VALID_ID)).thenReturn(Optional.of(task));
        when(taskMapper.toDTO(task)).thenReturn(taskDTO);
        TaskDTO result = taskService.getTask(VALID_ID);
        Assertions.assertEquals(taskDTO, result);
        Assertions.assertEquals("Do The Dishes", result.getTaskName());
        verify(taskRepository).findById(VALID_ID);
        verify(taskMapper).toDTO(task);
    }

    @Test
    public void givenInvalidId_whenGetTask_thenReturnTaskNotFoundException(){
        when(taskRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        TaskNotFoundException ex = Assertions.assertThrows(TaskNotFoundException.class, () -> taskService.getTask(INVALID_ID));
        Assertions.assertEquals("Task with id " + INVALID_ID + " not found", ex.getMessage());
        verify(taskRepository).findById(INVALID_ID);
        verify(taskMapper, never()).toDTO(any(Task.class));
    }

    @Test
    public void givenValidDTO_whenCreateTask_thenReturnTaskDTO() {
        Task task = createSampleTask();
        TaskDTO taskDTO = createSampleTaskDTO();
        when(taskMapper.toEntity(taskDTO)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(taskDTO);
        TaskDTO savedTask = taskService.createTask(taskDTO);
        Assertions.assertNotNull(savedTask);
        Assertions.assertEquals(taskDTO,savedTask);
        verify(taskMapper).toEntity(taskDTO);
        verify(taskRepository).save(task);
        verify((taskMapper)).toDTO(task);
    }

    @Test
    public void givenValidId_whenDeleteTask_thenDeleteTaskInRepository() {
        when(taskRepository.existsById(VALID_ID)).thenReturn(true);
        taskService.deleteTask(VALID_ID);
        verify(taskRepository).existsById(VALID_ID);
        verify(taskRepository).deleteById(VALID_ID);
    }

    @Test
    public void givenInvalidId_whenDeleteTask_thenThrowTaskNotFoundException() {
        when(taskRepository.existsById(INVALID_ID)).thenReturn(false);
        TaskNotFoundException ex = Assertions.assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(INVALID_ID));
        Assertions.assertEquals("Task with id " + INVALID_ID + " not found", ex.getMessage());
        verify(taskRepository).existsById(INVALID_ID);
        verify(taskRepository, never()).deleteById(anyLong());
    }

    @Test
    public void givenValidIdAndTaskDTO_whenEditTask_thenReturnUpdatedTaskDTO() {
        Task task = createSampleTask();
        TaskDTO taskDTO = createSampleTaskDTO();
        Task updatedTask = Task.builder()
                .taskName("Wipe the floor")
                .taskDescription("Updated Description")
                .dueDate(LOCAL_DATE_TIME.plusDays(5))
                .completed(true)
                .build();
        TaskDTO updatedTaskDTO = TaskDTO.builder()
                .taskName("Wipe the floor")
                .taskDescription("Updated Description")
                .dueDate(LOCAL_DATE_TIME.plusDays(5))
                .completed(true)
                .build();
        when(taskRepository.findById(VALID_ID)).thenReturn(Optional.of(task));
        doNothing().when(taskMapper).editTaskFromDTO(taskDTO, task);
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskMapper.toDTO(updatedTask)).thenReturn(updatedTaskDTO);
        TaskDTO result = taskService.editTask(VALID_ID, taskDTO);
        Assertions.assertEquals(result, updatedTaskDTO);
        verify(taskRepository).findById(VALID_ID);
        verify(taskMapper).editTaskFromDTO(taskDTO, task);
        verify(taskRepository).save(any(Task.class));
        verify(taskMapper).toDTO(updatedTask);
    }

    @Test
    public void givenInvalidId_whenEditTask_thenThrowTaskNotFoundException() {
        TaskDTO taskDTO = createSampleTaskDTO();
        when(taskRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        TaskNotFoundException ex = Assertions.assertThrows(TaskNotFoundException.class, () -> taskService.editTask(INVALID_ID, taskDTO));
        Assertions.assertEquals("Task with id " + INVALID_ID + " not found", ex.getMessage());
        verify(taskRepository).findById(INVALID_ID);
        verify(taskMapper, never()).editTaskFromDTO(any(TaskDTO.class), any(Task.class));
        verify(taskRepository, never()).save(any(Task.class));
        verify(taskMapper, never()).toDTO(any(Task.class));
    }

    @Test
    public void givenValidIdAndTaskDTO_whenPatchTask_thenReturnUpdatedTaskDTO() {
        TaskPatchDTO taskDTO = TaskPatchDTO.builder()
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
        when(taskRepository.findById(VALID_ID)).thenReturn(Optional.of(task));
        doNothing().when(taskMapper).patchTaskFromDTO(taskDTO, task);
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskMapper.toDTO(updatedTask)).thenReturn(updatedTaskDTO);
        TaskDTO result = taskService.patchTask(VALID_ID, taskDTO);
        Assertions.assertEquals(result, updatedTaskDTO);
        verify(taskRepository).findById(VALID_ID);
        verify(taskMapper).patchTaskFromDTO(taskDTO, task);
        verify(taskRepository).save(any(Task.class));
        verify(taskMapper).toDTO(updatedTask);
    }

    @Test
    public void givenInvalidId_whenPatchTask_thenThrowTaskNotFoundException() {
        TaskPatchDTO taskDTO = TaskPatchDTO.builder()
                .taskName("Clean your room")
                .taskDescription("Description")
                .build();
        when(taskRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        TaskNotFoundException ex = Assertions.assertThrows(TaskNotFoundException.class, () -> taskService.patchTask(INVALID_ID, taskDTO));
        Assertions.assertEquals("Task with id " + INVALID_ID + " not found", ex.getMessage());
        verify(taskRepository).findById(INVALID_ID);
        verify(taskMapper, never()).patchTaskFromDTO(any(TaskPatchDTO.class), any(Task.class));
        verify(taskRepository, never()).save(any(Task.class));
        verify(taskMapper, never()).toDTO(any(Task.class));
    }

    @Test
    public void givenTasksExist_whenGetAllTasks_thenReturnTaskDTOList() {
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
        Assertions.assertEquals("Task 1", result.getFirst().getTaskName());
        Assertions.assertEquals("Desc 1", result.getFirst().getTaskDescription());
        verify(taskRepository).findAll();
        verify(taskMapper).toDTOList(taskList);
    }

    @Test
    public void givenNoTasks_whenGetAllTasks_thenReturnEmptyTaskDTOList() {
        List<Task> taskList = List.of();
        List<TaskDTO> taskDTOList = List.of();
        when(taskRepository.findAll()).thenReturn(taskList);
        when(taskMapper.toDTOList(taskList)).thenReturn(taskDTOList);
        List<TaskDTO> result = taskService.getAllTasks();
        Assertions.assertEquals(result, taskDTOList);
        Assertions.assertEquals(0, result.size());
        Assertions.assertNotNull(result);
        verify(taskRepository).findAll();
        verify(taskMapper).toDTOList(taskList);
    }

    private Task createSampleTask(){
        return Task.builder()
                .taskName("Do The Dishes")
                .taskDescription("Description")
                .dueDate(LOCAL_DATE_TIME)
                .completed(false)
                .build();
    }

    private TaskDTO createSampleTaskDTO(){
        return TaskDTO.builder()
                .taskName("Do The Dishes")
                .taskDescription("Description")
                .dueDate(LOCAL_DATE_TIME)
                .completed(false)
                .build();
    }

}