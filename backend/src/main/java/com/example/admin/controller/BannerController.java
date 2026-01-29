package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.banner.BannerDto;
import com.example.admin.dto.banner.CreateBannerRequest;
import com.example.admin.dto.banner.UpdateBannerRequest;
import com.example.admin.security.RequiresPermission;
import com.example.admin.security.UserPrincipal;
import com.example.admin.service.BannerService;
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
@RequestMapping("/api/banners")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping("/public")
    public Result<PageResult<BannerDto>> listPublic(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(bannerService.listPublic(current, size));
    }

    @GetMapping
    @RequiresPermission("banner:list")
    public Result<PageResult<BannerDto>> listAll(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(bannerService.listAll(current, size));
    }

    @PostMapping
    @RequiresPermission("banner:manage")
    public Result<BannerDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateBannerRequest request
    ) {
        Long adminUserId = principal == null ? null : principal.userId();
        return Result.ok(bannerService.create(adminUserId, request));
    }

    @PutMapping("/{id}")
    @RequiresPermission("banner:manage")
    public Result<BannerDto> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateBannerRequest request
    ) {
        Long adminUserId = principal == null ? null : principal.userId();
        return Result.ok(bannerService.update(adminUserId, id, request));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("banner:manage")
    public Result<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Long adminUserId = principal == null ? null : principal.userId();
        bannerService.delete(adminUserId, id);
        return Result.ok(null);
    }
}

