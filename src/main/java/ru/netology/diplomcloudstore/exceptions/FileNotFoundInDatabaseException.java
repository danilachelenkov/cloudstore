package ru.netology.diplomcloudstore.exceptions;

public class FileNotFoundInDatabaseException extends Exception {
    private Integer numberException;
    public FileNotFoundInDatabaseException(String msg, Integer numberException) {
        super(msg);
    }

    public Integer getNumberException() {
        return numberException;
    }
}
