package com.classic.maple.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ItemEquipmentDTO {

    @JsonProperty("item_equipment") private List<ItemData> itemEquipment;

    @Getter
    @NoArgsConstructor
    public static class ItemData {
        @JsonProperty("item_name") private String itemName;
        @JsonProperty("item_grade") private String itemGrade; // "레전더리", "유니크" 등 한글
        @JsonProperty("starforce_upgrade") private String starforce; // "20" 같은 문자열로 옴
        @JsonProperty("cuttable_count") private Integer cuttableCount;

        // 옵션 배열들 이름표 수정
        @JsonProperty("item_basic_option") private List<ItemOption> baseOption;
        @JsonProperty("item_additional_option") private List<ItemOption> addOption;
        @JsonProperty("item_potential_option") private List<ItemOption> potentialOption;
        @JsonProperty("item_additional_potential_option") private List<ItemOption> additionalOption;

        // 소울 정보 구조 변경
        @JsonProperty("soul_info") private SoulInfo soulInfo;
    }

    @Getter
    @NoArgsConstructor
    public static class ItemOption {
        @JsonProperty("option_name") private String optionName;
        @JsonProperty("option_value") private String optionValue;
    }

    @Getter
    @NoArgsConstructor
    public static class SoulInfo {
        @JsonProperty("soul_name") private String soulName;
        @JsonProperty("soul_option") private String soulOption;
    }
}
