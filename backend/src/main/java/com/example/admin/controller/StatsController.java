package com.example.admin.controller;

import com.example.admin.common.Result;
import com.example.admin.dto.stats.ActivitySignupRatioDto;
import com.example.admin.dto.stats.IncomeTrendDto;
import com.example.admin.security.RequiresPermission;
import com.example.admin.service.StatsService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/activity-signup-ratio")
    @RequiresPermission("stats:view")
    public Result<List<ActivitySignupRatioDto>> activitySignupRatio(
            @RequestParam(defaultValue = "7") int days
    ) {
        return Result.ok(statsService.activitySignupRatio(days));
    }

    @GetMapping("/income-trend")
    @RequiresPermission("stats:view")
    public Result<List<IncomeTrendDto>> incomeTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(statsService.incomeTrend(days));
    }
}
