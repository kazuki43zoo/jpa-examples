package com.example.domain.repository;

import com.example.domain.model.Task;
import com.example.domain.repository.helper.DBLog;
import com.example.domain.repository.helper.LockedCallback;
import com.example.domain.repository.helper.TaskEntityHelper;
import com.example.domain.repository.helper.TransactionalCommand;
import com.example.domain.repository.task.TaskRepository;
import org.hibernate.OptimisticLockException;
import org.hibernate.PessimisticLockException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.hibernate3.HibernateJdbcException;
import org.springframework.orm.hibernate3.HibernateSystemException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * エンティティへのロックエラーをテストする。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class EntityLockErrorTest {

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
     * LockModeType.PESSIMISTIC_WRITEのロックの取得に失敗する。
     */
    @Test
    public void lockErrorWithinPessimisticWriteLock() throws InterruptedException {
        // setup
        DateTime currentDateTime = new DateTime();

        Task newTask = new Task();
        newTask.setTitle("[Test] dummy");
        newTask.setDescription("Testing that lock error.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        final Task createdTask = taskRepository.save(newTask);


        final CountDownLatch countDownLatchForTesting = new CountDownLatch(1);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                taskEntityHelper.lockWithinNewTransaction(createdTask.getId(), 1000, new LockedCallback() {
                    @Override
                    public void locked() {
                        countDownLatchForTesting.countDown();
                    }
                });

            }
        });
        thread.start();

        // test for exist
        countDownLatchForTesting.await();
        try {
            taskRepository.findOneWithinPessimisticWriteLockById(createdTask.getId());
            fail();
        } catch (HibernateJdbcException e) {
            assertThat(e.getCause().getClass().isAssignableFrom(PessimisticLockException.class), is(true));
        } finally {
            thread.interrupt();
            thread.join();
        }

    }

    /**
     * LockModeType.PESSIMISTIC_READのロックの取得に失敗する。
     */
    @Test
    public void pessimisticReadLock() throws InterruptedException {

        // setup
        DateTime currentDateTime = new DateTime();

        Task newTask = new Task();
        newTask.setTitle("[Test] dummy");
        newTask.setDescription("Testing that lock error.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        final Task createdTask = taskRepository.save(newTask);

        final CountDownLatch countDownLatchForTesting = new CountDownLatch(1);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                taskEntityHelper.lockWithinNewTransaction(createdTask.getId(), 1000, new LockedCallback() {
                    @Override
                    public void locked() {
                        countDownLatchForTesting.countDown();
                    }
                });

            }
        });
        thread.start();

        // test for exist
        countDownLatchForTesting.await();
        try {
            taskRepository.findOneWithinPessimisticReadLockById(createdTask.getId());
            if (taskEntityHelper.isH2()) {
                fail();
            }
        } catch (HibernateJdbcException e) {
            assertThat(e.getCause().getClass().isAssignableFrom(PessimisticLockException.class), is(true));
        } finally {
            thread.interrupt();
            thread.join();
        }

    }

    /**
     * LockModeType.PESSIMISTIC_FORCE_INCREMENTのロックの取得に失敗する。
     */
    @Test
    public void pessimisticForceIncrementLock() throws InterruptedException {

        // setup
        DateTime currentDateTime = new DateTime();

        Task newTask = new Task();
        newTask.setTitle("[Test] dummy");
        newTask.setDescription("Testing that lock error.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        final Task createdTask = taskRepository.save(newTask);

        final CountDownLatch countDownLatchForTesting = new CountDownLatch(1);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                taskEntityHelper.lockWithinNewTransaction(createdTask.getId(), 1000, new LockedCallback() {
                    @Override
                    public void locked() {
                        countDownLatchForTesting.countDown();
                    }
                });

            }
        });
        thread.start();

        // test for exist
        countDownLatchForTesting.await();
        try {
            taskRepository.findOneWithinPessimisticForceIncrementLockById(createdTask.getId());
            fail();
        } catch (HibernateJdbcException e) {
            assertThat(e.getCause().getClass().isAssignableFrom(PessimisticLockException.class), is(true));
        } finally {
            thread.interrupt();
            thread.join();
        }
    }

    /**
     * LockModeType.WRITEのロックの取得に失敗する。
     */
    @Test
    public void writeLock() throws InterruptedException {

        // setup
        DateTime currentDateTime = new DateTime();

        Task newTask = new Task();
        newTask.setTitle("[Test] write lock task");
        newTask.setDescription("Testing that write lock a task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        final Task createdTask = taskRepository.save(newTask);

        final CountDownLatch countDownLatchForTesting = new CountDownLatch(1);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                taskEntityHelper.lockWithinNewTransaction(createdTask.getId(), 1000, new LockedCallback() {
                    @Override
                    public void locked() {
                        countDownLatchForTesting.countDown();
                    }
                });

            }
        });
        thread.start();

        // test for exist
        countDownLatchForTesting.await();
        try {
            taskRepository.findOneWithinWriteLockById(createdTask.getId());
            if (taskEntityHelper.isH2()) {
                fail();
            }
        } catch (HibernateJdbcException e) {
            assertThat(e.getCause().getClass().isAssignableFrom(PessimisticLockException.class), is(true));
        } finally {
            thread.interrupt();
            thread.join();
        }
    }

    /**
     * LockModeType.OPTIMISTIC_FORCE_INCREMENTのロックの取得に失敗する。
     */
    @Test
    public void optimisticForceIncrementLock() throws InterruptedException {

        // setup
        DateTime currentDateTime = new DateTime();

        Task newTask = new Task();
        newTask.setTitle("[Test] optimistic force increment lock task");
        newTask.setDescription("Testing that optimistic force increment lock a task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        final Task createdTask = taskRepository.save(newTask);

        final CountDownLatch countDownLatchForTesting = new CountDownLatch(1);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                taskEntityHelper.lockWithinNewTransaction(createdTask.getId(), 1000, new LockedCallback() {
                    @Override
                    public void locked() {
                        countDownLatchForTesting.countDown();
                    }
                });

            }
        });
        thread.start();

        // test for exist
        countDownLatchForTesting.await();
        try {
            taskRepository.findOneWithinOptimisticForceIncrementLockById(createdTask.getId());
            if (taskEntityHelper.isH2()) {
                fail();
            }
        } catch (HibernateJdbcException e) {
            assertThat(e.getCause().getClass().isAssignableFrom(PessimisticLockException.class), is(true));
        } finally {
            thread.interrupt();
            thread.join();
        }

    }


    /**
     * LockModeType.READのロックの取得に失敗する。
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
        final Task createdTask = taskRepository.save(newTask);

        try {
            taskEntityHelper.executeWithinNewTransaction(new TransactionalCommand() {
                @Override
                public void execute() {
                    taskRepository.findOneWithinReadLockById(createdTask.getId());
                    taskEntityHelper.updateVersionWithinNewTransaction(createdTask.getId());
                }
            });
            fail();
        } catch (HibernateSystemException e) {
            assertThat(e.getCause().getClass().isAssignableFrom(OptimisticLockException.class), is(true));
        }

    }

    /**
     * LockModeType.OPTIMISTICのロックの取得に失敗する。
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
        final Task createdTask = taskRepository.save(newTask);

        try {
            taskEntityHelper.executeWithinNewTransaction(new TransactionalCommand() {
                @Override
                public void execute() {
                    taskRepository.findOneWithinOptimisticLockById(createdTask.getId());
                    taskEntityHelper.updateVersionWithinNewTransaction(createdTask.getId());
                }
            });
            fail();
        } catch (HibernateSystemException e) {
            assertThat(e.getCause().getClass().isAssignableFrom(OptimisticLockException.class), is(true));
        }

    }

    /**
     * merge時(更新時)に楽観排他の取得に失敗する。
     */
    @Test
    public void optimisticLockWhenMerge() {

        // setup
        final DateTime currentDateTime = new DateTime();

        Task newTask = new Task();
        newTask.setTitle("[Test] optimistic lock task");
        newTask.setDescription("Testing that optimistic lock a task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        final Task createdTask = taskRepository.save(newTask);

        try {
            taskEntityHelper.executeWithinNewTransaction(new TransactionalCommand() {
                @Override
                public void execute() {
                    Task loadedTask = taskRepository.findOne(createdTask.getId());
                    loadedTask.setFinished(true);
                    loadedTask.setFinishedAt(currentDateTime.toDate());
                    taskEntityHelper.updateVersionWithinNewTransaction(createdTask.getId());
                }
            });
            fail();
        } catch (ObjectOptimisticLockingFailureException e) {
            assertThat(e.getIdentifier().toString(), is(createdTask.getId()));
        }

    }

    /**
     * Entity指定のdelete時に楽観排他の取得に失敗する。
     */
    @Test
    public void optimisticLockWhenDelete() {

        // setup
        final DateTime currentDateTime = new DateTime();

        Task newTask = new Task();
        newTask.setTitle("[Test] optimistic lock task");
        newTask.setDescription("Testing that optimistic lock a task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        final Task createdTask = taskRepository.save(newTask);

        try {
            taskEntityHelper.executeWithinNewTransaction(new TransactionalCommand() {
                @Override
                public void execute() {
                    Task loadedTask = taskRepository.findOne(createdTask.getId());
                    taskRepository.delete(loadedTask);
                    taskEntityHelper.updateVersionWithinNewTransaction(createdTask.getId());
                }
            });
            fail();
        } catch (ObjectOptimisticLockingFailureException e) {
            assertThat(e.getIdentifier().toString(), is(createdTask.getId()));
        }

    }

    /**
     * ID指定のdelete時に楽観排他の取得に失敗する。
     */
    @Test
    public void optimisticLockWhenDeleteById() {

        // setup
        final DateTime currentDateTime = new DateTime();

        Task newTask = new Task();
        newTask.setTitle("[Test] optimistic lock task");
        newTask.setDescription("Testing that optimistic lock a task.");
        newTask.setDeadlineDate(currentDateTime.plusDays(7).toLocalDate().toDate());
        newTask.setCreatedAt(currentDateTime.toDate());
        final Task createdTask = taskRepository.save(newTask);

        try {
            taskEntityHelper.executeWithinNewTransaction(new TransactionalCommand() {
                @Override
                public void execute() {
                    taskRepository.delete(createdTask.getId());
                    taskEntityHelper.updateVersionWithinNewTransaction(createdTask.getId());
                }
            });
            fail();
        } catch (ObjectOptimisticLockingFailureException e) {
            assertThat(e.getIdentifier().toString(), is(createdTask.getId()));
        }

    }

}
