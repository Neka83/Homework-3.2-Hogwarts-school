package ru.hogwarts.school;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FacultyControllerRestTest {

    private static final String BASE = "/faculties";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FacultyService facultyService;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void shouldFindByNameOrColor() {
        Faculty f = new Faculty();
        f.setId(1L);
        f.setName("griffin");
        f.setColor("red");

        when(facultyService.findByNameOrColor("griffin")).thenReturn(List.of(f));

        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(url(BASE + "/filter?param=griffin"), Faculty[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getName()).isEqualTo("griffin");
    }

    @Test
    void shouldGetStudentsOfFaculty() {
        Student s = new Student(); s.setId(2L); s.setName("S"); s.setAge(10);
        when(facultyService.getStudentsOfFaculty(5L)).thenReturn(List.of(s));

        ResponseEntity<Student[]> response = restTemplate.getForEntity(url(BASE + "/5/students"), Student[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getName()).isEqualTo("S");
    }

    @Test
    void shouldGetAllFaculties() {
        Faculty f1 = new Faculty(); f1.setId(1L); f1.setName("A");
        when(facultyService.getAllFaculties()).thenReturn(List.of(f1));

        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(url(BASE), Faculty[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }
}