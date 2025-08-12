package com.example.todolist.repository;


import com.example.todolist.model.Task;
import org.springframework.data.repository.CrudRepository;

public interface TaskRepository extends CrudRepository<Task,Long> {

}
