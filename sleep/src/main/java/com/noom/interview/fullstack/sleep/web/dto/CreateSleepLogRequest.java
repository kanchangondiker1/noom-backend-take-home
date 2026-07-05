package com.noom.interview.fullstack.sleep.web.dto;

import java.time.LocalDateTime;

import com.noom.interview.fullstack.sleep.domain.Feeling;

import javax.validation.constraints.NotNull;

/**
 * Request body for logging last night's sleep. The sleep date itself is not accepted from
 * the client — a log always represents "last night", so the server stamps it with today's
 * date. The client only supplies the time-in-bed interval and the morning feeling.
 */
public record CreateSleepLogRequest(
        @NotNull(message = "bedTime is required") LocalDateTime bedTime,
        @NotNull(message = "wakeTime is required") LocalDateTime wakeTime,
        @NotNull(message = "feeling is required and must be one of BAD, OK, GOOD") Feeling feeling
) {
}
