package com.example.domain.repository.helper;

import com.example.domain.model.Task;
import org.springframework.stereotype.Component;

/**
 * Component for provide helper method that operate a task table.
 */
@Component
public class MemberEntityHelper extends AbstractEntityHelper<Task> {

    public MemberEntityHelper() {
        super(Task.class, null);
    }

    /**
     * Helper method to delete task entities excluding test data.
     */
    public void deleteCreatedRecordsOnTesting() {
        getJdbcOperations().update("DELETE FROM member WHERE id NOT LIKE 'TEST%';COMMIT;");
    }


}
