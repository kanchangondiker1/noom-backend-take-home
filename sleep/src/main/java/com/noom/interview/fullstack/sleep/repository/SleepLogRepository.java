package com.noom.interview.fullstack.sleep.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.noom.interview.fullstack.sleep.domain.SleepLog;

/**
 * Persistence operations for sleep logs. Kept as an interface so the service layer can
 * be unit-tested against a fake and the JDBC implementation against a real Postgres.
 */
public interface SleepLogRepository {

    /** Inserts a new sleep log and returns the stored row (including db-populated fields). */
    SleepLog save(SleepLog sleepLog);

    /** The user's most recent sleep log, i.e. "last night". */
    Optional<SleepLog> findMostRecent(UUID userId);

    /** All logs for the user whose sleep_date falls within [from, to], oldest first. */
    List<SleepLog> findInDateRange(UUID userId, LocalDate from, LocalDate to);

    /** Whether the user already has a log for the given date (one per night). */
    boolean existsForDate(UUID userId, LocalDate sleepDate);
}
