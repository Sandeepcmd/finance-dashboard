package com.finance.service;

import com.finance.dto.dashboard.CategorySummary;
import com.finance.dto.dashboard.DashboardSummary;
import com.finance.dto.dashboard.MonthlyTrend;
import com.finance.entity.RecordType;
import com.finance.mapper.RecordMapper;
import com.finance.repository.FinancialRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private FinancialRecordRepository recordRepository;
    @Mock private RecordMapper recordMapper;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Should return correct financial summary")
    void shouldReturnSummary() {
        when(recordRepository.sumByType(RecordType.INCOME)).thenReturn(new BigDecimal("10000"));
        when(recordRepository.sumByType(RecordType.EXPENSE)).thenReturn(new BigDecimal("4000"));
        when(recordRepository.countActiveRecords()).thenReturn(15L);

        DashboardSummary summary = dashboardService.getSummary();

        assertThat(summary.getTotalIncome()).isEqualByComparingTo("10000");
        assertThat(summary.getTotalExpense()).isEqualByComparingTo("4000");
        assertThat(summary.getNetBalance()).isEqualByComparingTo("6000");
        assertThat(summary.getTotalRecords()).isEqualTo(15L);
    }

    @Test
    @DisplayName("Should return category summary")
    void shouldReturnCategorySummary() {
        List<CategorySummary> expected = List.of(
                new CategorySummary("Rent", new BigDecimal("3600"), 3),
                new CategorySummary("Food", new BigDecimal("1070"), 3)
        );

        when(recordRepository.getCategorySummary()).thenReturn(expected);

        List<CategorySummary> result = dashboardService.getCategorySummary();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategory()).isEqualTo("Rent");
    }

    @Test
    @DisplayName("Should return monthly trends")
    void shouldReturnMonthlyTrends() {
        List<Object[]> rawData = List.of(
                new Object[]{RecordType.INCOME, 2025, 1, new BigDecimal("5200")},
                new Object[]{RecordType.EXPENSE, 2025, 1, new BigDecimal("1700")},
                new Object[]{RecordType.INCOME, 2025, 2, new BigDecimal("5600")},
                new Object[]{RecordType.EXPENSE, 2025, 2, new BigDecimal("1700")}
        );

        when(recordRepository.getMonthlyTrends()).thenReturn(rawData);

        List<MonthlyTrend> trends = dashboardService.getMonthlyTrends();

        assertThat(trends).hasSize(2);
        assertThat(trends.get(0).getMonth()).isEqualTo(1);
        assertThat(trends.get(0).getTotalIncome()).isEqualByComparingTo("5200");
        assertThat(trends.get(0).getTotalExpense()).isEqualByComparingTo("1700");
        assertThat(trends.get(0).getNetBalance()).isEqualByComparingTo("3500");
    }
}
