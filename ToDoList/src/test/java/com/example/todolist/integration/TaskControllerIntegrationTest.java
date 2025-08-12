package com.example.todolist.integration;

import com.example.todolist.dto.TaskDTO;
import com.example.todolist.dto.TaskPatchDTO;
import com.example.todolist.exception.TaskNotFoundException;
import com.example.todolist.model.Task;
import com.example.todolist.repository.TaskRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest //loads full app context to make sure all the components are interacting correctly.
@AutoConfigureMockMvc //self-explanatory
@ActiveProfiles("test")
//sets the h2 db in test properties as the one being used to not affect the application if it's running.
@Transactional //rolls back any changes in db after every test
public class TaskControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.parse("07-19-2026 23:30", FORMATTER);

    @Test
    public void givenValidId_whenGetTask_thenReturnTaskDTO() throws Exception {
        Task task = createSampleTask();
        taskRepository.save(task);
        mockMvc.perform(get("/tasks/{id}", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskName").value(task.getTaskName()))
                .andExpect(jsonPath("$.taskDescription").value(task.getTaskDescription()))
                .andExpect(jsonPath("$.dueDate").value(task.getDueDate().format(FORMATTER)))
                .andExpect(jsonPath("$.completed").value(task.getCompleted()));
    }

    @Test
    public void givenInvalidId_whenGetTask_thenRespondWith404() throws Exception {
        mockMvc.perform(get("/tasks/{id}", 100L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenWrongFormatId_whenGetTask_thenRespondWith404() throws Exception {
        mockMvc.perform(get("/tasks/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void givenValidTask_whenCreateTask_thenReturnCreatedTask() throws Exception {
        TaskDTO taskDTO = createSampleTaskDTO();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskName").value(taskDTO.getTaskName()))
                .andExpect(jsonPath("$.taskDescription").value(taskDTO.getTaskDescription()))
                .andExpect(jsonPath("$.dueDate").value(taskDTO.getDueDate().format(FORMATTER)))
                .andExpect(jsonPath("$.completed").value(taskDTO.getCompleted()));
        Task savedTask = StreamSupport.stream(taskRepository.findAll().spliterator(), false)
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals(1, taskRepository.count());
        Assertions.assertEquals(taskDTO.getTaskName(), savedTask.getTaskName());
        Assertions.assertEquals(taskDTO.getTaskDescription(), savedTask.getTaskDescription());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = "  ")
    public void givenInvalidTasksNames_whenCreateTask_thenRespondWith400(String taskName) throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName(taskName)
                .taskDescription("Description")
                .dueDate(LOCAL_DATE_TIME)
                .completed(false)
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
        Assertions.assertEquals(0, taskRepository.count());
    }

    @Test
    public void givenMalformedJson_whenCreateTask_thenRespondWith400() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskName": 12345,
                                    "taskDescription": "Description",
                                    "dueDate": "20-07-2025 23:30",
                                    "completed": false
                                }
                                """))
                .andExpect(status().isBadRequest());
        Assertions.assertEquals(0, taskRepository.count());
    }


    @Test
    public void givenEmptyBody_whenCreateTask_thenRespondWith400() throws Exception{
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        Assertions.assertEquals(0, taskRepository.count());
    }

    @Test
    public void givenValidId_whenDeleteTask_thenDeleteFromDatabaseAndRespondWith204() throws Exception {
        Task task = createSampleTask();
        taskRepository.save(task);
        mockMvc.perform(delete("/tasks/{id}", task.getId()))
                .andExpect(status().isNoContent());
        Assertions.assertEquals(0, taskRepository.count());
    }

    @Test
    public void givenInvalidId_whenDeleteTask_thenRespondWith404() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", 100L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenValidDTOAndId_whenEditTask_thenReturnEditedTaskDTO() throws Exception {
        Task task = createSampleTask();
        taskRepository.save(task);
        TaskDTO taskDTOEdit = TaskDTO.builder()
                .taskName("Wipe the floor")
                .taskDescription("Updated Description")
                .dueDate(LOCAL_DATE_TIME.plusDays(5))
                .completed(true)
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTOEdit);
        mockMvc.perform(put("/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskName").value(taskDTOEdit.getTaskName()))
                .andExpect(jsonPath("$.taskDescription").value(taskDTOEdit.getTaskDescription()))
                .andExpect(jsonPath("$.dueDate").value(taskDTOEdit.getDueDate().format(FORMATTER)))
                .andExpect(jsonPath("$.completed").value(taskDTOEdit.getCompleted()));
        Task updatedTask = taskRepository.findById(task.getId())
                .orElseThrow(() -> new TaskNotFoundException("Task with id " + task.getId() + " not found"));
        Assertions.assertEquals(taskDTOEdit.getTaskName(), updatedTask.getTaskName());
        Assertions.assertEquals(taskDTOEdit.getTaskDescription(), updatedTask.getTaskDescription());
        Assertions.assertEquals(taskDTOEdit.getDueDate(), updatedTask.getDueDate());
        Assertions.assertEquals(taskDTOEdit.getCompleted(), updatedTask.getCompleted());
    }

    @Test
    public void givenInvalidDTO_whenEditTask_thenRespondWith400() throws Exception {
        Task unchangedTask = createSampleTask();
        taskRepository.save(unchangedTask);
        TaskDTO taskDTOEdit = TaskDTO.builder()
                .taskName("Wipe the floor")
                .taskDescription("Updated Description")
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTOEdit);
        mockMvc.perform(put("/tasks/{id}", unchangedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
        Task task = taskRepository.findById(unchangedTask.getId())
                .orElseThrow(() -> new TaskNotFoundException("Task with id " + unchangedTask.getId() + " not found"));
        Assertions.assertEquals(unchangedTask.getTaskName(), task.getTaskName());
        Assertions.assertEquals(unchangedTask.getTaskDescription(), task.getTaskDescription());
        Assertions.assertEquals(unchangedTask.getDueDate(), task.getDueDate());
        Assertions.assertEquals(unchangedTask.getCompleted(), task.getCompleted());
    }

    @Test
    public void givenInvalidID_whenEditTask_thenRespondWith400() throws Exception {
        TaskDTO taskDTOEdit = TaskDTO.builder()
                .taskName("Wipe the floor")
                .taskDescription("Updated Description")
                .dueDate(LOCAL_DATE_TIME.plusDays(5))
                .completed(true)
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTOEdit);
        mockMvc.perform(put("/tasks/{id}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenValidPatchDTOAndId_whenPatchTask_thenReturnPatchedTaskDTO() throws Exception {
        Task task = createSampleTask();
        taskRepository.save(task);
        TaskPatchDTO taskDTOPatch = TaskPatchDTO.builder()
                .taskName("Wipe the floor")
                .taskDescription("Updated Description")
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTOPatch);
        mockMvc.perform(patch("/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskName").value(taskDTOPatch.getTaskName()))
                .andExpect(jsonPath("$.taskDescription").value(taskDTOPatch.getTaskDescription()));
        Task updatedTask = taskRepository.findById(task.getId())
                .orElseThrow(() -> new TaskNotFoundException("Task with id " + task.getId() + " not found"));
        Assertions.assertEquals(taskDTOPatch.getTaskName(), updatedTask.getTaskName());
        Assertions.assertEquals(taskDTOPatch.getTaskDescription(), updatedTask.getTaskDescription());
        Assertions.assertEquals(task.getDueDate(), updatedTask.getDueDate());
        Assertions.assertEquals(task.getCompleted(), updatedTask.getCompleted());
    }

    @Test
    public void givenInvalidPatchDTO_whenPatchTask_thenRespondWith400() throws Exception {
        Task task = createSampleTask();
        taskRepository.save(task);
        mockMvc.perform(patch("/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskName": invalidName
                                    "taskDescription": "Description"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenInvalidId_whenPatchTask_thenRespondWith404() throws Exception {
        TaskPatchDTO taskDTOPatch = TaskPatchDTO.builder()
                .taskName("Wipe the floor")
                .taskDescription("Updated Description")
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTOPatch);
        mockMvc.perform(patch("/tasks/{id}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }
    @Test
    public void givenEmptyBody_whenPatchTask_thenRespondWith400() throws Exception {
        Task task = createSampleTask();
        taskRepository.save(task);
        mockMvc.perform(patch("/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenMultipleTasksExists_whenGetAllTasks_thenReturnTaskDTOList() throws Exception{
        Task task = createSampleTask();
        Task anotherTask = Task.builder()
                .taskName("Sample Task Name")
                .taskDescription("Sample Task Description")
                .dueDate(LOCAL_DATE_TIME.plusDays(5))
                .completed(false)
                .build();
        taskRepository.save(task);
        taskRepository.save(anotherTask);
        MvcResult result = mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andReturn();
        String stringResponseInJson = result.getResponse().getContentAsString();
        List<TaskDTO> resultList = objectMapper.readValue(stringResponseInJson, new TypeReference<>(){});
        Assertions.assertEquals(task.getTaskName(), resultList.getFirst().getTaskName());
        Assertions.assertEquals(task.getTaskDescription(), resultList.getFirst().getTaskDescription());
        Assertions.assertEquals(task.getDueDate(), resultList.getFirst().getDueDate());
        Assertions.assertEquals(task.getCompleted(), resultList.getFirst().getCompleted());
        Assertions.assertEquals(anotherTask.getTaskName(), resultList.get(1).getTaskName());
        Assertions.assertEquals(anotherTask.getTaskDescription(), resultList.get(1).getTaskDescription());
        Assertions.assertEquals(anotherTask.getDueDate(), resultList.get(1).getDueDate());
        Assertions.assertEquals(anotherTask.getCompleted(), resultList.get(1).getCompleted());
    }

    @Test
    public void givenNoTasksExists_whenGetAllTasks_thenReturnEmptyTaskDTOList() throws Exception{
        MvcResult result = mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andReturn();
        String stringResponseInJson = result.getResponse().getContentAsString();
        List<TaskDTO> resultList = objectMapper.readValue(stringResponseInJson, new TypeReference<>(){});
        Assertions.assertTrue(resultList.isEmpty());
    }






    private TaskDTO createSampleTaskDTO() {
        return TaskDTO.builder()
                .taskName("Do The Dishes")
                .taskDescription("Description")
                .dueDate(LOCAL_DATE_TIME)
                .completed(false)
                .build();
    }

    private Task createSampleTask(){
        return Task.builder()
                .taskName("Do The Dishes")
                .taskDescription("Description")
                .dueDate(LOCAL_DATE_TIME)
                .completed(false)
                .build();
    }


}
