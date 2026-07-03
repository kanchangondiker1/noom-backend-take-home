package com.noom.interview.fullstack.sleep.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.noom.interview.fullstack.sleep.domain.Feeling;
import com.noom.interview.fullstack.sleep.domain.SleepLog;
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository;
import com.noom.interview.fullstack.sleep.repository.UserRepository;
import com.noom.interview.fullstack.sleep.web.error.ConflictException;
import com.noom.interview.fullstack.sleep.web.error.NotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SleepLogServiceTest {

    private static final UUID USER = UUID.fromString("11111111-1111-1111-1111-111111111111");
    // Fixed "today" so date logic is deterministic.
    private static final LocalDate TODAY = LocalDate.of(2024, 3, 15);
    private final Clock clock = Clock.fixed(
            TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    private SleepLogRepository sleepLogRepository;
    private UserRepository userRepository;
    private SleepLogService service;

    @BeforeEach
    void setUp() {
        sleepLogRepository = org.mockito.Mockito.mock(SleepLogRepository.class);
        userRepository = org.mockito.Mockito.mock(UserRepository.class);
        service = new SleepLogService(sleepLogRepository, userRepository, new SleepAverageCalculator(), clock);
        when(userRepository.existsById(USER)).thenReturn(true);
    }

    @Test
    void createStampsTodayDerivesTotalMinutesAndPersists() {
        LocalDateTime bed = LocalDateTime.of(2024, 3, 14, 23, 0);
        LocalDateTime wake = LocalDateTime.of(2024, 3, 15, 7, 30);
        when(sleepLogRepository.existsForDate(USER, TODAY)).thenReturn(false);
        when(sleepLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.createSleepLog(USER, bed, wake, Feeling.GOOD);

        ArgumentCaptor<SleepLog> captor = ArgumentCaptor.forClass(SleepLog.class);
        verify(sleepLogRepository).save(captor.capture());
        SleepLog saved = captor.getValue();
        assertThat(saved.sleepDate()).isEqualTo(TODAY);
        assertThat(saved.totalTimeInBedMinutes()).isEqualTo(8 * 60 + 30);
        assertThat(saved.userId()).isEqualTo(USER);
    }

    @Test
    void createRejectsUnknownUser() {
        UUID unknown = UUID.randomUUID();
        when(userRepository.existsById(unknown)).thenReturn(false);

        assertThatThrownBy(() -> service.createSleepLog(
                unknown, LocalDateTime.now(), LocalDateTime.now().plusHours(8), Feeling.OK))
                .isInstanceOf(NotFoundException.class);
        verify(sleepLogRepository, never()).save(any());
    }

    @Test
    void createRejectsNonPositiveInterval() {
        LocalDateTime bed = LocalDateTime.of(2024, 3, 15, 7, 0);
        LocalDateTime wake = LocalDateTime.of(2024, 3, 14, 23, 0); // wake before bed

        assertThatThrownBy(() -> service.createSleepLog(USER, bed, wake, Feeling.OK))
                .isInstanceOf(IllegalArgumentException.class);
        verify(sleepLogRepository, never()).save(any());
    }

    @Test
    void createRejectsSecondLogForSameNight() {
        when(sleepLogRepository.existsForDate(USER, TODAY)).thenReturn(true);

        assertThatThrownBy(() -> service.createSleepLog(
                USER,
                LocalDateTime.of(2024, 3, 14, 23, 0),
                LocalDateTime.of(2024, 3, 15, 7, 0),
                Feeling.OK))
                .isInstanceOf(ConflictException.class);
        verify(sleepLogRepository, never()).save(any());
    }

    @Test
    void getMostRecentThrowsWhenNoLogs() {
        when(sleepLogRepository.findMostRecent(USER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMostRecent(USER)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAveragesQueriesInclusiveThirtyDayWindowEndingToday() {
        when(sleepLogRepository.findInDateRange(eq(USER), any(), any())).thenReturn(List.of());

        SleepAverages averages = service.getAverages(USER, SleepLogService.DEFAULT_AVERAGE_WINDOW_DAYS);

        // 30-day inclusive window ending today: [today-29, today]
        assertThat(averages.rangeStart()).isEqualTo(TODAY.minusDays(29));
        assertThat(averages.rangeEnd()).isEqualTo(TODAY);
        verify(sleepLogRepository).findInDateRange(USER, TODAY.minusDays(29), TODAY);
    }

    @Test
    void getAveragesRejectsNonPositiveWindow() {
        assertThatThrownBy(() -> service.getAverages(USER, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
