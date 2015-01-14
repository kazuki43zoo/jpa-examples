package com.example.domain.repository.member;

import com.example.domain.model.Member;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MemberRepository extends Repository<Member, String> {
    Member save(Member member);

    Member saveAndFlush(Member member);

    void flush();
}
