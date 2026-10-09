package com.classic.maple.service.command.info;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
public class GuideImageCommandService {

    // 명령어 키워드 → 정보 이미지 매핑
    private static final Map<String, GuideImage> GUIDE_IMAGES = Map.ofEntries(
            Map.entry("체크리스트", new GuideImage("ms31-checklist.jpg", "메이플라빈스31 셀프 체크리스트")),
            Map.entry("추가효과", new GuideImage("skill-bonus.jpg", "10레벨별 추가효과 스킬 정리표")),
            Map.entry("빛의성소", new GuideImage("sanctuary.jpg", "빛의 성소 성혼·기도·축복 정리")),
            Map.entry("유효옵", new GuideImage("option-range.jpg", "강환/영환/잠재/에디 유효옵 수치표")),
            Map.entry("링크표", new GuideImage("link-skill.jpg", "링크스킬 개편 한눈에 보기")),
            Map.entry("소울표", new GuideImage("great-soul.jpg", "위대한 소울 옵션 정리")),
            Map.entry("입장컷", new GuideImage("boss-entry.jpg", "보스 입장 전투력")),
            Map.entry("문장표", new GuideImage("emblem.jpg", "문장 마력의 증표")),
            Map.entry("하이로스", new GuideImage("hiros-seal.jpg", "하이로스의 봉인 층별 보상")),
            Map.entry("헥사효과표", new GuideImage("hexa-stat.jpg", "헥사스탯 메인/부가 효과표"))
    );

    // 외부 접근 가능한 서버 주소
    @Value("${bot.public-base-url}")
    private String publicBaseUrl;

    // 이미지 교체 시 카카오톡 미리보기 캐시 우회용 버전
    @Value("${bot.guide-image-version:1}")
    private String imageVersion;

    // 채팅 응답: 제목 + 미리보기 페이지 URL
    public String getGuideImage(String keyword) {
        String key = normalizeKey(keyword);
        GuideImage guide = GUIDE_IMAGES.get(key);
        if (guide == null) return "등록되지 않은 이미지 명령어입니다.";

        return "🍁 【 " + guide.title() + " 】\n|||IMAGE|||" + buildPreviewUrl(key);
    }

    // 미리보기 페이지 HTML (og:image 썸네일 + 모바일 전체 폭 원본)
    public String buildPreviewHtml(String keyword) {
        GuideImage guide = GUIDE_IMAGES.get(normalizeKey(keyword));
        if (guide == null) return "<!DOCTYPE html><html lang=\"ko\"><body><p>등록되지 않은 이미지입니다.</p></body></html>";

        String title = HtmlUtils.htmlEscape(guide.title());
        String imageUrl = HtmlUtils.htmlEscape(publicBaseUrl + "/guide/" + guide.fileName() + "?v=" + imageVersion);

        return "<!DOCTYPE html><html lang=\"ko\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<meta property=\"og:title\" content=\"" + title + "\">"
                + "<meta property=\"og:description\" content=\"메이플스토리M 정보 이미지\">"
                + "<meta property=\"og:image\" content=\"" + imageUrl + "\">"
                + "<title>" + title + "</title>"
                + "<style>body{margin:0;background:#111}img{display:block;width:100%;height:auto}</style>"
                + "</head><body><img src=\"" + imageUrl + "\" alt=\"" + title + "\"></body></html>";
    }

    // 미리보기 페이지 URL (한글 키워드 UTF-8 인코딩, 캐시 버전 포함)
    private String buildPreviewUrl(String keyword) {
        return UriComponentsBuilder.fromUriString(publicBaseUrl + "/api/bot/guide/view")
                .queryParam("key", keyword)
                .queryParam("v", imageVersion)
                .build().encode(StandardCharsets.UTF_8).toUriString();
    }

    // 키워드 공백 제거 (".빛의 성소" / ".빛의성소" 동일 처리)
    private String normalizeKey(String keyword) {
        return keyword.replaceAll("\\s+", "");
    }

    // 이미지 파일명·제목 묶음
    private record GuideImage(String fileName, String title) {}
}
