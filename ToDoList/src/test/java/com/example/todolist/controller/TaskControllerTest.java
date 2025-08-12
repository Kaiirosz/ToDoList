package com.example.todolist.controller;

import com.example.todolist.dto.TaskDTO;
import com.example.todolist.dto.TaskPatchDTO;
import com.example.todolist.exception.TaskNotFoundException;
import com.example.todolist.service.TaskService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.parse("07-19-2026 23:30", FORMATTER);
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 5L;

    @Test
    public void givenValidId_whenGetTask_thenReturnTaskDTO() throws Exception {
        TaskDTO taskDTO = createSampleTaskDTO();
        when(taskService.getTask(VALID_ID)).thenReturn(taskDTO);
        mockMvc.perform(get("/tasks/{id}", VALID_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskName").value(taskDTO.getTaskName()))
                .andExpect(jsonPath("$.taskDescription").value(taskDTO.getTaskDescription()))
                .andExpect(jsonPath("$.dueDate").value(taskDTO.getDueDate().format(FORMATTER)))
                .andExpect(jsonPath("$.completed").value(taskDTO.getCompleted()));
        verify(taskService).getTask(VALID_ID);
    }

    @Test
    public void givenInvalidId_whenGetTask_thenRespondWith404() throws Exception {
        when(taskService.getTask(INVALID_ID)).thenThrow(new TaskNotFoundException("Task with id " + INVALID_ID + " not found"));
        mockMvc.perform(get("/tasks/{id}", INVALID_ID))
                .andExpect(status().isNotFound());
        verify(taskService).getTask(INVALID_ID);
    }

    @Test
    public void givenWrongFormatId_whenGetTask_thenRespondWith400() throws Exception {
        mockMvc.perform(get("/tasks/{id}", "abc"))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).getTask(anyLong());
    }

    @Test
    public void givenTask_whenCreateTask_thenReturnSuccessfulTaskDTO() throws Exception {
        TaskDTO taskDTO = createSampleTaskDTO();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        when(taskService.createTask(taskDTO)).thenReturn(taskDTO);
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskName").value(taskDTO.getTaskName()))
                .andExpect(jsonPath("$.taskDescription").value(taskDTO.getTaskDescription()))
                .andExpect(jsonPath("$.dueDate").value(taskDTO.getDueDate().format(FORMATTER)))
                .andExpect(jsonPath("$.completed").value(taskDTO.getCompleted()));
        verify(taskService).createTask(taskDTO);
    }

    @Test
    public void givenMissingTaskName_whenCreateTask_thenRespondWith400() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskDescription("Description")
                .dueDate(LOCAL_DATE_TIME)
                .completed(false)
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).createTask(any());
    }


    @ParameterizedTest //marks the test as parameterized
    @NullSource //provides null as input
    @EmptySource // "" as input
    @ValueSource(strings = {"   "})
        //this as input
    public void givenInvalidTaskNames_whenCreateTask_thenRespondWith400(String taskName) throws Exception {
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
        verify(taskService, never()).createTask(any());
    }

    @Test
    public void givenEmptyBody_whenCreateTask_thenRespondWith400() throws Exception{
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).createTask(any(TaskDTO.class));
    }

    @Test
    public void givenNumberTaskName_whenCreateTask_thenRespondWith400() throws Exception {
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
        verify(taskService, never()).createTask(any());
    }

    @Test
    public void givenNullTaskDescription_whenCreateTask_thenRespondWith400() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName("Dishes")
                .taskDescription(null)
                .dueDate(LOCAL_DATE_TIME)
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
        verify(taskService, never()).createTask(any());
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
        verify(taskService, never()).createTask(any());
    }

    @Test
    public void givenNonFutureDueDate_whenCreateTask_thenRespondWith400() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName("Dishes")
                .taskDescription("Description")
                .dueDate(LOCAL_DATE_TIME.minusYears(3))
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
    public void givenMissingCompleted_whenCreateTask_thenRespondWith400() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName("Dishes")
                .taskDescription("Description")
                .dueDate(LOCAL_DATE_TIME)
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).createTask(any());
    }

    @Test
    public void givenNullCompleted_whenCreateTask_thenRespondWith400() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName("Dishes")
                .taskDescription("Description")
                .dueDate(LOCAL_DATE_TIME)
                .completed(null)
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).createTask(any());
    }


    @Test
    public void givenStringCompleted_whenCreateTask_thenRespondWith400() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "taskName": 12345,
                                        "taskDescription": "Description",
                                        "dueDate": "07-20-2025 23:30",
                                        "completed": "not boolean"
                                    }
                                """))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).createTask(any());
    }

    @Test
    public void givenValidId_whenDeleteTask_thenDeleteTask() throws Exception {
        doNothing().when(taskService).deleteTask(VALID_ID);
        mockMvc.perform(delete("/tasks/{id}", VALID_ID))
                .andExpect(status().isNoContent());
        verify(taskService).deleteTask(VALID_ID);
    }

    @Test
    public void givenInvalidId_whenDeleteTask_thenRespondWith404() throws Exception {
        doThrow(new TaskNotFoundException("Task with id " + INVALID_ID + " not found")).when(taskService).deleteTask(INVALID_ID);
        mockMvc.perform(delete("/tasks/{id}", INVALID_ID))
                .andExpect(status().isNotFound());
        verify(taskService).deleteTask(INVALID_ID);
    }

    @Test
    public void givenValidDTO_whenEditTask_thenEditTask() throws Exception {
        TaskDTO taskDTO = createSampleTaskDTO();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        when(taskService.editTask(VALID_ID, taskDTO)).thenReturn(taskDTO);
        mockMvc.perform(put("/tasks/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskName").value(taskDTO.getTaskName()))
                .andExpect(jsonPath("$.taskDescription").value(taskDTO.getTaskDescription()))
                .andExpect(jsonPath("$.dueDate").value(taskDTO.getDueDate().format(FORMATTER)))
                .andExpect(jsonPath("$.completed").value(taskDTO.getCompleted()));
        verify(taskService).editTask(VALID_ID, taskDTO);
    }

    @Test
    public void givenInvalidId_whenEditTask_thenRespondWith404() throws Exception {
        TaskDTO taskDTO = createSampleTaskDTO();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        when(taskService.editTask(INVALID_ID, taskDTO)).thenThrow(new TaskNotFoundException("Task with id " + INVALID_ID + " not found")); //better for non-void return methods compared to doThrow - when
        mockMvc.perform(put("/tasks/{id}", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
        verify(taskService).editTask(INVALID_ID, taskDTO);
    }

    @Test
    public void givenInvalidDTO_whenEditTask_thenRespondWith400() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .taskName("Sweep")
                .taskDescription("The Floor")
                .build();
        String requestBody = objectMapper.writeValueAsString(taskDTO);
        mockMvc.perform(put("/tasks/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).editTask(VALID_ID, taskDTO);
    }

    @Test
    public void givenValidPatchDTO_whenPatchTask_thenEditTask() throws Exception {
        TaskPatchDTO taskPatchDTO = createSampleTaskPatchDTO();
        TaskDTO taskDTO = createSampleTaskDTO();
        String requestBody = objectMapper.writeValueAsString(taskPatchDTO);
        when(taskService.patchTask(VALID_ID, taskPatchDTO)).thenReturn(taskDTO);
        mockMvc.perform(patch("/tasks/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskName").value(taskPatchDTO.getTaskName()))
                .andExpect(jsonPath("$.taskDescription").value(taskPatchDTO.getTaskDescription()));
        verify(taskService).patchTask(VALID_ID, taskPatchDTO);
    }

    @Test
    public void givenInvalidId_whenPatchTask_thenRespondWith404() throws Exception {
        TaskPatchDTO taskPatchDTO = createSampleTaskPatchDTO();
        String requestBody = objectMapper.writeValueAsString(taskPatchDTO);
        when(taskService.patchTask(INVALID_ID, taskPatchDTO)).thenThrow(new TaskNotFoundException("Task with id " + INVALID_ID + " not found")); //better for non-void return methods compared to doThrow - when
        mockMvc.perform(patch("/tasks/{id}", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
        verify(taskService).patchTask(INVALID_ID, taskPatchDTO);
    }

    @Test
    public void givenInvalidDTO_whenPatchTask_thenRespondWith400() throws Exception {
        mockMvc.perform(patch("/tasks/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "taskName": invalidName
                                "taskDescription": "Description"
                                }
                                """))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).patchTask(anyLong(), any(TaskPatchDTO.class));
    }

    @Test
    public void givenEmptyBody_whenPatchTask_thenRespondWith400() throws Exception {
        mockMvc.perform(patch("/tasks/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).patchTask(anyLong(), any(TaskPatchDTO.class));
    }

    @Test
    public void givenNonNumericId_whenPatchTask_thenRespondWith400() throws Exception {
        TaskPatchDTO taskPatchDTO = createSampleTaskPatchDTO();
        String requestBody = objectMapper.writeValueAsString(taskPatchDTO);
        mockMvc.perform(patch("/tasks/{id}", "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).patchTask(anyLong(), any(TaskPatchDTO.class));
    }


    @Test
    public void givenTasksExist_whenGetAllTasks_thenReturnTaskDTOList() throws Exception {
        List<TaskDTO> taskDTOList = List.of(
                TaskDTO.builder().taskName("Task 1").taskDescription("Desc 1").build(),
                TaskDTO.builder().taskName("Task 2").taskDescription("Desc 2").build()
        );
        when(taskService.getAllTasks()).thenReturn(taskDTOList);
        MvcResult result = mockMvc.perform(get("/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn(); //get the result of the request
        String responseBody = result.getResponse().getContentAsString(); //transform the result into java String. The format is a json.
        List<TaskDTO> responseList = objectMapper.readValue( //read responseBody and -
                responseBody,
                new TypeReference<>() {
                } //Deserialize this JSON as a list of TaskDTO objects
        );
        Assertions.assertEquals(taskDTOList, responseList);
        Assertions.assertEquals("Task 1", responseList.getFirst().getTaskName());
        Assertions.assertEquals(2, responseList.size());
    }

    @Test
    public void givenNoTasks_whenGetAllTasks_thenReturnTaskDTOList() throws Exception {
        List<TaskDTO> taskDTOList = List.of();
        when(taskService.getAllTasks()).thenReturn(taskDTOList);
        MvcResult result = mockMvc.perform(get("/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn(); //get the result of the request
        String responseBody = result.getResponse().getContentAsString(); //transform the result into java String. The format is a json.
        List<TaskDTO> responseList = objectMapper.readValue( //read responseBody and -
                responseBody,
                new TypeReference<>() {
                } //Deserialize this JSON as a list of TaskDTO objects
        );
        Assertions.assertEquals(0, responseList.size());
    }

    private TaskDTO createSampleTaskDTO() {
        return TaskDTO.builder()
                .taskName("Do The Dishes")
                .taskDescription("Description")
                .dueDate(LOCAL_DATE_TIME)
                .completed(false)
                .build();
    }

    public TaskPatchDTO createSampleTaskPatchDTO() {
        return TaskPatchDTO.builder()
                .taskName("Do The Dishes")
                .taskDescription("Description")
                .build();
    }
}


