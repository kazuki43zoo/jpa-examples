package com.example.domain.repository;

import com.example.domain.model.Task;
import com.example.domain.repository.helper.DBLog;
import com.example.domain.repository.helper.TaskEntityHelper;
import com.example.domain.repository.task.TaskRepository;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * エンティティへのロック操作をテストする。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class EntityLockTest {

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
     * LockModeType.PESSIMISTIC_WRITEを指定してロックを取得する。
     */
    @Test
    public void pessimisticWriteLock() {

        // test for exist
        Task loadedTask = taskRepository.findOneWithinPessimisticWriteLockById("00000000-0000-0000-0000-000000000001");

        // assert
        Task selectedTask = taskEntityHelper.selectById(loadedTask.getId());
        assertThat(loadedTask.getTitle(), is(selectedTask.getTitle()));
        assertThat(loadedTask.getDescription(), is(selectedTask.getDescription()));
        assertThat(loadedTask.getDeadlineDate(), is(selectedTask.getDeadlineDate()));
        assertThat(loadedTask.getCreatedAt(), is(selectedTask.getCreatedAt()));
        assertThat(loadedTask.isFinished(), is(selectedTask.isFinished()));
        assertThat(loadedTask.getFinishedAt(), is(selectedTask.getFinishedAt()));
        assertThat(loadedTask.getVersion(), is(0L));
        assertThat(selectedTask.getVersion(), is(0L));

        assertThat(dbLog.countByMessagePattern("/\\* select generatedAlias0 from Task as generatedAlias0 where generatedAlias0\\.id=:param0 \\*/ select .* from .* where .* for update"), is((1)));
        assertThat(dbLog.countByMessagePattern("/\\* forced version increment \\*/"), is((0)));

    }

    /**
     * LockModeType.PESSIMISTIC_READを指定してロックを取得する。
     */
    @Test
    public void pessimisticReadLock() {

        // test for exist
        Task loadedTask = taskRepository.findOneWithinPessimisticReadLockById("00000000-0000-0000-0000-000000000001");

        // assert
        Task selectedTask = taskEntityHelper.selectById(loadedTask.getId());
        assertThat(loadedTask.getTitle(), is(selectedTask.getTitle()));
        assertThat(loadedTask.getDescription(), is(selectedTask.getDescription()));
        assertThat(loadedTask.getDeadlineDate(), is(selectedTask.getDeadlineDate()));
        assertThat(loadedTask.getCreatedAt(), is(selectedTask.getCreatedAt()));
        assertThat(loadedTask.isFinished(), is(selectedTask.isFinished()));
        assertThat(loadedTask.getFinishedAt(), is(selectedTask.getFinishedAt()));
        assertThat(loadedTask.getVersion(), is(0L));
        assertThat(selectedTask.getVersion(), is(0L));

        if (taskEntityHelper.isPostgreSQL()) {
            assertThat(dbLog.countByMessagePattern("/\\* select generatedAlias0 from Task as generatedAlias0 where generatedAlias0\\.id=:param0 \\*/ select .* from .* where .* for share"), is((1)));
        } else {
            assertThat(dbLog.countByMessagePattern("/\\* select generatedAlias0 from Task as generatedAlias0 where generatedAlias0\\.id=:param0 \\*/ select .* from .* where .* for update"), is((1)));
        }
        assertThat(dbLog.countByMessagePattern("/\\* forced version increment \\*/"), is((0)));

    }

    /**
     * LockModeType.PESSIMISTIC_FORCE_INCREMENTを指定してロックを取得する。
     */
    @Transactional
    @Test
    public void pessimisticForceIncrementLock() {

        // test for exist
        Task loadedTask = taskRepository.findOneWithinPessimisticForceIncrementLockById("00000000-0000-0000-0000-000000000001");

        // assert
        Task selectedTask = taskEntityHelper.selectById(loadedTask.getId());
        assertThat(loadedTask.getTitle(), is(selectedTask.getTitle()));
        assertThat(loadedTask.getDescription(), is(selectedTask.getDescription()));
        assertThat(loadedTask.getDeadlineDate(), is(selectedTask.getDeadlineDate()));
        assertThat(loadedTask.getCreatedAt(), is(selectedTask.getCreatedAt()));
        assertThat(loadedTask.isFinished(), is(selectedTask.isFinished()));
        assertThat(loadedTask.getFinishedAt(), is(selectedTask.getFinishedAt()));
        assertThat(loadedTask.getVersion(), is(1L));
        assertThat(selectedTask.getVersion(), is(1L));

        assertThat(dbLog.countByMessagePattern("/\\* select generatedAlias0 from Task as generatedAlias0 where generatedAlias0\\.id=:param0 \\*/ select .* from .* where .* for update"), is((1)));
        assertThat(dbLog.countByMessagePattern("/\\* forced version increment \\*/ update .* set version=1 where .* and version=0"), is((1)));

    }

    /**
     * LockModeType.WRITEを指定してロックを取得する。
     */
    @Test
    public void writeLock() {

        // setup
        DateTime currentDateTime = new DateTime();

        Task newTask = new Task();
        newTask.setTitle("[Test] write lock task");
        newTask.setDescription("Testing that write lock a task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);

        // test
        Task loadedTask = taskRepository.findOneWithinWriteLockById(createdTask.getId());

        // assert
        Task selectedTask = taskEntityHelper.selectById(loadedTask.getId());
        assertThat(loadedTask.getTitle(), is(selectedTask.getTitle()));
        assertThat(loadedTask.getDescription(), is(selectedTask.getDescription()));
        assertThat(loadedTask.getDeadlineDate(), is(selectedTask.getDeadlineDate()));
        assertThat(loadedTask.getCreatedAt(), is(selectedTask.getCreatedAt()));
        assertThat(loadedTask.isFinished(), is(selectedTask.isFinished()));
        assertThat(loadedTask.getFinishedAt(), is(selectedTask.getFinishedAt()));
        assertThat(loadedTask.getVersion(), is(1L));
        assertThat(selectedTask.getVersion(), is(1L));

        assertThat(dbLog.countByMessagePattern("/\\* select generatedAlias0 from Task as generatedAlias0 where generatedAlias0\\.id=:param0 \\*/ select .* from .* where .* for update"), is((0)));
        assertThat(dbLog.countByMessagePattern("/\\* forced version increment \\*/ update .* set version=1 where .* and version=0"), is((1)));

    }

    /**
     * LockModeType.OPTIMISTIC_FORCE_INCREMENTを指定してロックを取得する。
     */
    @Test
    public void optimisticForceIncrementLock() {

        // setup
        DateTime currentDateTime = new DateTime();

        Task newTask = new Task();
        newTask.setTitle("[Test] optimistic force increment lock task");
        newTask.setDescription("Testing that optimistic force increment lock a task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);

        // test
        Task loadedTask = taskRepository.findOneWithinOptimisticForceIncrementLockById(createdTask.getId());

        // assert
        Task selectedTask = taskEntityHelper.selectById(loadedTask.getId());
        assertThat(loadedTask.getTitle(), is(selectedTask.getTitle()));
        assertThat(loadedTask.getDescription(), is(selectedTask.getDescription()));
        assertThat(loadedTask.getDeadlineDate(), is(selectedTask.getDeadlineDate()));
        assertThat(loadedTask.getCreatedAt(), is(selectedTask.getCreatedAt()));
        assertThat(loadedTask.isFinished(), is(selectedTask.isFinished()));
        assertThat(loadedTask.getFinishedAt(), is(selectedTask.getFinishedAt()));
        assertThat(loadedTask.getVersion(), is(1L));
        assertThat(selectedTask.getVersion(), is(1L));

        assertThat(dbLog.countByMessagePattern("/\\* select generatedAlias0 from Task as generatedAlias0 where generatedAlias0\\.id=:param0 \\*/ select .* from .* where .* for update"), is((0)));
        assertThat(dbLog.countByMessagePattern("/\\* forced version increment \\*/ update .* set version=1 where .* and version=0"), is((1)));

    }


    /**
     * LockModeType.READを指定してロックを取得する。
     */
    @Test
    public void readLock() {

        // setup
        DateTime currentDateTime = new DateTime();

        Task newTask = new Task();
        newTask.setTitle("[Test] read lock task");
        newTask.setDescription("Testing that read lock a task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);

        // test
        Task loadedTask = taskRepository.findOneWithinReadLockById(createdTask.getId());

        // assert
        Task selectedTask = taskEntityHelper.selectById(loadedTask.getId());
        assertThat(loadedTask.getTitle(), is(selectedTask.getTitle()));
        assertThat(loadedTask.getDescription(), is(selectedTask.getDescription()));
        assertThat(loadedTask.getDeadlineDate(), is(selectedTask.getDeadlineDate()));
        assertThat(loadedTask.getCreatedAt(), is(selectedTask.getCreatedAt()));
        assertThat(loadedTask.isFinished(), is(selectedTask.isFinished()));
        assertThat(loadedTask.getFinishedAt(), is(selectedTask.getFinishedAt()));
        assertThat(loadedTask.getVersion(), is(0L));
        assertThat(selectedTask.getVersion(), is(0L));

        assertThat(dbLog.countByMessagePattern("/\\* select generatedAlias0 from Task as generatedAlias0 where generatedAlias0\\.id=:param0 \\*/ select .* from .* where .* for update"), is((0)));
        assertThat(dbLog.countByMessagePattern("/\\* get version com.example.domain.model.Task \\*/"), is((1)));

    }

    /**
     * LockModeType.OPTIMISTICを指定してロックを取得する。
     */
    @Test
    public void optimisticLock() {

        // setup
        DateTime currentDateTime = new DateTime();

        Task newTask = new Task();
        newTask.setTitle("[Test] optimistic lock task");
        newTask.setDescription("Testing that optimistic lock a task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);

        // test
        Task loadedTask = taskRepository.findOneWithinOptimisticLockById(createdTask.getId());

        // assert
        Task selectedTask = taskEntityHelper.selectById(loadedTask.getId());
        assertThat(loadedTask.getTitle(), is(selectedTask.getTitle()));
        assertThat(loadedTask.getDescription(), is(selectedTask.getDescription()));
        assertThat(loadedTask.getDeadlineDate(), is(selectedTask.getDeadlineDate()));
        assertThat(loadedTask.getCreatedAt(), is(selectedTask.getCreatedAt()));
        assertThat(loadedTask.isFinished(), is(selectedTask.isFinished()));
        assertThat(loadedTask.getFinishedAt(), is(selectedTask.getFinishedAt()));
        assertThat(loadedTask.getVersion(), is(0L));
        assertThat(selectedTask.getVersion(), is(0L));

        assertThat(dbLog.countByMessagePattern("/\\* select generatedAlias0 from Task as generatedAlias0 where generatedAlias0\\.id=:param0 \\*/ select .* from .* where .* for update"), is((0)));
        assertThat(dbLog.countByMessagePattern("/\\* get version com.example.domain.model.Task \\*/"), is((1)));

    }


}
