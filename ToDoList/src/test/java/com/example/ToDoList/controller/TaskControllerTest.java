package com.example.ToDoList.controller;

import com.example.ToDoList.dto.TaskDTO;
import com.example.ToDoList.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
    private final LocalDateTime localDateTime = LocalDateTime.parse("07-19-2026 23:30",formatter);

    @Test
    public void givenTask_whenCreateTask_thenReturnSuccessfulTaskDTO() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName("Dishes")
                .taskDescription("Description")
                .dueDate(localDateTime)
                .completed(false)
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        when(taskService.createTask(taskDTO)).thenReturn(taskDTO);
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskName").value("Dishes"))
                .andExpect(jsonPath("$.taskDescription").value("Description"))
                .andExpect(jsonPath("$.dueDate").value("07-19-2026 23:30"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    public void givenMissingTaskName_whenCreateTask_thenRespondWith400() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskDescription("Description")
                .dueDate(localDateTime)
                .completed(false)
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
        verify(taskService,never()).createTask(any());
    }


    @ParameterizedTest //marks the test as parameterized
    @NullSource //provides null as input
    @EmptySource // "" as input
    @ValueSource(strings = {"   "}) //this as input
    void givenInvalidTaskNames_whenCreateTask_thenRespondWith400(String taskName) throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName(taskName)
                .taskDescription("Description")
                .dueDate(localDateTime)
                .completed(false)
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).createTask(any());

    }

    @Test
    public void givenNumberTaskName_whenCreateTask_thenRespondWith400() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
                "taskName": 12345,
                "taskDescription": "Description",
                "dueDate": "20-07-2025 23:30",  // wrong format
                "completed": false
            }
        """))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void givenMissingTaskDescription_whenCreateTask_thenRespondWith400() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName("Dishes")
                .dueDate(localDateTime)
                .completed(false)
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNullTaskDescription_whenCreateTask_thenRespondWith400() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName("Dishes")
                .taskDescription(null)
                .dueDate(localDateTime)
                .completed(false)
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNumberTaskDescription_whenCreateTask_thenRespondWith400() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
                "taskName": "Dishes",
                "taskDescription": 12345,
                "dueDate": "20-07-2025 23:30",  // wrong format
                "completed": false
            }
        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenWrongDueDateFormat_whenCreateTask_thenRespondWith400() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
                "taskName": "Dishes",
                "taskDescription": "Description",
                "dueDate": "20-07-2025 23:30",
                "completed": false
            }
        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNonFutureDueDate_whenCreateTask_thenRespondWith400() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName("Dishes")
                .taskDescription("Description")
                .dueDate(localDateTime)
                .completed(false)
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenMissingCompleted_whenCreateTask_thenRespondWith400() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName("Dishes")
                .taskDescription("Description")
                .dueDate(localDateTime)
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNullCompleted_whenCreateTask_thenRespondWith400() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName("Dishes")
                .taskDescription("Description")
                .dueDate(localDateTime)
                .completed(null)
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void givenStringCompleted_whenCreateTask_thenRespondWith400() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
                "taskName": 12345,
                "taskDescription": "Description",
                "dueDate": "07-20-2025 23:30",  // wrong format
                "completed": "not boolean"
            }
        """))
                .andExpect(status().isBadRequest());
    }



}