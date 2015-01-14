package com.example.domain.repository.task;

import com.example.domain.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Date;
import java.util.List;

@Transactional
public interface TaskRepository extends JpaRepository<Task, String> {

    @Query("SELECT t FROM Task t WHERE t.id LIKE :idPrefix% AND t.finished = :finished ORDER BY t.createdAt")
    List<Task> findAllByFinished(@Param("idPrefix") String idPrefix, @Param("finished") boolean finished);

    @Query("SELECT t FROM Task t WHERE t.id LIKE :idPrefix% AND t.finished = :finished")
    List<Task> findSortedAllByFinished(@Param("idPrefix") String idPrefix, @Param("finished") boolean finished, Sort sort);

    @Query("SELECT t FROM Task t WHERE t.id LIKE :idPrefix% AND t.finished = :finished")
    Page<Task> findPageByFinished(@Param("idPrefix") String idPrefix, @Param("finished") boolean finished, Pageable pageable);

    Task findOneById(String id);

    List<Task> findAllByIdStartingWithAndFinished(String idPrefix, boolean finished);

    List<Task> findSortedAllByIdStartingWithAndFinished(String idPrefix, boolean finished, Sort sort);

    Page<Task> findPageByIdStartingWithAndFinished(String idPrefix, boolean finished, Pageable pageable);

    List<Task> findAllByFinished(@Param("finished") boolean finished);

    @Query(name = "Task.findByFinishedWithoutOrderBy")
    List<Task> findSortedAllByFinished(@Param("finished") boolean finished, Sort sort);

    @Query(name = "Task.findByFinishedWithoutOrderBy")
    Page<Task> findPageByFinished(@Param("finished") boolean finished, Pageable pageable);

    @QueryHints(value = {@QueryHint(name = "javax.persistence.lock.timeout", value = "0")})
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Task findOneWithinPessimisticWriteLockById(String id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Task findOneWithinPessimisticReadLockById(String id);

    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    Task findOneWithinPessimisticForceIncrementLockById(String id);

    @Lock(LockModeType.WRITE)
    Task findOneWithinWriteLockById(String id);

    @Lock(LockModeType.READ)
    Task findOneWithinReadLockById(String id);

    @Lock(LockModeType.OPTIMISTIC)
    Task findOneWithinOptimisticLockById(String id);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    Task findOneWithinOptimisticForceIncrementLockById(String id);

    @Modifying
    @Query("UPDATE Task t SET t.finished = TRUE, t.finishedAt = :finishedAt, t.version = (t.version + 1) WHERE t.id NOT LIKE '00000000-%' AND t.finished = FALSE")
    int finishAll(@Param("finishedAt") Date finishedAt);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Task t SET t.finished = TRUE, t.finishedAt = :finishedAt, t.version = (t.version + 1) WHERE t.id NOT LIKE '00000000-%' AND t.finished = FALSE")
    int finishAllWithClear(@Param("finishedAt") Date finishedAt);

    @Modifying
    @Query("DELETE FROM Task t WHERE t.id NOT LIKE '00000000-%' AND t.finished = TRUE AND t.finishedAt < :finishedAt")
    int deleteByFinishedAtBefore(@Param("finishedAt") Date finishedAt);

}
