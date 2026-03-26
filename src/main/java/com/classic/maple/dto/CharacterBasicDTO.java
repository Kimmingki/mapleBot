package com.classic.maple.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterBasicDTO {

    @JsonProperty("character_name") private String characterName;
    @JsonProperty("world_name") private String worldName;
    @JsonProperty("character_class") private String characterJobName;
    @JsonProperty("character_level") private Integer characterLevel;
    @JsonProperty("character_exp") private Long characterExp;
    @JsonProperty("character_guild_name") private String characterGuildName;
    @JsonProperty("character_gender") private String characterGender;
}
