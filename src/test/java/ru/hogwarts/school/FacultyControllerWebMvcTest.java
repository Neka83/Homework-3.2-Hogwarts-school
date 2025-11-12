package ru.hogwarts.school;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.controller.FacultyController;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FacultyController.class)
public class FacultyControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FacultyService facultyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldFilterFacultiesByNameOrColorUsingParam() throws Exception {
        Faculty f = new Faculty(); f.setId(1L); f.setName("gr"); f.setColor("red");
        when(facultyService.findByNameOrColor("gr")).thenReturn(List.of(f));

        mockMvc.perform(get("/faculties/filter").param("param", "gr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("gr"));
    }

    @Test
    void shouldGetStudentsOfFaculty() throws Exception {
        Student s = new Student(); s.setId(2L); s.setName("St"); s.setAge(20);
        when(facultyService.getStudentsOfFaculty(4L)).thenReturn(List.of(s));

        mockMvc.perform(get("/faculties/4/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("St"));
    }

    @Test
    void shouldGetAllFaculties() throws Exception {
        Faculty f1 = new Faculty(); f1.setId(7L); f1.setName("X");
        when(facultyService.getAllFaculties()).thenReturn(List.of(f1));

        mockMvc.perform(get("/faculties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}