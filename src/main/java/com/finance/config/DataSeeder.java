package com.finance.config;

import com.finance.entity.*;
import com.finance.repository.FinancialRecordRepository;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds the database with sample data on first run.
 * Only runs when no users exist in the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FinancialRecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping.");
            return;
        }

        log.info("Seeding database with sample data...");

        // ── Create users ────────────────────────────────────────────────
        User admin = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin@finance.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build());

        User analyst = userRepository.save(User.builder()
                .name("Jane Analyst")
                .email("analyst@finance.com")
                .password(passwordEncoder.encode("analyst123"))
                .role(Role.ANALYST)
                .status(Status.ACTIVE)
                .build());

        User viewer = userRepository.save(User.builder()
                .name("Bob Viewer")
                .email("viewer@finance.com")
                .password(passwordEncoder.encode("viewer123"))
                .role(Role.VIEWER)
                .status(Status.ACTIVE)
                .build());

        // ── Create financial records ────────────────────────────────────
        createRecord(new BigDecimal("5000.00"), RecordType.INCOME, "Salary", LocalDate.of(2025, 1, 5), "Monthly salary - January", admin);
        createRecord(new BigDecimal("1200.00"), RecordType.EXPENSE, "Rent", LocalDate.of(2025, 1, 1), "January apartment rent", admin);
        createRecord(new BigDecimal("350.00"), RecordType.EXPENSE, "Food", LocalDate.of(2025, 1, 10), "Grocery shopping", analyst);
        createRecord(new BigDecimal("150.00"), RecordType.EXPENSE, "Transport", LocalDate.of(2025, 1, 15), "Monthly transit pass", analyst);
        createRecord(new BigDecimal("200.00"), RecordType.INCOME, "Freelance", LocalDate.of(2025, 1, 20), "Logo design project", admin);

        createRecord(new BigDecimal("5000.00"), RecordType.INCOME, "Salary", LocalDate.of(2025, 2, 5), "Monthly salary - February", admin);
        createRecord(new BigDecimal("1200.00"), RecordType.EXPENSE, "Rent", LocalDate.of(2025, 2, 1), "February apartment rent", admin);
        createRecord(new BigDecimal("420.00"), RecordType.EXPENSE, "Food", LocalDate.of(2025, 2, 12), "Grocery shopping", analyst);
        createRecord(new BigDecimal("80.00"), RecordType.EXPENSE, "Utilities", LocalDate.of(2025, 2, 18), "Electric bill", admin);
        createRecord(new BigDecimal("600.00"), RecordType.INCOME, "Freelance", LocalDate.of(2025, 2, 25), "Website redesign project", admin);

        createRecord(new BigDecimal("5000.00"), RecordType.INCOME, "Salary", LocalDate.of(2025, 3, 5), "Monthly salary - March", admin);
        createRecord(new BigDecimal("1200.00"), RecordType.EXPENSE, "Rent", LocalDate.of(2025, 3, 1), "March apartment rent", admin);
        createRecord(new BigDecimal("300.00"), RecordType.EXPENSE, "Food", LocalDate.of(2025, 3, 8), "Grocery shopping", analyst);
        createRecord(new BigDecimal("250.00"), RecordType.EXPENSE, "Entertainment", LocalDate.of(2025, 3, 15), "Concert tickets", analyst);
        createRecord(new BigDecimal("1000.00"), RecordType.INCOME, "Bonus", LocalDate.of(2025, 3, 28), "Quarterly performance bonus", admin);

        log.info("Database seeded successfully with {} users and {} records",
                userRepository.count(), recordRepository.count());
        log.info("==========================================================");
        log.info("  Default credentials:");
        log.info("  Admin:   admin@finance.com   / admin123");
        log.info("  Analyst: analyst@finance.com / analyst123");
        log.info("  Viewer:  viewer@finance.com  / viewer123");
        log.info("==========================================================");
    }

    private void createRecord(BigDecimal amount, RecordType type, String category,
                               LocalDate date, String description, User createdBy) {
        recordRepository.save(FinancialRecord.builder()
                .amount(amount)
                .type(type)
                .category(category)
                .date(date)
                .description(description)
                .createdBy(createdBy)
                .build());
    }
}
