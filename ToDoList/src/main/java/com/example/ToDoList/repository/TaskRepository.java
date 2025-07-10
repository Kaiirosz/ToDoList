package com.example.ToDoList.repository;


import com.example.ToDoList.model.Task;
import org.springframework.data.repository.CrudRepository;

public interface TaskRepository extends CrudRepository<Task,Long> {

}
