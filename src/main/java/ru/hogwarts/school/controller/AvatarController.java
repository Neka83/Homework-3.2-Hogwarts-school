package ru.hogwarts.school.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.service.AvatarService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/avatars")
public class AvatarController {

    private final AvatarService avatarService;

    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    // POST: загрузка аватара
    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAvatar(@PathVariable Long id, @RequestParam("avatar") MultipartFile file) throws IOException {
        if (file.getSize() > 1024 * 300) {
            return ResponseEntity.badRequest().body("File is too big");
        }
        avatarService.uploadAvatar(id, file);
        return ResponseEntity.ok("Avatar uploaded successfully");
    }

    // GET: получить аватар из БД
    @GetMapping("/{id}/from-db")
    public ResponseEntity<byte[]> getAvatarFromDB(@PathVariable Long id) {
        Avatar avatar = avatarService.getAvatarFromDB(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(avatar.getMediaType()));
        headers.setContentLength(avatar.getData().length);

        return ResponseEntity.ok().headers(headers).body(avatar.getData());
    }

    // GET: получить аватар с диска
    @GetMapping("/{id}/from-file")
    public void getAvatarFromFile(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Avatar avatar = avatarService.getAvatarFromDB(id);
        Path path = Path.of(avatar.getFilePath());

        response.setStatus(200);
        response.setContentType(avatar.getMediaType());
        response.setContentLength((int) avatar.getFileSize());

        try (InputStream is = Files.newInputStream(path);
             OutputStream os = response.getOutputStream()) {
            is.transferTo(os);
        }
    }
}