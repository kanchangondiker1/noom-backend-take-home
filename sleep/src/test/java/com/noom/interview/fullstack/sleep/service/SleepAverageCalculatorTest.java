package com.noom.interview.fullstack.sleep.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.noom.interview.fullstack.sleep.domain.Feeling;
import com.noom.interview.fullstack.sleep.domain.SleepLog;

import org.junit.jupiter.api.Test;

class SleepAverageCalculatorTest {

    private static final LocalDate RANGE_START = LocalDate.of(2024, 1, 1);
    private static final LocalDate RANGE_END = LocalDate.of(2024, 1, 30);
    private static final UUID USER = UUID.randomUUID();

    private final SleepAverageCalculator calculator = new SleepAverageCalculator();

    @Test
    void emptyInputYieldsNullAveragesButKeepsRangeAndZeroFrequencies() {
        SleepAverages averages = calculator.calculate(List.of(), RANGE_START, RANGE_END);

        assertThat(averages.rangeStart()).isEqualTo(RANGE_START);
        assertThat(averages.rangeEnd()).isEqualTo(RANGE_END);
        assertThat(averages.sampleSize()).isZero();
        assertThat(averages.averageTotalTimeInBedMinutes()).isNull();
        assertThat(averages.averageBedTime()).isNull();
        assertThat(averages.averageWakeTime()).isNull();
        assertThat(averages.feelingFrequencies())
                .containsEntry(Feeling.BAD, 0)
                .containsEntry(Feeling.OK, 0)
                .containsEntry(Feeling.GOOD, 0);
    }

    @Test
    void averagesTotalTimeInBedRoundedToWholeMinutes() {
        // 400 and 401 minutes -> mean 400.5 -> rounds to 401
        List<SleepLog> logs = List.of(
                logWithMinutes(400, Feeling.OK),
                logWithMinutes(401, Feeling.OK));

        SleepAverages averages = calculator.calculate(logs, RANGE_START, RANGE_END);

        assertThat(averages.sampleSize()).isEqualTo(2);
        assertThat(averages.averageTotalTimeInBedMinutes()).isEqualTo(401);
    }

    @Test
    void countsFeelingFrequencies() {
        List<SleepLog> logs = List.of(
                logWithMinutes(420, Feeling.GOOD),
                logWithMinutes(420, Feeling.GOOD),
                logWithMinutes(420, Feeling.OK));

        SleepAverages averages = calculator.calculate(logs, RANGE_START, RANGE_END);

        assertThat(averages.feelingFrequencies())
                .containsEntry(Feeling.GOOD, 2)
                .containsEntry(Feeling.OK, 1)
                .containsEntry(Feeling.BAD, 0);
    }

    @Test
    void averageBedAndWakeTimesUseArithmeticMeanForSameDayTimes() {
        List<SleepLog> logs = List.of(
                logWithTimes(LocalDateTime.of(2024, 1, 10, 22, 0), LocalDateTime.of(2024, 1, 11, 6, 0)),
                logWithTimes(LocalDateTime.of(2024, 1, 11, 23, 0), LocalDateTime.of(2024, 1, 12, 8, 0)));

        SleepAverages averages = calculator.calculate(logs, RANGE_START, RANGE_END);

        // bedtimes 22:00 & 23:00 -> 22:30 ; waketimes 06:00 & 08:00 -> 07:00
        assertThat(averages.averageBedTime()).isEqualTo(LocalTime.of(22, 30));
        assertThat(averages.averageWakeTime()).isEqualTo(LocalTime.of(7, 0));
    }

    @Test
    void averageBedTimeWrapsCorrectlyAroundMidnight() {
        // 23:00 and 01:00 should average to midnight, not to midday.
        List<SleepLog> logs = List.of(
                logWithTimes(LocalDateTime.of(2024, 1, 10, 23, 0), LocalDateTime.of(2024, 1, 11, 7, 0)),
                logWithTimes(LocalDateTime.of(2024, 1, 12, 1, 0), LocalDateTime.of(2024, 1, 12, 9, 0)));

        SleepAverages averages = calculator.calculate(logs, RANGE_START, RANGE_END);

        assertThat(averages.averageBedTime()).isEqualTo(LocalTime.MIDNIGHT);
    }

    private static SleepLog logWithMinutes(int minutes, Feeling feeling) {
        LocalDateTime bed = LocalDateTime.of(2024, 1, 10, 22, 0);
        return new SleepLog(UUID.randomUUID(), USER, LocalDate.of(2024, 1, 11),
                bed, bed.plusMinutes(minutes), minutes, feeling, null);
    }

    private static SleepLog logWithTimes(LocalDateTime bedTime, LocalDateTime wakeTime) {
        return SleepLog.create(USER, wakeTime.toLocalDate(), bedTime, wakeTime, Feeling.OK);
    }
}
