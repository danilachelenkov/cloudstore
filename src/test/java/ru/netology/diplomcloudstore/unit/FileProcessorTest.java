package ru.netology.diplomcloudstore.unit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.testcontainers.shaded.org.apache.commons.io.FilenameUtils;
import ru.netology.diplomcloudstore.configurations.ProjectPropertyConfiguration;
import ru.netology.diplomcloudstore.domain.FileInfo;
import ru.netology.diplomcloudstore.exceptions.ProcessFileException;
import ru.netology.diplomcloudstore.processor.ExtentionCaseType;
import ru.netology.diplomcloudstore.processor.FileProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileProcessorTest {

    private String typeFileSystem;
    private FileProcessor fileProcessor;

    @BeforeEach
    public void beforeEachTests() {
        System.out.println("Start. Initialize the instance of FileProcessor");
        fileProcessor = new FileProcessor();
        typeFileSystem = "windows";
    }

    @AfterEach
    public void afterEachTests() {
        System.out.println("Finish. De-initialize created instance of FileProcessor\r\n");
        fileProcessor = null;
    }

    @Test
    public void test_GetExtention_LowOrUpperExtention() throws ProcessFileException {
        System.out.println("\t1.1 Check a result method of getting extention by MODE = ExtentionCaseType.LOW");
        String filename = "abracadabra.JPG";
        String expected = "jpg";
        String result = FileProcessor.getExtention(filename, ExtentionCaseType.LOW);
        System.out.println(String.format("\tResult of method = %s", result));
        Assertions.assertEquals(expected, result);

        System.out.println("\t1.2 Check a result method of getting extention by MODE = ExtentionCaseType.UPPER");
        filename = "abracadabra.jpg";
        expected = "JPG";
        result = FileProcessor.getExtention(filename, ExtentionCaseType.UPPER);
        System.out.println(String.format("\tResult of method = %s", result));
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void test_GetFileNameByLowOrUpperParameter_FilenameWithLowOrUpperExtention() throws ProcessFileException {
        System.out.println("\t2.1 Check the result method of getting filename with correction extension by MODE = ExtentionCaseType.LOW");
        String filename = "chupacabra.JPG";
        String expected = "chupacabra.jpg";
        String result = FileProcessor.getFileName(filename, ExtentionCaseType.LOW);
        System.out.println(String.format("\tResult of method = %s", result));
        Assertions.assertEquals(expected, result);

        System.out.println("\t2.2 Check the result method of getting filename with correction extension by MODE = ExtentionCaseType.UPPER");
        filename = "chupacabra.jpg";
        expected = "chupacabra.JPG";
        result = FileProcessor.getFileName(filename, ExtentionCaseType.UPPER);
        System.out.println(String.format("\tResult of method = %s", result));
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void test_GenerateDeletedFileName_newDeletedFilename() {
        System.out.println("\t3.1 Check the result of generate name for deleted file by id, prefix, filename");
        int id = 78;
        String filename = "cheburashka.pdf";
        String prefix = "deleted";
        String expected = "deleted_78_cheburashka.pdf";
        String result = FileProcessor.generateDeletedFileName(id, filename, prefix);
        System.out.println(String.format("\tResult of method = %s", result));
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void test_GetSizeOfBytes_MoreZeroOrZero() throws ProcessFileException {
        System.out.println("\t4.1 Check the result calculate size of file. Size more then 0");
        Long expected = 3212L;
        String fullpath = "C:\\Programming\\Netology\\Projects\\_diplom\\diplom-cloudstore\\src\\test\\java\\ru\\netology\\diplomcloudstore\\unit\\testfiles\\test-files-size.txt";
        Long result = FileProcessor.getSizeOfBytes(fullpath);
        System.out.println(String.format("\tResult of method = %s", result));
        Assertions.assertEquals(expected, result);

        System.out.println("\t4.2 Check the result calculate size of file. File not found. Size is 0");
        expected = 0L;
        fullpath = "C:\\Programming\\Netology\\Projects\\_diplom\\diplom-cloudstore\\src\\test\\java\\ru\\netology\\diplomcloudstore\\unit\\testfiles\\1test-files-size.txt";
        result = FileProcessor.getSizeOfBytes(fullpath);
        System.out.println(String.format("\tResult of method = %s", result));
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void test_CheckFileOnDisk_TrueOrFalse() throws ProcessFileException {
        System.out.println("\t5.1 Check the result method file-check on disk by path. File found");
        String fullpath = "C:\\Programming\\Netology\\Projects\\_diplom\\diplom-cloudstore\\src\\test\\java\\ru\\netology\\diplomcloudstore\\unit\\testfiles\\test-files-size.txt";
        boolean result = FileProcessor.checkFileOnDisk(fullpath);
        System.out.println(String.format("\tResult of method = %s", result));
        Assertions.assertEquals(true, result);

        System.out.println("\t5.2 Check the result method file-check on disk by path. File NOT found");
        fullpath = "C:\\Programming\\Netology\\Projects\\_diplom\\diplom-cloudstore\\src\\test\\java\\ru\\netology\\diplomcloudstore\\unit\\testfiles\\2test-files-size.txt";
        result = FileProcessor.checkFileOnDisk(fullpath);
        System.out.println(String.format("\tResult of method = %s", result));
        Assertions.assertEquals(false, result);
    }

    @Test
    public void test_GetFile_ByPath() throws ProcessFileException {
        System.out.println("\t6.1 Check the result get-file method from disk by path. File found");
        String fullpath = "C:\\Programming\\Netology\\Projects\\_diplom\\diplom-cloudstore\\src\\test\\java\\ru\\netology\\diplomcloudstore\\unit\\testfiles\\test-files-size.txt";
        boolean result = false;

        FileInfo fileInfo = fileProcessor.get(fullpath);
        byte[] bytes = fileInfo.getFile();

        if (bytes.length > 0)
            result = true;
        System.out.println(String.format("\tResult of method = %s", bytes.length));
        Assertions.assertEquals(true, result);

        System.out.println("\t6.2 Check the exception with fullPath = null");

        Assertions.assertThrows(ProcessFileException.class, () -> {
            fileProcessor.get(null);
        });

        System.out.println("\t6.3 Check the exception with fullPath = empty");
        Assertions.assertThrows(ProcessFileException.class, () -> {
            fileProcessor.get("");
        });
    }

    @Test
    public void tets_store_byParams() throws ProcessFileException, IOException {
        System.out.println("\t7.1 Check the result store-file method to disk by Params. File stored");
        String fullpath = "C:\\Programming\\Netology\\Projects\\_diplom\\diplom-cloudstore\\src\\test\\java\\ru\\netology\\diplomcloudstore\\unit\\testfiles\\test-files-size.txt";
        boolean result = false;

        FileInfo fileInfoGet = fileProcessor.get(fullpath);
        System.out.println("\t7.1.1 Get file for store. File geted");
        fileProcessor.setFile(fileInfoGet.getFile());
        fileProcessor.setRootPath("C:\\Programming\\Netology\\Projects\\_diplom\\diplom-cloudstore\\src\\test\\java\\ru\\netology\\diplomcloudstore\\unit\\testfiles");
        fileProcessor.setFilename("test.txt");
        fileProcessor.setUserId(1L);

        System.out.println("!!!!"+typeFileSystem);
        FileInfo fileInfoStore = fileProcessor.store(typeFileSystem);
        System.out.println(String.format("\t7.1.2 Store file on disk by path %s. File stored", fileInfoStore.getFullPath()));
        if (fileInfoStore.getSize() > 0) {
            result = true;
        }

        Files.delete(Path.of(fileInfoStore.getFullPath()));
        System.out.println(String.format("\t7.1.3 Delete stored file from disk by path %s. File deleted", fileInfoStore.getFullPath()));

        Assertions.assertEquals(true, result);
    }

    @Test
    public void test_renameFile() throws ProcessFileException, IOException {
        System.out.println("\t8.1 Check the result of rename-file method. File renamed");
        String fullpath = "C:\\Programming\\Netology\\Projects\\_diplom\\diplom-cloudstore\\src\\test\\java\\ru\\netology\\diplomcloudstore\\unit\\testfiles\\test-files-size.txt";

        FileInfo fileInfoGet = fileProcessor.get(fullpath);
        System.out.println("\t8.1.1 Get file for store. File geted");
        fileProcessor.setFile(fileInfoGet.getFile());
        fileProcessor.setRootPath("C:\\Programming\\Netology\\Projects\\_diplom\\diplom-cloudstore\\src\\test\\java\\ru\\netology\\diplomcloudstore\\unit\\testfiles");
        fileProcessor.setFilename("test.txt");
        fileProcessor.setUserId(2L);

        FileInfo fileInfoStore = fileProcessor.store(typeFileSystem);
        System.out.println(String.format("\t8.1.2 Store file on disk by path %s. File stored", fileInfoStore.getFullPath()));

        String newName = "test_renamed.txt";
        String fileNewName = fileProcessor.rename(fileInfoStore.getFullPath(), fileProcessor.getFilename(), newName);
        System.out.println(String.format("\t8.1.3 Renamed file on disk by path %s. File renamed", fileInfoStore.getFullPath()));
        System.out.println(String.format("\tResult of method = %s", fileNewName));

        Files.delete(Path.of(fileNewName));
        System.out.println(String.format("\t8.1.4 Delete stored file from disk by path %s. File deleted", fileInfoStore.getFullPath()));
        Assertions.assertEquals(FilenameUtils.getName(fileNewName), newName);
    }

    @Test
    public void test_removeFile() throws ProcessFileException {
        System.out.println("\t9.1 Check the result of remove-file method. File removed");
        String fullpath = "C:\\Programming\\Netology\\Projects\\_diplom\\diplom-cloudstore\\src\\test\\java\\ru\\netology\\diplomcloudstore\\unit\\testfiles\\test-files-size.txt";

        FileInfo fileInfoGet = fileProcessor.get(fullpath);
        System.out.println("\t9.1.1 Get file for store. File geted");
        fileProcessor.setFile(fileInfoGet.getFile());
        fileProcessor.setRootPath("C:\\Programming\\Netology\\Projects\\_diplom\\diplom-cloudstore\\src\\test\\java\\ru\\netology\\diplomcloudstore\\unit\\testfiles");
        fileProcessor.setFilename("test_for_removed.txt");
        fileProcessor.setUserId(3L);

        FileInfo fileInfoStore = fileProcessor.store(typeFileSystem);
        System.out.println(String.format("\t9.1.2 Store file on disk by path %s. File stored", fileInfoStore.getFullPath()));

        fileProcessor.remove(fileInfoStore.getFullPath());
        System.out.println(String.format("\t9.1.3 Remove file on disk by path %s. File stored", fileInfoStore.getFullPath()));
        Assertions.assertThrows(ProcessFileException.class, () -> {
            fileProcessor.get(fileInfoStore.getFullPath());
        });
    }
}
