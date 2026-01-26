package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.message.CreateMessageRequest;
import com.example.admin.dto.message.MessageDto;
import com.example.admin.dto.message.ReplyMessageRequest;
import com.example.admin.security.RequiresPermission;
import com.example.admin.security.UserPrincipal;
import com.example.admin.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public Result<MessageDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateMessageRequest request
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(messageService.create(userId, request));
    }

    @GetMapping("/my")
    public Result<PageResult<MessageDto>> listMy(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(messageService.listMy(userId, current, size));
    }

    @GetMapping
    @RequiresPermission("message:list")
    public Result<PageResult<MessageDto>> listAll(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(messageService.listAll(current, size));
    }

    @PutMapping("/{id}/reply")
    @RequiresPermission("message:reply")
    public Result<MessageDto> reply(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ReplyMessageRequest request
    ) {
        Long adminUserId = principal == null ? null : principal.userId();
        return Result.ok(messageService.reply(adminUserId, id, request));
    }
}

