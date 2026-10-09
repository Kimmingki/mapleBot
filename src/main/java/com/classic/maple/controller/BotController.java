package com.classic.maple.controller;

import com.classic.maple.service.command.character.*;
import com.classic.maple.service.command.equipment.BossCommandService;
import com.classic.maple.service.command.info.GuideImageCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bot")
public class BotController {

    private final InfoCommandService infoCommandService;
    private final BossCommandService bossCommandService;
    private final NaesilCommandService naesilCommandService;
    private final SymbolCommandService symbolCommandService;
    private final VMatrixCommandService vMatrixCommandService;
    private final CodiCommandService codiCommandService;
    private final GuildSkillCommandService guildSkillCommandService;
    private final HexaCommandService hexaCommandService;
    private final GuideImageCommandService guideImageCommandService;

    @GetMapping("/help")
    public String getHelpCommand() {
        // 카카오톡 '전체보기' 기능을 트리거하기 위한 제로-위드 스페이스(Zero-width space) 500개 생성
        String readMore = "\u200B".repeat(500);

        // Java 텍스트 블록을 사용하여 요청하신 양식을 그대로 구현
        String helpText = """
                모든 명령어 앞에 . 을 붙여주세요. (예: .명령어)


                『 캐릭터 정보 조회 』 (예: .정보 귀요밍키 스카니아)

                ⭑ 정보: 캐릭터 종합 정보

                ⭑ 내실: 유니온, 장비, 링크, 심볼, 헥사, 길스 요약, 링크 요약

                ⭑ 심볼: 아케인/어센틱 심볼 정보

                ⭑ 코디: 코디 아이템 정보

                ⭑ 헥사: 6차 헥사 스킬/스탯 정보

                ⭑ 코강: 5차 V매트릭스 코어 강화 정보

                ⭑ 링크: 장착 중인 링크 스킬 정보

                ⭑ 길스: 적용 중인 길드 스킬 정보

                ⭑ 경험치: 현재 레벨 및 누적 경험치


                『 장비 정보 조회 』

                ⭑ 사냥: 사냥 프리셋 장비 조회 (예: .사냥 닉네임)

                ⭑ 보스: 보스 프리셋 장비 조회 (예: .보스 닉네임)

                ⭑ 장비: 특정 부위 아이템 조회 (예: .장비 닉네임 서버명 무기)

                ⭑ 비교 / 헥사비교: 두 캐릭터의 보스 장비 비교 (예: .비교 닉네임1 닉네임2)


                『 게임 정보 』

                ⭑ 체크리스트: 메이플라빈스31 셀프 체크리스트

                ⭑ 추가효과: 10레벨별 추가효과 스킬 정리표

                ⭑ 빛의성소: 빛의 성소 성혼·기도·축복 정리

                ⭑ 유효옵: 강환/영환/잠재/에디 유효옵 수치표

                ⭑ 링크표: 링크스킬 개편 한눈에 보기

                ⭑ 소울표: 위대한 소울 옵션 정리

                ⭑ 입장컷: 보스 입장 전투력

                ⭑ 문장표: 문장 마력의 증표

                ⭑ 하이로스: 하이로스의 봉인 층별 보상

                ⭑ 헥사효과표: 헥사스탯 메인/부가 효과표""";

        // 제목 + 전체보기 공백 + 본문 내용 합쳐서 반환
        return "메이플스토리M 정보 안내 봇 도움말" + readMore + "\n\n" + helpText;
    }

    @GetMapping("/info")
    public String getInfoCommand(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info(".정보 요청 - 캐릭터명: {}, 월드: {}", name, world);
        return infoCommandService.getCharacterInfo(name, world);
    }

    @GetMapping("/exp")
    public String getExpCommand(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info(".경험치 요청 - 캐릭터명: {}, 월드: {}", name, world);
        return infoCommandService.getCharacterExp(name, world);
    }

    @GetMapping("/boss")
    public String getBossSettingCommand(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info(".보스 요청 - 캐릭터명: {}, 월드: {}", name, world);
        return bossCommandService.getBossEquipmentSetting(name, world);
    }

    @GetMapping("/naesil")
    public String getNaesilCommand(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info(".내실 요청 - 캐릭터명: {}, 월드: {}", name, world);
        return naesilCommandService.getCharacterNaesil(name, world);
    }

    @GetMapping("/symbol")
    public String getSymbolCommand(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info(".심볼 요청 - 캐릭터명: {}, 월드: {}", name, world);
        return symbolCommandService.getCharacterSymbol(name, world);
    }

    @GetMapping("/vmatrix")
    public String getVMatrixCommand(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info(".코강 요청 - 캐릭터명: {}, 월드: {}", name, world);
        return vMatrixCommandService.getCharacterVMatrix(name, world);
    }

    @GetMapping("/codi")
    public String getCodiCommand(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info(".코디 요청 - 캐릭터명: {}, 월드: {}", name, world);
        return codiCommandService.getCharacterCodi(name, world);
    }

    // 카카오톡 링크 미리보기용 HTML 페이지 (text/plain이 아닌 HTML 응답)
    @GetMapping(value = "/codi/view", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    public String getCodiPreviewPage(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info(".코디 미리보기 페이지 요청 - 캐릭터명: {}, 월드: {}", name, world);
        return codiCommandService.buildPreviewHtml(name, world);
    }

    @GetMapping("/guildskill")
    public String getGuildSkillCommand(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info(".길스 요청 - 캐릭터명: {}, 월드: {}", name, world);
        return guildSkillCommandService.getCharacterGuildSkill(name, world);
    }

    @GetMapping("/hexa")
    public String getHexaCommand(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info(".헥사 요청 - 캐릭터명: {}, 월드: {}", name, world);
        return hexaCommandService.getCharacterHexa(name, world);
    }

    @GetMapping("/guide")
    public String getGuideImageCommand(@RequestParam String key) {
        log.info(".{} 이미지 요청", key);
        return guideImageCommandService.getGuideImage(key);
    }

    // 정보 이미지 링크 미리보기용 HTML 페이지
    @GetMapping(value = "/guide/view", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    public String getGuidePreviewPage(@RequestParam String key) {
        return guideImageCommandService.buildPreviewHtml(key);
    }
}
