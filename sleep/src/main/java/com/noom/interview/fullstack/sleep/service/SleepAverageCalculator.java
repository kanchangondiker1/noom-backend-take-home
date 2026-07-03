package com.noom.interview.fullstack.sleep.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.noom.interview.fullstack.sleep.domain.Feeling;
import com.noom.interview.fullstack.sleep.domain.SleepLog;

import org.springframework.stereotype.Component;

/**
 * Pure computation of sleep averages over a set of logs. No persistence or Spring
 * dependencies, so it can be unit-tested directly.
 *
 * <p>Averaging bed/wake <em>clock times</em> is done with a circular mean rather than a
 * naive arithmetic mean of minutes-since-midnight. Clock time is periodic: a bedtime of
 * 23:00 on one night and 01:00 on another should average to 00:00, not 12:00. The circular
 * mean treats each time as an angle on the 24h clock and averages the unit vectors, which
 * gives the intuitively correct answer regardless of midnight crossings.
 */
@Component
public class SleepAverageCalculator {

    private static final int SECONDS_PER_DAY = 24 * 60 * 60;

    public SleepAverages calculate(List<SleepLog> logs, LocalDate rangeStart, LocalDate rangeEnd) {
        Map<Feeling, Integer> frequencies = emptyFrequencies();
        for (SleepLog log : logs) {
            frequencies.merge(log.feeling(), 1, Integer::sum);
        }

        if (logs.isEmpty()) {
            return new SleepAverages(rangeStart, rangeEnd, 0, null, null, null, frequencies);
        }

        long totalMinutes = 0;
        for (SleepLog log : logs) {
            totalMinutes += log.totalTimeInBedMinutes();
        }
        int averageMinutes = Math.toIntExact(Math.round((double) totalMinutes / logs.size()));

        LocalTime averageBedTime = averageClockTime(logs.stream().map(l -> l.bedTime().toLocalTime()).toList());
        LocalTime averageWakeTime = averageClockTime(logs.stream().map(l -> l.wakeTime().toLocalTime()).toList());

        return new SleepAverages(
                rangeStart,
                rangeEnd,
                logs.size(),
                averageMinutes,
                averageBedTime,
                averageWakeTime,
                frequencies);
    }

    /** Circular mean of a non-empty list of clock times. */
    private static LocalTime averageClockTime(List<LocalTime> times) {
        double sumSin = 0;
        double sumCos = 0;
        for (LocalTime time : times) {
            double angle = 2 * Math.PI * time.toSecondOfDay() / SECONDS_PER_DAY;
            sumSin += Math.sin(angle);
            sumCos += Math.cos(angle);
        }

        double meanAngle = Math.atan2(sumSin / times.size(), sumCos / times.size());
        if (meanAngle < 0) {
            meanAngle += 2 * Math.PI;
        }

        long second = Math.round(meanAngle / (2 * Math.PI) * SECONDS_PER_DAY) % SECONDS_PER_DAY;
        return LocalTime.ofSecondOfDay(second);
    }

    private static Map<Feeling, Integer> emptyFrequencies() {
        Map<Feeling, Integer> frequencies = new EnumMap<>(Feeling.class);
        for (Feeling feeling : Feeling.values()) {
            frequencies.put(feeling, 0);
        }
        return frequencies;
    }
}
