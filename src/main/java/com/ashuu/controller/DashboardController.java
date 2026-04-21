package com.ashuu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ashuu.dto.DashboardStat;
import com.ashuu.service.DashboardService;

@RestController
@RequestMapping("/api/auth")
public class DashboardController {
	private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public List<DashboardStat> getDashboardStats() {
        return dashboardService.getDashboardStats();
    }
}
