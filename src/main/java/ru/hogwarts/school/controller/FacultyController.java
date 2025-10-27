package ru.hogwarts.school.controller;

import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;

import java.util.List;

@RestController
@RequestMapping("/faculties")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping("/filter")
    public List<Faculty> findByNameOrColor(@RequestParam String param) {
        return facultyService.findByNameOrColor(param);
    }

    @GetMapping("/{id}/students")
    public List<Student> getStudentsOfFaculty(@PathVariable Long id) {
        return facultyService.getStudentsOfFaculty(id);
    }

    @GetMapping
    public List<Faculty> getAllFaculties() {
        return facultyService.getAllFaculties();
    }
}
