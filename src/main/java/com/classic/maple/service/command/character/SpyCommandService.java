package com.classic.maple.service.command.character;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Service
public class SpyCommandService {

    // maplemworlds 캐릭터 페이지 기본 경로 (경로 형식: /character/{서버}-{닉네임})
    private static final String MAPLEM_WORLDS_CHARACTER_URL = "https://maplemworlds.kr/character/";

    // 채팅 응답: 캐릭터 표시 + maplemworlds 페이지 URL
    public String getCharacterSpyLink(String characterName, String worldName) {
        String name = characterName.trim();
        String world = worldName.trim();

        return "🍁 【 염탐 】\n"
                + name + " (" + world + ")\n"
                + buildCharacterUrl(name, world);
    }

    // "{서버}-{닉네임}" 경로 세그먼트 UTF-8 인코딩 (한글·공백 처리)
    private String buildCharacterUrl(String characterName, String worldName) {
        String pathSegment = worldName + "-" + characterName;
        return MAPLEM_WORLDS_CHARACTER_URL + UriUtils.encodePathSegment(pathSegment, StandardCharsets.UTF_8);
    }
}
