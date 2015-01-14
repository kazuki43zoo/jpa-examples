package com.example.domain.repository.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Table;

abstract class AbstractEntityHelper<T> {

    private final Table table;
    private final RowMapper<T> rowMapper;

    @Inject
    @Named("jdbcTemplate")
    NamedParameterJdbcOperations namedParameterJdbcOperations;

    @Value("${database}")
    String database;

    AbstractEntityHelper(Class<?> entityClass, RowMapper<T> rowMapper) {
        this.table = AnnotationUtils.findAnnotation(entityClass, Table.class);
        this.rowMapper = rowMapper;
    }

    public NamedParameterJdbcOperations getNamedParameterJdbcOperations() {
        return namedParameterJdbcOperations;
    }

    public JdbcOperations getJdbcOperations() {
        return namedParameterJdbcOperations.getJdbcOperations();
    }

    /**
     * Helper method to retrieve a task entity that matches to specified id.
     */
    public T selectById(String id) {
        SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        return namedParameterJdbcOperations.queryForObject("SELECT * FROM " + table.name() + " WHERE id = :id", params, rowMapper);
    }

    /**
     * Helper method to check existing a task entity that matches to specified id.
     */
    public boolean exists(String id) {
        try {
            selectById(id);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    /**
     * Helper method to retrieve all record count.
     */
    public <T> T countAll(Class<T> returnClass) {
        return getJdbcOperations().queryForObject("SELECT COUNT(*) FROM " + table.name(), returnClass);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateVersionWithinNewTransaction(String id) {
        SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        namedParameterJdbcOperations.update("UPDATE " + table.name() + " SET version = version + 1  WHERE id = :id", params);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void lockWithinNewTransaction(String id, long lockTime, LockedCallback callback) {
        SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        namedParameterJdbcOperations.queryForObject("SELECT * FROM " + table.name() + " WHERE id = :id FOR UPDATE", params, rowMapper);
        callback.locked();
        try {
            Thread.sleep(lockTime);
        } catch (InterruptedException e) {
            // NOP
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeWithinNewTransaction(TransactionalCommand command) {
        command.execute();
    }

    public String getDatabase(){
        return database;
    }

    public boolean isH2(){
        return "H2".equalsIgnoreCase(database);
    }
    public boolean isPostgreSQL(){
        return "POSTGRESQL".equalsIgnoreCase(database);
    }
    public boolean isOracle(){
        return "ORACLE".equalsIgnoreCase(database);
    }

}
