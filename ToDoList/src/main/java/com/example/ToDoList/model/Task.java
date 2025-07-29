package com.example.ToDoList.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity //required for JPA
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor //creates a constructor with no args(parameters) which is required by JPA.
@AllArgsConstructor //creates a constructor with all attribute parameters to easily instantiate the entity.
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "task_description")
    private String taskDescription;

    @Column(name = "due_date")
    private LocalDateTime dueDate; //Hibernate automatically maps this to a TIMESTAMP column in the database.

    @Column(name = "completed")
    private Boolean completed;


}

