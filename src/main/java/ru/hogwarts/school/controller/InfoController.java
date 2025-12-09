package ru.hogwarts.school.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hogwarts.school.service.StudentService;

@RestController
public class InfoController {

    @Value("${server.port}")
    private String port;

    @Autowired
    private StudentService studentService;

    @GetMapping("/port")
    public String getPort() {
        return port;
    }

    @GetMapping("/fast-sum")
    public int getFastSum() {
        return studentService.getFastSum();
    }
}