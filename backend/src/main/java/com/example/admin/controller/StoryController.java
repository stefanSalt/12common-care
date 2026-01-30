package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.story.CreateStoryRequest;
import com.example.admin.dto.story.StoryDetailDto;
import com.example.admin.dto.story.StoryDto;
import com.example.admin.dto.story.UpdateStoryRequest;
import com.example.admin.security.RequiresPermission;
import com.example.admin.security.UserPrincipal;
import com.example.admin.service.StoryService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @GetMapping("/public")
    public Result<PageResult<StoryDto>> listPublic(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(storyService.listPublic(current, size));
    }

    @GetMapping("/{id}/public")
    public Result<StoryDetailDto> publicDetail(@PathVariable Long id) {
        return Result.ok(storyService.publicDetail(id));
    }

    @GetMapping
    @RequiresPermission("story:list")
    public Result<PageResult<StoryDto>> listAll(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(storyService.listAll(current, size));
    }

    @GetMapping("/{id}")
    @RequiresPermission("story:list")
    public Result<StoryDetailDto> detail(@PathVariable Long id) {
        return Result.ok(storyService.detail(id));
    }

    @PostMapping
    @RequiresPermission("story:manage")
    public Result<StoryDetailDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateStoryRequest request
    ) {
        Long adminUserId = principal == null ? null : principal.userId();
        return Result.ok(storyService.create(adminUserId, request));
    }

    @PutMapping("/{id}")
    @RequiresPermission("story:manage")
    public Result<StoryDetailDto> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody UpdateStoryRequest request
    ) {
        Long adminUserId = principal == null ? null : principal.userId();
        return Result.ok(storyService.update(adminUserId, id, request));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("story:manage")
    public Result<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Long adminUserId = principal == null ? null : principal.userId();
        storyService.delete(adminUserId, id);
        return Result.ok(null);
    }
}

