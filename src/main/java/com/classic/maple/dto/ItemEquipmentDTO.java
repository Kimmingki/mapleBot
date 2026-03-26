package com.classic.maple.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemEquipmentDTO {
    @JsonProperty("item_equipment") private List<ItemData> itemEquipment;
    @JsonProperty("equipment_preset") private List<Preset> equipmentPreset;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Preset {
        @JsonProperty("preset_no") private Integer presetNo;
        @JsonProperty("item_equipment") private List<ItemData> itemEquipment;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemData {
        @JsonProperty("item_name") private String itemName;
        @JsonProperty("item_grade") private String itemGrade;
        @JsonProperty("item_equipment_slot_name") private String itemSlotName; // 🌟 한벌옷 판별용 추가
        @JsonProperty("starforce_upgrade") private String starforce;
        @JsonProperty("cuttable_count") private Integer cuttableCount;

        @JsonProperty("item_basic_option") private List<ItemOption> baseOption;
        @JsonProperty("item_additional_option") private List<ItemOption> addOption;
        @JsonProperty("item_potential_option") private List<ItemOption> potentialOption;
        @JsonProperty("item_additional_potential_option") private List<ItemOption> additionalOption;
        @JsonProperty("soul_info") private SoulInfo soulInfo;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemOption {
        @JsonProperty("option_name") private String optionName;
        @JsonProperty("option_value") private String optionValue;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SoulInfo {
        @JsonProperty("soul_name") private String soulName;
        @JsonProperty("soul_option") private String soulOption;
    }
}