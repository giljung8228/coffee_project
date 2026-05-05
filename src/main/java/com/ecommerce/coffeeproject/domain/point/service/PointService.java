package com.ecommerce.coffeeproject.domain.point.service;

import com.ecommerce.coffeeproject.domain.member.entity.Member;
import com.ecommerce.coffeeproject.domain.member.repository.MemberRepository;
import com.ecommerce.coffeeproject.domain.point.dto.PointChargeRequest;
import com.ecommerce.coffeeproject.domain.point.dto.PointChargeResponse;
import com.ecommerce.coffeeproject.domain.point.entity.Point;
import com.ecommerce.coffeeproject.domain.point.entity.PointHistory;
import com.ecommerce.coffeeproject.domain.point.repository.PointHistoryRepository;
import com.ecommerce.coffeeproject.domain.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final MemberRepository memberRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public PointChargeResponse chargePoint(PointChargeRequest request) {
        Member member = memberRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Point point = pointRepository.findByMemberIdWithLock(member.getId())
                .orElseGet(() -> pointRepository.save(new Point(member)));

        point.charge(request.amount());

        PointHistory pointHistory = PointHistory.createCharge(
                member,
                point,
                request.amount(),
                point.getBalance()
        );

        pointHistoryRepository.save(pointHistory);

        return new PointChargeResponse(
                member.getId(),
                request.amount(),
                point.getBalance()
        );
    }
}