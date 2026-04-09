package com.classic.maple.service.command.character;

import com.classic.maple.dto.GuildSkillDTO;
import com.classic.maple.service.core.NexonApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;

@Service
@RequiredArgsConstructor
public class GuildSkillCommandService {

    private final NexonApiClient apiClient;

    public String getCharacterGuildSkill(String characterName, String worldName) {
        String ocid = apiClient.getCharacterOcid(characterName, worldName);
        if (ocid == null) return "캐릭터 정보를 찾을 수 없습니다.";

        // 1. 캐릭터가 가입된 길드의 이름 조회
        GuildSkillDTO.NameOnly guildInfo = apiClient.fetchApiData("/character/guild", ocid, GuildSkillDTO.NameOnly.class);
        if (guildInfo == null || guildInfo.getGuildName() == null || guildInfo.getGuildName().isEmpty()) {
            return "가입된 길드가 없거나 길드 정보를 불러올 수 없습니다.";
        }
        String gName = guildInfo.getGuildName();

        // 2. 길드 이름으로 길드 식별자(oguild_id) 발급
        String oguildId = apiClient.getGuildId(gName, worldName);
        if (oguildId == null) {
            return "길드 식별자(ID)를 발급받지 못했습니다.";
        }

        // 3. 식별자로 길드 상세 정보(길드원 명단) 조회
        GuildSkillDTO.Basic guildBasic = apiClient.fetchGuildData("/guild/basic", oguildId, GuildSkillDTO.Basic.class);
        if (guildBasic == null || guildBasic.getGuildMember() == null) {
            return "길드 상세 정보를 불러올 수 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        String readMore = "\u200B".repeat(500);
        DecimalFormat df = new DecimalFormat("#,###"); // 🌟 기여도 콤마 포맷팅용

        // 4. 길드원 리스트에서 요청한 닉네임과 일치하는 데이터 찾기
        boolean foundUser = false;
        for (GuildSkillDTO.Basic.GuildMember member : guildBasic.getGuildMember()) {
            if (characterName.equals(member.getCharacterName())) {
                foundUser = true;
                long activity = member.getGuildActivity() != null ? member.getGuildActivity() : 0L;

                // 상단 헤더 조립
                sb.append("🍁 【 ").append(gName).append(" 길드 스킬 정보 】\n");
                sb.append(characterName).append("님의 기여도: ").append(df.format(activity));
                sb.append(readMore).append("\n\n");

                sb.append("【 상세 스킬 옵션 】\n\n");

                // 개인 스킬 조립
                if (member.getGuildPersonalSkill() != null && !member.getGuildPersonalSkill().isEmpty()) {
                    for (GuildSkillDTO.Basic.PersonalSkill skill : member.getGuildPersonalSkill()) {
                        String sName = skill.getSkillName() != null ? skill.getSkillName() : "알 수 없는 스킬";
                        String sOption = skill.getSkillOption() != null ? skill.getSkillOption() : "설명 없음";

                        // 원본 JSON의 줄바꿈 문자를 ' / ' 로 치환하여 깔끔하게 포맷팅
                        sOption = sOption.replace("\n", " / ");

                        sb.append("▪ ").append(sName).append("\n");
                        sb.append("  └ ").append(sOption).append("\n\n");
                    }
                } else {
                    sb.append("적용 중인 길드 개인 스킬이 없습니다.\n");
                }
                break; // 유저를 찾았으므로 순회 종료
            }
        }

        if (!foundUser) {
            return "길드원 목록에서 '" + characterName + "' 캐릭터를 찾을 수 없습니다.";
        }

        return sb.toString().trim();
    }
}
