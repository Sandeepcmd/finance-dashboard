package com.finance.controller;

import com.finance.dto.dashboard.CategorySummary;
import com.finance.dto.dashboard.DashboardSummary;
import com.finance.dto.dashboard.MonthlyTrend;
import com.finance.dto.record.RecordResponse;
import com.finance.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard Analytics", description = "Aggregated financial insights and analytics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get financial summary",
            description = "Returns total income, total expense, net balance, and total active records")
    public ResponseEntity<DashboardSummary> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/category-summary")
    @Operation(summary = "Get category summary",
            description = "Returns aggregated amounts grouped by category")
    public ResponseEntity<List<CategorySummary>> getCategorySummary() {
        return ResponseEntity.ok(dashboardService.getCategorySummary());
    }

    @GetMapping("/monthly-trends")
    @Operation(summary = "Get monthly trends",
            description = "Returns month-by-month income vs expense comparison")
    public ResponseEntity<List<MonthlyTrend>> getMonthlyTrends() {
        return ResponseEntity.ok(dashboardService.getMonthlyTrends());
    }

    @GetMapping("/recent-activity")
    @Operation(summary = "Get recent activity",
            description = "Returns the 10 most recent financial records")
    public ResponseEntity<List<RecordResponse>> getRecentActivity() {
        return ResponseEntity.ok(dashboardService.getRecentActivity());
    }
}
