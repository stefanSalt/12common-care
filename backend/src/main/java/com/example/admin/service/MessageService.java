package com.example.admin.service;

import com.example.admin.common.PageResult;
import com.example.admin.dto.message.CreateMessageRequest;
import com.example.admin.dto.message.MessageDto;
import com.example.admin.dto.message.ReplyMessageRequest;

public interface MessageService {
    MessageDto create(Long userId, CreateMessageRequest request);

    PageResult<MessageDto> listMy(Long userId, long current, long size);

    PageResult<MessageDto> listAll(long current, long size);

    MessageDto reply(Long adminUserId, Long messageId, ReplyMessageRequest request);
}

