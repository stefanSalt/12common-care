package com.example.admin.dto.file;

import lombok.Data;

@Data
public class FileInfoDto {
    private Long id;
    private String originalName;
    private Long size;
    private String contentType;
    /** PUBLIC / PRIVATE */
    private String visibility;
}

