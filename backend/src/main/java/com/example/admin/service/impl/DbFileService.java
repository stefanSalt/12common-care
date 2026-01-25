package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.PageResult;
import com.example.admin.dto.file.FileInfoDto;
import com.example.admin.entity.SysFile;
import com.example.admin.exception.BusinessException;
import com.example.admin.mapper.SysFileMapper;
import com.example.admin.service.FileService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DbFileService implements FileService {

    private final SysFileMapper fileMapper;
    private final Path storageRoot;

    public DbFileService(
            SysFileMapper fileMapper,
            @Value("${app.file.storage-path:./storage}") String storagePath
    ) {
        this.fileMapper = fileMapper;
        this.storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    @Override
    public FileInfoDto upload(Long userId, MultipartFile file, String visibility) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        String vis = normalizeVisibility(visibility);

        long id = IdWorker.getId();
        String originalName = StringUtils.getFilename(file.getOriginalFilename());
        String safeOriginalName = (originalName == null || originalName.isBlank()) ? (id + "") : originalName;

        String ext = safeExtension(safeOriginalName);
        String storedName = id + ext;

        LocalDate now = LocalDate.now();
        String relativePath = String.format(
                "%04d/%02d/%02d/%d%s",
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                id,
                ext
        );

        Path absolutePath = resolveAbsolutePath(relativePath);
        try {
            Files.createDirectories(absolutePath.getParent());
            file.transferTo(absolutePath);
        } catch (IOException e) {
            throw new RuntimeException("保存文件失败", e);
        }

        SysFile record = new SysFile();
        record.setId(id);
        record.setOriginalName(safeOriginalName);
        record.setStoredName(storedName);
        record.setPath(relativePath);
        record.setSize(file.getSize());
        record.setContentType(file.getContentType());
        record.setVisibility(vis);
        record.setUserId(userId);
        record.setDeleted(0);

        try {
            fileMapper.insert(record);
        } catch (Exception e) {
            try {
                Files.deleteIfExists(absolutePath);
            } catch (IOException ignored) {
                // best-effort cleanup
            }
            throw e;
        }

        return toDto(record);
    }

    @Override
    public PageResult<FileInfoDto> listMyFiles(Long userId, long current, long size) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }

        Page<SysFile> page = fileMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(SysFile.class)
                        .eq(SysFile::getUserId, userId)
                        .orderByDesc(SysFile::getId)
        );

        List<FileInfoDto> records = page.getRecords().stream().map(this::toDto).toList();

        PageResult<FileInfoDto> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    @Override
    public FileDownload download(Long fileId, Long requesterUserId) {
        SysFile file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException(1008, "文件不存在");
        }

        boolean isPublic = "PUBLIC".equalsIgnoreCase(file.getVisibility());
        if (!isPublic) {
            if (requesterUserId == null) {
                throw new AuthenticationCredentialsNotFoundException("未登录");
            }
            if (!requesterUserId.equals(file.getUserId())) {
                throw new AccessDeniedException("无权访问该文件");
            }
        }

        Path absolutePath = resolveAbsolutePath(file.getPath());
        if (!Files.exists(absolutePath)) {
            throw new BusinessException(1008, "文件不存在");
        }

        return new FileDownload(file.getOriginalName(), file.getSize(), file.getContentType(), absolutePath);
    }

    @Override
    public void delete(Long fileId, Long userId) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }

        SysFile file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException(1008, "文件不存在");
        }
        if (!userId.equals(file.getUserId())) {
            throw new AccessDeniedException("无权访问该文件");
        }

        file.setDeleted(1);
        fileMapper.updateById(file);

        // Best-effort: remove physical file if present.
        try {
            Files.deleteIfExists(resolveAbsolutePath(file.getPath()));
        } catch (IOException ignored) {
            // ignore
        }
    }

    private FileInfoDto toDto(SysFile file) {
        FileInfoDto dto = new FileInfoDto();
        dto.setId(file.getId());
        dto.setOriginalName(file.getOriginalName());
        dto.setSize(file.getSize());
        dto.setContentType(file.getContentType());
        dto.setVisibility(file.getVisibility());
        return dto;
    }

    private Path resolveAbsolutePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new BusinessException(1008, "文件不存在");
        }
        Path absolute = storageRoot.resolve(relativePath.replace("/", File.separator)).normalize();
        if (!absolute.startsWith(storageRoot)) {
            // Defensive: prevent path traversal if DB data is tampered with.
            throw new BusinessException(1008, "文件不存在");
        }
        return absolute;
    }

    private String normalizeVisibility(String visibility) {
        if (visibility == null || visibility.isBlank()) {
            return "PRIVATE";
        }
        String v = visibility.trim().toUpperCase(Locale.ROOT);
        if (!"PUBLIC".equals(v) && !"PRIVATE".equals(v)) {
            throw new BusinessException(400, "visibility 参数错误");
        }
        return v;
    }

    private String safeExtension(String filename) {
        if (filename == null) {
            return "";
        }
        String ext = StringUtils.getFilenameExtension(filename);
        if (ext == null || ext.isBlank()) {
            return "";
        }
        String normalized = ext.replaceAll("[^A-Za-z0-9]", "");
        if (normalized.isBlank()) {
            return "";
        }
        // Keep extension reasonably small to avoid weird long suffixes.
        if (normalized.length() > 16) {
            normalized = normalized.substring(0, 16);
        }
        return "." + normalized;
    }
}
