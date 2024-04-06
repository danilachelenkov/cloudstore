package ru.netology.diplomcloudstore.exceptions;

public class NotFoundSettingInDatabaseException extends Exception{
    public NotFoundSettingInDatabaseException(String msg){
        super(msg);
    }
}