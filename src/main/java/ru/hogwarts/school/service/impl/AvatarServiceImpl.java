package ru.hogwarts.school.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.dto.AvatarDto;
import ru.hogwarts.school.mapper.AvatarMapper;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;
import ru.hogwarts.school.service.AvatarService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service

public class AvatarServiceImpl implements AvatarService {
    @Value("${path.to.avatars.folder}")
    private String avatarsDir;
    private final StudentRepository studentRepository;
    private final AvatarRepository avatarRepository;
    private final int BUFFER_SIZE = 1024;
    private final AvatarMapper avatarMapper;

    private final Logger logger= LoggerFactory.getLogger(AvatarService.class);



    public AvatarServiceImpl(StudentRepository studentRepository, AvatarRepository avatarRepository, AvatarMapper avatarMapper) {
        this.studentRepository = studentRepository;
        this.avatarRepository = avatarRepository;
        this.avatarMapper = avatarMapper;
    }

    @Override

    public Avatar uploadAvatar(Long studentId, MultipartFile avatarFile) throws IOException {
        logger.info("Was invoked method for upload avatar");
        Student student = studentRepository.findById(studentId).orElseThrow(() ->
                new IllegalArgumentException("Student with id " + studentId + " is not found in database"
                ));
        Path avatarPath = saveToLocalDirectory(student, avatarFile);
        Avatar avatar = saveToDb(student, avatarPath, avatarFile);
        return avatar;

    }

    @Override
    public Avatar findAvatar(Long avatarId) {
        logger.info("Was invoked method for find avatar");
        return avatarRepository.findById(avatarId).orElseThrow(() ->
                new IllegalArgumentException("Avatar with id " + avatarId + " is not found in database"
                ));

    }

    private Path saveToLocalDirectory(Student student, MultipartFile avatarFile) throws IOException {
        logger.info("Was invoked method for save To Local Directory preview");
        Path avatarPath = Path.of(avatarsDir, "Student " + student.getId() + "." + getExtensions(avatarFile.getOriginalFilename()));
        Files.createDirectories(avatarPath.getParent());
        Files.deleteIfExists(avatarPath);
        try (
                InputStream is = avatarFile.getInputStream();
                OutputStream os = Files.newOutputStream(avatarPath, CREATE_NEW);
                BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);
                BufferedOutputStream bos = new BufferedOutputStream(os, BUFFER_SIZE);
        ) {
            bis.transferTo(bos);
        }
        return avatarPath;

    }

    private Avatar saveToDb(Student student, Path avatarPath, MultipartFile avatarFile) throws IOException {
        logger.info("Was invoked method for save To Db preview");
        Avatar avatar = getAvatarByStudent(student);
        avatar.setStudent(student);
        avatar.setFilePath(avatarPath.toString());
        avatar.setFileSize(avatarFile.getSize());
        avatar.setMediaType(avatarFile.getContentType());
        avatar.setData(avatarFile.getBytes());
        avatarRepository.save(avatar);
        return avatar;
    }

    private Avatar getAvatarByStudent(Student student) {
        logger.info("Was invoked method for get Avatar By Student preview");
        return avatarRepository.findByStudent(student).orElseGet(() -> {
            Avatar avatar = new Avatar();
            avatar.setStudent(student);
            return avatar;
        });
    }


    private String getExtensions(String fileName) {
        logger.info("Was invoked method for get extensions");
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    @Override
    public List<AvatarDto> getAllAvatars(int pageNumber, int pageSize) {
        logger.info("Was invoked method for get all avatars");
        return avatarRepository.findAll(PageRequest.of(pageNumber - 1, pageSize)).get()
                .map(avatarMapper::toDto)
                .collect(Collectors.toList());
    }

}