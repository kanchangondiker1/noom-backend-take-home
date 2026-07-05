package com.noom.interview.fullstack.sleep.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.noom.interview.fullstack.sleep.domain.Feeling;
import com.noom.interview.fullstack.sleep.domain.SleepLog;

/** API view of a single sleep log (requirements #1 create and #2 fetch last night). */
public record SleepLogResponse(
        UUID id,
        LocalDate date,
        LocalDateTime bedTime,
        LocalDateTime wakeTime,
        int totalTimeInBedMinutes,
        String totalTimeInBed,
        Feeling feeling
) {

    public static SleepLogResponse from(SleepLog log) {
        return new SleepLogResponse(
                log.id(),
                log.sleepDate(),
                log.bedTime(),
                log.wakeTime(),
                log.totalTimeInBedMinutes(),
                DurationFormat.humanize(log.totalTimeInBedMinutes()),
                log.feeling());
    }
}
