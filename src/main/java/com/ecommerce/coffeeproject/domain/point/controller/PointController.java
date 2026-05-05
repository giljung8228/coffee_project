package com.ecommerce.coffeeproject.domain.point.controller;

import com.ecommerce.coffeeproject.domain.point.dto.PointChargeRequest;
import com.ecommerce.coffeeproject.domain.point.dto.PointChargeResponse;
import com.ecommerce.coffeeproject.domain.point.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    @PostMapping("/charge")
    public PointChargeResponse chargePoint(@Valid @RequestBody PointChargeRequest request){
        return pointService.chargePoint(request);
    }
}
