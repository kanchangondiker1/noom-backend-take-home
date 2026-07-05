package com.noom.interview.fullstack.sleep.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.noom.interview.fullstack.sleep.domain.Feeling;
import com.noom.interview.fullstack.sleep.domain.SleepLog;
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository;
import com.noom.interview.fullstack.sleep.repository.UserRepository;
import com.noom.interview.fullstack.sleep.web.error.ConflictException;
import com.noom.interview.fullstack.sleep.web.error.NotFoundException;

import org.springframework.stereotype.Service;

/**
 * Business logic for the sleep logger: creating last night's log, fetching it back, and
 * computing multi-day averages. Enforces the domain rules (valid interval, one log per
 * night, known user) independently of the web and persistence layers.
 */
@Service
public class SleepLogService {

    /** Default look-back window for averages, per the assignment ("last 30-day averages"). */
    public static final int DEFAULT_AVERAGE_WINDOW_DAYS = 30;

    private final SleepLogRepository sleepLogRepository;
    private final UserRepository userRepository;
    private final SleepAverageCalculator averageCalculator;
    private final Clock clock;

    public SleepLogService(
            SleepLogRepository sleepLogRepository,
            UserRepository userRepository,
            SleepAverageCalculator averageCalculator,
            Clock clock) {
        this.sleepLogRepository = sleepLogRepository;
        this.userRepository = userRepository;
        this.averageCalculator = averageCalculator;
        this.clock = clock;
    }

    /**
     * Logs last night's sleep for the user, stamped with today's date.
     *
     * @throws NotFoundException if the user does not exist
     * @throws IllegalArgumentException if the interval is not strictly positive
     * @throws ConflictException if the user already logged a sleep for today
     */
    public SleepLog createSleepLog(UUID userId, LocalDateTime bedTime, LocalDateTime wakeTime, Feeling feeling) {
        requireExistingUser(userId);

        if (!wakeTime.isAfter(bedTime)) {
            throw new IllegalArgumentException("wakeTime must be after bedTime");
        }

        LocalDate today = LocalDate.now(clock);
        if (sleepLogRepository.existsForDate(userId, today)) {
            throw new ConflictException("A sleep log already exists for " + today);
        }

        return sleepLogRepository.save(SleepLog.create(userId, today, bedTime, wakeTime, feeling));
    }

    /**
     * Returns the user's most recent sleep log ("last night").
     *
     * @throws NotFoundException if the user does not exist or has no logs yet
     */
    public SleepLog getMostRecent(UUID userId) {
        requireExistingUser(userId);
        return sleepLogRepository.findMostRecent(userId)
                .orElseThrow(() -> new NotFoundException("No sleep log found for user " + userId));
    }

    /** Averages over the last {@code windowDays} days (inclusive of today). */
    public SleepAverages getAverages(UUID userId, int windowDays) {
        if (windowDays < 1) {
            throw new IllegalArgumentException("days must be a positive number");
        }
        requireExistingUser(userId);

        LocalDate rangeEnd = LocalDate.now(clock);
        LocalDate rangeStart = rangeEnd.minusDays(windowDays - 1L);
        List<SleepLog> logs = sleepLogRepository.findInDateRange(userId, rangeStart, rangeEnd);
        return averageCalculator.calculate(logs, rangeStart, rangeEnd);
    }

    private void requireExistingUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Unknown user " + userId);
        }
    }
}
