package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.comment.CommentDto;
import com.example.admin.dto.comment.CreateCommentRequest;
import com.example.admin.security.RequiresPermission;
import com.example.admin.security.UserPrincipal;
import com.example.admin.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/public")
    public Result<PageResult<CommentDto>> listPublic(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(commentService.listPublic(targetType, targetId, current, size));
    }

    @PostMapping
    public Result<CommentDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(commentService.create(userId, request));
    }

    @GetMapping("/my")
    public Result<PageResult<CommentDto>> myComments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(commentService.listMy(userId, current, size));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMy(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Long userId = principal == null ? null : principal.userId();
        commentService.deleteMy(userId, id);
        return Result.ok(null);
    }

    @GetMapping
    @RequiresPermission("comment:list")
    public Result<PageResult<CommentDto>> listAll(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) Long userId
    ) {
        return Result.ok(commentService.listAll(current, size, targetType, targetId, userId));
    }

    @DeleteMapping("/{id}/manage")
    @RequiresPermission("comment:manage")
    public Result<Void> deleteAny(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Long adminUserId = principal == null ? null : principal.userId();
        commentService.deleteAny(adminUserId, id);
        return Result.ok(null);
    }
}

