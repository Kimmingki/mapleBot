package com.classic.maple.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class HexaDTO {

    // 🌟 헥사 스킬 코어
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SkillInfo {
        @JsonProperty("hexamatrix_skill") private List<SkillCore> hexamatrixSkill;

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class SkillCore {
            @JsonProperty("skill_type") private String skillType; // 스킬 코어, 마스터리 코어, 강화 코어
            @JsonProperty("skill_name") private String skillName;
            @JsonProperty("slot_level") private Integer slotLevel;
        }
    }

    // 🌟 헥사 스탯 코어
    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatInfo {
        @JsonProperty("hexamatrix_stat") private List<StatCore> hexamatrixStat;

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class StatCore {
            @JsonProperty("stat_info") private List<StatPage> statInfo;
        }

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class StatPage {
            @JsonProperty("page_no") private Integer pageNo;
            @JsonProperty("activate_flag") private String activateFlag; // "1"이면 활성
            @JsonProperty("main_stat") private String mainStat;
            @JsonProperty("main_stat_level") private Integer mainStatLevel;
            @JsonProperty("sub_1_stat") private String sub1Stat;
            @JsonProperty("sub_1_stat_level") private Integer sub1StatLevel;
            @JsonProperty("sub_2_stat") private String sub2Stat;
            @JsonProperty("sub_2_stat_level") private Integer sub2StatLevel;
        }
    }
}
