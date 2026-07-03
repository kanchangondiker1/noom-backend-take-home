package com.noom.interview.fullstack.sleep.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import com.noom.interview.fullstack.sleep.domain.Feeling;

/**
 * Aggregated view of a user's sleep over a date range.
 *
 * @param rangeStart                   first day of the range (inclusive)
 * @param rangeEnd                     last day of the range (inclusive)
 * @param sampleSize                   number of sleep logs the averages are based on
 * @param averageTotalTimeInBedMinutes mean time in bed, rounded to whole minutes; null when empty
 * @param averageBedTime               mean clock time the user got to bed; null when empty
 * @param averageWakeTime              mean clock time the user got out of bed; null when empty
 * @param feelingFrequencies           count of each morning feeling (always contains every value)
 */
public record SleepAverages(
        LocalDate rangeStart,
        LocalDate rangeEnd,
        int sampleSize,
        Integer averageTotalTimeInBedMinutes,
        LocalTime averageBedTime,
        LocalTime averageWakeTime,
        Map<Feeling, Integer> feelingFrequencies
) {
}
