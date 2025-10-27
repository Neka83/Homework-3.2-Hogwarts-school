package ru.hogwarts.school.service;

import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.List;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public List<Faculty> findByNameOrColor(String param) {
        return facultyRepository.findByNameIgnoreCaseOrColorIgnoreCase(param, param);
    }

    public List<Student> getStudentsOfFaculty(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .map(Faculty::getStudents)
                .orElse(List.of());
    }

    public List<Faculty> getAllFaculties() {
        return facultyRepository.findAll();
    }
}
