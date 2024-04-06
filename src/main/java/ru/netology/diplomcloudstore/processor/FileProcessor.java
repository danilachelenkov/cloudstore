package ru.netology.diplomcloudstore.processor;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import ru.netology.diplomcloudstore.domain.FileInfo;
import ru.netology.diplomcloudstore.exceptions.ProcessFileException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileProcessor implements Processiable {
    private String filename;
    private byte[] file;
    private String rootPath;
    private Long userId;


    /***
     * Метод сохранения файла на диск
     * @return FileInfo как результат сохранения файла
     * @throws ProcessFileException ошибка если файл невозможно создать
     */
    public FileInfo store(String typeFileSystem) throws ProcessFileException {
        String fPath = getFullPathString(typeFileSystem);

        if (!isCorrectPath(fPath)) {
            String msg = String.format("FileProcessor.store: The file path '%s' is not correct. The path is longer then 255 chars. Change the filename.", fPath);
            log.debug(msg);
            throw new ProcessFileException(msg, 101);
        }

        File newFile = new File(fPath);

        try {
            if (newFile.getParentFile().mkdirs()) {
                log.debug(String.format("FileProcessor.store: Catalog '%s' was create", FilenameUtils.getPath(fPath)));
            } else {
                log.debug(String.format("FileProcessor.store: Catalog '%s' exists yet", FilenameUtils.getPath(fPath)));
            }

            try (FileOutputStream fileOutputStream = new FileOutputStream(fPath)) {
                fileOutputStream.write(file);
                log.debug(String.format("FileProcessor.store: The file by path '%s' was created", fPath));
            }
        } catch (SecurityException | IOException ex) {
            String msg = String.format(
                    "FileProcessor.store: The file '%s' not written or not catalog created. Exception: %s",
                    fPath,
                    ex.getMessage()
            );
            log.error(String.format(msg));
            if (ex instanceof IOException) {
                throw new ProcessFileException(msg, 102);
            }
            throw new ProcessFileException(msg, 112);
        }
        return new FileInfo(
                fPath,
                getSizeOfBytes(fPath),
                null);
    }

    /***
     * Метод удаления файла с диска
     * @param fullPath полный путь к файлу
     * @throws ProcessFileException ошибка если файл недоступен для удаления
     */
    public void remove(String fullPath) throws ProcessFileException {
        if (fullPath == "" || fullPath == null) {
            String msg = "FileProcessor.getFileName:  The fullPath is empty or null in method argument";
            log.error(msg);
            throw new ProcessFileException(msg, 126);
        }

        Path path = Path.of(fullPath);
        try {
            Files.delete(path);
            log.debug(String.format("FileProcessor.remove: The file by path '%s' was deleted from disk", fullPath));

        } catch (IOException | SecurityException ex) {
            String msg = String.format("FileProcessor.remove: The file by path '%s' can not delete. Exception: %s", fullPath, ex.getMessage());

            if (ex instanceof IOException) {
                throw new ProcessFileException(msg, 103);
            }
            throw new ProcessFileException(msg, 111);
        }
    }

    /***
     * Метод приведение расширения в наименовании файла к нижнему регистру
     * @param filename наименование файла
     * @return приведенное расширение к нижнему регистру и имя файла.
     */
    public static String getFileName(String filename, ExtentionCaseType extentionCaseType) throws ProcessFileException {
        if (filename == "" || filename == null) {
            String msg = "FileProcessor.getFileName:  The filename is empty or null in method argument";
            log.error(msg);
            throw new ProcessFileException(msg, 125);
        }

        return extentionCaseType.equals(ExtentionCaseType.LOW)
                ? filename.replace(
                FilenameUtils.getExtension(filename),
                FilenameUtils.getExtension(filename).toLowerCase())
                : filename.replace(
                FilenameUtils.getExtension(filename),
                FilenameUtils.getExtension(filename).toUpperCase()
        );
    }

    /***
     * Метод получения расширения по названию файла и приведения расширения к нижнему регистру
     * @param filename наименование файла
     * @return приведенное имя файла с расширением
     */
    public static String getExtention(String filename, ExtentionCaseType extentionCaseType) throws ProcessFileException {
        if (filename == null || filename == "") {
            String msg = "FileProcessor.getSizeOfBytes: Null filename in argument";
            log.error(msg);
            throw new ProcessFileException(msg, 124);
        }

        return extentionCaseType.equals(ExtentionCaseType.LOW)
                ? FilenameUtils.getExtension(filename).toLowerCase()
                : FilenameUtils.getExtension(filename).toUpperCase();
    }

    /***
     * Метод переименования файла
     * @param fullPath полный путь к файлу
     * @param oldFileName текущее имя файла
     * @param newFileName имя которое следует задать файлу
     * @return полный путь к файлу с изменным именем файла
     * @throws ProcessFileException ошибка если файл недоступен для переименования
     */
    public String rename(String fullPath, String oldFileName, String newFileName) throws ProcessFileException {
        if (fullPath == null || fullPath == "") {
            String msg = "FileProcessor.rename: Null fullPath in argument";
            log.error(msg);
            throw new ProcessFileException(msg, 122);
        }
        if (oldFileName == null || oldFileName == "") {
            String msg = "FileProcessor.rename: Null oldFileName in argument";
            log.error(msg);
            throw new ProcessFileException(msg, 123);
        }
        if (newFileName == null || newFileName == "") {
            String msg = "FileProcessor.rename: Null newFileName in argument";
            log.error(msg);
            throw new ProcessFileException(msg, 123);
        }


        File oldFile = new File(fullPath);
        String newFullPath = fullPath.replace(oldFileName, newFileName);

        try {
            oldFile.renameTo(new File(newFullPath));
            log.debug(String.format("FileProcessor.renameFile: The file was renamed with '%s' to '%s'. Path is '%s'",
                    oldFileName,
                    newFileName,
                    newFullPath));

        } catch (NullPointerException | SecurityException ex) {
            String msg = String.format(
                    "FileProcessor.renameFile: oldFileName ='%s' and newFileName='%s'. Exception: %s",
                    oldFileName,
                    newFileName,
                    ex.getMessage()
            );
            log.error(String.format(msg));

            if (ex instanceof SecurityException) {
                throw new ProcessFileException(msg, 104);
            }
            throw new ProcessFileException(msg, 110);
        }

        return newFullPath;
    }

    /***
     * Метод генерации имени файла для записи в бд, в случае если отключен режим физического удаления файла
     * @param id уникальный идентификатор записи файла
     * @param filename текушее имя файла
     * @param prefix префикс для пометки удаленного файла
     * @return имя удаленного файла для последующего изменения
     */
    public static String generateDeletedFileName(long id, String filename, String prefix) {
        return String.format("%s_%s_%s", prefix, id, filename);
    }

    /***
     * Метод генерации полного пути
     * @return полный путь к файлу
     */
    private String getFullPathString(String type) throws ProcessFileException {
        String pathTemplate = switch (type) {
            case "windows" -> String.format("%s\\%s\\%s", rootPath, userId, filename);
            case "docker" -> String.format("%s/%s/%s", rootPath, userId, filename);
            default ->
                    throw new ProcessFileException("FileProcessor.getFullPathString: Error Unknown File System Type", 127);
        };
        return pathTemplate;
    }

    /***
     * Метод выполняющий определение размера файла на диске
     * @param pathFile путь к файлу
     * @return размер файла
     */
    public static Long getSizeOfBytes(String pathFile) throws ProcessFileException {
        if (pathFile == null || pathFile == "") {
            String msg = String.format("FileProcessor.getSizeOfBytes: Null path in argument '%s'", pathFile);
            log.error(msg);
            throw new ProcessFileException(msg, 119);
        }

        File file = new File(pathFile);
        try {
            if (file.exists()) {
                return file.length();
            } else {
                return 0L;
            }
        } catch (SecurityException e) {
            String msg = String.format("FileProcessor.getSizeOfBytes: The process not have permission to file '%s'", pathFile);
            log.error(msg);
            throw new ProcessFileException(msg + " exception: " + e.getMessage(), 109);
        }
    }

    /***
     * Метод выгрузки файла по заданному пути
     * @param fullPath полный путь к файлу
     * @return FileInfo обьъект файла
     * @throws ProcessFileException если произошла ошибка при обращении к файлу
     */
    public FileInfo get(String fullPath) throws ProcessFileException {
        if (fullPath == null || fullPath == "") {
            String msg = String.format("FileProcessor.get: Null path in argument '%s'", fullPath);
            log.error(msg);
            throw new ProcessFileException(msg, 120);
        }

        if (checkFileOnDisk(fullPath)) {
            try (FileInputStream fis = new FileInputStream(fullPath)) {
                FileInfo loadFile = new FileInfo(fullPath, getSizeOfBytes(fullPath), fis.readAllBytes());
                log.debug(String.format("FileProcessor.getFile: The file by path '%s' was loaded", fullPath));
                return loadFile;
            } catch (IOException ex) {
                String msg = String.format("FileProcessor.getFile: The file by fullpath = '%s' can not read by FileInputStream.readAllBytes()", fullPath);
                log.error(msg + " exception: " + ex.getMessage());
                throw new ProcessFileException(msg, 105);
            }
        } else {
            String msg = String.format("FileProcessor.getFile.checkFileOnDisk: The file by fullpath = '%s' not found in storehouse disk", fullPath);
            log.error(msg);
            throw new ProcessFileException(msg, 106);
        }
    }

    /***
     * Метод проверки допустимой длины
     * @param path полный путь к файлу
     * @return истина \ложь
     */
    private boolean isCorrectPath(String path) {
        return path.length() < 255;
    }

    /***
     * Метод проверки существования файла
     * @param path путь к проверяемому файлу
     * @return истина \ложь
     * @throws ProcessFileException если произошла ошибка при проверки существования файла
     */
    public static boolean checkFileOnDisk(String path) throws ProcessFileException {
        if (path == null || path == "") {
            String msg = String.format("FileProcessor.checkFileOnDisk: Null path in argument '%s'", path);
            log.error(msg);
            throw new ProcessFileException(msg, 121);
        }

        try {
            File file = new File(path);
            return file.exists();
        } catch (SecurityException e) {
            String msg = String.format("FileProcessor.checkFileOnDisk: The process not have permission to file '%s'", path);
            log.error(msg);
            throw new ProcessFileException(msg + " exception: " + e.getMessage(), 108);
        }
    }
}
