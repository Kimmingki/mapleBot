package com.classic.maple.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CharacterBasicDTO {

    @JsonProperty("character_name") private String characterName;
    @JsonProperty("world_name") private String worldName;
    @JsonProperty("character_level") private Integer characterLevel;
    @JsonProperty("character_job_name") private String characterJobName;
    @JsonProperty("character_gender") private String characterGender;
    @JsonProperty("character_guild_name") private String characterGuildName;
    @JsonProperty("character_date_create") private String characterDateCreate;
    @JsonProperty("character_exp") private Long characterExp;
}
