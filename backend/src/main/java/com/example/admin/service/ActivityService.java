package com.example.admin.service;

import com.example.admin.common.PageResult;
import com.example.admin.dto.activity.ActivityDetailDto;
import com.example.admin.dto.activity.ActivityDonationDto;
import com.example.admin.dto.activity.ActivityDto;
import com.example.admin.dto.activity.ActivityFavoriteDto;
import com.example.admin.dto.activity.ActivitySignupDto;
import com.example.admin.dto.activity.CreateActivityDonationRequest;
import com.example.admin.dto.activity.CreateActivityRequest;
import com.example.admin.dto.activity.MyActivityStateDto;
import com.example.admin.dto.activity.UpdateActivityRequest;

public interface ActivityService {
    PageResult<ActivityDto> listPublic(long current, long size);

    ActivityDetailDto getPublicDetail(Long id);

    PageResult<ActivityDto> listAll(long current, long size);

    ActivityDetailDto getDetail(Long id);

    ActivityDetailDto create(Long adminUserId, CreateActivityRequest request);

    ActivityDetailDto update(Long adminUserId, Long id, UpdateActivityRequest request);

    void delete(Long adminUserId, Long id);

    ActivitySignupDto signup(Long userId, Long activityId);

    ActivitySignupDto cancelSignup(Long userId, Long activityId);

    ActivitySignupDto checkIn(Long userId, Long activityId);

    ActivityDonationDto donate(Long userId, Long activityId, CreateActivityDonationRequest request);

    ActivityFavoriteDto favorite(Long userId, Long activityId);

    void unfavorite(Long userId, Long activityId);

    PageResult<ActivitySignupDto> listMySignups(Long userId, long current, long size);

    PageResult<ActivityDonationDto> listMyDonations(Long userId, long current, long size);

    PageResult<ActivityFavoriteDto> listMyFavorites(Long userId, long current, long size);

    PageResult<ActivitySignupDto> listAllSignups(long current, long size, Long activityId);

    PageResult<ActivityDonationDto> listAllDonations(long current, long size, Long activityId);

    PageResult<ActivityFavoriteDto> listAllFavorites(long current, long size, Long activityId);

    MyActivityStateDto myState(Long userId, Long activityId);
}
