package ru.netology.diplomcloudstore.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class FileDataDto {
    @JsonSetter("filename")
    private String newName;
    private String oldName;
}
