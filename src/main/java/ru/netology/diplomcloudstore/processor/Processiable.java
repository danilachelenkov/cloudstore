package ru.netology.diplomcloudstore.processor;

import ru.netology.diplomcloudstore.domain.FileInfo;
import ru.netology.diplomcloudstore.exceptions.ProcessFileException;

public interface Processiable {
    FileInfo store(String typeFileSystem) throws ProcessFileException;
    void remove(String fullPath) throws ProcessFileException;
    String rename(String fullPath, String oldFileName, String newFileName) throws ProcessFileException;
    FileInfo get(String fullPath) throws ProcessFileException;
}
