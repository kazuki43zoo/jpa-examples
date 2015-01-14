package com.example.domain.repository.helper;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Component for provide helper method that operate logs.
 */
@Component
public class DBLog {

    @Inject
    @Named("jdbcTemplateForLogging")
    NamedParameterJdbcOperations jdbcOperations;

    /**
     * Delete logs.
     */
    public void delete() {
        jdbcOperations.getJdbcOperations().update("DELETE FROM logging_event_property");
        jdbcOperations.getJdbcOperations().update("DELETE FROM logging_event_exception");
        jdbcOperations.getJdbcOperations().update("DELETE FROM logging_event");
    }

    /**
     * Count message that matches to specified message pattern(regex).
     */
    public int countByMessagePattern(String messagePattern) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("messagePattern", messagePattern);

        return jdbcOperations.queryForObject("SELECT COUNT(*) FROM logging_event WHERE formatted_message REGEXP :messagePattern", params, Integer.class);
    }

}
