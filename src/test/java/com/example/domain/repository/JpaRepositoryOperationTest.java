package com.example.domain.repository;

import com.example.domain.model.Task;
import com.example.domain.repository.helper.DBLog;
import com.example.domain.repository.helper.TaskEntityHelper;
import com.example.domain.repository.task.TaskRepository;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

/**
 * エンティティへの基本操作をテストする。
 * <p/>
 * JpaRepositoryから提供されているメソッドのテストを行う。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class JpaRepositoryOperationTest {

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
     * JpaRepository#save(T) を使用して、Entityを1件登録する。
     * <p/>
     * EntityManager#persistメソッドのテスト
     */
    @Test
    public void persistEntityUsingSave() {

        // setup
        DateTime currentDateTime = new DateTime();

        // test
        Task newTask = new Task();
        newTask.setTitle("[Test] create new task");
        newTask.setDescription("Testing that create a new task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);

        // assert
        Task selectedTask = taskEntityHelper.selectById(createdTask.getId());
        assertThat(selectedTask.getTitle(), is("[Test] create new task"));
        assertThat(selectedTask.getDescription(), is("Testing that create a new task."));
        assertThat(selectedTask.getDeadlineDate(), is(currentDateTime.plusDays(7).toLocalDate().toDate()));
        assertThat(selectedTask.getCreatedAt(), is((Date) new Timestamp(currentDateTime.toDate().getTime())));
        assertThat(selectedTask.isFinished(), is(Boolean.FALSE));
        assertThat(selectedTask.getFinishedAt(), is(nullValue()));
        assertThat(selectedTask.getVersion(), is(0L));

    }


    /**
     * JpaRepository#flush(T) を使用して、蓄積したEntityの登録操作(persist)をデータベースに反映する。
     * <p/>
     * EntityManager#flushメソッドのテスト
     * <p/>
     * flushメソッドの動作を確認するために、@Transactionalアノテーションを付与してテストケース自体をトランザクション管理下にする。
     */
    @Transactional
    @Test
    public void flush() {

        // setup
        DateTime currentDateTime = new DateTime();

        Task newTask = new Task();
        newTask.setTitle("[Test] create new task");
        newTask.setDescription("Testing that create a new task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);

        //test
        taskRepository.flush();

        // assert
        Task selectedTask = taskEntityHelper.selectById(createdTask.getId());
        assertThat(selectedTask.getTitle(), is("[Test] create new task"));
        assertThat(selectedTask.getDescription(), is("Testing that create a new task."));
        assertThat(selectedTask.getDeadlineDate(), is(currentDateTime.plusDays(7).toLocalDate().toDate()));
        assertThat(selectedTask.getCreatedAt(), is((Date) new Timestamp(currentDateTime.toDate().getTime())));
        assertThat(selectedTask.isFinished(), is(Boolean.FALSE));
        assertThat(selectedTask.getFinishedAt(), is(nullValue()));
        assertThat(selectedTask.getVersion(), is(0L));

    }

    /**
     * JpaRepository#saveAndFlush(T) を使用して、指定したEntityを即時にデータベースに登録する。
     * <p/>
     * flushメソッドの動作を確認するために、@Transactionalアノテーションを付与してテストケース自体をトランザクション管理下にする。
     */
    @Transactional
    @Test
    public void saveAndFlush() {

        // setup
        DateTime currentDateTime = new DateTime();

        // test
        Task newTask = new Task();
        newTask.setTitle("[Test] create new task");
        newTask.setDescription("Testing that create a new task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.saveAndFlush(newTask);

        // assert
        Task selectedTask = taskEntityHelper.selectById(createdTask.getId());
        assertThat(selectedTask.getTitle(), is("[Test] create new task"));
        assertThat(selectedTask.getDescription(), is("Testing that create a new task."));
        assertThat(selectedTask.getDeadlineDate(), is(currentDateTime.plusDays(7).toLocalDate().toDate()));
        assertThat(selectedTask.getCreatedAt(), is((Date) new Timestamp(currentDateTime.toDate().getTime())));
        assertThat(selectedTask.isFinished(), is(Boolean.FALSE));
        assertThat(selectedTask.getFinishedAt(), is(nullValue()));
        assertThat(selectedTask.getVersion(), is(0L));

    }


    /**
     * JpaRepository#save(Iterable<S>) を使用して、複数のEntityを登録する。
     */
    @Test
    public void persistEntitiesUsingSave() {

        // setup
        DateTime currentDateTime = new DateTime();

        // test
        Task newTask1 = new Task();
        newTask1.setTitle("[Test] create new task 1");
        newTask1.setDescription("Testing that create a new task 1.");
        newTask1.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask1.setCreatedAt(currentDateTime.toDate());

        Task newTask2 = new Task();
        newTask2.setTitle("[Test] create new task 2");
        newTask2.setDescription("Testing that create a new task 2.");
        newTask2.setDeadlineDate(currentDateTime.plusDays(14).toLocalDate().toDate());
        newTask2.setCreatedAt(currentDateTime.toDate());

        List<Task> createdTasks = taskRepository.save(Arrays.asList(newTask1, newTask2));

        // assert first task.
        {
            Task createdTask = createdTasks.get(0);
            Task selectedTask = taskEntityHelper.selectById(createdTask.getId());
            assertThat(selectedTask.getTitle(), is("[Test] create new task 1"));
            assertThat(selectedTask.getDescription(), is("Testing that create a new task 1."));
            assertThat(selectedTask.getDeadlineDate(), is(currentDateTime.plusDays(7).toLocalDate().toDate()));
            assertThat(selectedTask.getCreatedAt(), is((Date) new Timestamp(currentDateTime.toDate().getTime())));
            assertThat(selectedTask.isFinished(), is(Boolean.FALSE));
            assertThat(selectedTask.getFinishedAt(), is(nullValue()));
            assertThat(selectedTask.getVersion(), is(0L));
        }

        // assert first task.
        {
            Task createdTask = createdTasks.get(1);
            Task selectedTask = taskEntityHelper.selectById(createdTask.getId());
            assertThat(selectedTask.getTitle(), is("[Test] create new task 2"));
            assertThat(selectedTask.getDescription(), is("Testing that create a new task 2."));
            assertThat(selectedTask.getDeadlineDate(), is(currentDateTime.plusDays(14).toLocalDate().toDate()));
            assertThat(selectedTask.getCreatedAt(), is((Date) new Timestamp(currentDateTime.toDate().getTime())));
            assertThat(selectedTask.isFinished(), is(Boolean.FALSE));
            assertThat(selectedTask.getFinishedAt(), is(nullValue()));
            assertThat(selectedTask.getVersion(), is(0L));
        }

    }

    /**
     * JpaRepository#save(T) を使用して、Entityを1件更新する。
     * <p/>
     * EntityManager#mergeメソッドのテスト
     */
    @Test
    public void mergeEntityUsingSave() {

        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask = new Task();
        newTask.setTitle("[Test] create new task");
        newTask.setDescription("Testing that create a new task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);

        // test
        createdTask.setTitle("[Test] update new task");
        createdTask.setDescription("Testing that update a new task.");
        createdTask.setDeadlineDate(currentDateTime.plusDays(14).toLocalDate().toDate());
        createdTask.setFinished(true);
        createdTask.setFinishedAt(currentDateTime.plusDays(1).toDate());
        Task updatedTask = taskRepository.save(createdTask);

        // assert
        assertFalse(newTask == updatedTask);
        Task selectedTask = taskEntityHelper.selectById(updatedTask.getId());
        assertThat(selectedTask.getTitle(), is("[Test] update new task"));
        assertThat(selectedTask.getDescription(), is("Testing that update a new task."));
        assertThat(selectedTask.getDeadlineDate(), is(currentDateTime.plusDays(14).toLocalDate().toDate()));
        assertThat(selectedTask.getCreatedAt(), is((Date) new Timestamp(currentDateTime.toDate().getTime())));
        assertThat(selectedTask.isFinished(), is(Boolean.TRUE));
        assertThat(selectedTask.getFinishedAt(), is((Date) new Timestamp(currentDateTime.plusDays(1).toDate().getTime())));
        assertThat(selectedTask.getVersion(), is(1L));

    }


    /**
     * EntityManagerの管理下のEntityのプロパティを更新して、Entityを1件更新する。
     * <p/>
     * EntityManagerの管理下にあるEntityを操作するために、@Transactionalアノテーションを付与してテストケース自体をトランザクション管理下にする。
     * また、プロパティ変更によって更新処理が実行されることを確認するために、明示的にflushメソッドを呼び出す。（本来であればトランザクションコミット時に行われる処理を明示的に行う）
     */
    @Transactional
    @Test
    public void updateEntityUsingUpdateProperty() {

        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask = new Task();
        newTask.setTitle("[Test] create new task");
        newTask.setDescription("Testing that create a new task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.saveAndFlush(newTask);

        // test
        createdTask.setTitle("[Test] update new task");
        createdTask.setDescription("Testing that update a new task.");
        createdTask.setDeadlineDate(currentDateTime.plusDays(14).toLocalDate().toDate());
        createdTask.setFinished(true);
        createdTask.setFinishedAt(currentDateTime.plusDays(1).toDate());

        taskRepository.flush();

        // assert
        Task selectedTask = taskEntityHelper.selectById(createdTask.getId());
        assertThat(selectedTask.getTitle(), is("[Test] update new task"));
        assertThat(selectedTask.getDescription(), is("Testing that update a new task."));
        assertThat(selectedTask.getDeadlineDate(), is(currentDateTime.plusDays(14).toLocalDate().toDate()));
        assertThat(selectedTask.getCreatedAt(), is((Date) new Timestamp(currentDateTime.toDate().getTime())));
        assertThat(selectedTask.isFinished(), is(Boolean.TRUE));
        assertThat(selectedTask.getFinishedAt(), is((Date) new Timestamp(currentDateTime.plusDays(1).toDate().getTime())));
        assertThat(selectedTask.getVersion(), is(1L));

    }

    /**
     * JpaRepository#save(Iterable<S>) を使用して、複数のEntityを更新する。
     */
    @Test
    public void mergeEntitiesUsingSave() {

        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask1 = new Task();
        newTask1.setTitle("[Test] create new task 1");
        newTask1.setDescription("Testing that create a new task 1.");
        newTask1.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask1.setCreatedAt(currentDateTime.toDate());
        Task createdTask1 = taskRepository.save(newTask1);

        Task newTask2 = new Task();
        newTask2.setTitle("[Test] create new task 2");
        newTask2.setDescription("Testing that create a new task 2.");
        newTask2.setDeadlineDate(currentDateTime.plusDays(14).toLocalDate().toDate());
        newTask2.setCreatedAt(currentDateTime.toDate());
        Task createdTask2 = taskRepository.save(newTask2);

        // test
        createdTask1.setTitle("[Test] update new task 1");
        createdTask1.setDescription("Testing that update a new task 1.");
        createdTask1.setDeadlineDate(currentDateTime.plusDays(8).toLocalDate().toDate());
        createdTask1.setFinished(true);
        createdTask1.setFinishedAt(currentDateTime.plusDays(1).toDate());

        createdTask2.setTitle("[Test] update new task 2");
        createdTask2.setDescription("Testing that update a new task 2.");
        createdTask2.setDeadlineDate(currentDateTime.plusDays(15).toLocalDate().toDate());
        createdTask2.setFinished(true);
        createdTask2.setFinishedAt(currentDateTime.plusDays(1).toDate());

        List<Task> updatedTasks = taskRepository.save(Arrays.asList(createdTask1, createdTask2));

        // assert
        {
            Task updatedTask = updatedTasks.get(0);
            assertFalse(newTask1 == updatedTask);
            Task selectedTask = taskEntityHelper.selectById(updatedTask.getId());
            assertThat(selectedTask.getTitle(), is("[Test] update new task 1"));
            assertThat(selectedTask.getDescription(), is("Testing that update a new task 1."));
            assertThat(selectedTask.getDeadlineDate(), is(currentDateTime.plusDays(8).toLocalDate().toDate()));
            assertThat(selectedTask.getCreatedAt(), is((Date) new Timestamp(currentDateTime.toDate().getTime())));
            assertThat(selectedTask.isFinished(), is(Boolean.TRUE));
            assertThat(selectedTask.getFinishedAt(), is((Date) new Timestamp(currentDateTime.plusDays(1).toDate().getTime())));
            assertThat(selectedTask.getVersion(), is(1L));
        }
        {
            Task updatedTask = updatedTasks.get(1);
            assertFalse(newTask2 == updatedTask);
            Task selectedTask = taskEntityHelper.selectById(updatedTask.getId());
            assertThat(selectedTask.getTitle(), is("[Test] update new task 2"));
            assertThat(selectedTask.getDescription(), is("Testing that update a new task 2."));
            assertThat(selectedTask.getDeadlineDate(), is(currentDateTime.plusDays(15).toLocalDate().toDate()));
            assertThat(selectedTask.getCreatedAt(), is((Date) new Timestamp(currentDateTime.toDate().getTime())));
            assertThat(selectedTask.isFinished(), is(Boolean.TRUE));
            assertThat(selectedTask.getFinishedAt(), is((Date) new Timestamp(currentDateTime.plusDays(1).toDate().getTime())));
            assertThat(selectedTask.getVersion(), is(1L));
        }
    }

    /**
     * JpaRepository#delete(T) を使用して、Entityを1件削除する。
     * <p/>
     * EntityManager#removeメソッドのテスト
     */
    @Test
    public void deleteByEntity() {

        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask = new Task();
        newTask.setTitle("[Test] delete task");
        newTask.setDescription("Testing that delete a task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());

        Task createdTask = taskRepository.save(newTask);

        {
            // test
            taskRepository.delete(createdTask);
            // assert
            assertThat(taskEntityHelper.exists(createdTask.getId()), is(false));
        }

        {
            // test on status that already deleted
            taskRepository.delete(createdTask);
            // assert
            assertThat(taskEntityHelper.exists(createdTask.getId()), is(false));
        }
    }

    /**
     * JpaRepository#delete(ID) を使用して、Entityを1件削除する。
     */
    @Test
    public void deleteById() {

        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask = new Task();
        newTask.setTitle("[Test] delete task");
        newTask.setDescription("Testing that delete a task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);


        {
            // test
            taskRepository.delete(createdTask.getId());
            // assert
            assertThat(taskEntityHelper.exists(createdTask.getId()), is(false));
        }

        // test on status that already deleted
        {
            try {
                taskRepository.delete("foo");
                fail();
            } catch (EmptyResultDataAccessException e) {
                assertThat(e.getMessage(), is("No class com.example.domain.model.Task entity with id foo exists!"));
            }
        }
    }

    /**
     * JpaRepository#delete(Iterable<T>) を使用して、Entityを複数削除する。
     */
    @Test
    public void deleteByEntities() {

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

        {
            // test
            taskRepository.delete(createdTasks);
            // assert
            assertThat(taskEntityHelper.exists(createdTasks.get(0).getId()), is(false));
            assertThat(taskEntityHelper.exists(createdTasks.get(1).getId()), is(false));
        }

        {
            // test on status that already deleted
            taskRepository.delete(createdTasks);
            // assert
            assertThat(taskEntityHelper.exists(createdTasks.get(0).getId()), is(false));
            assertThat(taskEntityHelper.exists(createdTasks.get(1).getId()), is(false));
        }
    }


    /**
     * JpaRepository#deleteInBatch(Iterable<T>) を使用して、Entityを複数削除する。
     */
    @Test
    public void deleteInBatchEntities() {

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

        {
            // test
            taskRepository.deleteInBatch(createdTasks);
            // assert
            assertThat(taskEntityHelper.exists(createdTasks.get(0).getId()), is(false));
            assertThat(taskEntityHelper.exists(createdTasks.get(1).getId()), is(false));
        }

        {
            // test on status that already deleted
            taskRepository.deleteInBatch(createdTasks);
            // assert
            assertThat(taskEntityHelper.exists(createdTasks.get(0).getId()), is(false));
            assertThat(taskEntityHelper.exists(createdTasks.get(1).getId()), is(false));
        }
    }

    /**
     * JpaRepository#deleteAllInBatch() を使用して、Entityを全て削除する。
     */
    @Transactional
    @Test
    public void deleteAllInBatch() {

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

        taskRepository.save(Arrays.asList(newTask1, newTask2));

        {
            // test
            taskRepository.deleteAllInBatch();
            // assert
            assertThat(taskEntityHelper.countAll(Integer.class), is(0));
        }

        {
            // test on status that already deleted
            taskRepository.deleteAllInBatch();
            // assert
            assertThat(taskEntityHelper.countAll(Integer.class), is(0));
        }
    }


    /**
     * JpaRepository#deleteAll() を使用して、Entityを全て削除する。
     * <p/>
     * テストデータを物理削除しないようにするために、@Transactionalアノテーションを付与してテストケース自体をトランザクション管理下にする。テスト終了時にロールバックされるため、テストデータは物理的には削除されない。
     */
    @Transactional
    @Test
    public void deleteAll() {

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

        taskRepository.save(Arrays.asList(newTask1, newTask2));

        // test
        taskRepository.deleteAll();
        taskRepository.flush();

        // assert
        assertThat(taskEntityHelper.countAll(Integer.class), is(0));

    }

    /**
     * JpaRepository#findOne(ID) を使用して、Entityを1件取得する。
     * <p/>
     * EntityManager#findメソッドのテスト
     */
    @Test
    public void findOne() {
        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask = new Task();
        newTask.setTitle("[Test] findOne");
        newTask.setDescription("Testing that find a new task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);

        {
            // test for exist
            Task loadedTask = taskRepository.findOne(createdTask.getId());

            // assert
            assertThat(loadedTask.getTitle(), is("[Test] findOne"));
            assertThat(loadedTask.getDescription(), is("Testing that find a new task."));
            assertThat(loadedTask.getDeadlineDate(), is(currentDateTime.plusDays(7).toLocalDate().toDate()));
            assertThat(loadedTask.getCreatedAt(), is((Date) new Timestamp(currentDateTime.toDate().getTime())));
            assertThat(loadedTask.isFinished(), is(Boolean.FALSE));
            assertThat(loadedTask.getFinishedAt(), is(nullValue()));
            assertThat(loadedTask.getVersion(), is(0L));
        }

        {
            // test for not exist
            Task loadedTask = taskRepository.findOne("foo");
            // assert
            assertThat(loadedTask, is(nullValue()));
        }

    }

    /**
     * JpaRepository#exist(ID) を使用して、指定したEntityが存在するか確認する。
     * <p/>
     * EntityManager#createQueryメソッドのテスト
     */
    @Test
    public void exists() {
        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        Task newTask = new Task();
        newTask.setTitle("[Test] findOne");
        newTask.setDescription("Testing that find a new task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);

        {
            // test for exist
            boolean exists = taskRepository.exists(createdTask.getId());

            // assert
            assertThat(exists, is(true));
        }

        {
            // test for not exist
            boolean exists = taskRepository.exists("foo");

            // assert
            assertThat(exists, is(false));
        }

    }

    /**
     * JpaRepository#count() を使用して、Entityの件数を取得する。
     */
    @Test
    public void count() {
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

    }

    /**
     * JpaRepository#findAll() を使用して、全てのEntityを取得する。
     */
    @Test
    public void findAll() {
        // setup
        DateTime currentDateTime = new DateTime();

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
        List<Task> loadedTasks = taskRepository.findAll();

        // assert
        assertThat(loadedTasks.size(), is(taskEntityHelper.countAll(Integer.class)));
        for (int i = 0; i < loadedTasks.size(); i++) {
            Task loadedTask = loadedTasks.get(i);
            Task selectedTask = taskEntityHelper.selectById(loadedTask.getId());
            String recordId = "record" + i;
            assertThat(recordId, loadedTask.getTitle(), is(selectedTask.getTitle()));
            assertThat(recordId, loadedTask.getDescription(), is(selectedTask.getDescription()));
            assertThat(recordId, loadedTask.getDeadlineDate(), is(selectedTask.getDeadlineDate()));
            assertThat(recordId, loadedTask.getCreatedAt(), is(selectedTask.getCreatedAt()));
            assertThat(recordId, loadedTask.isFinished(), is(selectedTask.isFinished()));
            assertThat(recordId, loadedTask.getFinishedAt(), is(selectedTask.getFinishedAt()));
            assertThat(recordId, loadedTask.getVersion(), is(selectedTask.getVersion()));
        }

    }

    /**
     * JpaRepository#findAll(Sort) を使用して、指定したソート条件に並び替えて状態で全てのEntityを取得する。
     * <p/>
     * JPAのソート検索との連携のテスト
     */
    @Test
    public void findAllSort() {
        // setup
        DateTime currentDateTime = new DateTime();

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
        Sort sort = new Sort(Sort.Direction.DESC, "title");
        List<Task> loadedTasks = taskRepository.findAll(sort);

        // assert
        assertThat(loadedTasks.size(), is(taskEntityHelper.countAll(Integer.class)));
        List<String> titles = new ArrayList<>();
        for (Task loadedTask : loadedTasks) {
            if (loadedTask.getId().startsWith("00000000-")) {
                continue;
            }
            titles.add(loadedTask.getTitle());
        }

        List<String> expectedTitles = taskEntityHelper.getJdbcOperations().queryForList("SELECT title FROM task WHERE id NOT LIKE '00000000-%' ORDER BY title DESC", String.class);

        assertThat(titles, is(expectedTitles));

    }

    /**
     * JpaRepository#findAll(Pageable) を使用して、指定したページ条件(ページ位置、ソート条件)に一致するEntityを取得する。
     * <p/>
     * JPAのソート検索及びページ検索との連携のテスト
     */
    @Test
    public void findPage() {
        // setup
        DateTime currentDateTime = new DateTime();

        // create test data
        List<Task> newTasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Task newTask = new Task();
            newTask.setTitle("[Test] task " + i);
            newTask.setDescription("Task description " + i + ".");
            newTask.setDeadlineDate(currentDateTime.plusDays(i).toLocalDate().toDate());
            newTask.setCreatedAt(currentDateTime.plusMinutes(i + 1).toDate());
            newTasks.add(newTask);
        }
        taskRepository.save(newTasks);

        // test
        PageRequest pageRequest = new PageRequest(0, 5, Sort.Direction.DESC, "createdAt");
        Page<Task> loadedTasks = taskRepository.findAll(pageRequest);

        // assert
        assertThat(loadedTasks.getTotalElements(), is(taskEntityHelper.countAll(Long.class)));
        assertThat(loadedTasks.getContent().size(), is(5));
        List<String> titles = new ArrayList<>();
        for (Task loadedTask : loadedTasks) {
            titles.add(loadedTask.getTitle());
        }
        List<String> expectedTitles = taskEntityHelper.getJdbcOperations().queryForList("SELECT title FROM task WHERE id NOT LIKE '00000000-%' ORDER BY created_at DESC", String.class);

        assertThat(titles, is(expectedTitles));

    }

    /**
     * JpaRepository#findAll(Iterable<ID>) を使用して、指定したIDのリストに一致するEntityを取得する。
     */
    @Test
    public void findAllByIds() {
        // setup
        DateTime currentDateTime = new DateTime();

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
        List<Task> createdTasks = taskRepository.save(newTasks);

        List<String> ids = new ArrayList<>();
        for (Task task : createdTasks) {
            ids.add(task.getId());
        }

        // test
        List<Task> loadedTasks = taskRepository.findAll(ids);

        // assert
        assertThat(loadedTasks.size(), is(ids.size()));
        for (Task loadedTask : loadedTasks) {
            assertThat(ids.contains(loadedTask.getId()), is(true));
        }

        assertThat(dbLog.countByMessagePattern("/\\* select generatedAlias0 from Task as generatedAlias0 where generatedAlias0\\.id in \\(:param0\\) \\*/"), is((1)));


    }

}
