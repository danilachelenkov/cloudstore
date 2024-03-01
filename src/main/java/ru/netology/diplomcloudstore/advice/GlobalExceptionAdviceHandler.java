package ru.netology.diplomcloudstore.advice;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.netology.diplomcloudstore.domain.ExceptionInfo;

@RestControllerAdvice
public class GlobalExceptionAdviceHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> responseEntityAccountNotExist(UsernameNotFoundException e) {
        String msg = "User not found in database.";
        return new ResponseEntity<>(
                new ExceptionInfo(String.join(" ", msg, e.getMessage()), 666),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> responseEntityAccountNotExist(ExpiredJwtException e) {
        String msg = "Sorry! Jwt token is expired.";
        return new ResponseEntity<>(
                new ExceptionInfo(String.join(" ", msg, e.getMessage()), 666),
                HttpStatus.UNAUTHORIZED);
    }


}
