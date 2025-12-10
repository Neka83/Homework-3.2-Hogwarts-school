package ru.hogwarts.school.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public Long createStudent(@RequestBody Student student) {
        return studentService.createStudent(student).getId();
    }

    @GetMapping("/{id}")
    public Student getStudent(@PathVariable Long id) {
        return studentService.findStudent(id);
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @PutMapping
    public Student editStudent(@RequestBody Student student) {
        return studentService.editStudent(student);
    }

    @DeleteMapping("/{id}")
    public void deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
    }

    @GetMapping("/age-between")
    public List<Student> findByAgeBetween(@RequestParam int min, @RequestParam int max) {
        return studentService.findByAgeBetween(min, max);
    }

    // ------------------- NEW ENDPOINTS FOR HW 4.5 ---------------------

    @GetMapping("/names-starting-with-a")
    public List<String> getNamesStartingWithA() {
        return studentService.getStudentsNamesStartingWithA();
    }

    @GetMapping("/average-age-stream")
    public double getAverageAgeStream() {
        return studentService.getAverageAgeViaStream();
    }

    // --------------------------------------------------------------------

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Long uploadAvatar(@PathVariable Long id, @RequestParam("avatar") MultipartFile file) throws IOException {
        return studentService.uploadAvatarAndReturn(id, file).getId();
    }

    @GetMapping("/{id}/avatar")
    public void getAvatarFromDB(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Avatar avatar = studentService.findAvatar(id);

        response.setContentType(avatar.getMediaType());
        response.setContentLength((int) avatar.getFileSize());

        String filename = id + "." + avatar.getMediaType().split("/")[1];
        ContentDisposition contentDisposition = ContentDisposition.inline().filename(filename).build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

        try (OutputStream os = response.getOutputStream()) {
            os.write(avatar.getData());
        }
    }

    @GetMapping("/{id}/avatar/file")
    public void getAvatarFromFile(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Avatar avatar = studentService.findAvatar(id);

        response.setContentType(avatar.getMediaType());
        response.setContentLength((int) avatar.getFileSize());

        String filename = id + "." + avatar.getMediaType().split("/")[1];
        ContentDisposition contentDisposition = ContentDisposition.inline().filename(filename).build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

        try (OutputStream os = response.getOutputStream()) {
            os.write(Files.readAllBytes(Path.of(avatar.getFilePath())));
        }
    }

    @GetMapping("/print-parallel")
    public void printStudentsParallel() {
        studentService.printStudentsParallel();
    }

    @GetMapping("/print-synchronized")
    public void printStudentsSynchronized() {
        studentService.printStudentsSynchronized();
    }
}