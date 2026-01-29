package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.admin.dto.stats.ActivitySignupRatioDto;
import com.example.admin.dto.stats.IncomeTrendDto;
import com.example.admin.entity.BizActivity;
import com.example.admin.entity.BizActivityDonation;
import com.example.admin.entity.BizActivitySignup;
import com.example.admin.entity.BizCrowdfundingDonation;
import com.example.admin.mapper.BizActivityMapper;
import com.example.admin.mapper.BizActivityDonationMapper;
import com.example.admin.mapper.BizActivitySignupMapper;
import com.example.admin.mapper.BizCrowdfundingDonationMapper;
import com.example.admin.service.StatsService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DbStatsService implements StatsService {

    private static final String STATUS_SIGNED = "SIGNED";
    private static final String STATUS_CHECKED_IN = "CHECKED_IN";

    private final BizActivitySignupMapper signupMapper;
    private final BizActivityMapper activityMapper;
    private final BizActivityDonationMapper activityDonationMapper;
    private final BizCrowdfundingDonationMapper crowdfundingDonationMapper;

    public DbStatsService(
            BizActivitySignupMapper signupMapper,
            BizActivityMapper activityMapper,
            BizActivityDonationMapper activityDonationMapper,
            BizCrowdfundingDonationMapper crowdfundingDonationMapper
    ) {
        this.signupMapper = signupMapper;
        this.activityMapper = activityMapper;
        this.activityDonationMapper = activityDonationMapper;
        this.crowdfundingDonationMapper = crowdfundingDonationMapper;
    }

    @Override
    public List<ActivitySignupRatioDto> activitySignupRatio(int days) {
        int d = days <= 0 ? 7 : days;
        LocalDateTime start = LocalDateTime.now().minusDays(d);

        QueryWrapper<BizActivitySignup> wrapper = new QueryWrapper<>();
        wrapper.select("activity_id", "COUNT(*) AS cnt")
                .ge("signed_at", start)
                .in("status", STATUS_SIGNED, STATUS_CHECKED_IN)
                .groupBy("activity_id");

        List<Map<String, Object>> rows = signupMapper.selectMaps(wrapper);
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        List<Long> activityIds = rows.stream()
                .map(r -> toLong(r.getOrDefault("activity_id", r.get("ACTIVITY_ID"))))
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<Long, BizActivity> activityMap = new HashMap<>();
        if (!activityIds.isEmpty()) {
            for (BizActivity a : activityMapper.selectBatchIds(activityIds)) {
                activityMap.put(a.getId(), a);
            }
        }

        return rows.stream()
                .map(r -> {
                    Long activityId = toLong(r.getOrDefault("activity_id", r.get("ACTIVITY_ID")));
                    Object cntObj = r.getOrDefault("cnt", r.get("CNT"));
                    long cnt = cntObj == null ? 0L : ((Number) cntObj).longValue();

                    ActivitySignupRatioDto dto = new ActivitySignupRatioDto();
                    dto.setActivityId(activityId);
                    BizActivity activity = activityId == null ? null : activityMap.get(activityId);
                    dto.setActivityTitle(activity == null ? null : activity.getTitle());
                    dto.setSignupCount(cnt);
                    return dto;
                })
                .sorted(Comparator.comparingLong(ActivitySignupRatioDto::getSignupCount).reversed())
                .toList();
    }

    @Override
    public List<IncomeTrendDto> incomeTrend(int days) {
        int d = days <= 0 ? 7 : days;

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(d - 1L);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTimeExclusive = endDate.plusDays(1).atStartOfDay();

        Map<LocalDate, BigDecimal> sumByDate = new HashMap<>();

        List<BizActivityDonation> activityDonations = activityDonationMapper.selectList(
                Wrappers.lambdaQuery(BizActivityDonation.class)
                        .ge(BizActivityDonation::getCreatedAt, startTime)
                        .lt(BizActivityDonation::getCreatedAt, endTimeExclusive)
        );
        for (BizActivityDonation d0 : activityDonations) {
            if (d0 == null || d0.getCreatedAt() == null || d0.getAmount() == null) {
                continue;
            }
            LocalDate date = d0.getCreatedAt().toLocalDate();
            sumByDate.put(date, sumByDate.getOrDefault(date, BigDecimal.ZERO).add(d0.getAmount()));
        }

        List<BizCrowdfundingDonation> crowdfundingDonations = crowdfundingDonationMapper.selectList(
                Wrappers.lambdaQuery(BizCrowdfundingDonation.class)
                        .ge(BizCrowdfundingDonation::getCreatedAt, startTime)
                        .lt(BizCrowdfundingDonation::getCreatedAt, endTimeExclusive)
        );
        for (BizCrowdfundingDonation d1 : crowdfundingDonations) {
            if (d1 == null || d1.getCreatedAt() == null || d1.getAmount() == null) {
                continue;
            }
            LocalDate date = d1.getCreatedAt().toLocalDate();
            sumByDate.put(date, sumByDate.getOrDefault(date, BigDecimal.ZERO).add(d1.getAmount()));
        }

        List<IncomeTrendDto> out = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            IncomeTrendDto dto = new IncomeTrendDto();
            dto.setDate(date.toString());
            dto.setAmount(sumByDate.getOrDefault(date, BigDecimal.ZERO));
            out.add(dto);
        }
        return out;
    }

    private Long toLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(v.toString());
        } catch (Exception ignored) {
            return null;
        }
    }
}
