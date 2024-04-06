package ru.netology.diplomcloudstore.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FileInfo {
    private String fullPath;
    private Long size;
    private byte[] file;
}
