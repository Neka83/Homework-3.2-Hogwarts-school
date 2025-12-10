package ru.hogwarts.school.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.service.StudentService;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
class StudentControllerThreadsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Test
    void printParallelShouldCallService() throws Exception {
        mockMvc.perform(get("/students/print-parallel"))
                .andExpect(status().isOk());

        verify(studentService).printStudentsParallel();
    }

    @Test
    void printSynchronizedShouldCallService() throws Exception {
        mockMvc.perform(get("/students/print-synchronized"))
                .andExpect(status().isOk());

        verify(studentService).printStudentsSynchronized();
    }
}