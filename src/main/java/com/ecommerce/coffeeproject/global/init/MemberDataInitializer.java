package com.ecommerce.coffeeproject.global.init;

import com.ecommerce.coffeeproject.domain.member.entity.Member;
import com.ecommerce.coffeeproject.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@RequiredArgsConstructor
public class MemberDataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;

    @Override
    public void run(String... args) {
        if (memberRepository.count() > 0) {
            return;
        }

        memberRepository.save(new Member("테스트사용자"));
    }
}