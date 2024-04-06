package ru.netology.diplomcloudstore.mock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.netology.diplomcloudstore.domain.FileInfo;
import ru.netology.diplomcloudstore.domain.ProgramSettings;
import ru.netology.diplomcloudstore.dto.FileDataDto;
import ru.netology.diplomcloudstore.entities.FileEntity;
import ru.netology.diplomcloudstore.entities.UserEntity;
import ru.netology.diplomcloudstore.exceptions.FileNotFoundInDatabaseException;
import ru.netology.diplomcloudstore.exceptions.ProcessFileException;
import ru.netology.diplomcloudstore.services.CurrentUserService;
import ru.netology.diplomcloudstore.services.FileEntityService;
import ru.netology.diplomcloudstore.services.FileStorageService;
import ru.netology.diplomcloudstore.services.MediaService;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class MediaServiceTest {
    @Mock
    FileStorageService fileStorageService;
    @Mock
    CurrentUserService currentUserService;
    @Mock
    FileEntityService fileEntityService;
    @Mock
    ProgramSettings programSettings;

    @InjectMocks
    MediaService mediaService;


    @Test
    public void getListFiles_ReturnListEntityResponse() {
        //given
        Integer expected = 2;
        UserEntity user = new UserEntity();
        Mockito.doReturn(user).when(this.currentUserService).getCurrentUser();
        Mockito.doReturn(
                List.of(
                        FileEntity.builder()
                                .fileName("test1.pdf")
                                .size(20L)
                                .deleted(false)
                                .createdAt(new Date())
                                .build(),
                        FileEntity.builder()
                                .fileName("test2.pdf")
                                .size(30L)
                                .deleted(false)
                                .createdAt(new Date())
                                .build()
                )
        ).when(this.fileEntityService).getListFilesByUserWithLimit(user, 3);
        //when
        Integer result = mediaService.getListFiles(3).size();
        //then
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void save_ReturnException() throws ProcessFileException {
        //given
        UserEntity user = new UserEntity();
        String filename = "test.pdf";
        byte[] file = new byte[10];

        FileEntity fileEntity = FileEntity.builder()
                .fileName(filename)
                .createdAt(new Date())
                .deleted(false)
                .id(345L)
                .size(3000L)
                .fullPath("c:\\345\\test.pdf")
                .build();

        Mockito.doReturn(user).when(this.currentUserService).getCurrentUser();

        Mockito.doReturn(Optional.of(fileEntity)).when(this.fileEntityService)
                .getFileEntityByFileNameAndNotDeleted(user.getUsername(), "test.pdf", false);

        //when
        Mockito.when(this.fileStorageService.storeFile(filename, file, user)).thenThrow(ProcessFileException.class);
        //then
        Assertions.assertThrows(ProcessFileException.class, () -> mediaService.save(filename, file));
    }


    @Test
    public void get_ReturnNotNullByteArray() throws ProcessFileException, FileNotFoundInDatabaseException {
        //given
        boolean expected = true;
        boolean result = false;
        UserEntity user = new UserEntity();
        String filename = "test.pdf";
        byte[] file = new byte[10];
        String fullPath = "C:\\3\\test.pdf";

        FileInfo fileInfo = FileInfo.builder()
                .file(file)
                .size(30L)
                .fullPath(fullPath)
                .build();

        Optional<FileEntity> fileEntity = Optional.of(
                FileEntity.builder()
                        .fileName(filename)
                        .createdAt(new Date())
                        .deleted(false)
                        .size(30L)
                        .user(user)
                        .fullPath(fullPath)
                        .id(28L)
                        .broken(false)
                        .build());

        Mockito.doReturn(user).when(this.currentUserService).getCurrentUser();
        Mockito.doReturn(fileEntity).when(this.fileEntityService).getFileEntityByFileNameAndNotDeleted(
                user.getUsername(),
                filename,
                false);
        Mockito.doReturn(fileInfo).when(this.fileStorageService).getFile(fullPath);
        //when
        byte[] getedFile = mediaService.get(filename);

        if (getedFile.length > 0)
            result = true;

        //then
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void put_FileNotFoundInDatabaseException() throws ProcessFileException {
        //given
        UserEntity user = new UserEntity();
        String filename = "test.pdf";
        String newFileName = "test2.pdf";
        String fullPath = "C:\\3\\test.pdf";

        FileDataDto fileDataDto = new FileDataDto();
        fileDataDto.setOldName(filename);
        fileDataDto.setNewName(newFileName);

        Optional<FileEntity> fileEntity = Optional.of(
                FileEntity.builder()
                        .fileName(filename)
                        .createdAt(new Date())
                        .deleted(false)
                        .size(30L)
                        .user(user)
                        .fullPath(fullPath)
                        .id(28L)
                        .broken(false)
                        .build());

        Mockito.doReturn(user).when(this.currentUserService).getCurrentUser();
        Mockito.doReturn(fileEntity).when(this.fileEntityService).getFileEntityByFileNameAndNotDeleted(
                user.getUsername(),
                filename,
                false);


        //when
        Mockito.when(this.fileStorageService.renameFileOnStorage(
                fullPath,
                filename,
                newFileName,
                0L
        )).thenThrow(ProcessFileException.class);

        //then
        Assertions.assertThrows(ProcessFileException.class, () -> {
            mediaService.put(fileDataDto);
        });
    }

    @Test
    public void delete_ReturnThrowException() throws ProcessFileException {
        //given
        UserEntity user = new UserEntity();
        String filename = "test.pdf";
        String fullPath = "C:\\3\\test.pdf";

        Optional<FileEntity> fileEntity = Optional.of(
                FileEntity.builder()
                        .fileName(filename)
                        .createdAt(new Date())
                        .deleted(false)
                        .size(30L)
                        .user(user)
                        .fullPath(fullPath)
                        .id(28L)
                        .broken(false)
                        .build());
        Mockito.doReturn(user).when(this.currentUserService).getCurrentUser();
        Mockito.doReturn(fileEntity).when(this.fileEntityService).getFileEntityByFileNameAndNotDeleted(
                user.getUsername(),
                filename,
                false);

        Mockito.when(this.programSettings.isDoDelete()).thenReturn(true);

        //when
        Mockito.when(this.fileStorageService.removeFileFromStorage(fullPath, user))
                .thenThrow(ProcessFileException.class);
        //then
        Assertions.assertThrows(ProcessFileException.class, () -> {
            mediaService.delete(filename);
        });
    }
}
