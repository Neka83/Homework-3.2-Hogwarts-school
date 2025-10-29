package ru.hogwarts.school.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
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
        Student savedStudent = studentService.createStudent(student);
        return savedStudent.getId();
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




    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Long uploadAvatar(@PathVariable Long id, @RequestParam("avatar") MultipartFile file) throws IOException {
        Avatar avatar = studentService.uploadAvatarAndReturn(id, file);
        return avatar.getId();
    }


    @GetMapping("/{id}/avatar")
    public void getAvatarFromDB(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Avatar avatar = studentService.findAvatar(id);

        response.setContentType(avatar.getMediaType());
        response.setContentLength((int) avatar.getFileSize());
        response.setHeader("Content-Disposition", "inline; filename=\"" + id + "." +
                avatar.getMediaType().split("/")[1] + "\"");

        try (OutputStream os = response.getOutputStream()) {
            os.write(avatar.getData());
        }
    }


    @GetMapping("/{id}/avatar/preview")
    public byte[] getAvatarPreview(@PathVariable Long id) {
        Avatar avatar = studentService.findAvatar(id);
        return avatar.getData();
    }


    @GetMapping("/{id}/avatar/file")
    public void getAvatarFromFile(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Avatar avatar = studentService.findAvatar(id);

        response.setContentType(avatar.getMediaType());
        response.setContentLength((int) avatar.getFileSize());
        response.setHeader("Content-Disposition", "inline; filename=\"" + id + "." +
                avatar.getMediaType().split("/")[1] + "\"");

        try (OutputStream os = response.getOutputStream()) {
            os.write(Files.readAllBytes(Path.of(avatar.getFilePath())));
        }
    }
}