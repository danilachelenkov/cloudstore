package ru.netology.diplomcloudstore.services;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.netology.diplomcloudstore.domain.FileEntityResponse;
import ru.netology.diplomcloudstore.dto.FileDataDto;
import ru.netology.diplomcloudstore.exceptions.FileNotFoundInDatabaseException;
import ru.netology.diplomcloudstore.exceptions.InputParameterException;
import ru.netology.diplomcloudstore.exceptions.ProcessFileException;

import java.util.List;

public interface Storable {
    List<FileEntityResponse> getListFiles(Integer limit) throws InputParameterException;

    byte[] get(String filename) throws FileNotFoundInDatabaseException, UsernameNotFoundException, ProcessFileException;

    void save(String filename, byte[] file) throws UsernameNotFoundException, ProcessFileException;

    void put(FileDataDto fileDataRequest) throws FileNotFoundInDatabaseException, UsernameNotFoundException, ProcessFileException;

    void delete(String filename) throws UsernameNotFoundException, FileNotFoundInDatabaseException, ProcessFileException;
}
