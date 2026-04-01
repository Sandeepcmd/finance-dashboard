package com.finance.service;

import com.finance.dto.dashboard.CategorySummary;
import com.finance.dto.dashboard.DashboardSummary;
import com.finance.dto.dashboard.MonthlyTrend;
import com.finance.dto.record.RecordResponse;
import com.finance.entity.RecordType;
import com.finance.mapper.RecordMapper;
import com.finance.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;
    private final RecordMapper recordMapper;

    public DashboardSummary getSummary() {
        log.debug("Generating dashboard summary");

        BigDecimal totalIncome = recordRepository.sumByType(RecordType.INCOME);
        BigDecimal totalExpense = recordRepository.sumByType(RecordType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpense);
        long totalRecords = recordRepository.countActiveRecords();

        return DashboardSummary.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(netBalance)
                .totalRecords(totalRecords)
                .build();
    }

    public List<CategorySummary> getCategorySummary() {
        log.debug("Generating category summary");
        return recordRepository.getCategorySummary();
    }

    public List<MonthlyTrend> getMonthlyTrends() {
        log.debug("Generating monthly trends");

        List<Object[]> rawData = recordRepository.getMonthlyTrends();

        // Group by year-month, aggregate income vs expense
        Map<String, MonthlyTrend> trendMap = new LinkedHashMap<>();

        for (Object[] row : rawData) {
            RecordType type = (RecordType) row[0];
            int year = (int) row[1];
            int month = (int) row[2];
            BigDecimal sum = (BigDecimal) row[3];

            String key = year + "-" + month;

            MonthlyTrend trend = trendMap.computeIfAbsent(key, k ->
                    MonthlyTrend.builder()
                            .year(year)
                            .month(month)
                            .monthName(Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                            .totalIncome(BigDecimal.ZERO)
                            .totalExpense(BigDecimal.ZERO)
                            .netBalance(BigDecimal.ZERO)
                            .build()
            );

            if (type == RecordType.INCOME) {
                trend.setTotalIncome(sum);
            } else {
                trend.setTotalExpense(sum);
            }
            trend.setNetBalance(trend.getTotalIncome().subtract(trend.getTotalExpense()));
        }

        return new ArrayList<>(trendMap.values());
    }

    public List<RecordResponse> getRecentActivity() {
        log.debug("Fetching recent activity");
        return recordRepository.findRecentRecords(PageRequest.of(0, 10))
                .stream()
                .map(recordMapper::toResponse)
                .collect(Collectors.toList());
    }
}
