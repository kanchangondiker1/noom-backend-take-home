package com.noom.interview.fullstack.sleep.web;

import java.util.UUID;

import com.noom.interview.fullstack.sleep.domain.SleepLog;
import com.noom.interview.fullstack.sleep.service.SleepAverages;
import com.noom.interview.fullstack.sleep.service.SleepLogService;
import com.noom.interview.fullstack.sleep.web.dto.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.web.dto.SleepAveragesResponse;
import com.noom.interview.fullstack.sleep.web.dto.SleepLogResponse;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for the sleep logger.
 *
 * <p>Authentication is out of scope, but the API is user-aware: every request must carry
 * the {@code X-User-Id} header identifying the owner of the sleep data.
 */
@RestController
@RequestMapping("/api/sleep-logs")
public class SleepLogController {

    private static final String USER_HEADER = "X-User-Id";

    private final SleepLogService sleepLogService;

    public SleepLogController(SleepLogService sleepLogService) {
        this.sleepLogService = sleepLogService;
    }

    /** Requirement #1 — create the sleep log for last night. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SleepLogResponse create(
            @RequestHeader(USER_HEADER) UUID userId,
            @Valid @RequestBody CreateSleepLogRequest request) {
        SleepLog created = sleepLogService.createSleepLog(
                userId, request.bedTime(), request.wakeTime(), request.feeling());
        return SleepLogResponse.from(created);
    }

    /** Requirement #2 — fetch information about last night's sleep. */
    @GetMapping("/last-night")
    public SleepLogResponse lastNight(@RequestHeader(USER_HEADER) UUID userId) {
        return SleepLogResponse.from(sleepLogService.getMostRecent(userId));
    }

    /** Requirement #3 — averages over the last N days (default 30). */
    @GetMapping("/averages")
    public SleepAveragesResponse averages(
            @RequestHeader(USER_HEADER) UUID userId,
            @RequestParam(name = "days", defaultValue = "" + SleepLogService.DEFAULT_AVERAGE_WINDOW_DAYS) int days) {
        SleepAverages averages = sleepLogService.getAverages(userId, days);
        return SleepAveragesResponse.from(averages);
    }
}
