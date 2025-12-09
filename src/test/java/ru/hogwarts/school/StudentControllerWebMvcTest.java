package ru.hogwarts.school;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.controller.StudentController;
import ru.hogwarts.school.exceptions.StudentNotFoundException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StudentController.class)
public class StudentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateStudentReturnId() throws Exception {
        Student s = new Student(); s.setId(2L); s.setName("Z"); s.setAge(21);
        when(studentService.createStudent(ArgumentMatchers.any())).thenReturn(s);

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(s)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("2")));
    }

    @Test
    void shouldGetStudentByIdAndReturnBody() throws Exception {
        Student s = new Student(); s.setId(3L); s.setName("N"); s.setAge(19);
        when(studentService.findStudent(3L)).thenReturn(s);

        mockMvc.perform(get("/students/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("N"))
                .andExpect(jsonPath("$.age").value(19));
    }

    @Test
    void shouldGetAllStudents() throws Exception {
        Student s1 = new Student(); s1.setId(1L); s1.setName("A");
        Student s2 = new Student(); s2.setId(2L); s2.setName("B");
        when(studentService.getAllStudents()).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldEditStudentAndReturnBody() throws Exception {
        Student s = new Student(); s.setId(5L); s.setName("Edited"); s.setAge(30);
        when(studentService.editStudent(ArgumentMatchers.any())).thenReturn(s);

        mockMvc.perform(put("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(s)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Edited"));
    }

    @Test
    void shouldDeleteStudent() throws Exception {
        doNothing().when(studentService).deleteStudent(7L);

        mockMvc.perform(delete("/students/7"))
                .andExpect(status().isOk());

        verify(studentService, times(1)).deleteStudent(7L);
    }

    @Test
    void shouldFindByAgeBetweenWithParams() throws Exception {
        Student s = new Student(); s.setId(8L); s.setName("YY"); s.setAge(16);
        when(studentService.findByAgeBetween(10, 20)).thenReturn(List.of(s));

        mockMvc.perform(get("/students/age-between").param("min", "10").param("max", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].age").value(16));
    }

    @Test
    void shouldUploadAvatarWithMockMvcMultipart() throws Exception {
        Avatar avatar = new Avatar(); avatar.setId(13L);
        when(studentService.uploadAvatarAndReturn(eq(3L), any())).thenReturn(avatar);

        MockMultipartFile file = new MockMultipartFile("avatar", "img.jpg", MediaType.IMAGE_JPEG_VALUE, "data".getBytes());

        mockMvc.perform(multipart("/students/3/avatar").file(file))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetAvatarFromDBAndReturnBytes() throws Exception {
        Avatar avatar = new Avatar();
        avatar.setId(14L);
        avatar.setMediaType("image/png");
        avatar.setData("b".getBytes());
        avatar.setFileSize(2L);

        when(studentService.findAvatar(14L)).thenReturn(avatar);

        mockMvc.perform(get("/students/14/avatar"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"));
    }

    @Test
    void negativeWhenStudentNotFoundShouldReturnNotFound() throws Exception {
        when(studentService.findStudent(999L))
                .thenThrow(new StudentNotFoundException("Student with id=999 not found"));

        mockMvc.perform(get("/students/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Student with id=999 not found"));
    }
}