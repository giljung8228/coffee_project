package com.ecommerce.coffeeproject.domain.menu.entity;

import com.ecommerce.coffeeproject.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "coffee_menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoffeeMenu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private MenuStatus status;

    public CoffeeMenu(String name, Long price, MenuStatus status){
        this.name = name;
        this.price = price;
        this.status = status;
    }
}
