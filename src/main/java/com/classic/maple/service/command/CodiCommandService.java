package com.classic.maple.service.command;

import com.classic.maple.dto.CodiDTO;
import com.classic.maple.service.core.NexonApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodiCommandService {

    private final NexonApiClient apiClient;

    public String getCharacterCodi(String characterName, String worldName) {
        String ocid = apiClient.getCharacterOcid(characterName, worldName);
        if (ocid == null) return "캐릭터 정보를 찾을 수 없습니다.";

        CodiDTO.Basic basic = apiClient.fetchApiData("/character/basic", ocid, CodiDTO.Basic.class);
        if (basic == null) return "기본 정보를 불러올 수 없습니다.";

        CodiDTO.Beauty beauty = apiClient.fetchApiData("/character/beauty-equipment", ocid, CodiDTO.Beauty.class);
        CodiDTO.CashEquip cashEquip = apiClient.fetchApiData("/character/cashitem-equipment", ocid, CodiDTO.CashEquip.class);

        StringBuilder sb = new StringBuilder();
        sb.append("🍁 【 ").append(basic.getCharacterName()).append("님의 코디 정보 】\n\n");

        // 🌟 1. 뷰티 정보 출력
        sb.append("🍁 【 뷰티 정보 】\n");
        if (beauty != null) {
            String hairText = "정보 없음";
            if (beauty.getCharacterHair() != null) {
                CodiDTO.Beauty.Hair hair = beauty.getCharacterHair();
                hairText = formatBeautyInfo(hair.getHairName(), hair.getBaseColor(), hair.getMixColor(), hair.getMixRate());
            }
            sb.append("• 헤어: ").append(hairText).append("\n");

            String faceText = "정보 없음";
            if (beauty.getCharacterFace() != null) {
                CodiDTO.Beauty.Face face = beauty.getCharacterFace();
                faceText = formatBeautyInfo(face.getFaceName(), face.getBaseColor(), face.getMixColor(), face.getMixRate());
            }
            sb.append("• 얼굴: ").append(faceText).append("\n");
            sb.append("• 피부: ").append(beauty.getCharacterSkinName() != null ? beauty.getCharacterSkinName() : "정보 없음").append("\n\n");
        } else {
            sb.append("• 뷰티 정보를 불러올 수 없습니다.\n\n");
        }

        // 🌟 2. 캐시 장비 출력
        sb.append("🍁 【 캐시 장비 】\n");
        if (cashEquip != null && cashEquip.getCashItemEquipment() != null) {
            List<CodiDTO.CashEquip.CashItem> items = cashEquip.getCashItemEquipment();
            if (!items.isEmpty()) {
                for (CodiDTO.CashEquip.CashItem item : items) {
                    String sName = item.getSlotName() != null ? item.getSlotName() : "알 수 없음";
                    String iName = item.getItemName() != null ? item.getItemName() : "알 수 없음";
                    sb.append("• ").append(sName).append(": ").append(iName).append("\n");
                }
            } else {
                sb.append("• 장착 중인 캐시 장비가 없습니다.\n");
            }
        } else {
            sb.append("• 캐시 장비 정보를 불러올 수 없습니다.\n");
        }

        // 🌟 3. 이미지 전송을 위한 식별자 결합
        String imageUrl = basic.getCharacterImage() != null ? basic.getCharacterImage() : "이미지없음";
        sb.append("\n|||IMAGE|||").append(imageUrl);

        return sb.toString().trim();
    }

    /**
     * 뷰티 정보를 "헤어명 (파란색 + 보라색 48%)" 형태로 예쁘게 묶어주는 헬퍼 메서드입니다.
     */
    private String formatBeautyInfo(String name, String baseColor, String mixColor, String mixRate) {
        if (name == null) return "알 수 없음";

        if (mixColor == null || mixColor.isEmpty() || "0".equals(mixRate) || "정보 없음".equals(mixColor)) {
            return name + " (" + (baseColor != null ? baseColor : "색상 없음") + ")";
        } else {
            return name + " (" + (baseColor != null ? baseColor : "알 수 없음") + " + " + mixColor + " " + (mixRate != null ? mixRate : "0") + "%)";
        }
    }
}
