package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.PageResult;
import com.example.admin.dto.banner.BannerDto;
import com.example.admin.dto.banner.CreateBannerRequest;
import com.example.admin.dto.banner.UpdateBannerRequest;
import com.example.admin.entity.BizBanner;
import com.example.admin.entity.SysFile;
import com.example.admin.exception.BusinessException;
import com.example.admin.mapper.BizBannerMapper;
import com.example.admin.mapper.SysFileMapper;
import com.example.admin.service.BannerService;
import java.util.List;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DbBannerService implements BannerService {

    private final BizBannerMapper bannerMapper;
    private final SysFileMapper fileMapper;

    public DbBannerService(BizBannerMapper bannerMapper, SysFileMapper fileMapper) {
        this.bannerMapper = bannerMapper;
        this.fileMapper = fileMapper;
    }

    @Override
    public PageResult<BannerDto> listPublic(long current, long size) {
        Page<BizBanner> page = bannerMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizBanner.class)
                        .eq(BizBanner::getEnabled, 1)
                        .orderByDesc(BizBanner::getSortNo)
                        .orderByDesc(BizBanner::getId)
        );
        return toPageResult(page.getRecords(), page);
    }

    @Override
    public PageResult<BannerDto> listAll(long current, long size) {
        Page<BizBanner> page = bannerMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizBanner.class)
                        .orderByDesc(BizBanner::getSortNo)
                        .orderByDesc(BizBanner::getId)
        );
        return toPageResult(page.getRecords(), page);
    }

    @Override
    @Transactional
    public BannerDto create(Long adminUserId, CreateBannerRequest request) {
        if (adminUserId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        Long imageFileId = request.getImageFileId();
        validatePublicImage(imageFileId);

        BizBanner banner = new BizBanner();
        banner.setTitle(trimToNull(request.getTitle()));
        banner.setImageFileId(imageFileId);
        banner.setLinkUrl(trimToNull(request.getLinkUrl()));
        banner.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        banner.setEnabled(normalizeEnabled(request.getEnabled()));
        banner.setDeleted(0);

        bannerMapper.insert(banner);

        BizBanner created = bannerMapper.selectById(banner.getId());
        return toDto(created);
    }

    @Override
    @Transactional
    public BannerDto update(Long adminUserId, Long id, UpdateBannerRequest request) {
        if (adminUserId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        BizBanner existing = bannerMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "轮播图不存在");
        }

        BizBanner update = new BizBanner();
        update.setId(id);

        if (request.getTitle() != null) {
            update.setTitle(trimToNull(request.getTitle()));
        }
        if (request.getImageFileId() != null) {
            validatePublicImage(request.getImageFileId());
            update.setImageFileId(request.getImageFileId());
        }
        if (request.getLinkUrl() != null) {
            update.setLinkUrl(trimToNull(request.getLinkUrl()));
        }
        if (request.getSortNo() != null) {
            update.setSortNo(request.getSortNo());
        }
        if (request.getEnabled() != null) {
            update.setEnabled(normalizeEnabled(request.getEnabled()));
        }

        bannerMapper.updateById(update);

        BizBanner latest = bannerMapper.selectById(id);
        return toDto(latest);
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

        int deleted = bannerMapper.deleteById(id);
        if (deleted <= 0) {
            throw new BusinessException(404, "轮播图不存在");
        }
    }

    private void validatePublicImage(Long fileId) {
        if (fileId == null) {
            throw new BusinessException(400, "imageFileId不能为空");
        }
        SysFile file = fileMapper.selectById(fileId);
        if (file == null || (file.getDeleted() != null && file.getDeleted() == 1)) {
            throw new BusinessException(1008, "文件不存在");
        }
        if (!"PUBLIC".equalsIgnoreCase(file.getVisibility())) {
            throw new BusinessException(400, "轮播图图片必须为PUBLIC");
        }
    }

    private PageResult<BannerDto> toPageResult(List<BizBanner> records, Page<?> page) {
        List<BannerDto> dtos = records.stream().map(this::toDto).toList();
        PageResult<BannerDto> result = new PageResult<>();
        result.setRecords(dtos);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    private BannerDto toDto(BizBanner banner) {
        if (banner == null) {
            return null;
        }
        BannerDto dto = new BannerDto();
        dto.setId(banner.getId());
        dto.setTitle(banner.getTitle());
        dto.setImageFileId(banner.getImageFileId());
        dto.setLinkUrl(banner.getLinkUrl());
        dto.setSortNo(banner.getSortNo() == null ? 0 : banner.getSortNo());
        dto.setEnabled(banner.getEnabled() == null ? 0 : banner.getEnabled());
        dto.setCreatedAt(banner.getCreatedAt());
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

