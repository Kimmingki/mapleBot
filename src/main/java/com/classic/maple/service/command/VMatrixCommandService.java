package com.classic.maple.service.command;

import com.classic.maple.dto.VMatrixDTO;
import com.classic.maple.service.core.NexonApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VMatrixCommandService {

    private final NexonApiClient apiClient;

    public String getCharacterVMatrixDebug(String characterName, String worldName) {
        String ocid = apiClient.getCharacterOcid(characterName, worldName);
        if (ocid == null) {
            return "캐릭터 정보를 찾을 수 없습니다.";
        }

        // 🌟 1. 넥슨 API로부터 전달받는 원본 JSON 데이터를 문자열(String)로 직접 가져옵니다.
        String vmatrixRaw = apiClient.fetchApiData("/character/vmatrix", ocid, String.class);
        String skillRaw = apiClient.fetchApiData("/character/skill-equipment", ocid, String.class);

        // 🌟 2. IDE 콘솔 로그에 출력 (터미널에서 긁어서 분석하기 가장 좋습니다)
        log.info("================ [DEBUG: V-MATRIX RAW JSON] ================");
        log.info(vmatrixRaw);
        log.info("================ [DEBUG: SKILL-EQUIP RAW JSON] ================");
        log.info(skillRaw);
        log.info("==============================================================");

        // 🌟 3. 채팅창에서도 바로 확인할 수 있도록 텍스트를 합쳐서 반환합니다.
        StringBuilder sb = new StringBuilder();
        sb.append("🍁 [넥슨 API 응답 데이터 확인]\n\n");
        sb.append("💎 V매트릭스 원본 JSON:\n").append(vmatrixRaw).append("\n\n");
        sb.append("⚔️ 장착 스킬 원본 JSON:\n").append(skillRaw);

        return sb.toString();
    }

    public String getCharacterVMatrix(String characterName, String worldName) {
        String ocid = apiClient.getCharacterOcid(characterName, worldName);
        if (ocid == null) return "캐릭터 정보를 찾을 수 없습니다.";

        VMatrixDTO.Basic basic = apiClient.fetchApiData("/character/basic", ocid, VMatrixDTO.Basic.class);
        if (basic == null) return "기본 정보를 불러올 수 없습니다.";

        VMatrixDTO.VCoreInfo vCoreInfo = apiClient.fetchApiData("/character/vmatrix", ocid, VMatrixDTO.VCoreInfo.class);
        VMatrixDTO.SkillEquip skillEquip = apiClient.fetchApiData("/character/skill-equipment", ocid, VMatrixDTO.SkillEquip.class);

        StringBuilder sb = new StringBuilder();
        String readMore = "\u200B".repeat(500);

        sb.append("🍁 메이플스토리M V코어 현황\n");
        sb.append(basic.getCharacterName()).append(" (")
                .append(basic.getWorldName() != null ? basic.getWorldName() : worldName).append(") / ")
                .append(basic.getCharacterJobName()).append("\n")
                .append(readMore).append("\n\n");

        List<String> enhanceCores = new ArrayList<>();
        List<String> skillCores = new ArrayList<>();
        List<String> decentCores = new ArrayList<>();
        List<String> specialCores = new ArrayList<>();

        // 🌟 1. V코어 분류 로직
        if (vCoreInfo != null && vCoreInfo.getVCores() != null) {
            for (VMatrixDTO.VCoreInfo.VCore core : vCoreInfo.getVCores()) {
                String cType = core.getVcoreType() != null ? core.getVcoreType() : "";
                String cName = core.getVcoreName() != null ? core.getVcoreName() : "알 수 없음";
                int cLevel = core.getVcoreLevel() != null ? core.getVcoreLevel() : 0;
                int sLevel = core.getSlotLevel() != null ? core.getSlotLevel() : 0;

                if ("Enhancement".equals(cType)) {
                    // 강화 코어는 3개의 스킬 이름을 조합
                    List<String> skills = new ArrayList<>();
                    if (core.getSkillName1() != null) skills.add(core.getSkillName1());
                    if (core.getSkillName2() != null) skills.add(core.getSkillName2());
                    if (core.getSkillName3() != null) skills.add(core.getSkillName3());
                    String combinedSkills = String.join(" / ", skills);

                    enhanceCores.add(String.format("▪ [ Lv.%d ] %s [ %dP ]", cLevel, combinedSkills, sLevel));
                } else if ("Skill".equals(cType)) {
                    // 스킬 코어 중 '쓸만한' 분리
                    if (cName.startsWith("쓸만한")) {
                        decentCores.add(String.format("▪ [ Lv.%d ] %s", cLevel, cName));
                    } else {
                        skillCores.add(String.format("▪ [ Lv.%d ] %s", cLevel, cName));
                    }
                } else if ("Special".equals(cType)) {
                    specialCores.add(String.format("▪ [ Lv.%d ] %s", cLevel, cName));
                }
            }
        }

        sb.append("🍁 강화 코어\n");
        if (!enhanceCores.isEmpty()) {
            for (String core : enhanceCores) sb.append(core).append("\n");
        } else {
            sb.append("장착 중인 강화 코어가 없습니다.\n");
        }
        sb.append("\n");

        // 🌟 2. 스킬 프리셋 파싱 로직
        sb.append("🍁 스킬 프리셋\n");
        if (skillEquip != null && skillEquip.getSkill() != null && skillEquip.getSkill().getPreset() != null) {
            boolean hasPreset = false;
            for (VMatrixDTO.SkillEquip.Preset preset : skillEquip.getSkill().getPreset()) {
                int pNo = preset.getPresetSlotNo() != null ? preset.getPresetSlotNo() : 0;

                List<String> presetSkills = new ArrayList<>();
                if (preset.getSkillName1() != null) presetSkills.add(preset.getSkillName1());
                if (preset.getSkillName2() != null) presetSkills.add(preset.getSkillName2());
                if (preset.getSkillName3() != null) presetSkills.add(preset.getSkillName3());
                if (preset.getSkillName4() != null) presetSkills.add(preset.getSkillName4());

                if (!presetSkills.isEmpty()) {
                    sb.append(String.format("▪ [ %d번 ] %s\n", pNo, String.join(" / ", presetSkills)));
                    hasPreset = true;
                }
            }
            if (!hasPreset) sb.append("장착 중인 스킬 프리셋이 없습니다.\n");
        } else {
            sb.append("장착 중인 스킬 프리셋이 없습니다.\n");
        }
        sb.append("\n");

        sb.append("🍁 스킬 코어\n");
        if (!skillCores.isEmpty()) {
            for (String core : skillCores) sb.append(core).append("\n");
        } else {
            sb.append("장착 중인 스킬 코어가 없습니다.\n");
        }
        sb.append("\n");

        sb.append("🍁 쓸만한 스킬\n");
        if (!decentCores.isEmpty()) {
            for (String core : decentCores) sb.append(core).append("\n");
        } else {
            sb.append("장착 중인 쓸만한 스킬이 없습니다.\n");
        }
        sb.append("\n");

        sb.append("🍁 특수 코어\n");
        if (!specialCores.isEmpty()) {
            for (String core : specialCores) sb.append(core).append("\n");
        } else {
            sb.append("장착 중인 특수 코어가 없습니다.\n");
        }

        return sb.toString().trim();
    }
}
