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
import java.util.Comparator;
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
        Student saved = studentRepository.save(student);
        return saved;
    }

    public Student findStudent(Long id) {
        logger.info("Was invoked method for find student by id={}", id);
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
        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        logger.info("Was invoked method for delete student by id={}", id);
        studentRepository.deleteById(id);
    }

    public List<Student> findByAgeBetween(int min, int max) {
        logger.info("Was invoked method for find students age between {} and {}", min, max);
        return studentRepository.findByAgeBetween(min, max);
    }

    // ---------------------- NEW METHODS FOR 4.5 -------------------------

    public List<String> getStudentsNamesStartingWithA() {
        logger.info("Was invoked method getStudentsNamesStartingWithA");

        return studentRepository.findAll().stream()
                .map(Student::getName)
                .filter(name -> name != null && name.toUpperCase().startsWith("A"))
                .map(String::toUpperCase)
                .sorted()
                .toList();
    }

    public double getAverageAgeViaStream() {
        logger.info("Was invoked method getAverageAgeViaStream");

        return studentRepository.findAll().stream()
                .mapToInt(Student::getAge)
                .average()
                .orElse(0);
    }

    public int getFastSum() {
        logger.info("Was invoked method getFastSum");

        return java.util.stream.IntStream.rangeClosed(1, 1_000_000)
                .parallel()
                .sum();
    }

    // --------------------------------------------------------------------

    @Transactional
    public Avatar uploadAvatarAndReturn(Long studentId, MultipartFile file) throws IOException {
        logger.info("Was invoked method uploadAvatarAndReturn for student id={}", studentId);
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

        return avatarRepository.save(avatar);
    }

    @Transactional
    public Avatar findAvatar(Long studentId) {
        logger.info("Was invoked method findAvatar for id={}", studentId);
        return avatarRepository.findByStudentId(studentId)
                .orElseThrow(() -> {
                    logger.error("Avatar for student id={} not found", studentId);
                    return new StudentNotFoundException("Avatar for student id=" + studentId + " not found");
                });
    }

    private String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    public void printStudentsParallel() {
        logger.info("Was invoked method printStudentsParallel");

        List<Student> students = studentRepository.findAll();
        if (students.size() < 6) {
            logger.warn("Not enough students to print in parallel, size={}", students.size());
        }

        // первые два имени — в основном потоке
        printIfExists(students, 0);
        printIfExists(students, 1);

        // 3 и 4 — в одном потоке
        Thread thread1 = new Thread(() -> {
            printIfExists(students, 2);
            printIfExists(students, 3);
        }, "student-printer-1");

        // 5 и 6 — в другом потоке
        Thread thread2 = new Thread(() -> {
            printIfExists(students, 4);
            printIfExists(students, 5);
        }, "student-printer-2");

        thread1.start();
        thread2.start();
    }

    public void printStudentsSynchronized() {
        logger.info("Was invoked method printStudentsSynchronized");

        List<Student> students = studentRepository.findAll();
        if (students.size() < 6) {
            logger.warn("Not enough students to print in synchronized mode, size={}", students.size());
        }

        // первые два имени — в основном потоке
        printIfExistsSync(students, 0);
        printIfExistsSync(students, 1);

        // 3 и 4 — в одном потоке
        Thread thread1 = new Thread(() -> {
            printIfExistsSync(students, 2);
            printIfExistsSync(students, 3);
        }, "student-sync-printer-1");

        // 5 и 6 — в другом потоке
        Thread thread2 = new Thread(() -> {
            printIfExistsSync(students, 4);
            printIfExistsSync(students, 5);
        }, "student-sync-printer-2");

        thread1.start();
        thread2.start();
    }

    private void printIfExists(List<Student> students, int index) {
        if (index < students.size()) {
            String name = students.get(index).getName();
            System.out.println(Thread.currentThread().getName() + " - " + name);
        }
    }

    private void printIfExistsSync(List<Student> students, int index) {
        if (index < students.size()) {
            String name = students.get(index).getName();
            printStudentNameSynchronized(name);
        }
    }

    private synchronized void printStudentNameSynchronized(String name) {
        System.out.println(Thread.currentThread().getName() + " - " + name);
    }
}