package com.finance.repository;

import com.finance.dto.dashboard.CategorySummary;
import com.finance.entity.FinancialRecord;
import com.finance.entity.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // ── Active record lookups (exclude soft-deleted) ────────────────────

    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);

    Page<FinancialRecord> findByDeletedFalse(Pageable pageable);

    // ── Filtered queries ────────────────────────────────────────────────

    Page<FinancialRecord> findByTypeAndDeletedFalse(RecordType type, Pageable pageable);

    Page<FinancialRecord> findByCategoryIgnoreCaseAndDeletedFalse(String category, Pageable pageable);

    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false " +
           "AND r.date BETWEEN :startDate AND :endDate")
    Page<FinancialRecord> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false " +
           "AND LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<FinancialRecord> searchByDescription(
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false " +
           "AND (:type IS NULL OR r.type = :type) " +
           "AND (:category IS NULL OR LOWER(r.category) = LOWER(:category)) " +
           "AND (:startDate IS NULL OR r.date >= :startDate) " +
           "AND (:endDate IS NULL OR r.date <= :endDate) " +
           "AND (:keyword IS NULL OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<FinancialRecord> findWithFilters(
            @Param("type") RecordType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("keyword") String keyword,
            Pageable pageable);

    // ── Dashboard analytics queries ─────────────────────────────────────

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
           "WHERE r.type = :type AND r.deleted = false")
    BigDecimal sumByType(@Param("type") RecordType type);

    @Query("SELECT COUNT(r) FROM FinancialRecord r WHERE r.deleted = false")
    long countActiveRecords();

    @Query("SELECT new com.finance.dto.dashboard.CategorySummary(r.category, SUM(r.amount), COUNT(r)) " +
           "FROM FinancialRecord r WHERE r.deleted = false " +
           "GROUP BY r.category ORDER BY SUM(r.amount) DESC")
    List<CategorySummary> getCategorySummary();

    @Query("SELECT r.type, YEAR(r.date), MONTH(r.date), SUM(r.amount) " +
           "FROM FinancialRecord r WHERE r.deleted = false " +
           "GROUP BY r.type, YEAR(r.date), MONTH(r.date) " +
           "ORDER BY YEAR(r.date), MONTH(r.date)")
    List<Object[]> getMonthlyTrends();

    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false " +
           "ORDER BY r.createdAt DESC")
    List<FinancialRecord> findRecentRecords(Pageable pageable);
}
