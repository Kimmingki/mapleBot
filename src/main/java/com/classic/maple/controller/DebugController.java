package com.classic.maple.controller;

import com.classic.maple.service.debug.DebugCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private final DebugCommandService debugCommandService;

    public DebugController(DebugCommandService debugCommandService) {
        this.debugCommandService = debugCommandService;
    }

    @GetMapping("/info")
    public String debugInfo(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info("[DEBUG] .정보 원본 데이터 요청 - 캐릭터명: {}", name);
        return debugCommandService.debugInfo(name, world);
    }

    @GetMapping("/naesil")
    public String debugNaesil(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info("[DEBUG] .내실/.보스 원본 데이터 요청 - 캐릭터명: {}", name);
        return debugCommandService.debugBossAndNaesil(name, world);
    }

    @GetMapping("/vmatrix")
    public String debugVMatrix(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info("[DEBUG] .코강 원본 데이터 요청 - 캐릭터명: {}", name);
        return debugCommandService.debugVMatrix(name, world);
    }

    @GetMapping("/codi")
    public String debugCodi(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info("[DEBUG] .코디 원본 데이터 요청 - 캐릭터명: {}", name);
        return debugCommandService.debugCodi(name, world);
    }

    @GetMapping("/guildskill")
    public String debugGuildSkill(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info("[DEBUG] .길스 원본 데이터 요청 - 캐릭터명: {}, 월드: {}", name, world);
        return debugCommandService.debugGuildSkill(name, world);
    }

    @GetMapping("/hexa")
    public String debugHexa(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info("[DEBUG] .헥사 원본 데이터 요청 - 캐릭터명: {}, 월드: {}", name, world);
        return debugCommandService.debugHexa(name, world);
    }
}
