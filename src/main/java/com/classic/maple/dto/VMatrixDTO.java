package com.classic.maple.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class VMatrixDTO {

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Basic {
        @JsonProperty("character_name") private String characterName;
        @JsonProperty("world_name") private String worldName;
        @JsonProperty("character_class") private String characterJobName;
    }

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VCoreInfo {
        @JsonProperty("character_v_core_equipment") private List<VCore> vCores;

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class VCore {
            @JsonProperty("vcore_level") private Integer vcoreLevel;
            @JsonProperty("vcore_type") private String vcoreType; // Enhancement, Skill, Special
            @JsonProperty("vcore_score") private Long vcoreScore; // 코어 점수 (int64)

            // 코어 스킬명 1~4 (강화 코어: 조합 표시, 스킬·특수 코어: 1번이 코어명)
            @JsonProperty("vcore_skill_name1") private String skillName1;
            @JsonProperty("vcore_skill_name2") private String skillName2;
            @JsonProperty("vcore_skill_name3") private String skillName3;
            @JsonProperty("vcore_skill_name4") private String skillName4;

            @JsonProperty("vcore_equipment_flag") private String equipmentFlag; // 장착 여부 (0:미장착, 1:장착)
        }
    }

    // 🌟 장착 스킬(프리셋) 완벽 매핑
    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SkillEquip {
        @JsonProperty("skill") private SkillData skill;

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class SkillData {
            @JsonProperty("preset") private List<Preset> preset;
        }

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Preset {
            @JsonProperty("preset_slot_no") private Integer presetSlotNo;
            @JsonProperty("skill_name_1") private String skillName1;
            @JsonProperty("skill_name_2") private String skillName2;
            @JsonProperty("skill_name_3") private String skillName3;
            @JsonProperty("skill_name_4") private String skillName4;
        }
    }
}
