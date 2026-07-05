package com.noom.interview.fullstack.sleep.repository;

import java.util.UUID;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Minimal user lookups. Authentication is out of scope, so this only needs to answer
 * "does this user exist?" when a request supplies an {@code X-User-Id}.
 */
@Repository
public class UserRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public UserRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existsById(UUID userId) {
        Boolean exists = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM users WHERE id = :id)",
                new MapSqlParameterSource("id", userId),
                Boolean.class);
        return Boolean.TRUE.equals(exists);
    }
}
