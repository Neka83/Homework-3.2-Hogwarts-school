package ru.hogwarts.school.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.exceptions.StudentNotFoundException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;
    private final AvatarRepository avatarRepository;
    private final Path avatarsDir = Path.of("avatars");

    public StudentService(StudentRepository studentRepository, AvatarRepository avatarRepository) {
        this.studentRepository = studentRepository;
        this.avatarRepository = avatarRepository;
    }

    public Student createStudent(Student student) {
        logger.info("Was invoked method for create student");
        logger.debug("Creating student: {}", student);
        Student saved = studentRepository.save(student);
        logger.info("Student created with id={}", saved.getId());
        return saved;
    }

    public Student findStudent(Long id) {
        logger.info("Was invoked method for find student by id");
        logger.debug("Find student id={}", id);
        return studentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("There is no student with id = {}", id);
                    return new StudentNotFoundException("Student with id=" + id + " not found");
                });
    }

    public List<Student> getAllStudents() {
        logger.info("Was invoked method for get all students");
        return studentRepository.findAll();
    }

    public Student editStudent(Student student) {
        logger.info("Was invoked method for edit student");
        logger.debug("Edit student: {}", student);
        Student updated = studentRepository.save(student);
        logger.info("Student updated with id={}", updated.getId());
        return updated;
    }

    public void deleteStudent(Long id) {
        logger.info("Was invoked method for delete student");
        logger.debug("Delete student id={}", id);
        if (!studentRepository.existsById(id)) {
            logger.warn("Attempt to delete non-existent student id={}", id);
        } else {
            studentRepository.deleteById(id);
            logger.info("Student deleted id={}", id);
        }
    }

    public List<Student> findByAgeBetween(int min, int max) {
        logger.info("Was invoked method for find students by age between");
        logger.debug("Find students with min={} max={}", min, max);
        return studentRepository.findByAgeBetween(min, max);
    }


    public int getStudentsCount() {
        logger.info("Was invoked method for get students count");
        int count = studentRepository.getStudentsCount();
        logger.debug("Students count = {}", count);
        return count;
    }

    public double getAverageAge() {
        logger.info("Was invoked method for get average age");
        Double avg = studentRepository.getAverageAge();
        double result = avg == null ? 0.0 : avg;
        logger.debug("Average age = {}", result);
        return result;
    }

    public List<Student> getLastFiveStudents() {
        logger.info("Was invoked method for get last five students");
        return studentRepository.getLastFiveStudents();
    }


    @Transactional
    public Avatar uploadAvatarAndReturn(Long studentId, MultipartFile file) throws IOException {
        logger.info("Was invoked method for upload avatar for student id={}", studentId);
        Student student = findStudent(studentId);

        if (!Files.exists(avatarsDir)) Files.createDirectories(avatarsDir);

        Path filePath = avatarsDir.resolve(studentId + "." + getExtension(file.getOriginalFilename()));
        Files.deleteIfExists(filePath);

        try (InputStream is = file.getInputStream();
             OutputStream os = Files.newOutputStream(filePath, CREATE_NEW)) {
            is.transferTo(os);
        }

        Avatar avatar = avatarRepository.findByStudentId(studentId).orElse(new Avatar());
        avatar.setStudent(student);
        avatar.setFilePath(filePath.toString());
        avatar.setFileSize(file.getSize());
        avatar.setMediaType(file.getContentType());
        avatar.setData(file.getBytes());

        Avatar saved = avatarRepository.save(avatar);
        logger.info("Avatar saved for student id={}, avatarId={}", studentId, saved.getId());
        return saved;
    }

    @Transactional
    public Avatar findAvatar(Long studentId) {
        logger.info("Was invoked method for find avatar by student id={}", studentId);
        return avatarRepository.findByStudentId(studentId)
                .orElseThrow(() -> {
                    logger.error("Avatar for student id={} not found", studentId);
                    return new StudentNotFoundException("Avatar for student id=" + studentId + " not found");
                });
    }

    private String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}