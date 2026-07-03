package com.noom.interview.fullstack.sleep.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import com.noom.interview.fullstack.sleep.domain.Feeling;
import com.noom.interview.fullstack.sleep.service.SleepAverages;

/** API view of the sleep averages over a date range (requirement #3). */
public record SleepAveragesResponse(
        LocalDate rangeStart,
        LocalDate rangeEnd,
        int sampleSize,
        Integer averageTotalTimeInBedMinutes,
        String averageTotalTimeInBed,
        LocalTime averageBedTime,
        LocalTime averageWakeTime,
        Map<Feeling, Integer> feelingFrequencies
) {

    public static SleepAveragesResponse from(SleepAverages averages) {
        return new SleepAveragesResponse(
                averages.rangeStart(),
                averages.rangeEnd(),
                averages.sampleSize(),
                averages.averageTotalTimeInBedMinutes(),
                DurationFormat.humanize(averages.averageTotalTimeInBedMinutes()),
                averages.averageBedTime(),
                averages.averageWakeTime(),
                averages.feelingFrequencies());
    }
}
