package com.example.admin.service;

import com.example.admin.dto.stats.ActivitySignupRatioDto;
import java.util.List;

public interface StatsService {
    /** Activity signup count ratio (by activity) within last N days. */
    List<ActivitySignupRatioDto> activitySignupRatio(int days);
}

