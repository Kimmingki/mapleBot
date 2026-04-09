package com.classic.maple.controller;

import com.classic.maple.service.command.character.*;
import com.classic.maple.service.command.equipment.BossCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/help")
    public String getHelpCommand() {
        log.info(".명령어(도움말) 요청 수신");

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

                ⭑ 경험치: 현재 경험치 및 렙업 예상일


                『 장비 정보 조회 』

                ⭑ 사냥: 사냥 프리셋 장비 조회 (예: .사냥 닉네임)

                ⭑ 보스: 보스 프리셋 장비 조회 (예: .보스 닉네임)

                ⭑ 장비: 특정 부위 아이템 조회 (예: .장비 닉네임 서버명 무기)

                ⭑ 비교 / 헥사비교: 두 캐릭터의 보스 장비 비교 (예: .비교 닉네임1 닉네임2)

                ⭑ 장착: 두 캐릭터의 보스 장비 장착 (예: .장착 닉네임1 닉네임2 부위명)

                ⭑ 저장: 캐릭터의 보스 세팅 저장 (예: .저장 닉네임)

                ⭑ 삭제: 저장된 캐릭터의 세팅 삭제 (예: .삭제 닉네임)

                ⭑ 스펙변동: 저장된 캐릭터의 스펙변동 조회 (예: .스펙변동 닉네임)



                『 게임 정보 』

                ⭑ 쥬얼: 쥬얼 강화표

                ⭑ 기여도: 보스별 기여도 및 체력표

                ⭑ 도핑: 물리/마법 계열 도핑 정보

                ⭑ 문장: 문장 강화표 및 문장도박표

                ⭑ 극옵: 잠재능력,추옵,극옵 정보

                ⭑ 레드메소: 필드 레드메소 퀘스트

                ⭑ 유니온: 유니온 배치 정보

                ⭑ 하이퍼스탯: 하이퍼스탯 정보

                ⭑ 소울: 보스 소울 옵션표

                ⭑ 몬파: 몬스터파크 경험치표

                ⭑ 계산기: 헥사 코어 강화 비용 계산기 (예: .계산기 오리진 20)

                ⭑ 해방도움말: 제네시스 무기 해방일자 계산기 (예: .해방도움말)


                『 직업 이미지 』

                ⭑ 아이엘, 시아, 에릴, 라라, 데몬어벤져, 카데나, 듀얼블레이드, 카이저, 비숍, 메르세데스, 칼리, 보우마스터, 히어로, 은월


                『 보스 패턴 이미지 』

                ⭑ 데미안 ,아자젤 ,아칸, 윌, 검은마법사, 진힐라, 듄켈, 카윌, 세렌""";

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

    @GetMapping("/guildskill")
    public String getGuildSkillCommand(@RequestParam String name, @RequestParam(required = false, defaultValue = "스카니아") String world) {
        log.info(".길스 요청 - 캐릭터명: {}, 월드: {}", name, world);
        return guildSkillCommandService.getCharacterGuildSkill(name, world);
    }
}
