package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.activity.ActivityDetailDto;
import com.example.admin.dto.activity.ActivityDonationDto;
import com.example.admin.dto.activity.ActivityDto;
import com.example.admin.dto.activity.ActivityFavoriteDto;
import com.example.admin.dto.activity.ActivitySignupDto;
import com.example.admin.dto.activity.CreateActivityDonationRequest;
import com.example.admin.dto.activity.CreateActivityRequest;
import com.example.admin.dto.activity.MyActivityStateDto;
import com.example.admin.dto.activity.UpdateActivityRequest;
import com.example.admin.security.RequiresPermission;
import com.example.admin.security.UserPrincipal;
import com.example.admin.service.ActivityService;
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
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/public")
    public Result<PageResult<ActivityDto>> listPublic(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(activityService.listPublic(current, size));
    }

    @GetMapping("/{id}/public")
    public Result<ActivityDetailDto> publicDetail(@PathVariable Long id) {
        return Result.ok(activityService.getPublicDetail(id));
    }

    @GetMapping
    @RequiresPermission("activity:list")
    public Result<PageResult<ActivityDto>> listAll(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(activityService.listAll(current, size));
    }

    @GetMapping("/{id}")
    @RequiresPermission("activity:list")
    public Result<ActivityDetailDto> detail(@PathVariable Long id) {
        return Result.ok(activityService.getDetail(id));
    }

    @PostMapping
    @RequiresPermission("activity:manage")
    public Result<ActivityDetailDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateActivityRequest request
    ) {
        Long adminUserId = principal == null ? null : principal.userId();
        return Result.ok(activityService.create(adminUserId, request));
    }

    @PutMapping("/{id}")
    @RequiresPermission("activity:manage")
    public Result<ActivityDetailDto> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateActivityRequest request
    ) {
        Long adminUserId = principal == null ? null : principal.userId();
        return Result.ok(activityService.update(adminUserId, id, request));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("activity:manage")
    public Result<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Long adminUserId = principal == null ? null : principal.userId();
        activityService.delete(adminUserId, id);
        return Result.ok(null);
    }

    @GetMapping("/my/signups")
    public Result<PageResult<ActivitySignupDto>> mySignups(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(activityService.listMySignups(userId, current, size));
    }

    @GetMapping("/my/donations")
    public Result<PageResult<ActivityDonationDto>> myDonations(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(activityService.listMyDonations(userId, current, size));
    }

    @GetMapping("/my/favorites")
    public Result<PageResult<ActivityFavoriteDto>> myFavorites(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(activityService.listMyFavorites(userId, current, size));
    }

    @GetMapping("/{id}/my-state")
    public Result<MyActivityStateDto> myState(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(activityService.myState(userId, id));
    }

    @PostMapping("/{id}/signup")
    public Result<ActivitySignupDto> signup(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(activityService.signup(userId, id));
    }

    @PutMapping("/{id}/signup/cancel")
    public Result<ActivitySignupDto> cancelSignup(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(activityService.cancelSignup(userId, id));
    }

    @PutMapping("/{id}/check-in")
    public Result<ActivitySignupDto> checkIn(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(activityService.checkIn(userId, id));
    }

    @PostMapping("/{id}/donations")
    public Result<ActivityDonationDto> donate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CreateActivityDonationRequest request
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(activityService.donate(userId, id, request));
    }

    @PostMapping("/{id}/favorite")
    public Result<ActivityFavoriteDto> favorite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(activityService.favorite(userId, id));
    }

    @DeleteMapping("/{id}/favorite")
    public Result<Void> unfavorite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Long userId = principal == null ? null : principal.userId();
        activityService.unfavorite(userId, id);
        return Result.ok(null);
    }

    @GetMapping("/signups")
    @RequiresPermission("activitySignup:list")
    public Result<PageResult<ActivitySignupDto>> listSignups(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long activityId
    ) {
        return Result.ok(activityService.listAllSignups(current, size, activityId));
    }

    @GetMapping("/donations")
    @RequiresPermission("activityDonation:list")
    public Result<PageResult<ActivityDonationDto>> listDonations(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long activityId
    ) {
        return Result.ok(activityService.listAllDonations(current, size, activityId));
    }

    @GetMapping("/favorites")
    @RequiresPermission("activityFavorite:list")
    public Result<PageResult<ActivityFavoriteDto>> listFavorites(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long activityId
    ) {
        return Result.ok(activityService.listAllFavorites(current, size, activityId));
    }
}

