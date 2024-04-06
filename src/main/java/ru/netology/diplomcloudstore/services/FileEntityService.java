package ru.netology.diplomcloudstore.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.netology.diplomcloudstore.entities.FileEntity;
import ru.netology.diplomcloudstore.entities.UserEntity;
import ru.netology.diplomcloudstore.exceptions.ProcessFileException;
import ru.netology.diplomcloudstore.processor.ExtentionCaseType;
import ru.netology.diplomcloudstore.processor.FileProcessor;
import ru.netology.diplomcloudstore.repositories.FileEntityRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileEntityService {
    private final FileEntityRepository fileEntityRepository;

    public List<FileEntity> getListFilesByUserWithLimit(UserEntity currentUser, Integer limit) {

        List<FileEntity> list = fileEntityRepository.findAllByUserAndNotDeletedWithLimit(
                currentUser.getUsername(),
                false,
                limit);

        if (list.size() == 0) {
            log.debug(String.format("FileEntityService.getListFilesByUserWithLimit: For User {%s} No one file in select query by setting limit {%s}",
                    currentUser.getUsername(),
                    limit));
        } else {
            log.debug(String.format("FileEntityService.getListFilesByUserWithLimit: For User {%s} in select query {%s} rows found by setting limit {%s}",
                    currentUser.getUsername(),
                    list.size(),
                    limit));
        }

        return list;
    }

    public void saveFileEntity(FileEntity fileEntity, EntityTypeOperation entityTypeOperation) {
        FileEntity fileRow = fileEntityRepository.save(fileEntity);

        if (fileRow.getId() > 0) {
            log.debug(String.format("FileEntityService.saveFileEntity: The row {%s} add to table successful. Field 'fullPath' = {%s}",
                    entityTypeOperation,
                    fileRow.getFullPath())
            );
        }
    }

    public Optional<FileEntity> getFileEntityByFileNameAndNotDeleted(String username, String filename, boolean deleted) throws ProcessFileException {
        return fileEntityRepository.findFirstByFileNameAndUserAndDeletedNot(
                username,
                FileProcessor.getFileName(filename, ExtentionCaseType.LOW),
                deleted);
    }

    public void deleteFileEntity(FileEntity fileEntity) {
        fileEntityRepository.delete(fileEntity);
        log.debug(String.format("FileEntityService.deleteFileEntity: The row field 'full_path' = {%s} deleted from table ", fileEntity.getFullPath()));
    }
}