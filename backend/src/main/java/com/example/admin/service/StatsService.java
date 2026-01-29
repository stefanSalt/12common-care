package com.example.admin.service;

import com.example.admin.dto.stats.ActivitySignupRatioDto;
import com.example.admin.dto.stats.IncomeTrendDto;
import java.util.List;

public interface StatsService {
    /** Activity signup count ratio (by activity) within last N days. */
    List<ActivitySignupRatioDto> activitySignupRatio(int days);

    /** Income trend (sum of donations) within last N days. */
    List<IncomeTrendDto> incomeTrend(int days);
}
