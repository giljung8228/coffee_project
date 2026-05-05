package com.ecommerce.coffeeproject.domain.member.repository;

import com.ecommerce.coffeeproject.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}