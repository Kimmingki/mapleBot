package com.classic.maple.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CharacterPetDTO {

    @JsonProperty("pet_1_name") private String pet1Name;
    @JsonProperty("pet_2_name") private String pet2Name;
    @JsonProperty("pet_3_name") private String pet3Name;
}
