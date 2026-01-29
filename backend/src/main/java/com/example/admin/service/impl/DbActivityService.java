package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.example.admin.entity.BizActivity;
import com.example.admin.entity.BizActivityDonation;
import com.example.admin.entity.BizActivityFavorite;
import com.example.admin.entity.BizActivitySignup;
import com.example.admin.entity.SysFile;
import com.example.admin.entity.SysUser;
import com.example.admin.exception.BusinessException;
import com.example.admin.mapper.BizActivityDonationMapper;
import com.example.admin.mapper.BizActivityFavoriteMapper;
import com.example.admin.mapper.BizActivityMapper;
import com.example.admin.mapper.BizActivitySignupMapper;
import com.example.admin.mapper.SysFileMapper;
import com.example.admin.mapper.SysUserMapper;
import com.example.admin.service.ActivityService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DbActivityService implements ActivityService {

    private static final String STATUS_SIGNED = "SIGNED";
    private static final String STATUS_CANCELED = "CANCELED";
    private static final String STATUS_CHECKED_IN = "CHECKED_IN";

    private static final Pattern FILE_DOWNLOAD_ID_PATTERN = Pattern.compile("/api/files/(\\d+)/download");

    private final BizActivityMapper activityMapper;
    private final BizActivitySignupMapper signupMapper;
    private final BizActivityDonationMapper donationMapper;
    private final BizActivityFavoriteMapper favoriteMapper;
    private final SysFileMapper fileMapper;
    private final SysUserMapper userMapper;

    public DbActivityService(
            BizActivityMapper activityMapper,
            BizActivitySignupMapper signupMapper,
            BizActivityDonationMapper donationMapper,
            BizActivityFavoriteMapper favoriteMapper,
            SysFileMapper fileMapper,
            SysUserMapper userMapper
    ) {
        this.activityMapper = activityMapper;
        this.signupMapper = signupMapper;
        this.donationMapper = donationMapper;
        this.favoriteMapper = favoriteMapper;
        this.fileMapper = fileMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PageResult<ActivityDto> listPublic(long current, long size) {
        Page<BizActivity> page = activityMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizActivity.class)
                        .eq(BizActivity::getEnabled, 1)
                        .orderByDesc(BizActivity::getStartTime)
                        .orderByDesc(BizActivity::getId)
        );
        return toPageResult(page.getRecords(), page);
    }

    @Override
    public ActivityDetailDto getPublicDetail(Long id) {
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }
        BizActivity activity = activityMapper.selectById(id);
        if (activity == null || (activity.getEnabled() != null && activity.getEnabled() != 1)) {
            throw new BusinessException(404, "活动不存在");
        }
        return toDetailDto(activity);
    }

    @Override
    public PageResult<ActivityDto> listAll(long current, long size) {
        Page<BizActivity> page = activityMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizActivity.class)
                        .orderByDesc(BizActivity::getStartTime)
                        .orderByDesc(BizActivity::getId)
        );
        return toPageResult(page.getRecords(), page);
    }

    @Override
    public ActivityDetailDto getDetail(Long id) {
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }
        BizActivity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        return toDetailDto(activity);
    }

    @Override
    @Transactional
    public ActivityDetailDto create(Long adminUserId, CreateActivityRequest request) {
        if (adminUserId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        String title = requireTrimmed(request.getTitle(), "title不能为空");
        Long coverFileId = request.getCoverFileId();
        validatePublicFile(coverFileId, "coverFileId不能为空");

        String content = requireTrimmed(request.getContent(), "content不能为空");
        validatePublicImagesInHtml(content);

        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();
        validateTimeRange(startTime, endTime);

        Integer signupEnabled = normalizeEnabledFlag(request.getSignupEnabled());
        Integer donateEnabled = normalizeEnabledFlag(request.getDonateEnabled());

        Integer maxParticipants = request.getMaxParticipants();
        if (signupEnabled == 1) {
            if (maxParticipants == null || maxParticipants <= 0) {
                throw new BusinessException(400, "maxParticipants必须大于0");
            }
        } else {
            maxParticipants = null;
        }

        BigDecimal donationTarget = request.getDonationTarget();
        if (donationTarget != null && donationTarget.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "donationTarget不能小于0");
        }

        BizActivity activity = new BizActivity();
        activity.setTitle(title);
        activity.setCoverFileId(coverFileId);
        activity.setContent(content);
        activity.setAddress(trimToNull(request.getAddress()));
        activity.setStartTime(startTime);
        activity.setEndTime(endTime);
        activity.setSignupEnabled(signupEnabled);
        activity.setDonateEnabled(donateEnabled);
        activity.setMaxParticipants(maxParticipants);
        activity.setDonationTarget(donationTarget);
        activity.setDonatedAmount(BigDecimal.ZERO);
        activity.setEnabled(normalizeEnabledFlag(request.getEnabled()));
        activity.setDeleted(0);

        activityMapper.insert(activity);

        BizActivity created = activityMapper.selectById(activity.getId());
        return toDetailDto(created);
    }

    @Override
    @Transactional
    public ActivityDetailDto update(Long adminUserId, Long id, UpdateActivityRequest request) {
        if (adminUserId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        BizActivity existing = activityMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "活动不存在");
        }

        BizActivity update = new BizActivity();
        update.setId(id);

        if (request.getTitle() != null) {
            update.setTitle(requireTrimmed(request.getTitle(), "title不能为空"));
        }
        if (request.getCoverFileId() != null) {
            validatePublicFile(request.getCoverFileId(), "coverFileId不能为空");
            update.setCoverFileId(request.getCoverFileId());
        }
        if (request.getContent() != null) {
            String content = requireTrimmed(request.getContent(), "content不能为空");
            validatePublicImagesInHtml(content);
            update.setContent(content);
        }
        if (request.getAddress() != null) {
            update.setAddress(trimToNull(request.getAddress()));
        }

        LocalDateTime finalStartTime = request.getStartTime() != null ? request.getStartTime() : existing.getStartTime();
        LocalDateTime finalEndTime = request.getEndTime() != null ? request.getEndTime() : existing.getEndTime();
        if (request.getStartTime() != null || request.getEndTime() != null) {
            validateTimeRange(finalStartTime, finalEndTime);
            update.setStartTime(finalStartTime);
            update.setEndTime(finalEndTime);
        }

        Integer signupEnabled = request.getSignupEnabled() != null
                ? normalizeEnabledFlag(request.getSignupEnabled())
                : normalizeEnabledFlag(existing.getSignupEnabled());
        Integer donateEnabled = request.getDonateEnabled() != null
                ? normalizeEnabledFlag(request.getDonateEnabled())
                : normalizeEnabledFlag(existing.getDonateEnabled());

        Integer maxParticipants = request.getMaxParticipants() != null ? request.getMaxParticipants() : existing.getMaxParticipants();
        if (signupEnabled == 1) {
            if (maxParticipants == null || maxParticipants <= 0) {
                throw new BusinessException(400, "maxParticipants必须大于0");
            }
        } else {
            maxParticipants = null;
        }

        if (request.getSignupEnabled() != null) {
            update.setSignupEnabled(signupEnabled);
            update.setMaxParticipants(maxParticipants);
        } else if (request.getMaxParticipants() != null) {
            if (signupEnabled != 1) {
                throw new BusinessException(400, "当前活动不支持报名");
            }
            update.setMaxParticipants(maxParticipants);
        }

        if (request.getDonateEnabled() != null) {
            update.setDonateEnabled(donateEnabled);
        }

        if (request.getDonationTarget() != null) {
            BigDecimal donationTarget = request.getDonationTarget();
            if (donationTarget != null && donationTarget.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(400, "donationTarget不能小于0");
            }
            update.setDonationTarget(donationTarget);
        }

        if (request.getEnabled() != null) {
            update.setEnabled(normalizeEnabledFlag(request.getEnabled()));
        }

        activityMapper.updateById(update);

        BizActivity latest = activityMapper.selectById(id);
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

        int deleted = activityMapper.deleteById(id);
        if (deleted <= 0) {
            throw new BusinessException(404, "活动不存在");
        }
    }

    @Override
    @Transactional
    public ActivitySignupDto signup(Long userId, Long activityId) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        BizActivity activity = getEnabledActivityOrThrow(activityId);
        if (normalizeEnabledFlag(activity.getSignupEnabled()) != 1) {
            throw new BusinessException(400, "该活动不支持报名");
        }
        if (activity.getEndTime() != null && LocalDateTime.now().isAfter(activity.getEndTime())) {
            throw new BusinessException(400, "活动已结束，无法报名");
        }

        BizActivitySignup existing = signupMapper.selectOne(Wrappers.lambdaQuery(BizActivitySignup.class)
                .eq(BizActivitySignup::getActivityId, activityId)
                .eq(BizActivitySignup::getUserId, userId));
        if (existing != null) {
            if (STATUS_CANCELED.equalsIgnoreCase(existing.getStatus())) {
                ensureCapacity(activityId, activity.getMaxParticipants());
                BizActivitySignup update = new BizActivitySignup();
                update.setId(existing.getId());
                update.setStatus(STATUS_SIGNED);
                update.setSignedAt(LocalDateTime.now());
                update.setCanceledAt(null);
                update.setCheckedInAt(null);
                signupMapper.updateById(update);
                BizActivitySignup latest = signupMapper.selectById(existing.getId());
                return toSignupDto(latest, activity, null);
            }
            return toSignupDto(existing, activity, null);
        }

        ensureCapacity(activityId, activity.getMaxParticipants());

        BizActivitySignup signup = new BizActivitySignup();
        signup.setActivityId(activityId);
        signup.setUserId(userId);
        signup.setStatus(STATUS_SIGNED);
        signup.setSignedAt(LocalDateTime.now());
        signupMapper.insert(signup);

        BizActivitySignup created = signupMapper.selectById(signup.getId());
        return toSignupDto(created, activity, null);
    }

    @Override
    @Transactional
    public ActivitySignupDto cancelSignup(Long userId, Long activityId) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        BizActivity activity = getEnabledActivityOrThrow(activityId);

        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime() != null && !now.isBefore(activity.getStartTime())) {
            throw new BusinessException(400, "活动已开始，无法取消报名");
        }

        BizActivitySignup signup = signupMapper.selectOne(Wrappers.lambdaQuery(BizActivitySignup.class)
                .eq(BizActivitySignup::getActivityId, activityId)
                .eq(BizActivitySignup::getUserId, userId));
        if (signup == null) {
            throw new BusinessException(404, "未报名");
        }
        if (STATUS_CANCELED.equalsIgnoreCase(signup.getStatus())) {
            return toSignupDto(signup, activity, null);
        }
        if (STATUS_CHECKED_IN.equalsIgnoreCase(signup.getStatus())) {
            throw new BusinessException(400, "已签到，无法取消报名");
        }

        BizActivitySignup update = new BizActivitySignup();
        update.setId(signup.getId());
        update.setStatus(STATUS_CANCELED);
        update.setCanceledAt(now);
        signupMapper.updateById(update);

        BizActivitySignup latest = signupMapper.selectById(signup.getId());
        return toSignupDto(latest, activity, null);
    }

    @Override
    @Transactional
    public ActivitySignupDto checkIn(Long userId, Long activityId) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        BizActivity activity = getEnabledActivityOrThrow(activityId);

        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime() != null && now.isBefore(activity.getStartTime())) {
            throw new BusinessException(400, "未到签到时间");
        }
        if (activity.getEndTime() != null && now.isAfter(activity.getEndTime())) {
            throw new BusinessException(400, "已超过签到时间");
        }

        BizActivitySignup signup = signupMapper.selectOne(Wrappers.lambdaQuery(BizActivitySignup.class)
                .eq(BizActivitySignup::getActivityId, activityId)
                .eq(BizActivitySignup::getUserId, userId));
        if (signup == null) {
            throw new BusinessException(404, "未报名");
        }
        if (STATUS_CHECKED_IN.equalsIgnoreCase(signup.getStatus())) {
            return toSignupDto(signup, activity, null);
        }
        if (STATUS_CANCELED.equalsIgnoreCase(signup.getStatus())) {
            throw new BusinessException(400, "已取消报名，无法签到");
        }

        BizActivitySignup update = new BizActivitySignup();
        update.setId(signup.getId());
        update.setStatus(STATUS_CHECKED_IN);
        update.setCheckedInAt(now);
        signupMapper.updateById(update);

        BizActivitySignup latest = signupMapper.selectById(signup.getId());
        return toSignupDto(latest, activity, null);
    }

    @Override
    @Transactional
    public ActivityDonationDto donate(Long userId, Long activityId, CreateActivityDonationRequest request) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        BizActivity activity = getEnabledActivityOrThrow(activityId);
        if (normalizeEnabledFlag(activity.getDonateEnabled()) != 1) {
            throw new BusinessException(400, "该活动不支持捐赠");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "amount必须大于0");
        }

        BizActivityDonation donation = new BizActivityDonation();
        donation.setActivityId(activityId);
        donation.setUserId(userId);
        donation.setAmount(amount);
        donation.setRemark(trimToNull(request.getRemark()));
        donationMapper.insert(donation);

        // Maintain donated_amount for progress visualization.
        UpdateWrapper<BizActivity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", activityId);
        updateWrapper.setSql("donated_amount = donated_amount + " + amount);
        activityMapper.update(null, updateWrapper);

        BizActivityDonation created = donationMapper.selectById(donation.getId());
        return toDonationDto(created, activity, null);
    }

    @Override
    @Transactional
    public ActivityFavoriteDto favorite(Long userId, Long activityId) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        BizActivity activity = getEnabledActivityOrThrow(activityId);

        BizActivityFavorite existing = favoriteMapper.selectOne(Wrappers.lambdaQuery(BizActivityFavorite.class)
                .eq(BizActivityFavorite::getActivityId, activityId)
                .eq(BizActivityFavorite::getUserId, userId));
        if (existing != null) {
            return toFavoriteDto(existing, activity, null);
        }

        BizActivityFavorite favorite = new BizActivityFavorite();
        favorite.setActivityId(activityId);
        favorite.setUserId(userId);
        favoriteMapper.insert(favorite);

        BizActivityFavorite created = favoriteMapper.selectById(favorite.getId());
        return toFavoriteDto(created, activity, null);
    }

    @Override
    @Transactional
    public void unfavorite(Long userId, Long activityId) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (activityId == null) {
            throw new BusinessException(400, "activityId不能为空");
        }

        favoriteMapper.delete(Wrappers.lambdaQuery(BizActivityFavorite.class)
                .eq(BizActivityFavorite::getActivityId, activityId)
                .eq(BizActivityFavorite::getUserId, userId));
    }

    @Override
    public PageResult<ActivitySignupDto> listMySignups(Long userId, long current, long size) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        Page<BizActivitySignup> page = signupMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizActivitySignup.class)
                        .eq(BizActivitySignup::getUserId, userId)
                        .orderByDesc(BizActivitySignup::getSignedAt)
                        .orderByDesc(BizActivitySignup::getId)
        );
        return toSignupPageResult(page.getRecords(), page, true);
    }

    @Override
    public PageResult<ActivityDonationDto> listMyDonations(Long userId, long current, long size) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        Page<BizActivityDonation> page = donationMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizActivityDonation.class)
                        .eq(BizActivityDonation::getUserId, userId)
                        .orderByDesc(BizActivityDonation::getCreatedAt)
                        .orderByDesc(BizActivityDonation::getId)
        );
        return toDonationPageResult(page.getRecords(), page, true);
    }

    @Override
    public PageResult<ActivityFavoriteDto> listMyFavorites(Long userId, long current, long size) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        Page<BizActivityFavorite> page = favoriteMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizActivityFavorite.class)
                        .eq(BizActivityFavorite::getUserId, userId)
                        .orderByDesc(BizActivityFavorite::getCreatedAt)
                        .orderByDesc(BizActivityFavorite::getId)
        );
        return toFavoritePageResult(page.getRecords(), page, true);
    }

    @Override
    public PageResult<ActivitySignupDto> listAllSignups(long current, long size, Long activityId) {
        Page<BizActivitySignup> page = signupMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizActivitySignup.class)
                        .eq(activityId != null, BizActivitySignup::getActivityId, activityId)
                        .orderByDesc(BizActivitySignup::getSignedAt)
                        .orderByDesc(BizActivitySignup::getId)
        );
        return toSignupPageResult(page.getRecords(), page, false);
    }

    @Override
    public PageResult<ActivityDonationDto> listAllDonations(long current, long size, Long activityId) {
        Page<BizActivityDonation> page = donationMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizActivityDonation.class)
                        .eq(activityId != null, BizActivityDonation::getActivityId, activityId)
                        .orderByDesc(BizActivityDonation::getCreatedAt)
                        .orderByDesc(BizActivityDonation::getId)
        );
        return toDonationPageResult(page.getRecords(), page, false);
    }

    @Override
    public PageResult<ActivityFavoriteDto> listAllFavorites(long current, long size, Long activityId) {
        Page<BizActivityFavorite> page = favoriteMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizActivityFavorite.class)
                        .eq(activityId != null, BizActivityFavorite::getActivityId, activityId)
                        .orderByDesc(BizActivityFavorite::getCreatedAt)
                        .orderByDesc(BizActivityFavorite::getId)
        );
        return toFavoritePageResult(page.getRecords(), page, false);
    }

    @Override
    public MyActivityStateDto myState(Long userId, Long activityId) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (activityId == null) {
            throw new BusinessException(400, "activityId不能为空");
        }

        BizActivitySignup signup = signupMapper.selectOne(Wrappers.lambdaQuery(BizActivitySignup.class)
                .eq(BizActivitySignup::getActivityId, activityId)
                .eq(BizActivitySignup::getUserId, userId));
        boolean favorited = favoriteMapper.selectCount(Wrappers.lambdaQuery(BizActivityFavorite.class)
                .eq(BizActivityFavorite::getActivityId, activityId)
                .eq(BizActivityFavorite::getUserId, userId)) > 0;

        MyActivityStateDto dto = new MyActivityStateDto();
        dto.setSignupStatus(signup == null ? "NONE" : normalizeStatus(signup.getStatus()));
        dto.setFavorited(favorited);
        return dto;
    }

    private PageResult<ActivityDto> toPageResult(List<BizActivity> records, Page<?> page) {
        List<ActivityDto> dtos = records.stream().map(this::toDto).toList();
        PageResult<ActivityDto> result = new PageResult<>();
        result.setRecords(dtos);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    private PageResult<ActivitySignupDto> toSignupPageResult(List<BizActivitySignup> records, Page<?> page, boolean forMy) {
        Map<Long, BizActivity> activities = loadActivities(records.stream().map(BizActivitySignup::getActivityId).toList());
        Map<Long, SysUser> users = forMy
                ? Map.of()
                : loadUsers(records.stream().map(BizActivitySignup::getUserId).toList());

        List<ActivitySignupDto> dtos = records.stream()
                .map(r -> toSignupDto(r, activities.get(r.getActivityId()), users.get(r.getUserId())))
                .toList();

        PageResult<ActivitySignupDto> result = new PageResult<>();
        result.setRecords(dtos);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    private PageResult<ActivityDonationDto> toDonationPageResult(List<BizActivityDonation> records, Page<?> page, boolean forMy) {
        Map<Long, BizActivity> activities = loadActivities(records.stream().map(BizActivityDonation::getActivityId).toList());
        Map<Long, SysUser> users = forMy
                ? Map.of()
                : loadUsers(records.stream().map(BizActivityDonation::getUserId).toList());

        List<ActivityDonationDto> dtos = records.stream()
                .map(r -> toDonationDto(r, activities.get(r.getActivityId()), users.get(r.getUserId())))
                .toList();

        PageResult<ActivityDonationDto> result = new PageResult<>();
        result.setRecords(dtos);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    private PageResult<ActivityFavoriteDto> toFavoritePageResult(List<BizActivityFavorite> records, Page<?> page, boolean forMy) {
        Map<Long, BizActivity> activities = loadActivities(records.stream().map(BizActivityFavorite::getActivityId).toList());
        Map<Long, SysUser> users = forMy
                ? Map.of()
                : loadUsers(records.stream().map(BizActivityFavorite::getUserId).toList());

        List<ActivityFavoriteDto> dtos = records.stream()
                .map(r -> toFavoriteDto(r, activities.get(r.getActivityId()), users.get(r.getUserId())))
                .toList();

        PageResult<ActivityFavoriteDto> result = new PageResult<>();
        result.setRecords(dtos);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    private Map<Long, BizActivity> loadActivities(List<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = activityIds.stream().distinct().toList();
        List<BizActivity> list = activityMapper.selectBatchIds(ids);
        Map<Long, BizActivity> map = new HashMap<>();
        for (BizActivity a : list) {
            map.put(a.getId(), a);
        }
        return map;
    }

    private Map<Long, SysUser> loadUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = userIds.stream().distinct().toList();
        List<SysUser> list = userMapper.selectBatchIds(ids);
        Map<Long, SysUser> map = new HashMap<>();
        for (SysUser u : list) {
            map.put(u.getId(), u);
        }
        return map;
    }

    private ActivityDto toDto(BizActivity activity) {
        if (activity == null) {
            return null;
        }
        ActivityDto dto = new ActivityDto();
        dto.setId(activity.getId());
        dto.setTitle(activity.getTitle());
        dto.setCoverFileId(activity.getCoverFileId());
        dto.setAddress(activity.getAddress());
        dto.setStartTime(activity.getStartTime());
        dto.setEndTime(activity.getEndTime());
        dto.setSignupEnabled(normalizeEnabledFlag(activity.getSignupEnabled()));
        dto.setDonateEnabled(normalizeEnabledFlag(activity.getDonateEnabled()));
        dto.setMaxParticipants(activity.getMaxParticipants());
        dto.setDonationTarget(activity.getDonationTarget());
        dto.setDonatedAmount(activity.getDonatedAmount());
        dto.setEnabled(normalizeEnabledFlag(activity.getEnabled()));
        dto.setCreatedAt(activity.getCreatedAt());
        return dto;
    }

    private ActivityDetailDto toDetailDto(BizActivity activity) {
        ActivityDetailDto dto = new ActivityDetailDto();
        ActivityDto base = toDto(activity);
        if (base != null) {
            dto.setId(base.getId());
            dto.setTitle(base.getTitle());
            dto.setCoverFileId(base.getCoverFileId());
            dto.setAddress(base.getAddress());
            dto.setStartTime(base.getStartTime());
            dto.setEndTime(base.getEndTime());
            dto.setSignupEnabled(base.getSignupEnabled());
            dto.setDonateEnabled(base.getDonateEnabled());
            dto.setMaxParticipants(base.getMaxParticipants());
            dto.setDonationTarget(base.getDonationTarget());
            dto.setDonatedAmount(base.getDonatedAmount());
            dto.setEnabled(base.getEnabled());
            dto.setCreatedAt(base.getCreatedAt());
        }
        dto.setContent(activity == null ? null : activity.getContent());
        return dto;
    }

    private ActivitySignupDto toSignupDto(BizActivitySignup signup, BizActivity activity, SysUser user) {
        if (signup == null) {
            return null;
        }
        ActivitySignupDto dto = new ActivitySignupDto();
        dto.setId(signup.getId());
        dto.setActivityId(signup.getActivityId());
        dto.setUserId(signup.getUserId());
        dto.setStatus(normalizeStatus(signup.getStatus()));
        dto.setSignedAt(signup.getSignedAt());
        dto.setCanceledAt(signup.getCanceledAt());
        dto.setCheckedInAt(signup.getCheckedInAt());
        if (activity != null) {
            dto.setActivityTitle(activity.getTitle());
            dto.setActivityCoverFileId(activity.getCoverFileId());
            dto.setActivityStartTime(activity.getStartTime());
            dto.setActivityEndTime(activity.getEndTime());
        }
        if (user != null) {
            dto.setUsername(user.getUsername());
        }
        return dto;
    }

    private ActivityDonationDto toDonationDto(BizActivityDonation donation, BizActivity activity, SysUser user) {
        if (donation == null) {
            return null;
        }
        ActivityDonationDto dto = new ActivityDonationDto();
        dto.setId(donation.getId());
        dto.setActivityId(donation.getActivityId());
        dto.setUserId(donation.getUserId());
        dto.setAmount(donation.getAmount());
        dto.setRemark(donation.getRemark());
        dto.setCreatedAt(donation.getCreatedAt());
        if (activity != null) {
            dto.setActivityTitle(activity.getTitle());
        }
        if (user != null) {
            dto.setUsername(user.getUsername());
        }
        return dto;
    }

    private ActivityFavoriteDto toFavoriteDto(BizActivityFavorite favorite, BizActivity activity, SysUser user) {
        if (favorite == null) {
            return null;
        }
        ActivityFavoriteDto dto = new ActivityFavoriteDto();
        dto.setId(favorite.getId());
        dto.setActivityId(favorite.getActivityId());
        dto.setUserId(favorite.getUserId());
        dto.setCreatedAt(favorite.getCreatedAt());
        if (activity != null) {
            dto.setActivityTitle(activity.getTitle());
            dto.setActivityCoverFileId(activity.getCoverFileId());
        }
        if (user != null) {
            dto.setUsername(user.getUsername());
        }
        return dto;
    }

    private void validatePublicFile(Long fileId, String msgWhenNull) {
        if (fileId == null) {
            throw new BusinessException(400, msgWhenNull);
        }
        SysFile file = fileMapper.selectById(fileId);
        if (file == null || (file.getDeleted() != null && file.getDeleted() == 1)) {
            throw new BusinessException(1008, "文件不存在");
        }
        if (!"PUBLIC".equalsIgnoreCase(file.getVisibility())) {
            throw new BusinessException(400, "图片必须为PUBLIC");
        }
    }

    private void validatePublicImagesInHtml(String html) {
        if (html == null || html.isBlank()) {
            return;
        }
        Matcher matcher = FILE_DOWNLOAD_ID_PATTERN.matcher(html);
        Set<Long> ids = new HashSet<>();
        while (matcher.find()) {
            try {
                ids.add(Long.parseLong(matcher.group(1)));
            } catch (Exception ignored) {
                // ignore invalid id segments
            }
        }
        for (Long id : ids) {
            validatePublicFile(id, "图片不能为空");
        }
    }

    private BizActivity getEnabledActivityOrThrow(Long activityId) {
        if (activityId == null) {
            throw new BusinessException(400, "activityId不能为空");
        }
        BizActivity activity = activityMapper.selectById(activityId);
        if (activity == null || normalizeEnabledFlag(activity.getEnabled()) != 1) {
            throw new BusinessException(404, "活动不存在");
        }
        return activity;
    }

    private void ensureCapacity(Long activityId, Integer maxParticipants) {
        if (maxParticipants == null || maxParticipants <= 0) {
            return;
        }
        Long count = signupMapper.selectCount(Wrappers.lambdaQuery(BizActivitySignup.class)
                .eq(BizActivitySignup::getActivityId, activityId)
                .in(BizActivitySignup::getStatus, STATUS_SIGNED, STATUS_CHECKED_IN));
        if (count != null && count >= maxParticipants) {
            throw new BusinessException(400, "报名人数已满");
        }
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException(400, "开始/结束时间不能为空");
        }
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException(400, "startTime必须早于endTime");
        }
    }

    private Integer normalizeEnabledFlag(Integer enabled) {
        if (enabled == null) {
            return 1;
        }
        return enabled == 0 ? 0 : 1;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return STATUS_SIGNED;
        }
        String s = status.trim().toUpperCase(Locale.ROOT);
        return switch (s) {
            case STATUS_CANCELED -> STATUS_CANCELED;
            case STATUS_CHECKED_IN -> STATUS_CHECKED_IN;
            default -> STATUS_SIGNED;
        };
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String requireTrimmed(String s, String msg) {
        String t = trimToNull(s);
        if (t == null) {
            throw new BusinessException(400, msg);
        }
        return t;
    }
}
