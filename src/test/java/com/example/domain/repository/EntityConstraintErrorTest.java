package com.example.domain.repository;

import com.example.domain.model.Member;
import com.example.domain.repository.helper.DBLog;
import com.example.domain.repository.helper.MemberEntityHelper;
import com.example.domain.repository.member.MemberRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static org.junit.Assert.fail;

/**
 * エンティティへの制約エラーをテストする。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class EntityConstraintErrorTest {

    @Inject
    MemberRepository memberRepository;

    @Inject
    MemberEntityHelper memberEntityHelper;

    @Inject
    DBLog dbLog;

    @Before
    public void setup() {
        memberEntityHelper.deleteCreatedRecordsOnTesting();
        dbLog.delete();
    }


    /**
     * 一意制約に違反する。
     */
    @Test
    public void entityDuplicateOnOtherTransaction() {

        Member member1 = new Member();
        member1.setLoginId("test@com.example");
        member1.setName("John");

        memberRepository.save(member1);

        Member member2 = new Member();
        member2.setLoginId("test@com.example");
        member2.setName("Ken");

        try {
            memberRepository.save(member2);
            fail();
        } catch (DataIntegrityViolationException e) {
            // NOP
        }

    }

    /**
     * 同一トランザクション内にて、saveAndFlushを使用して一意制約を検知する。
     */
    @Transactional
    @Test
    public void detectEntityDuplicateUsingSaveAndFlushOnSameTransaction() {

        Member member1 = new Member();
        member1.setLoginId("test@com.example");
        member1.setName("John");

        memberRepository.save(member1);

        Member member2 = new Member();
        member2.setLoginId("test@com.example");
        member2.setName("Ken");

        try {
            memberRepository.saveAndFlush(member2);
            fail();
        } catch (DataIntegrityViolationException e) {
            // NOP
        }

    }

    /**
     * 同一トランザクション内にて、flushを使用して一意制約を検知する。
     */
    @Transactional
    @Test
    public void detectEntityDuplicateUsingFlushOnSameTransaction() {

        Member member1 = new Member();
        member1.setLoginId("test@com.example");
        member1.setName("John");

        memberRepository.save(member1);

        Member member2 = new Member();
        member2.setLoginId("test@com.example");
        member2.setName("Ken");

        memberRepository.save(member2);

        try {
            memberRepository.flush();
            fail();
        } catch (DataIntegrityViolationException e) {
            // NOP
        }

    }


}
