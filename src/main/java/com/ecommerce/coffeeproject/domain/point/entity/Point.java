package com.ecommerce.coffeeproject.domain.point.entity;

import com.ecommerce.coffeeproject.domain.member.entity.Member;
import com.ecommerce.coffeeproject.global.entity.BaseEntity;
import com.ecommerce.coffeeproject.global.exception.BusinessException;
import com.ecommerce.coffeeproject.global.exception.domain.PointErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "point",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_point_member_id", columnNames = "member_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Long balance;

    public Point(Member member) {
        this.member = member;
        this.balance = 0L;
    }

    public void charge(Long amount) {
        this.balance += amount;
    }

    public void use(Long amount) {
        if (this.balance < amount) {
            throw new BusinessException(PointErrorCode.POINT_NOT_ENOUGH);
        }

        this.balance -= amount;
    }
}