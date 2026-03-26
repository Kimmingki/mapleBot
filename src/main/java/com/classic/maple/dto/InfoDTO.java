package com.classic.maple.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

public class InfoDTO {

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Basic {
        @JsonProperty("character_name") private String characterName;
        @JsonProperty("world_name") private String worldName;
        @JsonProperty("character_class") private String characterJobName;
        @JsonProperty("character_level") private Integer characterLevel;
        @JsonProperty("character_exp") private Long characterExp;
        @JsonProperty("character_gender") private String characterGender;
        @JsonProperty("character_date_create") private String characterDateCreate;
    }

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Guild {
        @JsonProperty("guild_name") private String guildName;
    }

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Union {
        @JsonProperty("union_grade") private String unionGrade;
        @JsonProperty("union_level") private Integer unionLevel;
    }

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pet {
        @JsonProperty("pet_1_name") private String pet1Name;
        @JsonProperty("pet_2_name") private String pet2Name;
        @JsonProperty("pet_3_name") private String pet3Name;

        // 🌟 펫 세트 옵션 리스트
        @JsonProperty("pet_set_option") private List<PetSetOption> petSetOption;

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PetSetOption {
            @JsonProperty("set_name") private String setName;
        }
    }
}