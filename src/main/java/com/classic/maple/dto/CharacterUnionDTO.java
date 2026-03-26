package com.classic.maple.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CharacterUnionDTO {

    @JsonProperty("union_grade") private String unionGrade;
    @JsonProperty("union_level") private Integer unionLevel;
}
