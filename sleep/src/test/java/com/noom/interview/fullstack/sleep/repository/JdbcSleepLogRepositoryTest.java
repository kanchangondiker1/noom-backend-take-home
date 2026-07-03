package com.noom.interview.fullstack.sleep.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.noom.interview.fullstack.sleep.domain.Feeling;
import com.noom.interview.fullstack.sleep.domain.SleepLog;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration test for {@link JdbcSleepLogRepository} against a real Postgres (Testcontainers),
 * with the actual Flyway migrations applied. Requires a running Docker daemon.
 */
@Tag("integration")
@Testcontainers
class JdbcSleepLogRepositoryTest {

    // Same Postgres major version as docker-compose.
    
    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:13-alpine");

    // The demo user seeded by V4.0__seed_demo_user.sql.
    private static final UUID DEMO_USER = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static NamedParameterJdbcTemplate jdbc;
    private JdbcSleepLogRepository repository;

    @BeforeAll
    static void migrate() {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .load()
                .migrate();

        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        dataSource.setDriverClassName("org.postgresql.Driver");
        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    @BeforeEach
    void cleanSleepLogs() {
        jdbc.getJdbcTemplate().update("DELETE FROM sleep_log");
        repository = new JdbcSleepLogRepository(jdbc);
    }

    @Test
    void savesAndReadsBackAllFields() {
        SleepLog toSave = SleepLog.create(
                DEMO_USER,
                LocalDate.of(2024, 3, 15),
                LocalDateTime.of(2024, 3, 14, 23, 15),
                LocalDateTime.of(2024, 3, 15, 7, 0),
                Feeling.GOOD);

        SleepLog saved = repository.save(toSave);

        assertThat(saved.id()).isEqualTo(toSave.id());
        assertThat(saved.userId()).isEqualTo(DEMO_USER);
        assertThat(saved.sleepDate()).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(saved.bedTime()).isEqualTo(LocalDateTime.of(2024, 3, 14, 23, 15));
        assertThat(saved.wakeTime()).isEqualTo(LocalDateTime.of(2024, 3, 15, 7, 0));
        assertThat(saved.totalTimeInBedMinutes()).isEqualTo(7 * 60 + 45);
        assertThat(saved.feeling()).isEqualTo(Feeling.GOOD);
        assertThat(saved.createdAt()).isNotNull();
    }

    @Test
    void findMostRecentReturnsLatestBySleepDate() {
        repository.save(logOn(LocalDate.of(2024, 3, 10)));
        repository.save(logOn(LocalDate.of(2024, 3, 14)));
        repository.save(logOn(LocalDate.of(2024, 3, 12)));

        Optional<SleepLog> mostRecent = repository.findMostRecent(DEMO_USER);

        assertThat(mostRecent).isPresent();
        assertThat(mostRecent.get().sleepDate()).isEqualTo(LocalDate.of(2024, 3, 14));
    }

    @Test
    void findMostRecentIsEmptyWhenNoLogs() {
        assertThat(repository.findMostRecent(DEMO_USER)).isEmpty();
    }

    @Test
    void findInDateRangeIsInclusiveAndOrdered() {
        repository.save(logOn(LocalDate.of(2024, 3, 1)));  // before range
        repository.save(logOn(LocalDate.of(2024, 3, 10))); // start (inclusive)
        repository.save(logOn(LocalDate.of(2024, 3, 15))); // middle
        repository.save(logOn(LocalDate.of(2024, 3, 20))); // end (inclusive)
        repository.save(logOn(LocalDate.of(2024, 3, 25))); // after range

        List<SleepLog> inRange = repository.findInDateRange(
                DEMO_USER, LocalDate.of(2024, 3, 10), LocalDate.of(2024, 3, 20));

        assertThat(inRange).extracting(SleepLog::sleepDate).containsExactly(
                LocalDate.of(2024, 3, 10),
                LocalDate.of(2024, 3, 15),
                LocalDate.of(2024, 3, 20));
    }

    @Test
    void existsForDateReflectsStoredLogs() {
        repository.save(logOn(LocalDate.of(2024, 3, 15)));

        assertThat(repository.existsForDate(DEMO_USER, LocalDate.of(2024, 3, 15))).isTrue();
        assertThat(repository.existsForDate(DEMO_USER, LocalDate.of(2024, 3, 16))).isFalse();
    }

    private static SleepLog logOn(LocalDate date) {
        LocalDateTime bed = date.minusDays(1).atTime(23, 0);
        LocalDateTime wake = date.atTime(7, 0);
        return SleepLog.create(DEMO_USER, date, bed, wake, Feeling.OK);
    }
}
