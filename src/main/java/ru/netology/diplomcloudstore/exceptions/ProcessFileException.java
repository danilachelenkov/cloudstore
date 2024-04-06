package ru.netology.diplomcloudstore.exceptions;

public class ProcessFileException extends Exception{
    private Integer exceptionNumber;
    public ProcessFileException(String msg, Integer number) {
        super(msg);
        this.exceptionNumber = number;
    }

    public Integer getExceptionNumber() {
        return exceptionNumber;
    }
}
