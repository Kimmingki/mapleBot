package com.classic.maple.controller;

import com.classic.maple.service.MapleApiService;
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

    private final MapleApiService mapleApiService;

    // API 연동 테스트 엔드포인트
    // URL 예시: http://localhost:8081/api/bot/ocid?name=본인캐릭터명&world=스카니아
    @GetMapping("/ocid")
    public String searchCharacter(@RequestParam String name, @RequestParam String world) {
        log.info("요청받은 캐릭터명: {}, 월드명: {}", name, world);

        String ocid = mapleApiService.getCharacterOcid(name, world);

        if (ocid == null) {
            return "캐릭터 정보를 찾을 수 없거나 서버 통신에 실패했습니다.";
        }

        return String.format("✅ 성공! [%s(%s)] 캐릭터의 고유 OCID: %s", name, world, ocid);
    }

    @GetMapping("/info")
    public String getInfoCommand(@RequestParam String name, @RequestParam(required = false) String world) {
        // 월드 값이 비어있거나 공백이라면 기본값 '스카니아' 적용
        if (world == null || world.trim().isEmpty()) {
            world = "스카니아";
        }

        log.info(".정보 명령어 요청 - 캐릭터명: {}, 월드명: {}", name, world);

        // Service 호출 및 가공된 문자열 반환
        return mapleApiService.getCharacterInfoDetail(name, world);
    }

    @GetMapping("/exp")
    public String getExpCommand(@RequestParam String name, @RequestParam(required = false) String world) {
        if (world == null || world.trim().isEmpty()) {
            world = "스카니아";
        }

        log.info(".경험치 명령어 요청 - 캐릭터명: {}, 월드명: {}", name, world);

        return mapleApiService.getCharacterExpHistory(name, world);
    }

    @GetMapping("/boss")
    public String getBossSettingCommand(@RequestParam String name, @RequestParam(required = false) String world) {
        if (world == null || world.trim().isEmpty()) {
            world = "스카니아";
        }

        log.info(".보스 명령어 요청 - 캐릭터명: {}, 월드명: {}", name, world);

        return mapleApiService.getBossEquipmentSetting(name, world);
    }
}
