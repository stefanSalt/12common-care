package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.file.FileInfoDto;
import com.example.admin.security.UserPrincipal;
import com.example.admin.service.FileService;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public Result<FileInfoDto> upload(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "visibility", required = false) String visibility
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(fileService.upload(userId, file, visibility));
    }

    @GetMapping
    public Result<PageResult<FileInfoDto>> listMyFiles(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(fileService.listMyFiles(userId, current, size));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long requesterUserId = principal == null ? null : principal.userId();
        FileService.FileDownload download = fileService.download(id, requesterUserId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(download.originalName(), StandardCharsets.UTF_8)
                .build());

        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        if (download.contentType() != null && !download.contentType().isBlank()) {
            try {
                contentType = MediaType.parseMediaType(download.contentType());
            } catch (Exception ignored) {
                // fallback to octet-stream
            }
        }

        FileSystemResource resource = new FileSystemResource(download.absolutePath());
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok().headers(headers).contentType(contentType);
        if (download.size() != null && download.size() >= 0) {
            builder.contentLength(download.size());
        }
        return builder.body(resource);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal == null ? null : principal.userId();
        fileService.delete(id, userId);
        return Result.ok(null);
    }
}

