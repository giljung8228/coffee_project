package com.ecommerce.coffeeproject.domain.point.repository;

import com.ecommerce.coffeeproject.domain.point.entity.Point;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Point p where p.member.id = :memberId")
    Optional<Point> findByMemberIdWithLock(@Param("memberId") Long memberId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select p from Point p where p.member.id = :memberId")
    Optional<Point> findByMemberIdWithOptimisticLock(@Param("memberId") Long memberId);
}
