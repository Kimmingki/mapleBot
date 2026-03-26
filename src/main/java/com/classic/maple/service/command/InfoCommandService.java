package com.classic.maple.service.command;

import com.classic.maple.dto.InfoDTO;
import com.classic.maple.service.core.NexonApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InfoCommandService {

    private final NexonApiClient apiClient;

    public String getCharacterInfo(String characterName, String worldName) {
        String ocid = apiClient.getCharacterOcid(characterName, worldName);
        if (ocid == null) return "캐릭터를 찾을 수 없습니다.";

        StringBuilder sb = new StringBuilder();

        // 1. 기본 정보
        InfoDTO.Basic basic = apiClient.fetchApiData("/character/basic", ocid, InfoDTO.Basic.class);
        if (basic == null) return "기본 정보를 불러올 수 없습니다.";

        // 2. 길드 정보
        InfoDTO.Guild guild = apiClient.fetchApiData("/character/guild", ocid, InfoDTO.Guild.class);
        String guildName = (guild != null && guild.getGuildName() != null) ? guild.getGuildName() : "없음";

        // 성별 및 생성일 포맷팅
        String gender = basic.getCharacterGender() != null && basic.getCharacterGender().equalsIgnoreCase("Female") ? "여" : (basic.getCharacterGender() != null ? "남" : "");
        String genderText = gender.isEmpty() ? "" : " (" + gender + ")";
        String createDate = basic.getCharacterDateCreate() != null && basic.getCharacterDateCreate().length() >= 10
                ? basic.getCharacterDateCreate().substring(0, 10) : "알 수 없음";

        // 상단 기본 정보 조립
        sb.append("🍁 【 메이플스토리M 정보 】\n\n");
        sb.append(basic.getCharacterName()).append("\n");
        sb.append(basic.getWorldName() != null ? basic.getWorldName() : worldName)
                .append(" / 【 Lv.").append(basic.getCharacterLevel()).append(" 】 / ")
                .append(basic.getCharacterJobName()).append(genderText).append("\n");
        sb.append("길드 : ").append(guildName).append("\n");
        sb.append("생성일 : ").append(createDate).append("\n\n");

        // 3. 유니온 정보
        InfoDTO.Union union = apiClient.fetchApiData("/user/union", ocid, InfoDTO.Union.class);
        sb.append("🍁 【 유니온 】\n");
        if (union != null) {
            sb.append(union.getUnionGrade() != null ? union.getUnionGrade() : "정보 없음")
                    .append(" / 【 Lv.").append(union.getUnionLevel() != null ? union.getUnionLevel() : 0).append(" 】\n\n");
        } else {
            sb.append("정보 없음\n\n");
        }

        // 4. 펫 & 세트 효과 정보
        InfoDTO.Pet pet = apiClient.fetchApiData("/character/pet-equipment", ocid, InfoDTO.Pet.class);
        sb.append("🍁 【 장착 펫 】\n");
        if (pet != null) {
            List<String> pets = new ArrayList<>();
            if (pet.getPet1Name() != null && !pet.getPet1Name().isEmpty()) pets.add(pet.getPet1Name());
            if (pet.getPet2Name() != null && !pet.getPet2Name().isEmpty()) pets.add(pet.getPet2Name());
            if (pet.getPet3Name() != null && !pet.getPet3Name().isEmpty()) pets.add(pet.getPet3Name());

            sb.append(pets.isEmpty() ? "장착 중인 펫이 없습니다." : String.join(" / ", pets)).append("\n\n");

            sb.append("🍁 【 세트 효과 】\n");
            if (pet.getPetSetOption() != null && !pet.getPetSetOption().isEmpty()) {
                List<String> sets = new ArrayList<>();
                for (InfoDTO.Pet.PetSetOption setOpt : pet.getPetSetOption()) {
                    if (setOpt.getSetName() != null && !setOpt.getSetName().isEmpty()) {
                        sets.add(setOpt.getSetName());
                    }
                }
                sb.append(sets.isEmpty() ? "적용 중인 세트 효과가 없습니다." : String.join("\n", sets));
            } else {
                sb.append("적용 중인 세트 효과가 없습니다.");
            }
        } else {
            sb.append("장착 중인 펫이 없습니다.\n\n");
            sb.append("🍁 【 세트 효과 】\n적용 중인 세트 효과가 없습니다.");
        }

        return sb.toString().trim();
    }

    public String getCharacterExp(String characterName, String worldName) {
        String ocid = apiClient.getCharacterOcid(characterName, worldName);
        if (ocid == null) return "캐릭터를 찾을 수 없습니다.";

        InfoDTO.Basic basic = apiClient.fetchApiData("/character/basic", ocid, InfoDTO.Basic.class);
        if (basic == null) return "경험치 정보를 불러올 수 없습니다.";

        return String.format("🍁 【 경험치 정보 】\n%s (Lv.%d)\n현재 경험치: %d",
                basic.getCharacterName(), basic.getCharacterLevel(), basic.getCharacterExp());
    }
}