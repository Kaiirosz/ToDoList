package com.example.ToDoList.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity //required for JPA
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor //creates a constructor with no args(parameters) which is required by JPA.
@AllArgsConstructor //creates a constructor with all attribute parameters to easily instantiate the entity.
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "task_description")
    private String taskDescription;

    @Column(name = "due_date")
    private Date dueDate;

    @Column(name = "completed")
    private boolean completed;


}
