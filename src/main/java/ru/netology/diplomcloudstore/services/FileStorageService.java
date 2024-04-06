package ru.netology.diplomcloudstore.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.netology.diplomcloudstore.configurations.ProjectPropertyConfiguration;
import ru.netology.diplomcloudstore.domain.FileInfo;
import ru.netology.diplomcloudstore.domain.ProgramSettings;
import ru.netology.diplomcloudstore.entities.FileEntity;
import ru.netology.diplomcloudstore.entities.UserEntity;
import ru.netology.diplomcloudstore.exceptions.ProcessFileException;
import ru.netology.diplomcloudstore.processor.ExtentionCaseType;
import ru.netology.diplomcloudstore.processor.FileProcessor;
import ru.netology.diplomcloudstore.processor.Processiable;

@Service
@Slf4j
public class FileStorageService {
    private final String rootPath;
    private ProjectPropertyConfiguration projectPropertyConfiguration;

    public FileStorageService(ProgramSettings programSettings, ProjectPropertyConfiguration projectPropertyConfiguration) {
        this.projectPropertyConfiguration = projectPropertyConfiguration;
        this.rootPath = programSettings.getSettings().get("cloudDiskPath");
    }

    public FileEntity storeFile(String filename, byte[] file, UserEntity user) throws ProcessFileException {

        Processiable processor = new FileProcessor(
                FileProcessor.getFileName(filename, ExtentionCaseType.LOW),
                file,
                rootPath,
                user.getId());

        FileInfo fileInfo = processor.store(projectPropertyConfiguration.getType());
        if (fileInfo.getSize() > 0) {
            log.debug(String.format("FileStorageService.storeFile: Th file {%s} was created successful", fileInfo.getFullPath()));
        }else {
            String msg = String.format("FileStorageService.storeFile: Th file {%s} not saved to filesystem!", fileInfo.getFullPath());
            log.error(msg);
            throw new ProcessFileException(msg,6666);
        }


        return FileEntity.builder()
                .deleted(false)
                .size(fileInfo.getSize())
                .fullPath(fileInfo.getFullPath())
                .user(user)
                .fileName(FileProcessor.getFileName(filename, ExtentionCaseType.LOW))
                .extention(FileProcessor.getExtention(filename, ExtentionCaseType.LOW))
                .build();
    }

    public FileInfo getFile(String fullPath) throws ProcessFileException {
        return new FileProcessor().get(fullPath);
    }

    public boolean removeFileFromStorage(String fullPath, UserEntity user) throws ProcessFileException {
        Processiable fileProcessor = new FileProcessor();

        if (FileProcessor.checkFileOnDisk(fullPath)) {
            fileProcessor.remove(fullPath);
            return true;
        } else {
            log.error(String.format("FileStorageService.removeFileFromStorage: File {%s} is absent", fullPath));
            return false;
        }
    }


    public String renameFileOnStorage(String fullPath, String oldFile, String newFile, long idFile) throws ProcessFileException {

        if (idFile > 0) {
            newFile = FileProcessor.generateDeletedFileName(idFile, oldFile, "deleted");
            log.debug(String.format("FileStorageService.renameFileOnStorage: (Mode no-delete on) The NEW name {%s} generated ", newFile));
        }
        return new FileProcessor().rename(fullPath, oldFile, newFile);
    }
}
