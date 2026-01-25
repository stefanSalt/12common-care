package com.example.admin.service;

import com.example.admin.common.PageResult;
import com.example.admin.dto.file.FileInfoDto;
import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileInfoDto upload(Long userId, MultipartFile file, String visibility);

    PageResult<FileInfoDto> listMyFiles(Long userId, long current, long size);

    FileDownload download(Long fileId, Long requesterUserId);

    void delete(Long fileId, Long userId);

    record FileDownload(String originalName, Long size, String contentType, Path absolutePath) {}
}

