package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.exceptions.FacultyNotFoundException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class FacultyService {

    private static final Logger logger = LoggerFactory.getLogger(FacultyService.class);

    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public List<Faculty> findByNameOrColor(String param) {
        logger.info("Was invoked method findByNameOrColor");
        return facultyRepository.findByNameIgnoreCaseOrColorIgnoreCase(param, param);
    }

    public List<Student> getStudentsOfFaculty(Long facultyId) {
        logger.info("Was invoked method getStudentsOfFaculty id={}", facultyId);
        return facultyRepository.findById(facultyId)
                .map(Faculty::getStudents)
                .orElse(List.of());
    }

    public List<Faculty> getAllFaculties() {
        logger.info("Was invoked method getAllFaculties");
        return facultyRepository.findAll();
    }

    public Faculty findFaculty(Long id) {
        logger.info("Was invoked method findFaculty id={}", id);
        return facultyRepository.findById(id)
                .orElseThrow(() -> new FacultyNotFoundException("Faculty with id=" + id + " not found"));
    }

    public String getLongestFacultyName() {
        logger.info("Was invoked method getLongestFacultyName");

        return facultyRepository.findAll().stream()
                .map(Faculty::getName)
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(String::length))
                .orElse("");
    }
}