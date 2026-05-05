package com.ecommerce.coffeeproject.global.init;

import com.ecommerce.coffeeproject.domain.menu.entity.CoffeeMenu;
import com.ecommerce.coffeeproject.domain.menu.entity.MenuStatus;
import com.ecommerce.coffeeproject.domain.menu.repository.CoffeeMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("local")
@RequiredArgsConstructor
public class MenuDataInitializer implements CommandLineRunner {

    private final CoffeeMenuRepository coffeeMenuRepository;

    @Override
    public void run(String... args) {
        if (coffeeMenuRepository.count() > 0) {
            return;
        }

        coffeeMenuRepository.saveAll(List.of(
                new CoffeeMenu("아메리카노", 4500L, MenuStatus.ON_SALE),
                new CoffeeMenu("카페라떼", 5000L, MenuStatus.ON_SALE),
                new CoffeeMenu("바닐라라떼", 5500L, MenuStatus.ON_SALE),
                new CoffeeMenu("카라멜마끼아또", 6000L, MenuStatus.SOLD_OUT),
                new CoffeeMenu("콜드브루", 5500L, MenuStatus.HIDDEN)
        ));
    }
}