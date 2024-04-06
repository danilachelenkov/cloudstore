package ru.netology.diplomcloudstore.advice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.io.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.netology.diplomcloudstore.domain.ExceptionInfo;
import ru.netology.diplomcloudstore.exceptions.FileNotFoundInDatabaseException;
import ru.netology.diplomcloudstore.exceptions.InputParameterException;
import ru.netology.diplomcloudstore.exceptions.MultipartFileException;
import ru.netology.diplomcloudstore.exceptions.ProcessFileException;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionAdviceHandler {
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> responseEntityAccountNotExist(UsernameNotFoundException e) {
        String msg = "User not found in database.";
        return new ResponseEntity<>(
                new ExceptionInfo(msg, 666),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> responseEntityAccountNotExist(ExpiredJwtException e) {
        String msg = "Sorry! Your Jwt token is expired.";
        return new ResponseEntity<>(
                new ExceptionInfo(msg, 666),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> responseEntityBadCredentialsException(BadCredentialsException e) {
        String msg = "User can not authenticated.";
        return new ResponseEntity<>(
                new ExceptionInfo(String.format("%s : %s", msg, e.getMessage()), 400),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileNotFoundInDatabaseException.class)
    public ResponseEntity<?> responseEntityFileNotFoundInDatabaseException(FileNotFoundInDatabaseException e) {
        String msg = "File not found in the file-table";
        return new ResponseEntity<>(
                new ExceptionInfo(String.format("%s : %s", msg, e.getMessage()), 400),
                HttpStatus.BAD_REQUEST);
    }



    @ExceptionHandler(InputParameterException.class)
    public ResponseEntity<?> responseEntityInputParameterException(InputParameterException e) {
        return new ResponseEntity<>(
                new ExceptionInfo(e.getMessage(), e.getExceptionNumber()),
                HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MultipartFileException.class)
    public ResponseEntity<?> responseEntityMultipartFileException(MultipartFileException e) {
        return new ResponseEntity<>(
                new ExceptionInfo(e.getMessage(), 301),
                HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(DecodingException.class)
    public ResponseEntity<?> responseEntityDecodingException(DecodingException e) {
        return new ResponseEntity<>(
                new ExceptionInfo(e.getMessage(), 667),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> responseEntitySecurityException(SecurityException e) {
        return new ResponseEntity<>(
                new ExceptionInfo(e.getMessage(), 668),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> responseEntityIOException(IOException e) {
        return new ResponseEntity<>(
                new ExceptionInfo(e.getMessage(), 669),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ProcessFileException.class)
    public ResponseEntity<?> responseEntityProcessFileException(ProcessFileException e) {
        return new ResponseEntity<>(
                new ExceptionInfo(e.getMessage(), e.getExceptionNumber()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
