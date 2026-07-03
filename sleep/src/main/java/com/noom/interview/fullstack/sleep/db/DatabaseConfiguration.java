package com.noom.interview.fullstack.sleep.db;

import javax.sql.DataSource;

import com.noom.interview.fullstack.sleep.SleepApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Builds the Postgres {@link DataSource} and the {@link NamedParameterJdbcTemplate} used by the
 * repositories. Excluded from the unit-test profile, where Spring Boot autoconfigures a datasource
 * from the test properties instead.
 */
@Configuration
@Profile("!" + SleepApplication.UNIT_TEST_PROFILE)
public class DatabaseConfiguration {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(url)
                .username(username)
                .password(password)
                .build();
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}
