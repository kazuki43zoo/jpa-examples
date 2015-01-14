package com.example.domain.repository;

import com.example.domain.model.Task;
import com.example.domain.repository.helper.DBLog;
import com.example.domain.repository.helper.TaskEntityHelper;
import com.example.domain.repository.task.TaskRepository;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class InvalidDataAccessApiUsageOperationTest {

    @Inject
    TaskRepository taskRepository;

    @Inject
    TaskEntityHelper taskEntityHelper;

    @Inject
    DBLog dbLog;

    @Before
    public void setup() {
        taskEntityHelper.deleteCreatedRecordsOnTesting();
        dbLog.delete();
    }

    @Transactional
    @Test
    public void deleteAndDeleteOnSameTransaction() {

        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask = new Task();
        newTask.setTitle("[Test] delete task");
        newTask.setDescription("Testing that delete a task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());

        Task createdTask = taskRepository.save(newTask);

        // test
        taskRepository.delete(createdTask);
        try {
            taskRepository.delete(createdTask);
            fail();
        } catch (InvalidDataAccessApiUsageException e) {
            assertThat(e.getMessage(), is("org.hibernate.ObjectDeletedException: deleted instance passed to merge: [com.example.domain.model.Task#<null>]; nested exception is java.lang.IllegalArgumentException: org.hibernate.ObjectDeletedException: deleted instance passed to merge: [com.example.domain.model.Task#<null>]"));
        }

        // assert
        assertThat(taskEntityHelper.exists(createdTask.getId()), is(false));

    }

    @Transactional
    @Test
    public void deleteAndSaveOnSameTransaction() {

        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask = new Task();
        newTask.setTitle("[Test] delete task");
        newTask.setDescription("Testing that delete a task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());

        Task createdTask = taskRepository.save(newTask);

        // test
        taskRepository.delete(createdTask);
        try {
            taskRepository.save(createdTask);
            fail();
        } catch (InvalidDataAccessApiUsageException e) {
            assertThat(e.getMessage(), is("org.hibernate.ObjectDeletedException: deleted instance passed to merge: [com.example.domain.model.Task#<null>]; nested exception is java.lang.IllegalArgumentException: org.hibernate.ObjectDeletedException: deleted instance passed to merge: [com.example.domain.model.Task#<null>]"));
        }

        // assert
        assertThat(taskEntityHelper.exists(createdTask.getId()), is(false));

    }

}
