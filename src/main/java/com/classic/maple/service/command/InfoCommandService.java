package com.classic.maple.service.command;

import com.classic.maple.dto.CharacterBasicDTO;
import com.classic.maple.service.core.NexonApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InfoCommandService {

    private final NexonApiClient apiClient;

    public String getCharacterInfo(String characterName, String worldName) {
        String ocid = apiClient.getCharacterOcid(characterName, worldName);
        if (ocid == null) return "캐릭터를 찾을 수 없습니다.";

        CharacterBasicDTO basic = apiClient.fetchApiData("/character/basic", ocid, CharacterBasicDTO.class);
        if (basic == null) return "기본 정보를 불러올 수 없습니다.";

        return String.format("🍁 【 캐릭터 정보 】\n닉네임: %s\n서버: %s\n직업: %s\n레벨: %d\n길드: %s",
                basic.getCharacterName(), basic.getWorldName(), basic.getCharacterJobName(),
                basic.getCharacterLevel(), basic.getCharacterGuildName() != null ? basic.getCharacterGuildName() : "없음");
    }

    public String getCharacterExp(String characterName, String worldName) {
        String ocid = apiClient.getCharacterOcid(characterName, worldName);
        if (ocid == null) return "캐릭터를 찾을 수 없습니다.";

        CharacterBasicDTO basic = apiClient.fetchApiData("/character/basic", ocid, CharacterBasicDTO.class);
        if (basic == null) return "경험치 정보를 불러올 수 없습니다.";

        return String.format("🍁 【 경험치 정보 】\n%s (Lv.%d)\n현재 경험치: %d",
                basic.getCharacterName(), basic.getCharacterLevel(), basic.getCharacterExp());
    }
}
