package com.example.domain.repository;

import com.example.domain.model.Task;
import com.example.domain.repository.helper.DBLog;
import com.example.domain.repository.helper.TaskEntityHelper;
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

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

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
public class QueryMethodTest {

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
     * @Queryアノテーションを使用して追加した findAll系のメソッドの動作をテストする。
     */
    @Test
    public void findAllUsingQueryAnnotation() {

        // test that find unfinished task list
        List<Task> tasks = taskRepository.findAllByFinished("00000000-", false);

        assertThat(tasks.size(), is(2));
        assertThat(tasks.get(0).getId(), is("00000000-0000-0000-0000-000000000001"));
        assertThat(tasks.get(1).getId(), is("00000000-0000-0000-0000-000000000002"));

        // test that find finished task list
        tasks = taskRepository.findAllByFinished("00000000-", true);
        assertThat(tasks.size(), is(2));
        assertThat(tasks.get(0).getId(), is("00000000-1000-0000-0000-000000000003"));
        assertThat(tasks.get(1).getId(), is("00000000-1000-0000-0000-000000000004"));

    }

    /**
     * @Queryアノテーションを使用して追加した findSortedAll系のメソッドの動作をテストする。
     */
    @Test
    public void findSortedAllUsingQueryAnnotation() {

        Sort sort = new Sort(Sort.Direction.DESC, "createdAt");

        // test that find unfinished task list
        List<Task> tasks = taskRepository.findSortedAllByFinished("00000000-", false, sort);

        assertThat(tasks.size(), is(2));
        assertThat(tasks.get(0).getId(), is("00000000-0000-0000-0000-000000000002"));
        assertThat(tasks.get(1).getId(), is("00000000-0000-0000-0000-000000000001"));

        // test that find finished task list
        tasks = taskRepository.findSortedAllByFinished("00000000-", true, sort);
        assertThat(tasks.size(), is(2));
        assertThat(tasks.get(0).getId(), is("00000000-1000-0000-0000-000000000004"));
        assertThat(tasks.get(1).getId(), is("00000000-1000-0000-0000-000000000003"));
    }

    /**
     * @Queryアノテーションを使用して追加した findPage系のメソッドの動作をテストする。
     */
    @Test
    public void findPageUsingQueryAnnotation() {

        // test for finished.
        {
            // test that find unfinished task of 1 page
            PageRequest pageRequestOfPage1 = new PageRequest(0, 1, Sort.Direction.ASC, "createdAt");
            Page<Task> tasksOfPage1 = taskRepository.findPageByFinished("00000000-", false, pageRequestOfPage1);

            // test that find unfinished task of 2 page
            PageRequest pageRequestOfPage2 = new PageRequest(1, 1, Sort.Direction.ASC, "createdAt");
            Page<Task> tasksOfPage2 = taskRepository.findPageByFinished("00000000-", false, pageRequestOfPage2);

            assertThat(tasksOfPage1.getTotalElements(), is(2L));
            assertThat(tasksOfPage1.getContent().size(), is(1));
            assertThat(tasksOfPage1.getContent().get(0).getId(), is("00000000-0000-0000-0000-000000000001"));
            assertThat(tasksOfPage2.getTotalElements(), is(2L));
            assertThat(tasksOfPage2.getContent().size(), is(1));
            assertThat(tasksOfPage2.getContent().get(0).getId(), is("00000000-0000-0000-0000-000000000002"));
        }

        // test for finished.
        {

            // test that find finished task of 1 page
            PageRequest pageRequestOfPage1 = new PageRequest(0, 1, Sort.Direction.ASC, "createdAt");
            Page<Task> tasksOfPage1 = taskRepository.findPageByFinished("00000000-", true, pageRequestOfPage1);

            // test that find finished task of 2 page
            PageRequest pageRequestOfPage2 = new PageRequest(1, 1, Sort.Direction.ASC, "createdAt");
            Page<Task> tasksOfPage2 = taskRepository.findPageByFinished("00000000-", true, pageRequestOfPage2);

            assertThat(tasksOfPage1.getTotalElements(), is(2L));
            assertThat(tasksOfPage1.getContent().size(), is(1));
            assertThat(tasksOfPage1.getContent().get(0).getId(), is("00000000-1000-0000-0000-000000000003"));
            assertThat(tasksOfPage2.getTotalElements(), is(2L));
            assertThat(tasksOfPage2.getContent().size(), is(1));
            assertThat(tasksOfPage2.getContent().get(0).getId(), is("00000000-1000-0000-0000-000000000004"));
        }
    }


