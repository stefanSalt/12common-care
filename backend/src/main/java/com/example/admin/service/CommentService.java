package com.example.admin.service;

import com.example.admin.common.PageResult;
import com.example.admin.dto.comment.CommentDto;
import com.example.admin.dto.comment.CreateCommentRequest;

public interface CommentService {
    PageResult<CommentDto> listPublic(String targetType, Long targetId, long current, long size);

    CommentDto create(Long userId, CreateCommentRequest request);

    PageResult<CommentDto> listMy(Long userId, long current, long size);

    void deleteMy(Long userId, Long id);

    PageResult<CommentDto> listAll(long current, long size, String targetType, Long targetId, Long userId);

    void deleteAny(Long adminUserId, Long id);
}

