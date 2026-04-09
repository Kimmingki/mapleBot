package com.classic.maple.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class CodiDTO {

    // 🌟 캐릭터 기본 정보
    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Basic {
        @JsonProperty("character_name") private String characterName;
        @JsonProperty("character_image") private String characterImage;
    }

    // 🌟 뷰티 정보
    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Beauty {
        @JsonProperty("character_hair") private Hair characterHair;
        @JsonProperty("character_face") private Face characterFace;
        @JsonProperty("character_skin_name") private String characterSkinName;

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Hair {
            @JsonProperty("hair_name") private String hairName;
            @JsonProperty("base_color") private String baseColor;
            @JsonProperty("mix_color") private String mixColor;
            @JsonProperty("mix_rate") private String mixRate;
        }

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Face {
            @JsonProperty("face_name") private String faceName;
            @JsonProperty("base_color") private String baseColor;
            @JsonProperty("mix_color") private String mixColor;
            @JsonProperty("mix_rate") private String mixRate;
        }
    }

    // 🌟 캐시 장비 정보
    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CashEquip {
        @JsonProperty("cash_item_equipment") private List<CashItem> cashItemEquipment;

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class CashItem {
            @JsonProperty("cash_item_equipment_slot_name") private String slotName;
            @JsonProperty("cash_item_name") private String itemName;
        }
    }
}