    /**
     * メソッドシグネチャからクエリを生成したfindAll系のメソッドの動作をテストする。
     */
    @Test
    public void findAllUsingGenerateByMethodSignature() {

        // test that find unfinished task list
        List<Task> tasks = taskRepository.findAllByIdStartingWithAndFinished("00000000-", false);

        assertThat(tasks.size(), is(2));
        assertThat(tasks.get(0).getId(), is("00000000-0000-0000-0000-000000000001"));
        assertThat(tasks.get(1).getId(), is("00000000-0000-0000-0000-000000000002"));

        // test that find finished task list
        tasks = taskRepository.findAllByIdStartingWithAndFinished("00000000-", true);
        assertThat(tasks.size(), is(2));
        assertThat(tasks.get(0).getId(), is("00000000-1000-0000-0000-000000000003"));
        assertThat(tasks.get(1).getId(), is("00000000-1000-0000-0000-000000000004"));

    }

    /**
     * メソッドシグネチャからクエリを生成したfindSortedAll系のメソッドの動作をテストする。
     */
    @Test
    public void findSortedAllUsingGenerateByMethodSignature() {

        Sort sort = new Sort(Sort.Direction.DESC, "createdAt");

        // test that find unfinished task list
        List<Task> tasks = taskRepository.findSortedAllByIdStartingWithAndFinished("00000000-", false, sort);

        assertThat(tasks.size(), is(2));
        assertThat(tasks.get(0).getId(), is("00000000-0000-0000-0000-000000000002"));
        assertThat(tasks.get(1).getId(), is("00000000-0000-0000-0000-000000000001"));

        // test that find finished task list
        tasks = taskRepository.findSortedAllByIdStartingWithAndFinished("00000000-", true, sort);
        assertThat(tasks.size(), is(2));
        assertThat(tasks.get(0).getId(), is("00000000-1000-0000-0000-000000000004"));
        assertThat(tasks.get(1).getId(), is("00000000-1000-0000-0000-000000000003"));
    }

    /**
     * メソッドシグネチャからクエリを生成したfindPage系のメソッドの動作をテストする。
     */
    @Test
    public void findPageUsingGenerateByMethodSignature() {

        // test for finished.
        {
            // test that find unfinished task of 1 page
            PageRequest pageRequestOfPage1 = new PageRequest(0, 1, Sort.Direction.ASC, "createdAt");
            Page<Task> tasksOfPage1 = taskRepository.findPageByIdStartingWithAndFinished("00000000-", false, pageRequestOfPage1);

            // test that find unfinished task of 2 page
            PageRequest pageRequestOfPage2 = new PageRequest(1, 1, Sort.Direction.ASC, "createdAt");
            Page<Task> tasksOfPage2 = taskRepository.findPageByIdStartingWithAndFinished("00000000-", false, pageRequestOfPage2);

            assertThat(tasksOfPage1.getTotalElements(), is(2L));
            assertThat(tasksOfPage1.getContent().size(), is(1));
            assertThat(tasksOfPage1.getContent().get(0).getId(), is("00000000-0000-0000-0000-000000000001"));
            assertThat(tasksOfPage2.getTotalElements(), is(2L));
            assertThat(tasksOfPage2.getContent().size(), is(1));
            assertThat(tasksOfPage2.getContent().get(0).getId(), is("00000000-0000-0000-0000-000000000002"));
        }

        // test for finished.
        {

            // test that find finished task of 1 page
            PageRequest pageRequestOfPage1 = new PageRequest(0, 1, Sort.Direction.ASC, "createdAt");
            Page<Task> tasksOfPage1 = taskRepository.findPageByIdStartingWithAndFinished("00000000-", true, pageRequestOfPage1);

            // test that find finished task of 2 page
            PageRequest pageRequestOfPage2 = new PageRequest(1, 1, Sort.Direction.ASC, "createdAt");
            Page<Task> tasksOfPage2 = taskRepository.findPageByIdStartingWithAndFinished("00000000-", true, pageRequestOfPage2);

            assertThat(tasksOfPage1.getTotalElements(), is(2L));
            assertThat(tasksOfPage1.getContent().size(), is(1));
            assertThat(tasksOfPage1.getContent().get(0).getId(), is("00000000-1000-0000-0000-000000000003"));
            assertThat(tasksOfPage2.getTotalElements(), is(2L));
            assertThat(tasksOfPage2.getContent().size(), is(1));
            assertThat(tasksOfPage2.getContent().get(0).getId(), is("00000000-1000-0000-0000-000000000004"));
        }
    }

