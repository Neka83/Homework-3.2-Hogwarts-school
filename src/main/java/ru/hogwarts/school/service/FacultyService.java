package ru.hogwarts.school.service;

import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.List;

@Service
public class FacultyService {

    private final FacultyRepository repository;

    public FacultyService(FacultyRepository repository) {
        this.repository = repository;
    }

    public Faculty create(Faculty faculty) {
        return repository.save(faculty);
    }

    public Faculty get(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<Faculty> getAll() {
        return repository.findAll();
    }

    public Faculty update(Faculty faculty) {
        return repository.save(faculty);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Faculty> findByColor(String color) {
        return repository.findByColorIgnoreCase(color);
    }
}