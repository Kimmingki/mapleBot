package com.classic.maple.service.command.character;

import com.classic.maple.dto.HexaDTO;
import com.classic.maple.dto.VMatrixDTO;
import com.classic.maple.service.core.NexonApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HexaCommandService {

    private final NexonApiClient apiClient;

    // 🌟 메이플스토리M 전용: 각 코어별 1~30레벨 '누적' 소모량 테이블 (인덱스 = 레벨)
    // index 1: 1레벨(해금) 시점까지 소모된 총량
    // index 30: 만렙 달성 시 소모된 총량
    private static final int[] SKILL_CORE_SOL = {0, 16, 17, 18, 20, 22, 24, 27, 30, 34, 44, 49, 54, 60, 66, 73, 80, 88, 96, 105, 120, 126, 133, 141, 150, 160, 171, 183, 196, 210, 230};
    private static final int[] SKILL_CORE_FRAG = {0, 390, 415, 445, 480, 520, 565, 615, 670, 730, 1030, 1120, 1220, 1330, 1450, 1580, 1720, 1870, 2030, 2200, 2700, 2815, 2940, 3075, 3220, 3375, 3540, 3715, 3900, 4095, 4595};

    private static final int[] MASTERY_CORE_SOL = {0, 8, 9, 10, 11, 12, 13, 14, 16, 18, 28, 30, 32, 35, 38, 41, 45, 49, 53, 58, 73, 76, 80, 84, 88, 93, 98, 104, 110, 117, 141};
    private static final int[] MASTERY_CORE_FRAG = {0, 140, 155, 175, 200, 230, 265, 305, 350, 400, 600, 660, 730, 810, 900, 1000, 1110, 1230, 1360, 1500, 1800, 1885, 1980, 2085, 2200, 2325, 2460, 2605, 2760, 2925, 3225};

    private static final int[] ENHANCE_CORE_SOL = {0, 12, 13, 14, 15, 17, 19, 21, 23, 26, 36, 39, 42, 46, 50, 54, 59, 64, 69, 75, 90, 94, 99, 104, 110, 116, 123, 130, 138, 146, 181};
    private static final int[] ENHANCE_CORE_FRAG = {0, 235, 265, 300, 340, 385, 435, 490, 550, 615, 865, 945, 1035, 1135, 1245, 1365, 1495, 1635, 1785, 1945, 2345, 2465, 2595, 2735, 2885, 3045, 3215, 3395, 3585, 3785, 4400};


    public String getCharacterHexa(String characterName, String worldName) {
        String ocid = apiClient.getCharacterOcid(characterName, worldName);
        if (ocid == null) return "캐릭터 정보를 찾을 수 없습니다.";

        // 이름 추출을 위해 Basic 호출
        VMatrixDTO.Basic basic = apiClient.fetchApiData("/character/basic", ocid, VMatrixDTO.Basic.class);
        if (basic == null) return "기본 정보를 불러올 수 없습니다.";

        HexaDTO.SkillInfo skillInfo = apiClient.fetchApiData("/character/hexamatrix-skill", ocid, HexaDTO.SkillInfo.class);
        HexaDTO.StatInfo statInfo = apiClient.fetchApiData("/character/hexamatrix-stat", ocid, HexaDTO.StatInfo.class);

        StringBuilder sb = new StringBuilder();
        String readMore = "\u200B".repeat(500);
        DecimalFormat df = new DecimalFormat("#,###");

        sb.append("🍁 【 메이플스토리M 헥사 매트릭스 】\n");
        sb.append(basic.getCharacterName()).append(" (").append(basic.getWorldName() != null ? basic.getWorldName() : worldName).append(")\n").append(readMore).append("\n");

        int totalRequiredSol = 0;
        int totalRequiredFrag = 0;

        List<String> skillCoreTexts = new ArrayList<>();

        // 🌟 1. 헥사 스킬 파싱 및 전체 소모량 계산
        if (skillInfo != null && skillInfo.getHexamatrixSkill() != null) {
            for (HexaDTO.SkillInfo.SkillCore core : skillInfo.getHexamatrixSkill()) {
                String type = core.getSkillType() != null ? core.getSkillType() : "알 수 없음";
                String name = core.getSkillName() != null ? core.getSkillName() : "알 수 없음";
                int level = core.getSlotLevel() != null ? core.getSlotLevel() : 0;

                // 에러 방지를 위해 레벨 제한 (0~30)
                level = Math.max(0, Math.min(30, level));

                int reqSol = 0;
                int reqFrag = 0;

                // 타입별로 남은 요구량 계산 (만렙 총량 - 현재 레벨 총량)
                if ("스킬 코어".equals(type)) {
                    reqSol = SKILL_CORE_SOL[30] - SKILL_CORE_SOL[level];
                    reqFrag = SKILL_CORE_FRAG[30] - SKILL_CORE_FRAG[level];
                } else if ("마스터리 코어".equals(type)) {
                    reqSol = MASTERY_CORE_SOL[30] - MASTERY_CORE_SOL[level];
                    reqFrag = MASTERY_CORE_FRAG[30] - MASTERY_CORE_FRAG[level];
                } else if ("강화 코어".equals(type)) {
                    reqSol = ENHANCE_CORE_SOL[30] - ENHANCE_CORE_SOL[level];
                    reqFrag = ENHANCE_CORE_FRAG[30] - ENHANCE_CORE_FRAG[level];
                }

                totalRequiredSol += reqSol;
                totalRequiredFrag += reqFrag;

                // 스킬 상세 문자열 조립
                StringBuilder cSb = new StringBuilder();
                cSb.append("【 ").append(type).append(" 】 ").append(name).append(" 【 Lv.").append(level).append(" 】\n");

                if (level == 30) {
                    cSb.append(" · 남은 재화: MAX\n");
                } else {
                    cSb.append(" · 남은 재화: 솔 에르다 ").append(df.format(reqSol)).append("개 / 조각 ").append(df.format(reqFrag)).append("개\n");
                }
                skillCoreTexts.add(cSb.toString());
            }
        }

        // 🌟 2. 헥사 스탯 파싱 및 활성화된 페이지 추출
        List<String> statPageTexts = new ArrayList<>();
        String activeMainStat = "";

        if (statInfo != null && statInfo.getHexamatrixStat() != null) {
            for (HexaDTO.StatInfo.StatCore sCore : statInfo.getHexamatrixStat()) {
                if (sCore.getStatInfo() != null) {
                    for (HexaDTO.StatInfo.StatPage page : sCore.getStatInfo()) {
                        int pNo = page.getPageNo() != null ? page.getPageNo() : 0;
                        boolean isActive = "1".equals(page.getActivateFlag());

                        String mStat = page.getMainStat() != null ? page.getMainStat() : "정보 없음";
                        int mLevel = page.getMainStatLevel() != null ? page.getMainStatLevel() : 0;
                        String sub1 = page.getSub1Stat() != null ? page.getSub1Stat() : "정보 없음";
                        int s1Level = page.getSub1StatLevel() != null ? page.getSub1StatLevel() : 0;
                        String sub2 = page.getSub2Stat() != null ? page.getSub2Stat() : "정보 없음";
                        int s2Level = page.getSub2StatLevel() != null ? page.getSub2StatLevel() : 0;

                        StringBuilder pSb = new StringBuilder();
                        pSb.append("페이지 ").append(pNo);
                        if (isActive) pSb.append(" (활성)");
                        pSb.append("\n");
                        pSb.append(" · 메인: ").append(mStat).append(" 【 Lv.").append(mLevel).append(" 】\n");
                        pSb.append(" · 서브1: ").append(sub1).append(" 【 Lv.").append(s1Level).append(" 】\n");
                        pSb.append(" · 서브2: ").append(sub2).append(" 【 Lv.").append(s2Level).append(" 】\n");

                        statPageTexts.add(pSb.toString());

                        // 대표(활성화된) 스탯 저장
                        if (isActive && mLevel > 0) {
                            activeMainStat = pSb.toString();
                        }
                    }
                }
            }
        }

        // 🌟 3. 최종 출력 텍스트 조립
        sb.append("🍁 【 필요 솔 에르다 】 : ").append(df.format(totalRequiredSol)).append("개\n");
        sb.append("🍁 【 필요 솔 에르다 조각 】 : ").append(df.format(totalRequiredFrag)).append("개\n\n");

        sb.append("🍁 【 대표 헥사 스탯 】\n");
        if (!activeMainStat.isEmpty()) {
            sb.append(activeMainStat.replace(" (활성)", "")).append("\n");
        } else {
            sb.append("활성화된 헥사 스탯이 없습니다.\n\n");
        }

        sb.append("🍁 【 헥사 스킬 상세 (MAX 30) 】\n\n");
        if (!skillCoreTexts.isEmpty()) {
            for (String txt : skillCoreTexts) sb.append(txt).append("\n");
        } else {
            sb.append("장착 중인 헥사 코어가 없습니다.\n\n");
        }

        sb.append("🍁 【 헥사 스탯 상세 】\n");
        if (!statPageTexts.isEmpty()) {
            for (String txt : statPageTexts) sb.append(txt).append("\n");
        } else {
            sb.append("헥사 스탯 정보가 없습니다.\n");
        }

        return sb.toString().trim();
    }
}
