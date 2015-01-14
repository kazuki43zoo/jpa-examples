package com.example.domain.repository;

import com.example.domain.model.Task;
import com.example.domain.repository.helper.DBLog;
import com.example.domain.repository.helper.TaskEntityHelper;
import com.example.domain.repository.helper.TransactionalCommand;
import com.example.domain.repository.task.TaskRepository;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * 追加したクエリメソッドをテストする。
 * <p/>
 * Repositoryインタフェースに追加したクエリメソッド(@Query)のテストを行う。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class ModifyingQueryMethodTest {

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

    /**
     * @Modifyingアノテーションを使用して永続層のEntityを直接更新する。
     */
    @Test
    public void update() {

        // setup
        DateTime currentDateTime = new DateTime();
        Task newTask = new Task();
        newTask.setTitle("[Test] unfinished task");
        newTask.setDescription("Description for unfinished task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task unfinishedTask = taskRepository.save(newTask);

        newTask = new Task();
        newTask.setTitle("[Test] finished task");
        newTask.setDescription("Description for finished task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        newTask.setFinished(true);
        newTask.setFinishedAt(currentDateTime.toDate());
        Task finishedTask = taskRepository.save(newTask);

        // test
        Date finishedAt = currentDateTime.plusDays(3).toDate();
        int updatedCount = taskRepository.finishAll(finishedAt);

        // assert
        assertThat(updatedCount, is(1));
        {
            Task selectedTask = taskEntityHelper.selectById(unfinishedTask.getId());
            assertThat(selectedTask.isFinished(), is(true));
            assertThat(selectedTask.getFinishedAt(), is((Date) new Timestamp(finishedAt.getTime())));
            assertThat(selectedTask.getVersion(), is(1L));
        }
        {
            Task selectedTask = taskEntityHelper.selectById(finishedTask.getId());
            assertThat(selectedTask.isFinished(), is(true));
            assertThat(selectedTask.getFinishedAt(), is((Date) new Timestamp(currentDateTime.getMillis())));
            assertThat(selectedTask.getVersion(), is(0L));
        }

    }

    /**
     * @Modifyingアノテーションを使用して永続層のEntityを直接削除する。
     */
    @Test
    public void delete() {

        final DateTime currentDateTime = new DateTime();
        Task newTask = new Task();
        newTask.setTitle("[Test] delete target  task");
        newTask.setDescription("Description of delete target task.");
        newTask.setDeadlineDate(currentDateTime.minusDays(300).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.minusDays(370).toDate());
        newTask.setFinished(true);
        newTask.setFinishedAt(currentDateTime.minusDays(366).toDate());
        Task taskOfDeleteTarget = taskRepository.save(newTask);

        newTask = new Task();
        newTask.setTitle("[Test] delete target  task");
        newTask.setDescription("Description of delete target task.");
        newTask.setDeadlineDate(currentDateTime.minusDays(300).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.minusDays(370).toDate());
        newTask.setFinished(true);
        newTask.setFinishedAt(currentDateTime.minusDays(364).toDate());
        Task outsideTaskOfDeleteTarget = taskRepository.save(newTask);

        // test
        Date criteriaDateOfDeleteTarget = currentDateTime.minusDays(365).toLocalDate().toDate();
        int deletedCount = taskRepository.deleteByFinishedAtBefore(criteriaDateOfDeleteTarget);

        // assert
        assertThat(deletedCount, is(1));
        assertThat(taskEntityHelper.exists(taskOfDeleteTarget.getId()), is(false));
        assertThat(taskEntityHelper.exists(outsideTaskOfDeleteTarget.getId()), is(true));

    }

    /**
     * 同一トランザクションにて、Entityのプロパティの更新を行った後に、@Modifyingアノテーションを使用して永続層のEntityを直接更新する。
     */
    @Test
    public void updateAtAfterUpdatePropertyOnSameTransaction() {

        // setup
        final DateTime currentDateTime = new DateTime();
        Task newTask = new Task();
        newTask.setTitle("[Test] new task");
        newTask.setDescription("Description for a new task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        final Task createdTask = taskRepository.save(newTask);

        // test
        taskEntityHelper.executeWithinNewTransaction(new TransactionalCommand() {
            @Override
            public void execute() {
                Task loadedTask = taskRepository.findOne(createdTask.getId());
                loadedTask.setDeadlineDate(currentDateTime.plusDays(14).toLocalDate().toDate());
                taskRepository.finishAll(currentDateTime.plusDays(1).toDate());

                // note that not reflected modifying in entity (not reloaded)
                loadedTask = taskRepository.findOne(createdTask.getId());
                assertThat(loadedTask.getDeadlineDate(), is(currentDateTime.plusDays(14).toLocalDate().toDate()));
                assertThat(loadedTask.isFinished(), is(false));
                assertThat(loadedTask.getFinishedAt(), is(nullValue()));
                assertThat(loadedTask.getVersion(), is(1L));
            }
        });

        // assert
        Task selectedTask = taskEntityHelper.selectById(createdTask.getId());
        assertThat(selectedTask.getDeadlineDate(), is(currentDateTime.plusDays(14).toLocalDate().toDate()));
        assertThat(selectedTask.isFinished(), is(true));
        assertThat(selectedTask.getFinishedAt(), is((Date) new Timestamp(currentDateTime.plusDays(1).getMillis())));
        assertThat(selectedTask.getVersion(), is(2L));

    }

    /**
     * 同一トランザクションにて、Entityのプロパティの更新を行った後に、@Modifyingアノテーション(clearAutomatically = true)を使用して永続層のEntityを直接更新する。
     */
    @Test
    public void updateAndClearAtAfterUpdatePropertyOnSameTransaction() {

        // setup
        final DateTime currentDateTime = new DateTime();
        Task newTask = new Task();
        newTask.setTitle("[Test] new task");
        newTask.setDescription("Description for a new task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        final Task createdTask = taskRepository.save(newTask);

        // test
        taskEntityHelper.executeWithinNewTransaction(new TransactionalCommand() {
            @Override
            public void execute() {
                Task loadedTask = taskRepository.findOne(createdTask.getId());
                loadedTask.setDeadlineDate(currentDateTime.plusDays(14).toLocalDate().toDate());
                taskRepository.finishAllWithClear(currentDateTime.plusDays(1).toDate());

                // note that not reflected modifying in entity
                assertThat(loadedTask.getDeadlineDate(), is(currentDateTime.plusDays(14).toLocalDate().toDate()));
                assertThat(loadedTask.isFinished(), is(false));
                assertThat(loadedTask.getFinishedAt(), is(nullValue()));
                assertThat(loadedTask.getVersion(), is(1L));

                // reflected modifying in entity (reloaded)
                loadedTask = taskRepository.findOne(createdTask.getId());
                assertThat(loadedTask.getDeadlineDate(), is(currentDateTime.plusDays(14).toLocalDate().toDate()));
                assertThat(loadedTask.isFinished(), is(true));
                assertThat(loadedTask.getFinishedAt(), is((Date) new Timestamp(currentDateTime.plusDays(1).getMillis())));
                assertThat(loadedTask.getVersion(), is(2L));
            }
        });

        // assert
        Task selectedTask = taskEntityHelper.selectById(createdTask.getId());
        assertThat(selectedTask.getDeadlineDate(), is(currentDateTime.plusDays(14).toLocalDate().toDate()));
        assertThat(selectedTask.isFinished(), is(true));
        assertThat(selectedTask.getFinishedAt(), is((Date) new Timestamp(currentDateTime.plusDays(1).getMillis())));
        assertThat(selectedTask.getVersion(), is(2L));

    }


}
