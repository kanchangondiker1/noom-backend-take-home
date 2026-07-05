package com.noom.interview.fullstack.sleep.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.noom.interview.fullstack.sleep.domain.Feeling;
import com.noom.interview.fullstack.sleep.domain.SleepLog;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * {@link SleepLogRepository} backed by Postgres via {@link NamedParameterJdbcTemplate}
 * (the JDBC access already wired up by the template's {@code DatabaseConfiguration}).
 */
@Repository
public class JdbcSleepLogRepository implements SleepLogRepository {

    private static final RowMapper<SleepLog> ROW_MAPPER = JdbcSleepLogRepository::mapRow;

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcSleepLogRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public SleepLog save(SleepLog log) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", log.id())
                .addValue("userId", log.userId())
                .addValue("sleepDate", log.sleepDate())
                .addValue("bedTime", log.bedTime())
                .addValue("wakeTime", log.wakeTime())
                .addValue("totalMinutes", log.totalTimeInBedMinutes())
                .addValue("feeling", log.feeling().name());

        return jdbc.queryForObject(
                "INSERT INTO sleep_log "
                        + "(id, user_id, sleep_date, bed_time, wake_time, total_time_in_bed_minutes, morning_feeling) "
                        + "VALUES (:id, :userId, :sleepDate, :bedTime, :wakeTime, :totalMinutes, :feeling) "
                        + "RETURNING *",
                params,
                ROW_MAPPER);
    }

    @Override
    public Optional<SleepLog> findMostRecent(UUID userId) {
        try {
            SleepLog log = jdbc.queryForObject(
                    "SELECT * FROM sleep_log WHERE user_id = :userId "
                            + "ORDER BY sleep_date DESC, created_at DESC LIMIT 1",
                    new MapSqlParameterSource("userId", userId),
                    ROW_MAPPER);
            return Optional.ofNullable(log);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<SleepLog> findInDateRange(UUID userId, LocalDate from, LocalDate to) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("from", from)
                .addValue("to", to);
        return jdbc.query(
                "SELECT * FROM sleep_log WHERE user_id = :userId "
                        + "AND sleep_date BETWEEN :from AND :to "
                        + "ORDER BY sleep_date",
                params,
                ROW_MAPPER);
    }

    @Override
    public boolean existsForDate(UUID userId, LocalDate sleepDate) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("sleepDate", sleepDate);
        Boolean exists = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM sleep_log WHERE user_id = :userId AND sleep_date = :sleepDate)",
                params,
                Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    private static SleepLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new SleepLog(
                rs.getObject("id", UUID.class),
                rs.getObject("user_id", UUID.class),
                rs.getObject("sleep_date", LocalDate.class),
                rs.getObject("bed_time", LocalDateTime.class),
                rs.getObject("wake_time", LocalDateTime.class),
                rs.getInt("total_time_in_bed_minutes"),
                Feeling.valueOf(rs.getString("morning_feeling")),
                rs.getObject("created_at", LocalDateTime.class));
    }
}
