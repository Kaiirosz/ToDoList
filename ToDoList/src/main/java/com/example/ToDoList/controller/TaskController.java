package com.example.ToDoList.controller;


import com.example.ToDoList.dto.TaskDTO;
import com.example.ToDoList.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService){
        this.taskService = taskService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO createTask(@Valid @RequestBody TaskDTO taskDTO){
        return taskService.createTask(taskDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
    }

    @PutMapping("/{id}")
    public TaskDTO editTask(@PathVariable Long id, @Valid @RequestBody TaskDTO taskDTO){
        return taskService.editTask(id, taskDTO);
    }

    @GetMapping()
    public List<TaskDTO> getAllTasks(){
        return taskService.getAllTasks();
    }
}
