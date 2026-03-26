package com.classic.maple.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class JewelEquipmentDTO {

    // 현재 장착 중인 쥬얼 페이지 번호
    @JsonProperty("use_jewel_page_no")
    private Integer useJewelPageNo;

    // 쥬얼 페이지들의 리스트
    @JsonProperty("jewel_equipment")
    private List<JewelPage> jewelEquipment;

    @Getter
    @NoArgsConstructor
    public static class JewelPage {
        @JsonProperty("jewel_page_no")
        private Integer jewelPageNo;

        // 해당 페이지에 장착된 쥬얼 5개
        @JsonProperty("jewel_info")
        private List<JewelData> jewelInfo;
    }

    @Getter
    @NoArgsConstructor
    public static class JewelData {
        @JsonProperty("jewel_name")
        private String jewelName;
    }
}
