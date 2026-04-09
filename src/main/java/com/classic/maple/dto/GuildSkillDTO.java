package com.classic.maple.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class GuildSkillDTO {

    // 길드 이름만 가져오는 용도
    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NameOnly {
        @JsonProperty("guild_name") private String guildName;
    }

    // 길드원 리스트와 개인 스킬 정보
    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Basic {
        @JsonProperty("guild_member") private List<GuildMember> guildMember;

        @Getter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class GuildMember {
            @JsonProperty("character_name") private String characterName;
            @JsonProperty("guild_activity") private Long guildActivity; // 🌟 기여도 매핑
            @JsonProperty("guild_personal_skill") private List<PersonalSkill> guildPersonalSkill; // 🌟 개인 스킬
        }

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PersonalSkill {
            @JsonProperty("skill_name") private String skillName;
            @JsonProperty("skill_level") private Integer skillLevel;
            @JsonProperty("skill_option") private String skillOption;
        }
    }
}
