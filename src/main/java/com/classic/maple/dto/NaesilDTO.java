package com.classic.maple.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

public class NaesilDTO {

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Basic {
        @JsonProperty("character_name") private String characterName;
        @JsonProperty("world_name") private String worldName;
        @JsonProperty("character_guild_name") private String characterGuildName;
    }

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Union {
        @JsonProperty("union_grade") private String unionGrade;
        @JsonProperty("union_level") private Integer unionLevel;
    }

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Equip {
        @JsonProperty("item_equipment") private List<Item> itemEquipment;
        @JsonProperty("equipment_preset") private List<Preset> equipmentPreset;

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Preset {
            @JsonProperty("preset_no") private Integer presetNo;
            @JsonProperty("item_equipment") private List<Item> itemEquipment;
        }

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Item {
            @JsonProperty("item_name") private String itemName;
            @JsonProperty("item_equipment_slot_name") private String itemSlotName; // 🌟 한벌옷 판별용 추가
            @JsonProperty("starforce_upgrade") private String starforce;
            @JsonProperty("item_potential_option") private List<Option> potential;
            @JsonProperty("item_additional_potential_option") private List<Option> additional;
        }

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Option {
            @JsonProperty("option_name") private String optionName;
            @JsonProperty("option_value") private String optionValue;
        }
    }

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Symbol {
        @JsonProperty("arcane_symbol") private List<Data> arcaneSymbol;
        @JsonProperty("authentic_symbol") private List<Data> authenticSymbol;

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Data {
            @JsonProperty("symbol_option") private String symbolOption;
        }
    }

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HexaSkill {
        @JsonProperty("hexamatrix_skill") private List<Core> hexaCores;

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Core {
            @JsonProperty("skill_name") private String coreName;
            @JsonProperty("slot_level") private Integer coreLevel;
            @JsonProperty("skill_type") private String coreType;
        }
    }

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HexaStat {
        @JsonProperty("hexamatrix_stat") private List<StatCore> hexamatrixStat;

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class StatCore {
            @JsonProperty("stat_info") private List<StatInfo> statInfo;
        }

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class StatInfo {
            @JsonProperty("activate_flag") private String activateFlag;
            @JsonProperty("main_stat") private String mainStat;
            @JsonProperty("main_stat_level") private Integer mainStatLevel;
            @JsonProperty("sub_1_stat_level") private Integer sub1StatLevel;
            @JsonProperty("sub_2_stat_level") private Integer sub2StatLevel;
        }
    }

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Link {
        @JsonProperty("use_preset_no") private Integer usePresetNo;
        @JsonProperty("use_prest_no") private Integer usePrestNo;
        @JsonProperty("link_skill") private List<Preset> linkSkill;

        public Integer getActivePresetNo() {
            return usePresetNo != null ? usePresetNo : usePrestNo;
        }

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Preset {
            @JsonProperty("preset_no") private Integer presetNo;
            @JsonProperty("link_skill_info") private List<Info> linkSkillInfo;
        }

        @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Info {
            @JsonProperty("skill_name") private String skillName;
        }
    }
}