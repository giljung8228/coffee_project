package com.ecommerce.coffeeproject.domain.point.entity;

import com.ecommerce.coffeeproject.domain.member.entity.Member;
import com.ecommerce.coffeeproject.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "point_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id", nullable = false)
    private Point point;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointHistoryType type;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long balanceAfter;

    private PointHistory(
            Member member,
            Point point,
            PointHistoryType type,
            Long amount,
            Long balanceAfter
    ) {
        this.member = member;
        this.point = point;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }

    public static PointHistory createCharge(
            Member member,
            Point point,
            Long amount,
            Long balanceAfter
    ) {
        return new PointHistory(
                member,
                point,
                PointHistoryType.CHARGE,
                amount,
                balanceAfter
        );
    }

    public static PointHistory createUse(
            Member member,
            Point point,
            Long amount,
            Long balanceAfter
    ) {
        return new PointHistory(
                member,
                point,
                PointHistoryType.USE,
                amount,
                balanceAfter
        );
    }
}