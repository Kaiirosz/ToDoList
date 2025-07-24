package com.example.ToDoList.controller;


import com.example.ToDoList.dto.TaskDTO;
import com.example.ToDoList.dto.TaskPatchDTO;
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

    @GetMapping("/{id}")
    public TaskDTO getTask(Long id){
        return taskService.getTask(id);
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

    @PatchMapping("/{id}")
    public TaskDTO patchTask(@PathVariable Long id, @RequestBody TaskPatchDTO patchDTO) {
        return taskService.patchTask(id, patchDTO);
    }

    @GetMapping()
    public List<TaskDTO> getAllTasks(){
        return taskService.getAllTasks();
    }
}
