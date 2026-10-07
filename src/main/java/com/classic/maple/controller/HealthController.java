package com.classic.maple.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 서버 생존 확인용 컨트롤러 (UptimeRobot 핑 대상)
@RestController
@RequestMapping("/api")
public class HealthController {

    // 슬립 방지 핑 응답, 넥슨 API 호출 없음
    // HEAD 요청도 GET 매핑으로 자동 처리
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
