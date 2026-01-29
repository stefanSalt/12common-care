package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.crowdfunding.CreateCrowdfundingDonationRequest;
import com.example.admin.dto.crowdfunding.CreateCrowdfundingProjectRequest;
import com.example.admin.dto.crowdfunding.CrowdfundingDonationDto;
import com.example.admin.dto.crowdfunding.CrowdfundingProjectDto;
import com.example.admin.dto.crowdfunding.CrowdfundingPublicDetailDto;
import com.example.admin.dto.crowdfunding.ReviewCrowdfundingProjectRequest;
import com.example.admin.dto.crowdfunding.UpdateCrowdfundingProjectRequest;
import com.example.admin.security.RequiresPermission;
import com.example.admin.security.UserPrincipal;
import com.example.admin.service.CrowdfundingService;
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
@RequestMapping("/api/crowdfunding")
public class CrowdfundingController {

    private final CrowdfundingService crowdfundingService;

    public CrowdfundingController(CrowdfundingService crowdfundingService) {
        this.crowdfundingService = crowdfundingService;
    }

    @GetMapping("/public")
    public Result<PageResult<CrowdfundingProjectDto>> listPublic(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(crowdfundingService.listPublic(current, size));
    }

    @GetMapping("/{id}/public")
    public Result<CrowdfundingPublicDetailDto> publicDetail(@PathVariable Long id) {
        return Result.ok(crowdfundingService.publicDetail(id));
    }

    @GetMapping
    @RequiresPermission("crowdfunding:list")
    public Result<PageResult<CrowdfundingProjectDto>> listAll(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(crowdfundingService.listAll(current, size));
    }

    @PostMapping
    public Result<CrowdfundingProjectDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateCrowdfundingProjectRequest request
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(crowdfundingService.create(userId, request));
    }

    @PutMapping("/{id}")
    public Result<CrowdfundingProjectDto> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody UpdateCrowdfundingProjectRequest request
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(crowdfundingService.update(userId, id, request));
    }

    @PutMapping("/{id}/submit")
    public Result<Void> submit(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Long userId = principal == null ? null : principal.userId();
        crowdfundingService.submit(userId, id);
        return Result.ok(null);
    }

    @PutMapping("/{id}/review")
    @RequiresPermission("crowdfunding:review")
    public Result<Void> review(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ReviewCrowdfundingProjectRequest request
    ) {
        Long adminUserId = principal == null ? null : principal.userId();
        crowdfundingService.review(adminUserId, id, request);
        return Result.ok(null);
    }

    @PostMapping("/{id}/donations")
    public Result<CrowdfundingDonationDto> donate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CreateCrowdfundingDonationRequest request
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(crowdfundingService.donate(userId, id, request));
    }
}

