package ru.netology.diplomcloudstore.exceptions;

import jakarta.persistence.criteria.CriteriaBuilder;

public class InputParameterException extends Exception {
    private Integer exceptionNumber;

    public InputParameterException(String msg, Integer number) {
        super(msg);
        this.exceptionNumber = number;
    }

    public Integer getExceptionNumber() {
        return exceptionNumber;
    }
}
