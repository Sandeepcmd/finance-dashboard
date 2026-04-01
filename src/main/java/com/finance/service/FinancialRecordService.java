package com.finance.service;

import com.finance.dto.record.RecordRequest;
import com.finance.dto.record.RecordResponse;
import com.finance.entity.FinancialRecord;
import com.finance.entity.RecordType;
import com.finance.entity.User;
import com.finance.exception.ResourceNotFoundException;
import com.finance.mapper.RecordMapper;
import com.finance.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final RecordMapper recordMapper;

    @Transactional
    public RecordResponse createRecord(RecordRequest request) {
        log.info("Creating financial record: type={}, amount={}", request.getType(), request.getAmount());

        RecordType type = parseRecordType(request.getType());
        User currentUser = getCurrentUser();

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(type)
                .category(request.getCategory())
                .date(request.getDate())
                .description(request.getDescription())
                .createdBy(currentUser)
                .build();

        FinancialRecord savedRecord = recordRepository.save(record);
        log.info("Financial record created with ID: {}", savedRecord.getId());
        return recordMapper.toResponse(savedRecord);
    }

    public Page<RecordResponse> getRecords(
            String type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            String search,
            Pageable pageable) {

        log.debug("Fetching records with filters: type={}, category={}, startDate={}, endDate={}, search={}",
                type, category, startDate, endDate, search);

        RecordType recordType = null;
        if (type != null && !type.isBlank()) {
            recordType = parseRecordType(type);
        }

        Page<FinancialRecord> records = recordRepository.findWithFilters(
                recordType, category, startDate, endDate, search, pageable);

        return records.map(recordMapper::toResponse);
    }

    public RecordResponse getRecordById(Long id) {
        log.debug("Fetching record with ID: {}", id);
        FinancialRecord record = findRecordOrThrow(id);
        return recordMapper.toResponse(record);
    }

    @Transactional
    public RecordResponse updateRecord(Long id, RecordRequest request) {
        log.info("Updating record with ID: {}", id);
        FinancialRecord record = findRecordOrThrow(id);

        record.setAmount(request.getAmount());
        record.setType(parseRecordType(request.getType()));
        record.setCategory(request.getCategory());
        record.setDate(request.getDate());
        record.setDescription(request.getDescription());

        FinancialRecord updatedRecord = recordRepository.save(record);
        log.info("Record updated successfully: {}", id);
        return recordMapper.toResponse(updatedRecord);
    }

    @Transactional
    public void deleteRecord(Long id) {
        log.info("Soft-deleting record with ID: {}", id);
        FinancialRecord record = findRecordOrThrow(id);
        record.setDeleted(true);
        recordRepository.save(record);
        log.info("Record soft-deleted: {}", id);
    }

    private FinancialRecord findRecordOrThrow(Long id) {
        return recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial Record", "id", id));
    }

    private RecordType parseRecordType(String type) {
        try {
            return RecordType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid record type: " + type + ". Must be INCOME or EXPENSE.");
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
