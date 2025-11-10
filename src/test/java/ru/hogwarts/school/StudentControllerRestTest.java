package ru.hogwarts.school;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerRestTest {

    private static final String BASE = "/students";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void shouldCreateStudentAndReturnId() {
        Student s = new Student();
        s.setId(42L);
        s.setName("Ivan");
        s.setAge(20);

        when(studentService.createStudent(ArgumentMatchers.any(Student.class))).thenReturn(s);

        ResponseEntity<Long> response = restTemplate.postForEntity(url(BASE), s, Long.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(42L);
        verify(studentService, times(1)).createStudent(any(Student.class));
    }

    @Test
    void shouldGetStudentById() {
        Student s = new Student();
        s.setId(10L);
        s.setName("Petya");
        s.setAge(18);

        when(studentService.findStudent(10L)).thenReturn(s);

        ResponseEntity<Student> response = restTemplate.getForEntity(url(BASE + "/10"), Student.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Petya");
    }

    @Test
    void shouldGetAllStudents() {
        Student s1 = new Student();
        s1.setId(1L);
        s1.setName("A");
        s1.setAge(11);

        Student s2 = new Student();
        s2.setId(2L);
        s2.setName("B");
        s2.setAge(12);

        when(studentService.getAllStudents()).thenReturn(List.of(s1, s2));

        ResponseEntity<Student[]> response = restTemplate.getForEntity(url(BASE), Student[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);


        assertThat(response.getBody()[0].getId()).isEqualTo(1L);
        assertThat(response.getBody()[0].getName()).isEqualTo("A");
        assertThat(response.getBody()[0].getAge()).isEqualTo(11);

        assertThat(response.getBody()[1].getId()).isEqualTo(2L);
        assertThat(response.getBody()[1].getName()).isEqualTo("B");
        assertThat(response.getBody()[1].getAge()).isEqualTo(12);
    }

    @Test
    void shouldEditStudent() {
        Student s = new Student();
        s.setId(5L);
        s.setName("Edited");
        s.setAge(30);

        when(studentService.editStudent(ArgumentMatchers.any(Student.class))).thenReturn(s);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Student> entity = new HttpEntity<>(s, headers);

        ResponseEntity<Student> response = restTemplate.exchange(url(BASE), HttpMethod.PUT, entity, Student.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Edited");
    }

    @Test
    void shouldDeleteStudent() {
        doNothing().when(studentService).deleteStudent(7L);

        ResponseEntity<Void> response = restTemplate.exchange(url(BASE + "/7"), HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(studentService, times(1)).deleteStudent(7L);
    }

    @Test
    void shouldFindByAgeBetween() {
        Student s = new Student();
        s.setId(2L);
        s.setName("X");
        s.setAge(15);
        when(studentService.findByAgeBetween(10, 20)).thenReturn(List.of(s));

        ResponseEntity<Student[]> response =
                restTemplate.getForEntity(url(BASE + "/age-between?min=10&max=20"), Student[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getAge()).isEqualTo(15);
        assertThat(response.getBody()[0].getName()).isEqualTo("X");
    }

    @Test
    void shouldUploadAvatarMultipart() throws Exception {
        Avatar avatar = new Avatar();
        avatar.setId(99L);

        when(studentService.uploadAvatarAndReturn(eq(3L), any())).thenReturn(avatar);

        byte[] content = "fake-image".getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return "img.jpg";
            }
        };

        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("avatar", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

        ResponseEntity<Long> response = restTemplate.postForEntity(url("/students/3/avatar"), requestEntity, Long.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldGetAvatarFromDB() {
        Avatar avatar = new Avatar();
        avatar.setId(11L);
        avatar.setMediaType("image/png");
        avatar.setData("bytes".getBytes(StandardCharsets.UTF_8));
        avatar.setFileSize(5L);

        when(studentService.findAvatar(11L)).thenReturn(avatar);

        ResponseEntity<byte[]> response = restTemplate.getForEntity(url("/students/11/avatar"), byte[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("image/png");
    }

    @Test
    void negativeGetNonExistingStudent() {
        when(studentService.findStudent(999L)).thenThrow(new RuntimeException("not found"));

        ResponseEntity<String> response = restTemplate.getForEntity(url("/students/999"), String.class);

        assertThat(response.getStatusCode().is5xxServerError()).isTrue();
    }
}