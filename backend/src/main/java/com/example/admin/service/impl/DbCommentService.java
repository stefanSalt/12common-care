package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.PageResult;
import com.example.admin.dto.comment.CommentDto;
import com.example.admin.dto.comment.CreateCommentRequest;
import com.example.admin.entity.BizComment;
import com.example.admin.entity.BizStory;
import com.example.admin.entity.SysUser;
import com.example.admin.exception.BusinessException;
import com.example.admin.mapper.BizCommentMapper;
import com.example.admin.mapper.BizStoryMapper;
import com.example.admin.mapper.SysUserMapper;
import com.example.admin.service.CommentService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DbCommentService implements CommentService {

    private static final String TARGET_TYPE_STORY = "STORY";

    private final BizCommentMapper commentMapper;
    private final BizStoryMapper storyMapper;
    private final SysUserMapper userMapper;

    public DbCommentService(BizCommentMapper commentMapper, BizStoryMapper storyMapper, SysUserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.storyMapper = storyMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PageResult<CommentDto> listPublic(String targetType, Long targetId, long current, long size) {
        String tt = normalizeTargetType(targetType);
        if (targetId == null) {
            throw new BusinessException(400, "targetId不能为空");
        }
        if (TARGET_TYPE_STORY.equals(tt)) {
            BizStory story = storyMapper.selectById(targetId);
            if (story == null || normalizeEnabled(story.getEnabled()) != 1) {
                throw new BusinessException(404, "文章不存在");
            }
        }

        Page<BizComment> page = commentMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizComment.class)
                        .eq(BizComment::getTargetType, tt)
                        .eq(BizComment::getTargetId, targetId)
                        .orderByDesc(BizComment::getCreatedAt)
                        .orderByDesc(BizComment::getId)
        );
        return toPageResult(page.getRecords(), page);
    }

    @Override
    @Transactional
    public CommentDto create(Long userId, CreateCommentRequest request) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        String tt = normalizeTargetType(request.getTargetType());
        Long targetId = request.getTargetId();
        if (targetId == null) {
            throw new BusinessException(400, "targetId不能为空");
        }

        String content = trimToNull(request.getContent());
        if (content == null) {
            throw new BusinessException(400, "content不能为空");
        }

        if (TARGET_TYPE_STORY.equals(tt)) {
            BizStory story = storyMapper.selectById(targetId);
            if (story == null || normalizeEnabled(story.getEnabled()) != 1) {
                throw new BusinessException(404, "文章不存在");
            }
        }

        BizComment comment = new BizComment();
        comment.setTargetType(tt);
        comment.setTargetId(targetId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setDeleted(0);

        commentMapper.insert(comment);

        BizComment created = commentMapper.selectById(comment.getId());
        return toDto(created, null, null);
    }

    @Override
    public PageResult<CommentDto> listMy(Long userId, long current, long size) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }

        Page<BizComment> page = commentMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizComment.class)
                        .eq(BizComment::getUserId, userId)
                        .orderByDesc(BizComment::getCreatedAt)
                        .orderByDesc(BizComment::getId)
        );
        return toPageResult(page.getRecords(), page);
    }

    @Override
    @Transactional
    public void deleteMy(Long userId, Long id) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }

        BizComment existing = commentMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "评论不存在");
        }
        if (existing.getUserId() == null || !existing.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除该评论");
        }

        int deleted = commentMapper.deleteById(id);
        if (deleted <= 0) {
            throw new BusinessException(404, "评论不存在");
        }
    }

    @Override
    public PageResult<CommentDto> listAll(long current, long size, String targetType, Long targetId, Long userId) {
        String tt = targetType == null || targetType.isBlank() ? null : normalizeTargetType(targetType);

        Page<BizComment> page = commentMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizComment.class)
                        .eq(tt != null, BizComment::getTargetType, tt)
                        .eq(targetId != null, BizComment::getTargetId, targetId)
                        .eq(userId != null, BizComment::getUserId, userId)
                        .orderByDesc(BizComment::getCreatedAt)
                        .orderByDesc(BizComment::getId)
        );
        return toPageResult(page.getRecords(), page);
    }

    @Override
    @Transactional
    public void deleteAny(Long adminUserId, Long id) {
        if (adminUserId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }
        int deleted = commentMapper.deleteById(id);
        if (deleted <= 0) {
            throw new BusinessException(404, "评论不存在");
        }
    }

    private PageResult<CommentDto> toPageResult(List<BizComment> records, Page<?> page) {
        List<Long> userIds = records.stream()
                .map(BizComment::getUserId)
                .filter(v -> v != null)
                .distinct()
                .toList();
        Map<Long, SysUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (SysUser u : userMapper.selectBatchIds(userIds)) {
                userMap.put(u.getId(), u);
            }
        }

        List<Long> storyIds = records.stream()
                .filter(r -> TARGET_TYPE_STORY.equalsIgnoreCase(trimToNull(r.getTargetType())))
                .map(BizComment::getTargetId)
                .filter(v -> v != null)
                .distinct()
                .toList();
        Map<Long, BizStory> storyMap = new HashMap<>();
        if (!storyIds.isEmpty()) {
            for (BizStory s : storyMapper.selectBatchIds(storyIds)) {
                storyMap.put(s.getId(), s);
            }
        }

        List<CommentDto> dtos = records.stream()
                .map(r -> toDto(r, userMap.get(r.getUserId()), storyMap.get(r.getTargetId())))
                .toList();

        PageResult<CommentDto> result = new PageResult<>();
        result.setRecords(dtos);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    private CommentDto toDto(BizComment comment, SysUser user, BizStory story) {
        if (comment == null) {
            return null;
        }
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setTargetType(comment.getTargetType());
        dto.setTargetId(comment.getTargetId());
        dto.setUserId(comment.getUserId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUsername(user == null ? null : user.getUsername());
        dto.setUserNickname(user == null ? null : user.getNickname());
        dto.setStoryTitle(story == null ? null : story.getTitle());
        return dto;
    }

    private String normalizeTargetType(String targetType) {
        String t = trimToNull(targetType);
        if (t == null) {
            throw new BusinessException(400, "targetType不能为空");
        }
        String tt = t.toUpperCase();
        if (!TARGET_TYPE_STORY.equals(tt)) {
            throw new BusinessException(400, "targetType不支持");
        }
        return tt;
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

