package com.example.todolist.service;

import com.example.todolist.dto.TaskDTO;
import com.example.todolist.dto.TaskPatchDTO;
import com.example.todolist.exception.TaskNotFoundException;
import com.example.todolist.mapper.TaskMapper;
import com.example.todolist.model.Task;
import com.example.todolist.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskService (TaskRepository taskRepository, TaskMapper taskMapper){
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    public TaskDTO getTask(long id){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " not found"));
        return taskMapper.toDTO(task);
    }

    public TaskDTO createTask(TaskDTO taskDTO){
        Task savedTask = taskRepository.save(taskMapper.toEntity(taskDTO));
        return taskMapper.toDTO(savedTask);
    }

    public void deleteTask(Long id){
        if (!taskRepository.existsById(id)){
            throw new TaskNotFoundException("Task with id " + id + " not found");
        }
        taskRepository.deleteById(id);
    }

    public TaskDTO editTask(Long id, TaskDTO taskDTO){
        Task taskToBeUpdated = taskRepository.findById(id).
                orElseThrow(() ->  new TaskNotFoundException("Task with id " + id + " not found"));
        taskMapper.editTaskFromDTO(taskDTO, taskToBeUpdated);
        Task updatedTask = taskRepository.save(taskToBeUpdated);
        return taskMapper.toDTO(updatedTask);
    }

    public TaskDTO patchTask(Long id, TaskPatchDTO taskPatchDTO){
        Task taskToBeUpdated = taskRepository.findById(id).
                orElseThrow(() ->  new TaskNotFoundException("Task with id " + id + " not found"));
        taskMapper.patchTaskFromDTO(taskPatchDTO, taskToBeUpdated);
        Task updatedTask = taskRepository.save(taskToBeUpdated);
        return taskMapper.toDTO(updatedTask);
    }


    public List<TaskDTO> getAllTasks(){
        return taskMapper.toDTOList(taskRepository.findAll());
    }
}