    /**
     * メソッドシグネチャからクエリを生成したfindOne系のメソッドの動作をテストする。
     */
    @Test
    public void findOneUsingGenerateByMethodSignature() {
        // setup
        DateTime currentDateTime = new DateTime();

        Task newTask = new Task();
        newTask.setTitle("[Test] findOneUsingAutoGenerateQuery");
        newTask.setDescription("Testing that generate query method automatically by method signature.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        Task createdTask = taskRepository.save(newTask);

        {
            // test for exist
            Task loadedTask = taskRepository.findOneById(createdTask.getId());

            // assert
            assertThat(loadedTask.getTitle(), is("[Test] findOneUsingAutoGenerateQuery"));
            assertThat(loadedTask.getDescription(), is("Testing that generate query method automatically by method signature."));
            assertThat(loadedTask.getDeadlineDate(), is(currentDateTime.plusDays(7).toLocalDate().toDate()));
            assertThat(loadedTask.getCreatedAt(), is((Date) new Timestamp(currentDateTime.toDate().getTime())));
            assertThat(loadedTask.isFinished(), is(Boolean.FALSE));
            assertThat(loadedTask.getFinishedAt(), is(nullValue()));
            assertThat(loadedTask.getVersion(), is(0L));
        }

        {
            // test for not exist
            Task loadedTask = taskRepository.findOneById("foo");
            // assert
            assertThat(loadedTask, is(nullValue()));
        }

    }

    /**
     * Named Queryを使用したfindAll系のメソッドの動作をテストする。
     */
    @Test
    public void findAllUsingNamedQuery() {

        // test that find unfinished task list
        List<Task> tasks = taskRepository.findAllByFinished(false);

        assertThat(tasks.size(), is(2));
        assertThat(tasks.get(0).getId(), is("00000000-0000-0000-0000-000000000001"));
        assertThat(tasks.get(1).getId(), is("00000000-0000-0000-0000-000000000002"));

        // test that find finished task list
        tasks = taskRepository.findAllByFinished(true);
        assertThat(tasks.size(), is(2));
        assertThat(tasks.get(0).getId(), is("00000000-1000-0000-0000-000000000003"));
        assertThat(tasks.get(1).getId(), is("00000000-1000-0000-0000-000000000004"));

    }


    /**
     * Named Queryを使用したfindSortedAll系のメソッドの動作をテストする。
     */
    @Test
    public void findSortedAllUsingNamedQuery() {

        Sort sort = new Sort(Sort.Direction.DESC, "createdAt");

        // test that find unfinished task list
        List<Task> tasks = taskRepository.findSortedAllByFinished(false, sort);

        assertThat(tasks.size(), is(2));
        assertThat(tasks.get(0).getId(), is("00000000-0000-0000-0000-000000000002"));
        assertThat(tasks.get(1).getId(), is("00000000-0000-0000-0000-000000000001"));

        // test that find finished task list
        tasks = taskRepository.findSortedAllByFinished(true, sort);
        assertThat(tasks.size(), is(2));
        assertThat(tasks.get(0).getId(), is("00000000-1000-0000-0000-000000000004"));
        assertThat(tasks.get(1).getId(), is("00000000-1000-0000-0000-000000000003"));
    }

    /**
     * Named Queryを使用したfindPage系のメソッドの動作をテストする。
     */
    @Test
    public void findPageUsingNamedQuery() {

        // test for finished.
        {
            // test that find unfinished task of 1 page
            PageRequest pageRequestOfPage1 = new PageRequest(0, 1, Sort.Direction.ASC, "createdAt");
            Page<Task> tasksOfPage1 = taskRepository.findPageByFinished(false, pageRequestOfPage1);

            // test that find unfinished task of 2 page
            PageRequest pageRequestOfPage2 = new PageRequest(1, 1, Sort.Direction.ASC, "createdAt");
            Page<Task> tasksOfPage2 = taskRepository.findPageByFinished(false, pageRequestOfPage2);

            assertThat(tasksOfPage1.getTotalElements(), is(2L));
            assertThat(tasksOfPage1.getContent().size(), is(1));
            assertThat(tasksOfPage1.getContent().get(0).getId(), is("00000000-0000-0000-0000-000000000001"));
            assertThat(tasksOfPage2.getTotalElements(), is(2L));
            assertThat(tasksOfPage2.getContent().size(), is(1));
            assertThat(tasksOfPage2.getContent().get(0).getId(), is("00000000-0000-0000-0000-000000000002"));
        }

        // test for finished.
        {

            // test that find finished task of 1 page
            PageRequest pageRequestOfPage1 = new PageRequest(0, 1, Sort.Direction.ASC, "createdAt");
            Page<Task> tasksOfPage1 = taskRepository.findPageByFinished(true, pageRequestOfPage1);

            // test that find finished task of 2 page
            PageRequest pageRequestOfPage2 = new PageRequest(1, 1, Sort.Direction.ASC, "createdAt");
            Page<Task> tasksOfPage2 = taskRepository.findPageByFinished(true, pageRequestOfPage2);

            assertThat(tasksOfPage1.getTotalElements(), is(2L));
            assertThat(tasksOfPage1.getContent().size(), is(1));
            assertThat(tasksOfPage1.getContent().get(0).getId(), is("00000000-1000-0000-0000-000000000003"));
            assertThat(tasksOfPage2.getTotalElements(), is(2L));
            assertThat(tasksOfPage2.getContent().size(), is(1));
            assertThat(tasksOfPage2.getContent().get(0).getId(), is("00000000-1000-0000-0000-000000000004"));
        }
    }

}
