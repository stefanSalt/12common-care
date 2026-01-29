package com.example.admin.service;

import com.example.admin.common.PageResult;
import com.example.admin.dto.banner.BannerDto;
import com.example.admin.dto.banner.CreateBannerRequest;
import com.example.admin.dto.banner.UpdateBannerRequest;

public interface BannerService {
    PageResult<BannerDto> listPublic(long current, long size);

    PageResult<BannerDto> listAll(long current, long size);

    BannerDto create(Long adminUserId, CreateBannerRequest request);

    BannerDto update(Long adminUserId, Long id, UpdateBannerRequest request);

    void delete(Long adminUserId, Long id);
}

