package com.example.admin.service;

import com.example.admin.common.PageResult;
import com.example.admin.dto.crowdfunding.CreateCrowdfundingDonationRequest;
import com.example.admin.dto.crowdfunding.CreateCrowdfundingProjectRequest;
import com.example.admin.dto.crowdfunding.CrowdfundingDonationDto;
import com.example.admin.dto.crowdfunding.CrowdfundingProjectDto;
import com.example.admin.dto.crowdfunding.CrowdfundingPublicDetailDto;
import com.example.admin.dto.crowdfunding.ReviewCrowdfundingProjectRequest;
import com.example.admin.dto.crowdfunding.UpdateCrowdfundingProjectRequest;

public interface CrowdfundingService {
    PageResult<CrowdfundingProjectDto> listPublic(long current, long size);

    CrowdfundingPublicDetailDto publicDetail(Long id);

    PageResult<CrowdfundingProjectDto> listAll(long current, long size);

    CrowdfundingProjectDto create(Long userId, CreateCrowdfundingProjectRequest request);

    CrowdfundingProjectDto update(Long userId, Long id, UpdateCrowdfundingProjectRequest request);

    void submit(Long userId, Long id);

    void review(Long adminUserId, Long id, ReviewCrowdfundingProjectRequest request);

    CrowdfundingDonationDto donate(Long userId, Long projectId, CreateCrowdfundingDonationRequest request);
}

