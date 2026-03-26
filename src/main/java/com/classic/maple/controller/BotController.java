package com.classic.maple.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bot")
public class BotController {

    // 봇 연결 테스트용 엔드포인트
    // URL: http://localhost:8080/api/bot/boss?name=자쿰
    @GetMapping("/boss")
    public String searchBoss(@RequestParam String name) {
        log.info("요청받은 보스이름: {}", name);

        return String.format("[%s] 보스 정보를 성공적으로 요청했습니다! (DB/API 연동 대기 중)", name);
    }
}
