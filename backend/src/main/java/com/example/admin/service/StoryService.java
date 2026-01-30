package com.example.admin.service;

import com.example.admin.common.PageResult;
import com.example.admin.dto.story.CreateStoryRequest;
import com.example.admin.dto.story.StoryDetailDto;
import com.example.admin.dto.story.StoryDto;
import com.example.admin.dto.story.UpdateStoryRequest;

public interface StoryService {
    PageResult<StoryDto> listPublic(long current, long size);

    StoryDetailDto publicDetail(Long id);

    PageResult<StoryDto> listAll(long current, long size);

    StoryDetailDto detail(Long id);

    StoryDetailDto create(Long adminUserId, CreateStoryRequest request);

    StoryDetailDto update(Long adminUserId, Long id, UpdateStoryRequest request);

    void delete(Long adminUserId, Long id);
}

