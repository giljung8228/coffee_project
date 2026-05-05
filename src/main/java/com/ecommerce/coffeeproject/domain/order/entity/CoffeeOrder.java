package com.ecommerce.coffeeproject.domain.order.entity;

import com.ecommerce.coffeeproject.domain.member.entity.Member;
import com.ecommerce.coffeeproject.domain.menu.entity.CoffeeMenu;
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

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "coffee_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoffeeOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private CoffeeMenu menu;

    @Column(nullable = false)
    private Long paymentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    private CoffeeOrder(Member member, CoffeeMenu menu, Long paymentAmount) {
        this.member = member;
        this.menu = menu;
        this.paymentAmount = paymentAmount;
        this.status = OrderStatus.PAID;
        this.orderedAt = LocalDateTime.now();
    }

    public static CoffeeOrder createPaid(Member member, CoffeeMenu menu, Long paymentAmount) {
        return new CoffeeOrder(member, menu, paymentAmount);
    }
}