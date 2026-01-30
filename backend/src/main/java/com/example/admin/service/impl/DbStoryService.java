package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.PageResult;
import com.example.admin.dto.story.CreateStoryRequest;
import com.example.admin.dto.story.StoryDetailDto;
import com.example.admin.dto.story.StoryDto;
import com.example.admin.dto.story.UpdateStoryRequest;
import com.example.admin.entity.BizStory;
import com.example.admin.entity.SysFile;
import com.example.admin.exception.BusinessException;
import com.example.admin.mapper.BizStoryMapper;
import com.example.admin.mapper.SysFileMapper;
import com.example.admin.service.StoryService;
import java.util.List;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DbStoryService implements StoryService {

    private final BizStoryMapper storyMapper;
    private final SysFileMapper fileMapper;

    public DbStoryService(BizStoryMapper storyMapper, SysFileMapper fileMapper) {
        this.storyMapper = storyMapper;
        this.fileMapper = fileMapper;
    }

    @Override
    public PageResult<StoryDto> listPublic(long current, long size) {
        Page<BizStory> page = storyMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizStory.class)
                        .eq(BizStory::getEnabled, 1)
                        .orderByDesc(BizStory::getCreatedAt)
                        .orderByDesc(BizStory::getId)
        );
        return toPageResult(page.getRecords(), page);
    }

    @Override
    public StoryDetailDto publicDetail(Long id) {
        BizStory story = storyMapper.selectById(id);
        if (story == null || normalizeEnabled(story.getEnabled()) != 1) {
            throw new BusinessException(404, "文章不存在");
        }
        return toDetailDto(story);
    }

    @Override
    public PageResult<StoryDto> listAll(long current, long size) {
        Page<BizStory> page = storyMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizStory.class)
                        .orderByDesc(BizStory::getCreatedAt)
                        .orderByDesc(BizStory::getId)
        );
        return toPageResult(page.getRecords(), page);
    }

    @Override
    public StoryDetailDto detail(Long id) {
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }
        BizStory story = storyMapper.selectById(id);
        if (story == null) {
            throw new BusinessException(404, "文章不存在");
        }
        return toDetailDto(story);
    }

    @Override
    @Transactional
    public StoryDetailDto create(Long adminUserId, CreateStoryRequest request) {
        if (adminUserId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        validatePublicFile(request.getCoverFileId());

        BizStory story = new BizStory();
        story.setTitle(trimToNull(request.getTitle()));
        story.setCoverFileId(request.getCoverFileId());
        story.setContent(request.getContent());
        story.setEnabled(normalizeEnabled(request.getEnabled()));
        story.setDeleted(0);

        storyMapper.insert(story);

        BizStory created = storyMapper.selectById(story.getId());
        return toDetailDto(created);
    }

    @Override
    @Transactional
    public StoryDetailDto update(Long adminUserId, Long id, UpdateStoryRequest request) {
        if (adminUserId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        BizStory existing = storyMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "文章不存在");
        }

        BizStory update = new BizStory();
        update.setId(id);

        if (request.getTitle() != null) {
            update.setTitle(trimToNull(request.getTitle()));
        }
        if (request.getCoverFileId() != null) {
            validatePublicFile(request.getCoverFileId());
            update.setCoverFileId(request.getCoverFileId());
        }
        if (request.getContent() != null) {
            update.setContent(request.getContent());
        }
        if (request.getEnabled() != null) {
            update.setEnabled(normalizeEnabled(request.getEnabled()));
        }

        storyMapper.updateById(update);

        BizStory latest = storyMapper.selectById(id);
        return toDetailDto(latest);
    }

    @Override
    @Transactional
    public void delete(Long adminUserId, Long id) {
        if (adminUserId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }

        int deleted = storyMapper.deleteById(id);
        if (deleted <= 0) {
            throw new BusinessException(404, "文章不存在");
        }
    }

    private void validatePublicFile(Long fileId) {
        if (fileId == null) {
            throw new BusinessException(400, "coverFileId不能为空");
        }
        SysFile file = fileMapper.selectById(fileId);
        if (file == null || (file.getDeleted() != null && file.getDeleted() == 1)) {
            throw new BusinessException(1008, "文件不存在");
        }
        if (!"PUBLIC".equalsIgnoreCase(file.getVisibility())) {
            throw new BusinessException(400, "封面图必须为PUBLIC");
        }
    }

    private PageResult<StoryDto> toPageResult(List<BizStory> records, Page<?> page) {
        List<StoryDto> dtos = records.stream().map(this::toDto).toList();
        PageResult<StoryDto> result = new PageResult<>();
        result.setRecords(dtos);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    private StoryDto toDto(BizStory story) {
        if (story == null) {
            return null;
        }
        StoryDto dto = new StoryDto();
        dto.setId(story.getId());
        dto.setTitle(story.getTitle());
        dto.setCoverFileId(story.getCoverFileId());
        dto.setEnabled(normalizeEnabled(story.getEnabled()));
        dto.setCreatedAt(story.getCreatedAt());
        return dto;
    }

    private StoryDetailDto toDetailDto(BizStory story) {
        if (story == null) {
            return null;
        }
        StoryDetailDto dto = new StoryDetailDto();
        dto.setId(story.getId());
        dto.setTitle(story.getTitle());
        dto.setCoverFileId(story.getCoverFileId());
        dto.setEnabled(normalizeEnabled(story.getEnabled()));
        dto.setCreatedAt(story.getCreatedAt());
        dto.setContent(story.getContent());
        return dto;
    }

    private Integer normalizeEnabled(Integer enabled) {
        if (enabled == null) {
            return 1;
        }
        return enabled == 0 ? 0 : 1;
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

