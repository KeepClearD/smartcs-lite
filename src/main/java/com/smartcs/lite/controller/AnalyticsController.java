package com.smartcs.lite.controller;

import com.smartcs.lite.common.Result;
import com.smartcs.lite.interceptor.TenantContext;
import com.smartcs.lite.model.dto.AnalyticsVO;
import com.smartcs.lite.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    public Result<AnalyticsVO> overview() {
        return Result.ok(analyticsService.getOverview(TenantContext.get()));
    }
}
