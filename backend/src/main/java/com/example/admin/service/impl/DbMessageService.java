package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.PageResult;
import com.example.admin.dto.message.CreateMessageRequest;
import com.example.admin.dto.message.MessageDto;
import com.example.admin.dto.message.ReplyMessageRequest;
import com.example.admin.entity.SysMessage;
import com.example.admin.entity.SysUser;
import com.example.admin.exception.BusinessException;
import com.example.admin.mapper.SysMessageMapper;
import com.example.admin.mapper.SysUserMapper;
import com.example.admin.service.MessageService;
import com.example.admin.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DbMessageService implements MessageService {

    private final SysMessageMapper messageMapper;
    private final SysUserMapper userMapper;
    private final NotificationService notificationService;

    public DbMessageService(SysMessageMapper messageMapper, SysUserMapper userMapper, NotificationService notificationService) {
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public MessageDto create(Long userId, CreateMessageRequest request) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }

        SysMessage msg = new SysMessage();
        msg.setUserId(userId);
        msg.setTitle(requireNotBlank(request.getTitle(), "标题不能为空"));
        msg.setContent(requireNotBlank(request.getContent(), "留言内容不能为空"));
        msg.setContactEmail(requireNotBlank(request.getContactEmail(), "联系邮箱不能为空"));
        msg.setStatus(0);
        messageMapper.insert(msg);

        SysMessage created = messageMapper.selectById(msg.getId());
        return toDto(created, user.getUsername());
    }

    @Override
    public PageResult<MessageDto> listMy(Long userId, long current, long size) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }

        Page<SysMessage> page = messageMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(SysMessage.class)
                        .eq(SysMessage::getUserId, userId)
                        .orderByDesc(SysMessage::getId)
        );

        List<MessageDto> records = page.getRecords().stream().map(m -> toDto(m, user.getUsername())).toList();

        PageResult<MessageDto> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    @Override
    public PageResult<MessageDto> listAll(long current, long size) {
        Page<SysMessage> page = messageMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(SysMessage.class).orderByDesc(SysMessage::getId)
        );

        List<Long> userIds = page.getRecords().stream()
                .map(SysMessage::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> usernameById = userIds.isEmpty()
                ? Map.of()
                : userMapper.selectList(Wrappers.lambdaQuery(SysUser.class).in(SysUser::getId, userIds))
                        .stream()
                        .filter(u -> u.getId() != null)
                        .collect(Collectors.toMap(SysUser::getId, SysUser::getUsername, (a, b) -> a));

        List<MessageDto> records = page.getRecords().stream()
                .map(m -> toDto(m, usernameById.getOrDefault(m.getUserId(), "")))
                .toList();

        PageResult<MessageDto> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    @Override
    @Transactional
    public MessageDto reply(Long adminUserId, Long messageId, ReplyMessageRequest request) {
        if (adminUserId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (messageId == null) {
            throw new BusinessException(400, "id不能为空");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        SysMessage msg = messageMapper.selectById(messageId);
        if (msg == null) {
            throw new BusinessException(404, "留言不存在");
        }

        String replyContent = requireNotBlank(request.getReplyContent(), "回复内容不能为空");

        SysMessage update = new SysMessage();
        update.setId(messageId);
        update.setReplyContent(replyContent);
        update.setRepliedAt(LocalDateTime.now());
        update.setStatus(1);
        messageMapper.updateById(update);

        // Notify the user (per decision 5B).
        String title = "留言已回复";
        String content = "你的留言《" + safeTitle(msg.getTitle()) + "》已回复：\n" + replyContent;
        notificationService.sendToUser(msg.getUserId(), title, content, "BUSINESS");

        SysUser user = msg.getUserId() == null ? null : userMapper.selectById(msg.getUserId());
        SysMessage latest = messageMapper.selectById(messageId);
        return toDto(latest, user == null ? "" : user.getUsername());
    }

    private MessageDto toDto(SysMessage m, String username) {
        if (m == null) {
            return null;
        }
        MessageDto dto = new MessageDto();
        dto.setId(m.getId());
        dto.setUserId(m.getUserId());
        dto.setUsername(username);
        dto.setTitle(m.getTitle());
        dto.setContent(m.getContent());
        dto.setContactEmail(m.getContactEmail());
        dto.setStatus(m.getStatus() == null ? 0 : m.getStatus());
        dto.setReplyContent(m.getReplyContent());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setRepliedAt(m.getRepliedAt());
        return dto;
    }

    private String requireNotBlank(String value, String message) {
        if (value == null || value.trim().isBlank()) {
            throw new BusinessException(400, message);
        }
        return value.trim();
    }

    private String safeTitle(String title) {
        if (title == null) {
            return "";
        }
        String t = title.trim();
        return t.length() > 60 ? t.substring(0, 60) : t;
    }
}
