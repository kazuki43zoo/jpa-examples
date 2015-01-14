package com.example.domain.repository.helper;

import com.example.domain.model.Task;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Component for provide helper method that operate a task table.
 */
@Component
public class TaskEntityHelper extends AbstractEntityHelper<Task> {

    private static final RowMapper<Task> rowMapper = new RowMapper<Task>() {
        @Override
        public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
            Task task = new Task();
            task.setId(rs.getString("ID"));
            task.setTitle(rs.getString("TITLE"));
            task.setDescription(rs.getString("DESCRIPTION"));
            task.setDeadlineDate(rs.getDate("DEADLINE_DATE"));
            task.setFinished(rs.getBoolean("FINISHED"));
            task.setFinishedAt(rs.getTimestamp("FINISHED_AT"));
            task.setCreatedAt(rs.getTimestamp("CREATED_AT"));
            task.setVersion(rs.getLong("VERSION"));
            return task;
        }
    };

    public TaskEntityHelper() {
        super(Task.class, rowMapper);
    }

    /**
     * Helper method to delete task entities excluding test data.
     */
    public void deleteCreatedRecordsOnTesting() {
        getJdbcOperations().update("DELETE FROM task WHERE id NOT LIKE '00000000-%';COMMIT;");
    }


}
