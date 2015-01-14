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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class EntityLifecycleTest {

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
     * 同一トランザクション内にて、save、Entityの更新、Entityの削除を行った際のEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void saveAndUpdatePropertyAndDeleteAndFlushOnSameTransaction() {

        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask = new Task();
        newTask.setTitle("[Test] save, update and flush task");
        newTask.setDescription("Testing that save, update a task and flush.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());

        // test
        Task createdTask = taskRepository.save(newTask);

        createdTask.setFinished(true);
        createdTask.setFinishedAt(currentDateTime.toDate());

        taskRepository.delete(createdTask);

        taskRepository.flush();

        // assert
        assertThat(taskEntityHelper.exists(createdTask.getId()), is(false));

        assertThat(dbLog.countByMessagePattern("/\\* update com.example.domain.model.Task \\*/"), is((0)));

    }

    /**
     * 同一トランザクション内にて、delete(T)を使用してEntityを削除した際のEntityのライフサイクルの動作をテストする。（データベースへの反映(flush)は行わない）
     */
    @Transactional
    @Test
    public void findOneAtAfterDeleteOnSameTransaction() {

        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask = new Task();
        newTask.setTitle("[Test] delete task");
        newTask.setDescription("Testing that delete a task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());

        Task createdTask = taskRepository.save(newTask);
        taskRepository.delete(createdTask);

        // test
        Task loadedTask = taskRepository.findOne(createdTask.getId());

        // assert
        assertNull(loadedTask);
        assertThat(dbLog.countByMessagePattern("/\\* insert com.example.domain.model.Task \\*/"), is((0)));
        assertThat(dbLog.countByMessagePattern("/\\* delete com.example.domain.model.Task \\*/"), is((0)));
        assertThat(dbLog.countByMessagePattern("select .* from task .* where .*\\.id='.*' \\{executed in .* msec\\}"), is((0)));

    }

    /**
     * 同一トランザクション内にて、delete(T)を使用してEntityを削除した際のEntityのライフサイクルの動作をテストする。（delete後にデータベースへの反映(flush)を行う）
     */
    @Transactional
    @Test
    public void findOneAtAfterDeleteAndFlushOnSameTransaction() {

        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask = new Task();
        newTask.setTitle("[Test] delete task");
        newTask.setDescription("Testing that delete a task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());

        Task createdTask = taskRepository.save(newTask);
        taskRepository.delete(createdTask);
        taskRepository.flush();

        // test
        Task loadedTask = taskRepository.findOne(createdTask.getId());

        // assert
        assertNull(loadedTask);
        assertThat(dbLog.countByMessagePattern("/\\* insert com.example.domain.model.Task \\*/"), is((1)));
        assertThat(dbLog.countByMessagePattern("/\\* delete com.example.domain.model.Task \\*/"), is((1)));
        assertThat(dbLog.countByMessagePattern("select .* from task .* where .*\\.id='.*' \\{executed in .* msec\\}"), is((1)));

    }


    /**
     * 同一トランザクション内にて、deleteInBatch(Iterable<T>) を使用して削除した際のEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void findOneAtAfterDeleteInBatchEntitiesOnSameTransaction() {

        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask1 = new Task();
        newTask1.setTitle("[Test] delete task 1");
        newTask1.setDescription("Testing that delete a task 1.");
        newTask1.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask1.setCreatedAt(currentDateTime.toDate());

        Task newTask2 = new Task();
        newTask2.setTitle("[Test] delete task 2");
        newTask2.setDescription("Testing that delete a task 2.");
        newTask2.setDeadlineDate(currentDateTime.plusDays(8).toLocalDate().toDate());
        newTask2.setCreatedAt(currentDateTime.toDate());

        List<Task> createdTasks = taskRepository.save(Arrays.asList(newTask1, newTask2));

        // test
        taskRepository.deleteInBatch(createdTasks);
        Task removedTask1 = taskRepository.findOne(createdTasks.get(0).getId());
        Task removedTask2 = taskRepository.findOne(createdTasks.get(1).getId());

        // assert
        assertThat(taskEntityHelper.exists(createdTasks.get(0).getId()), is(false));
        assertThat(taskEntityHelper.exists(createdTasks.get(1).getId()), is(false));
        assertThat(removedTask1, is(notNullValue()));
        assertThat(removedTask2, is(notNullValue()));

        assertThat(dbLog.countByMessagePattern("select .* from task .* where .*\\.id='.*' \\{executed in .* msec\\}"), is((0)));

    }

    /**
     * 同一トランザクション内にて、deleteAllInBatch() を使用して削除した際のEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void findOneAtAfterDeleteAllInBatchOnSameTransaction() {

        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask1 = new Task();
        newTask1.setTitle("[Test] delete task 1");
        newTask1.setDescription("Testing that delete a task 1.");
        newTask1.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask1.setCreatedAt(currentDateTime.toDate());

        Task newTask2 = new Task();
        newTask2.setTitle("[Test] delete task 2");
        newTask2.setDescription("Testing that delete a task 2.");
        newTask2.setDeadlineDate(currentDateTime.plusDays(8).toLocalDate().toDate());
        newTask2.setCreatedAt(currentDateTime.toDate());

        List<Task> createdTasks = taskRepository.save(Arrays.asList(newTask1, newTask2));

        // test
        taskRepository.deleteAllInBatch();
        Task removedTask1 = taskRepository.findOne(createdTasks.get(0).getId());
        Task removedTask2 = taskRepository.findOne(createdTasks.get(1).getId());

        // assert
        assertThat(taskEntityHelper.countAll(Integer.class), is(0));
        assertThat(removedTask1, is(notNullValue()));
        assertThat(removedTask2, is(notNullValue()));

        assertThat(dbLog.countByMessagePattern("select .* from task .* where .*\\.id='.*' \\{executed in .* msec\\}"), is((0)));

    }

    /**
     * 同一トランザクション内にて、deleteAll() を使用して削除した際のEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void findOneAtAfterDeleteAllOnSameTransaction() {

        // test
        List<String> ids = taskEntityHelper.getJdbcOperations().queryForList("SELECT id FROM task", String.class);
        taskRepository.deleteAll();

        // assert
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            String recordId = "record" + i;
            assertThat(recordId, taskRepository.findOne(id) == null, is(true));
        }

        assertThat(dbLog.countByMessagePattern("select .* from task .* where .*\\.id='.*' \\{executed in .* msec\\}"), is((0)));

    }

    /**
     * 別トランザクションにて、findOne(ID) を使用してEntityを取得した際のEntityのライフサイクルの動作をテストする。
     */
    @Test
    public void findOneOnOtherTransaction() {

        // test & assert
        String id = "00000000-0000-0000-0000-000000000001";
        assertThat(taskRepository.findOne(id) == taskRepository.findOne(id), is(false));
        assertThat(dbLog.countByMessagePattern("select .* from task .* where .*\\.id='.*' \\{executed in .* msec\\}"), is((2)));

    }

    /**
     * 同一トランザクションにて、findOne(ID) を使用してEntityを取得した際のEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void findOneOnSameTransaction() {

        // test & assert
        String id = "00000000-0000-0000-0000-000000000001";
        assertThat(taskRepository.findOne(id) == taskRepository.findOne(id), is(true));
        assertThat(dbLog.countByMessagePattern("select .* from task .* where .*\\.id='.*' \\{executed in .* msec\\}"), is((1)));

    }

    /**
     * 同一トランザクションにて、saveAndFlush(T) メソッドを呼び出した後にfindOne(ID) を使用してEntityを取得した際のEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void findOneAtAfterSaveAndFlushOnSameTransaction() {
        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask = new Task();
        newTask.setTitle("[Test] findOne");
        newTask.setDescription("Testing that find a new task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.saveAndFlush(newTask);

        // test & assert
        assertThat(taskRepository.findOne(createdTask.getId()) == taskRepository.findOne(createdTask.getId()), is(true));
        assertThat(dbLog.countByMessagePattern("select .* from task .* where .*\\.id='.*' \\{executed in .* msec\\}"), is((0)));

    }

    /**
     * 同一トランザクションにて、save(T) メソッドを呼び出した後にfindAll() を使用してEntityを取得した際のEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void findAllAtAfterSaveOnSameTransaction() {
        // setup
        DateTime currentDateTime = new DateTime();

        // test
        Task newTask = new Task();
        newTask.setTitle("[Test] findOne");
        newTask.setDescription("Testing that find a new task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);

        // test
        List<Task> loadedTasks = taskRepository.findAll();

        // assert
        assertThat(loadedTasks.size(), is(taskEntityHelper.countAll(Integer.class)));
        assertThat(taskEntityHelper.exists(createdTask.getId()), is(true));

    }

    /**
     * 同一トランザクションにて、save(T) メソッドを呼び出した後にfindAll(Sort) を使用してEntityを取得した際のEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void findAllSortAtAfterSaveOnSameTransaction() {
        // setup
        DateTime currentDateTime = new DateTime();

        // test
        Task newTask = new Task();
        newTask.setTitle("[Test] findOne");
        newTask.setDescription("Testing that find a new task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);

        // test
        Sort sort = new Sort(Sort.Direction.DESC, "title");
        List<Task> loadedTasks = taskRepository.findAll(sort);

        // assert
        assertThat(loadedTasks.size(), is(taskEntityHelper.countAll(Integer.class)));
        assertThat(taskEntityHelper.exists(createdTask.getId()), is(true));

    }

    /**
     * 同一トランザクションにて、save(T) メソッドを呼び出した後にfindAll(Pageable) を使用してEntityを取得した際のEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void findPageAtAfterSaveOnSameTransaction() {
        // setup
        DateTime currentDateTime = new DateTime();

        // test
        Task newTask = new Task();
        newTask.setTitle("[Test] findOne");
        newTask.setDescription("Testing that find a new task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);

        // test
        PageRequest pageRequest = new PageRequest(0, 100, Sort.Direction.DESC, "createdAt");
        Page<Task> loadedTasks = taskRepository.findAll(pageRequest);

        // assert
        assertThat(loadedTasks.getContent().size(), is(taskEntityHelper.countAll(Integer.class)));
        assertThat(taskEntityHelper.exists(createdTask.getId()), is(true));

    }

    /**
     * 同一トランザクションにて、save(T) メソッドを呼び出した後にfindAll(Iterable<ID>) を使用してEntityを取得した際のEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void findAllByIdsAtAfterSaveOnSameTransaction() {
        // setup
        DateTime currentDateTime = new DateTime();

        // test
        Task newTask = new Task();
        newTask.setTitle("[Test] findOne");
        newTask.setDescription("Testing that find a new task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);

        // test
        List<String> ids = taskEntityHelper.getJdbcOperations().queryForList("SELECT id FROM task", String.class);
        List<Task> loadedTasks = taskRepository.findAll(ids);

        // assert
        assertThat(loadedTasks.size(), is(taskEntityHelper.countAll(Integer.class) - 1));
        assertThat(taskEntityHelper.exists(createdTask.getId()), is(true));

    }

    /**
     * findAll() を使用して取得したEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void findOneAtAfterFindAllOnSameTransaction() {

        // setup
        final List<Task> loadedTasks = taskRepository.findAll();


        taskEntityHelper.executeWithinNewTransaction(new TransactionalCommand() {
            @Override
            public void execute() {
                DateTime currentDateTime = new DateTime();
                Task newTask = new Task();
                newTask.setTitle("[Test] create new task");
                newTask.setDescription("Testing that create a new task.");
                newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
                newTask.setCreatedAt(currentDateTime.toDate());
                taskRepository.save(newTask);
            }
        });

        // test
        List<Task> latestLoadedTasks = taskRepository.findAll();

        // assert
        assertThat(latestLoadedTasks.size(), is(not(0)));
        assertThat(latestLoadedTasks.size() - loadedTasks.size(), is(1));
        for (int i = 0; i < latestLoadedTasks.size(); i++) {
            Task loadedTask = latestLoadedTasks.get(i);
            String recordId = "record" + i;
            assertThat(recordId, loadedTask == taskRepository.findOne(loadedTask.getId()), is(true));
            assertThat(recordId, loadedTask.getVersion(), is(0L));
        }
        assertThat(dbLog.countByMessagePattern("select .* from task .* where .*\\.id='.*' \\{executed in .* msec\\}"), is((0)));

    }

    /**
     * findAll(Sort) を使用して取得したEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void findOneAtAfterFindAllSortOnSameTransaction() {

        // test
        Sort sort = new Sort(Sort.Direction.DESC, "title");
        final List<Task> loadedTasks = taskRepository.findAll(sort);

        taskEntityHelper.executeWithinNewTransaction(new TransactionalCommand() {
            @Override
            public void execute() {
                DateTime currentDateTime = new DateTime();
                Task newTask = new Task();
                newTask.setTitle("[Test] create new task");
                newTask.setDescription("Testing that create a new task.");
                newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
                newTask.setCreatedAt(currentDateTime.toDate());
                taskRepository.save(newTask);
            }
        });

        // test
        List<Task> latestLoadedTasks = taskRepository.findAll(sort);

        // assert
        assertThat(latestLoadedTasks.size(), is(not(0)));
        assertThat(latestLoadedTasks.size() - loadedTasks.size(), is(1));
        for (int i = 0; i < latestLoadedTasks.size(); i++) {
            Task loadedTask = latestLoadedTasks.get(i);
            String recordId = "record" + i;
            assertThat(recordId, loadedTask == taskRepository.findOne(loadedTask.getId()), is(true));
            assertThat(recordId, loadedTask.getVersion(), is(0L));
        }
        assertThat(dbLog.countByMessagePattern("select .* from task .* where .*\\.id='.*' \\{executed in .* msec\\}"), is((0)));

    }

    /**
     * findAll(Pageable) を使用して取得したEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void findOneAtAfterFindPageOnSameTransaction() {

        // test
        PageRequest pageRequest = new PageRequest(0, 100, Sort.Direction.DESC, "createdAt");
        final Page<Task> loadedTasks = taskRepository.findAll(pageRequest);

        taskEntityHelper.executeWithinNewTransaction(new TransactionalCommand() {
            @Override
            public void execute() {
                DateTime currentDateTime = new DateTime();
                Task newTask = new Task();
                newTask.setTitle("[Test] create new task");
                newTask.setDescription("Testing that create a new task.");
                newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
                newTask.setCreatedAt(currentDateTime.toDate());
                taskRepository.save(newTask);
            }
        });

        final Page<Task> latestLoadedTasks = taskRepository.findAll(pageRequest);

        // assert
        assertThat(latestLoadedTasks.getContent().size(), is(not(0)));
        assertThat(latestLoadedTasks.getContent().size() - loadedTasks.getContent().size(), is(1));
        for (int i = 0; i < latestLoadedTasks.getContent().size(); i++) {
            Task loadedTask = latestLoadedTasks.getContent().get(i);
            String recordId = "record" + i;
            assertThat(recordId, loadedTask == taskRepository.findOne(loadedTask.getId()), is(true));
            assertThat(recordId, loadedTask.getVersion(), is(0L));
        }
        assertThat(dbLog.countByMessagePattern("select .* from task .* where .*\\.id='.*' \\{executed in .* msec\\}"), is((0)));

    }

    /**
     * findAll(Iterable<ID>) を使用して取得したEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void findOneAtAfterFindAllByIdsOnSameTransaction() {

        // test
        final List<String> ids = taskEntityHelper.getJdbcOperations().queryForList("SELECT id FROM task", String.class);
        final List<Task> loadedTasks = taskRepository.findAll(ids);

        taskEntityHelper.executeWithinNewTransaction(new TransactionalCommand() {
            @Override
            public void execute() {
                DateTime currentDateTime = new DateTime();
                Task newTask = new Task();
                newTask.setTitle("[Test] create new task");
                newTask.setDescription("Testing that create a new task.");
                newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
                newTask.setCreatedAt(currentDateTime.toDate());
                Task createdTask = taskRepository.save(newTask);
                ids.add(createdTask.getId());
            }
        });

        final List<Task> latestLoadedTasks = taskRepository.findAll(ids);

        // assert
        assertThat(latestLoadedTasks.size(), is(not(0)));
        assertThat(latestLoadedTasks.size() - loadedTasks.size(), is(1));
        for (int i = 0; i < latestLoadedTasks.size(); i++) {
            Task loadedTask = latestLoadedTasks.get(i);
            String recordId = "record" + i;
            assertThat(recordId, loadedTask == taskRepository.findOne(loadedTask.getId()), is(true));
            assertThat(recordId, loadedTask.getVersion(), is(0L));
        }
        assertThat(dbLog.countByMessagePattern("select .* from task .* where .*\\.id='.*' \\{executed in .* msec\\}"), is((0)));

    }

    /**
     * 同一トランザクションにて、save(T) メソッドを呼び出した後にexists(ID) を使用してEntityを取得した際のEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void existsAtAfterSaveOnSameTransaction() {
        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask = new Task();
        newTask.setTitle("[Test] findOne");
        newTask.setDescription("Testing that find a new task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);

        // test
        boolean exists = taskRepository.exists(createdTask.getId());

        // assert
        assertThat(exists, is(true));
        assertThat(taskEntityHelper.selectById(createdTask.getId()) != null, is(true));
        assertThat(dbLog.countByMessagePattern("/\\* insert com.example.domain.model.Task \\*/"), is((1)));
        assertThat(dbLog.countByMessagePattern("/\\* select count\\(\\*\\) from Task x WHERE x\\.id = :id AND 1 = 1 \\*/"), is((1)));

    }

    /**
     * 同一トランザクションにて、save(T) メソッドを呼び出した後にcount() を使用してEntityを取得した際のEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void countAtAfterSaveOnSameTransaction() {
        // setup
        DateTime currentDateTime = new DateTime();

        long beforeCount = taskEntityHelper.countAll(Long.class);

        // create test data
        List<Task> newTasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Task newTask = new Task();
            newTask.setTitle("[Test] task " + i);
            newTask.setDescription("Task description " + i + ".");
            newTask.setDeadlineDate(currentDateTime.plusDays(i).toLocalDate().toDate());
            newTask.setCreatedAt(currentDateTime.toDate());
            newTasks.add(newTask);
        }
        taskRepository.save(newTasks);

        // test
        long count = taskRepository.count();

        // assert
        assertThat(count - beforeCount, is(5L));
        assertThat(count, is(taskEntityHelper.countAll(Long.class)));
        assertThat(dbLog.countByMessagePattern("/\\* insert com.example.domain.model.Task \\*/"), is((1)));
        assertThat(dbLog.countByMessagePattern("/\\* select count\\(\\*\\) from Task x \\*/"), is((1)));

    }

    /**
     * Query Method を使用して取得したEntityのライフサイクルの動作をテストする。
     */
    @Transactional
    @Test
    public void findOneAtAfterFindAllUsingQueryMethodOnSameTransaction() {

        // test
        final List<Task> loadedTasks = taskRepository.findAllByIdStartingWithAndFinished("00000000-", true);


        // assert
        assertThat(loadedTasks.size(), is(not(0)));
        for (int i = 0; i < loadedTasks.size(); i++) {
            Task loadedTask = loadedTasks.get(i);
            String recordId = "record" + i;
            assertThat(recordId, loadedTask == taskRepository.findOne(loadedTask.getId()), is(true));
        }
        assertThat(dbLog.countByMessagePattern("select .* from task .* where .*\\.id='.*' \\{executed in .* msec\\}"), is((0)));

    }

    /**
     * 同一トランザクションにて、findOne系のQuery Methodを使用して取得したEntityのライフサイクルの動作をテストする。(Query Method同士)
     */
    @Transactional
    @Test
    public void findOneUsingQuerySameTransaction() {
        String id = "00000000-0000-0000-0000-000000000001";
        assertThat(taskRepository.findOneById(id) == taskRepository.findOneById(id), is(true));
        assertThat(dbLog.countByMessagePattern("/\\* select generatedAlias0 from Task as generatedAlias0 where generatedAlias0\\.id=:param0 \\*/"), is((2)));

    }

    /**
     * 同一トランザクションにて、findOne系のQuery Methodを使用して取得したEntityのライフサイクルの動作をテストする。(Query Method + findOne)
     */
    @Transactional
    @Test
    public void findOneAtAfterFindOneUsingQueryMethodOnSameTransaction() {

        // test
        String id = "00000000-0000-0000-0000-000000000001";
        assertThat(taskRepository.findOneById(id) == taskRepository.findOne(id), is(true));
        assertThat(dbLog.countByMessagePattern("select .* from task .* where .*\\.id='.*' \\{executed in .* msec\\}"), is((1)));

    }

}
