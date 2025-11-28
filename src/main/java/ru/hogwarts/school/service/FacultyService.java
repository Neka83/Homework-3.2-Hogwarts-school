package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.exceptions.FacultyNotFoundException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.List;

@Service
public class FacultyService {

    private static final Logger logger = LoggerFactory.getLogger(FacultyService.class);

    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public List<Faculty> findByNameOrColor(String param) {
        logger.info("Was invoked method for find faculty by name or color with param={}", param);
        return facultyRepository.findByNameIgnoreCaseOrColorIgnoreCase(param, param);
    }

    public List<Student> getStudentsOfFaculty(Long facultyId) {
        logger.info("Was invoked method for get students of faculty id={}", facultyId);
        return facultyRepository.findById(facultyId)
                .map(Faculty::getStudents)
                .orElseGet(() -> {
                    logger.warn("Faculty with id={} not found or has no students", facultyId);
                    return List.of();
                });
    }

    public List<Faculty> getAllFaculties() {
        logger.info("Was invoked method for get all faculties");
        return facultyRepository.findAll();
    }

    public Faculty findFaculty(Long id) {
        logger.info("Was invoked method for find faculty by id={}", id);
        return facultyRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Faculty with id={} not found", id);
                    return new FacultyNotFoundException("Faculty with id=" + id + " not found");
                });
    }
}