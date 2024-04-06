package ru.netology.diplomcloudstore.services;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.diplomcloudstore.domain.*;
import ru.netology.diplomcloudstore.dto.FileDataDto;
import ru.netology.diplomcloudstore.entities.FileEntity;
import ru.netology.diplomcloudstore.entities.UserEntity;
import ru.netology.diplomcloudstore.exceptions.FileNotFoundInDatabaseException;
import ru.netology.diplomcloudstore.exceptions.ProcessFileException;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaService implements Storable {
    private final ProgramSettings programSettings;
    private final CurrentUserService currentUserService;
    private final FileStorageService fileStorageService;
    private final FileEntityService fileEntityService;

    @Transactional(
            readOnly = true
    )
    public List<FileEntityResponse> getListFiles(Integer limit) {
        UserEntity user = currentUserService.getCurrentUser();
        List<FileEntity> listFileEntity = fileEntityService.getListFilesByUserWithLimit(user, limit);

        log.debug(String.format("MediaService.getListFiles: {%s} rows found for user {%s} by limit {%s}",
                listFileEntity.size(),
                user.getUsername(),
                limit));

        return listFileEntity.stream()
                .map(
                        (x) -> new FileEntityResponse(x.getFileName(), x.getSize())
                )
                .collect(Collectors.toList());
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = ProcessFileException.class
    )
    public void save(@NotNull String filename, @NotNull byte[] file) throws ProcessFileException {
        UserEntity user = currentUserService.getCurrentUser();
        Optional<FileEntity> fileInTable = fileEntityService.getFileEntityByFileNameAndNotDeleted(
                user.getUsername(),
                filename,
                false);

        if (fileInTable.isPresent()) {
            log.debug(String.format("MediaService.save.isPresent: The file {%s} exists yet.", fileInTable.get().getFullPath()));

            fileStorageService.removeFileFromStorage(fileInTable.get().getFullPath(), user);
            log.debug(String.format("MediaService.save.removeFileFromStorage: The file {%s} was removed.", fileInTable.get().getFullPath()));

            FileEntity fileStoreNew = fileStorageService.storeFile(filename, file, user);
            log.debug(String.format("MediaService.save.storeFile: The file {%s} was re-stored.", fileInTable.get().getFullPath()));

            fileInTable.get().setSize(fileStoreNew.getSize());
            fileInTable.get().setUpdatedAt(new Date());

            fileEntityService.saveFileEntity(fileInTable.get(), EntityTypeOperation.SAVE);
            log.debug(String.format("MediaService.save.saveFileEntity: The row of file {%s} was re-saved.", fileInTable.get().getFullPath()));

        } else {
            FileEntity fileEntity = fileStorageService.storeFile(filename, file, user);
            log.debug(String.format("MediaService.save.storeFile: The file {%s} was stored.", fileEntity.getFullPath()));

            fileEntityService.saveFileEntity(fileEntity, EntityTypeOperation.SAVE);
            log.debug(String.format("MediaService.save.saveFileEntity: The row of file {%s} was saved.", fileEntity.getFullPath()));
        }
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED
    )
    public byte[] get(@NotNull String filename) throws FileNotFoundInDatabaseException, UsernameNotFoundException, ProcessFileException {

        UserEntity user = currentUserService.getCurrentUser();
        Optional<FileEntity> fileEntity = fileEntityService.getFileEntityByFileNameAndNotDeleted(user.getUsername(), filename, false);

        if (fileEntity.isPresent()) {
            try {
                FileInfo fileInfo = fileStorageService.getFile(fileEntity.get().getFullPath());
                log.debug(String.format("MediaService.get: The file {%s} byte size {%s} was load successful", fileInfo.getFullPath(), fileInfo.getSize()));

                return fileInfo.getFile();
            } catch (ProcessFileException e) {
                log.error(String.format("MediaService.get: The file {%s} is broken. Check the path or file on storage", fileEntity.get().getFullPath()));

                fileEntity.get().setBroken(true);
                fileEntityService.saveFileEntity(fileEntity.get(), EntityTypeOperation.SAVE);
                log.error(String.format("MediaService.get: The file {%s} was marked as broken in table", fileEntity.get().getFullPath()));

                throw new ProcessFileException(e.getMessage(), e.getExceptionNumber());
            }
        } else {
            String msg = String.format("MediaService.get: The file '%s' for user '%s' not found in Database", filename, user.getUsername());
            log.error(msg);
            throw new FileNotFoundInDatabaseException(msg, 201);
        }
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = {ProcessFileException.class, FileNotFoundInDatabaseException.class}
    )
    public void put(@NotNull FileDataDto fileDataRequest) throws FileNotFoundInDatabaseException, ProcessFileException {

        UserEntity user = currentUserService.getCurrentUser();
        Optional<FileEntity> fileEntity = fileEntityService.getFileEntityByFileNameAndNotDeleted(user.getUsername(), fileDataRequest.getOldName(), false);

        if (fileEntity.isPresent()) {

            String fullPathNewFile = fileStorageService.renameFileOnStorage(fileEntity.get().getFullPath(),
                    fileEntity.get().getFileName(),
                    fileDataRequest.getNewName(),
                    0L);

            fileEntity.get().setFileName(fileDataRequest.getNewName());
            fileEntity.get().setFullPath(fullPathNewFile);
            fileEntity.get().setUpdatedAt(new Date());

            fileEntityService.saveFileEntity(fileEntity.get(), EntityTypeOperation.UPDATE);
        } else {
            String msg = String.format("MediaService.put: The file '%s' for user '%s' not found in Database", fileDataRequest.getOldName(), user.getUsername());
            log.error(msg);
            throw new FileNotFoundInDatabaseException(msg, 202);
        }
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = {ProcessFileException.class, FileNotFoundInDatabaseException.class}
    )
    public void delete(@NotNull String filename) throws FileNotFoundInDatabaseException, ProcessFileException {

        UserEntity user = currentUserService.getCurrentUser();
        Optional<FileEntity> fileEntity = fileEntityService.getFileEntityByFileNameAndNotDeleted(user.getUsername(), filename, false);

        if (fileEntity.isPresent()) {
            if (programSettings.isDoDelete()) {
                    fileStorageService.removeFileFromStorage(fileEntity.get().getFullPath(), user);
                    fileEntityService.deleteFileEntity(fileEntity.get());
                    log.debug(String.format("MediaService.delete: The file {'%s'} for user {'%s'} was physical deleted from storage and table", fileEntity.get().getFullPath(),user.getUsername()));
            } else {
                String fullPath = fileStorageService.renameFileOnStorage(
                        fileEntity.get().getFullPath(),
                        fileEntity.get().getFileName(),
                        null,
                        fileEntity.get().getId()
                );

                log.debug(String.format("MediaService.delete: The file {'%s'} for user {'%s'} was virtual delete. File renamed to {%s}",
                        fileEntity.get().getFullPath(),
                        user.getUsername(),
                        fullPath));

                fileEntity.get().setDeleted(true);
                fileEntity.get().setFileName(new File(fullPath).getName());
                fileEntity.get().setFullPath(fullPath);
                fileEntityService.saveFileEntity(fileEntity.get(), EntityTypeOperation.DELETE);
            }
        } else {
            String msg = String.format("MediaService.delete: The file {'%s'} for user {'%s'} not found in database", filename, user.getUsername());
            log.error(msg);
            throw new FileNotFoundInDatabaseException(msg, 203);
        }
    }


}
