package com.classic.maple.service.command.character;

import com.classic.maple.dto.CodiDTO;
import com.classic.maple.service.core.NexonApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodiCommandService {

    private final NexonApiClient apiClient;

    // 외부 접근 가능한 서버 주소 (카카오톡 미리보기 크롤러 접근용)
    @Value("${bot.public-base-url}")
    private String publicBaseUrl;

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
        // 넥슨 원본 URL 대신 og:image 미리보기 페이지 URL 전달
        String imageUrl = basic.getCharacterImage() != null ? buildPreviewUrl(characterName, worldName) : "이미지없음";
        sb.append("\n|||IMAGE|||").append(imageUrl);

        return sb.toString().trim();
    }

    // 캐릭터 이미지 URL 조회 (조회 실패 시 null)
    public String getCharacterImageUrl(String characterName, String worldName) {
        String ocid = apiClient.getCharacterOcid(characterName, worldName);
        if (ocid == null) return null;

        CodiDTO.Basic basic = apiClient.fetchApiData("/character/basic", ocid, CodiDTO.Basic.class);
        return basic != null ? basic.getCharacterImage() : null;
    }

    // 미리보기 페이지 HTML 조립 (og:image 메타 태그로 카카오톡 썸네일 노출)
    public String buildPreviewHtml(String characterName, String worldName) {
        String imageUrl = getCharacterImageUrl(characterName, worldName);
        String title = HtmlUtils.htmlEscape(characterName + "님의 코디");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"ko\"><head><meta charset=\"UTF-8\">");
        html.append("<meta property=\"og:title\" content=\"").append(title).append("\">");
        html.append("<meta property=\"og:description\" content=\"메이플스토리M 코디 이미지\">");
        if (imageUrl != null) {
            html.append("<meta property=\"og:image\" content=\"").append(HtmlUtils.htmlEscape(imageUrl)).append("\">");
        }
        html.append("<title>").append(title).append("</title></head><body>");

        // 브라우저로 직접 열었을 때 표시용 본문
        if (imageUrl != null) {
            html.append("<img src=\"").append(HtmlUtils.htmlEscape(imageUrl)).append("\" alt=\"").append(title).append("\">");
        } else {
            html.append("<p>캐릭터 이미지를 찾을 수 없습니다.</p>");
        }
        html.append("</body></html>");
        return html.toString();
    }

    // 미리보기 페이지 URL 조립 (한글 닉네임·월드 UTF-8 인코딩)
    private String buildPreviewUrl(String characterName, String worldName) {
        return UriComponentsBuilder.fromUriString(publicBaseUrl + "/api/bot/codi/view")
                .queryParam("name", characterName)
                .queryParam("world", worldName)
                .build().encode(StandardCharsets.UTF_8).toUriString();
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
