package com.noom.interview.fullstack.sleep.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single night's sleep for a user. Immutable domain record that mirrors a row of
 * the {@code sleep_log} table.
 *
 * <p>{@code totalTimeInBedMinutes} is derived from the {@code [bedTime, wakeTime]}
 * interval and persisted so that range/average queries stay simple.
 */
public record SleepLog(
        UUID id,
        UUID userId,
        LocalDate sleepDate,
        LocalDateTime bedTime,
        LocalDateTime wakeTime,
        int totalTimeInBedMinutes,
        Feeling feeling,
        LocalDateTime createdAt
) {

    /**
     * Builds a new sleep log for persistence, generating the id and deriving the total
     * time in bed from the interval. {@code createdAt} is left to the database default.
     */
    public static SleepLog create(
            UUID userId,
            LocalDate sleepDate,
            LocalDateTime bedTime,
            LocalDateTime wakeTime,
            Feeling feeling) {
        return new SleepLog(
                UUID.randomUUID(),
                userId,
                sleepDate,
                bedTime,
                wakeTime,
                minutesBetween(bedTime, wakeTime),
                feeling,
                null);
    }

    private static int minutesBetween(LocalDateTime bedTime, LocalDateTime wakeTime) {
        return Math.toIntExact(Duration.between(bedTime, wakeTime).toMinutes());
    }
}
