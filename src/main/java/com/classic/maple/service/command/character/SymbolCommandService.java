package com.classic.maple.service.command.character;

import com.classic.maple.dto.NaesilDTO;
import com.classic.maple.service.core.NexonApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SymbolCommandService {

    private final NexonApiClient apiClient;

    // 메이플스토리M 심볼 최대 레벨
    private static final int ARCANE_MAX_LEVEL = 20;
    private static final int AUTHENTIC_MAX_LEVEL = 11;

    // 메이플스토리M 아케인심볼 레벨별 강화 요구 성장치 (인덱스 = 현재 레벨, 값 = 다음 레벨까지 필요 개수)
    // 1→20 합계 5,622개
    private static final int[] ARCANE_REQUIRED_GROWTH = {
            0, 15, 19, 24, 31, 40, 50, 62, 77, 96, 120,
            156, 202, 262, 340, 442, 574, 746, 1007, 1359
    };

    // 메이플스토리M 아케인심볼 레벨별 강화 메소 - 소멸의 여로 (1→20 합계 4,890,449,405)
    private static final long[] ARCANE_MESO_VANISHING_JOURNEY = {
            0L, 13_000_000L, 17_160_000L, 18_720_000L, 22_464_000L, 26_956_800L,
            33_696_000L, 42_120_000L, 54_756_000L, 71_182_800L, 92_600_798L,
            112_433_234L, 147_568_619L, 193_525_702L, 262_967_278L, 382_497_858L,
            518_760_000L, 702_830_000L, 951_176_547L, 1_226_033_769L
    };

    // 메이플스토리M 아케인심볼 레벨별 강화 메소 - 츄츄 아일랜드 이후 지역 공통 (1→20 합계 5,375,363,957)
    private static final long[] ARCANE_MESO_OTHERS = {
            0L, 13_000_000L, 17_160_000L, 20_592_000L, 24_710_400L, 29_652_480L,
            37_065_600L, 46_332_000L, 60_231_600L, 78_301_080L, 101_791_404L,
            123_676_740L, 162_325_625L, 212_878_930L, 289_264_719L, 420_748_240L,
            570_630_000L, 772_071_368L, 1_046_294_480L, 1_348_637_291L
    };

    // 메이플스토리M 어센틱심볼 레벨별 강화 요구 성장치 (전 지역 공통, 10n² + 3n, 1→11 합계 4,015개)
    private static final int[] AUTHENTIC_REQUIRED_GROWTH = {
            0, 13, 46, 99, 172, 265, 378, 511, 664, 837, 1030
    };

    // 메이플스토리M 어센틱심볼 레벨별 강화 메소 - 에세테라 (1→11 합계 5,582,500,000)
    private static final long[] AUTHENTIC_MESO_ESETERA = {
            0L, 8_500_000L, 30_000_000L, 73_500_000L, 148_000_000L, 262_500_000L,
            426_000_000L, 647_500_000L, 936_000_000L, 1_300_500_000L, 1_750_000_000L
    };

    // 메이플스토리M 어센틱심볼 레벨별 강화 메소 - 세르니움 (1→11 합계 5,885,000,000)
    private static final long[] AUTHENTIC_MESO_CERNIUM = {
            0L, 8_600_000L, 30_800_000L, 76_200_000L, 154_400_000L, 275_000_000L,
            447_600_000L, 681_800_000L, 987_200_000L, 1_373_400_000L, 1_850_000_000L
    };

    // 메이플스토리M 어센틱심볼 레벨별 강화 메소 - 아르크스 (1→11 합계 6,184,015,000)
    private static final long[] AUTHENTIC_MESO_ARCUS = {
            0L, 9_000_000L, 32_370_000L, 80_100_000L, 162_270_000L, 289_020_000L,
            470_420_000L, 716_560_000L, 1_037_530_000L, 1_446_300_000L, 1_940_445_000L
    };

    // 메이플스토리M 어센틱심볼 레벨별 강화 메소 - 아에르 (1→11 합계 6,190,720,000, 일부 추정치)
    private static final long[] AUTHENTIC_MESO_AER = {
            0L, 9_050_000L, 32_400_000L, 80_160_000L, 162_420_000L, 289_290_000L,
            470_850_000L, 717_220_000L, 1_038_480_000L, 1_444_750_000L, 1_946_100_000L
    };

    // 메이플스토리M 어센틱심볼 레벨별 강화 메소 - 오디움 (1→11 합계 6,773,180,000, 일부 추정치)
    private static final long[] AUTHENTIC_MESO_ODIUM = {
            0L, 9_900_000L, 35_450_000L, 87_700_000L, 177_700_000L, 316_500_000L,
            515_150_000L, 784_700_000L, 1_136_190_000L, 1_580_680_000L, 2_129_210_000L
    };

    // 메이플스토리M 어센틱심볼 레벨별 강화 메소 - 도원경 (1→11 합계 7,069,070,000, 일부 추정치)
    private static final long[] AUTHENTIC_MESO_DOWONGYEONG = {
            0L, 10_330_000L, 37_000_000L, 91_530_000L, 185_470_000L, 330_340_000L,
            537_490_000L, 819_000_000L, 1_185_860_000L, 1_649_770_000L, 2_222_280_000L
    };

    public String getCharacterSymbol(String characterName, String worldName) {
        String ocid = apiClient.getCharacterOcid(characterName, worldName);
        if (ocid == null) return "캐릭터 정보를 찾을 수 없습니다.";

        // 기본 정보·심볼 동시 호출
        CompletableFuture<NaesilDTO.Basic> basicF = apiClient.fetchApiDataAsync("/character/basic", ocid, NaesilDTO.Basic.class);
        CompletableFuture<NaesilDTO.Symbol> symbolF = apiClient.fetchApiDataAsync("/character/symbol", ocid, NaesilDTO.Symbol.class);

        NaesilDTO.Basic basic = NexonApiClient.join(basicF);
        NaesilDTO.Symbol symbol = NexonApiClient.join(symbolF);

        if (basic == null || symbol == null) return "심볼 정보를 불러올 수 없습니다.";

        DecimalFormat df = new DecimalFormat("#,###");

        int arcForce = 0;
        int autForce = 0;
        Pattern p = Pattern.compile("포스 증가 ([0-9]+)");

        // MAX까지 필요 메소 합계 (int 범위 초과 대비 long)
        long arcTotalMeso = 0L;
        long autTotalMeso = 0L;
        // 비용표 미확인 어센틱 지역 존재 여부
        boolean hasUnknownAutMeso = false;

        StringBuilder arcSb = new StringBuilder();
        StringBuilder autSb = new StringBuilder();

        // 🌟 아케인 심볼 파싱
        if (symbol.getArcaneSymbol() != null) {
            for (NaesilDTO.Symbol.Data sym : symbol.getArcaneSymbol()) {
                String name = sym.getSymbolName() != null ? sym.getSymbolName().replace("아케인심볼 : ", "") : "알 수 없음";
                int level = sym.getSymbolLevel() != null ? sym.getSymbolLevel() : 0;
                int growth = sym.getSymbolGrowthValue() != null ? sym.getSymbolGrowthValue() : 0; // 현재 레벨 구간 성장치 (강화 시 소모)

                if (sym.getSymbolOption() != null) {
                    Matcher m = p.matcher(sym.getSymbolOption());
                    if (m.find()) arcForce += Integer.parseInt(m.group(1));
                }

                arcSb.append(name).append(" 【 Lv.").append(level).append(" 】\n");

                if (level >= ARCANE_MAX_LEVEL) {
                    arcSb.append("· 남은 심볼: MAX\n\n");
                    continue;
                }

                long remainGrowth = calcRemainingGrowth(ARCANE_REQUIRED_GROWTH, level, growth);
                long remainMeso = calcRemainingMeso(resolveArcaneMesoTable(name), level);
                arcTotalMeso += remainMeso;

                arcSb.append("· 현재 성장치: ").append(df.format(growth)).append(" / ").append(df.format(ARCANE_REQUIRED_GROWTH[Math.max(1, level)])).append("개\n");
                arcSb.append("· 남은 심볼: ").append(df.format(remainGrowth)).append("개\n");
                arcSb.append("· 필요 메소: ").append(df.format(remainMeso)).append("메소\n\n");
            }
        }

        // 🌟 어센틱 심볼 파싱
        if (symbol.getAuthenticSymbol() != null) {
            for (NaesilDTO.Symbol.Data sym : symbol.getAuthenticSymbol()) {
                String name = sym.getSymbolName() != null ? sym.getSymbolName().replace("어센틱심볼 : ", "") : "알 수 없음";
                int level = sym.getSymbolLevel() != null ? sym.getSymbolLevel() : 0;
                int growth = sym.getSymbolGrowthValue() != null ? sym.getSymbolGrowthValue() : 0; // 현재 레벨 구간 성장치 (강화 시 소모)

                if (sym.getSymbolOption() != null) {
                    Matcher m = p.matcher(sym.getSymbolOption());
                    if (m.find()) autForce += Integer.parseInt(m.group(1));
                }

                autSb.append(name).append(" 【 Lv.").append(level).append(" 】\n");

                if (level >= AUTHENTIC_MAX_LEVEL) {
                    autSb.append("· 남은 심볼: MAX\n\n");
                    continue;
                }

                long remainGrowth = calcRemainingGrowth(AUTHENTIC_REQUIRED_GROWTH, level, growth);

                autSb.append("· 현재 성장치: ").append(df.format(growth)).append(" / ").append(df.format(AUTHENTIC_REQUIRED_GROWTH[Math.max(1, level)])).append("개\n");
                autSb.append("· 남은 심볼: ").append(df.format(remainGrowth)).append("개\n");

                // 지역별 비용표 확인된 경우만 메소 계산
                long[] mesoTable = resolveAuthenticMesoTable(name);
                if (mesoTable != null) {
                    long remainMeso = calcRemainingMeso(mesoTable, level);
                    autTotalMeso += remainMeso;
                    autSb.append("· 필요 메소: ").append(df.format(remainMeso)).append("메소\n\n");
                } else {
                    hasUnknownAutMeso = true;
                    autSb.append("· 필요 메소: 비용표 미확인\n\n");
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        String readMore = "​".repeat(500);

        sb.append("🍁 【 메이플스토리M 심볼 】\n");
        sb.append(basic.getCharacterName()).append(" (").append(basic.getWorldName() != null ? basic.getWorldName() : worldName).append(")\n").append(readMore).append("\n");

        sb.append("🍁 【 아케인포스 】 : ").append(arcForce).append("\n");
        sb.append("· 필요 메소 합계 : ").append(df.format(arcTotalMeso)).append(" 메소\n\n");

        sb.append("🍁 【 어센틱포스 】 : ").append(autForce).append("\n");
        sb.append("· 필요 메소 합계 : ").append(df.format(autTotalMeso)).append(" 메소");
        if (hasUnknownAutMeso) sb.append(" (비용표 미확인 지역 제외)");
        sb.append("\n\n");

        sb.append("🍁 【 아케인 심볼 (MAX 20) 】\n\n");
        sb.append(arcSb.length() > 0 ? arcSb.toString() : "장착 중인 아케인 심볼이 없습니다.\n\n");

        sb.append("🍁 【 어센틱 심볼 (MAX 11) 】\n\n");
        sb.append(autSb.length() > 0 ? autSb.toString() : "장착 중인 어센틱 심볼이 없습니다.\n");

        return sb.toString().trim();
    }

    // MAX까지 남은 심볼 개수 = 현재 레벨~MAX 요구 성장치 합 - 현재 성장치 (음수 방지)
    private static long calcRemainingGrowth(int[] requiredGrowth, int level, int growth) {
        long total = 0L;
        for (int lv = Math.max(1, level); lv < requiredGrowth.length; lv++) {
            total += requiredGrowth[lv];
        }
        return Math.max(0L, total - growth);
    }

    // MAX까지 필요 메소 = 현재 레벨~MAX 강화 메소 합
    private static long calcRemainingMeso(long[] mesoTable, int level) {
        long total = 0L;
        for (int lv = Math.max(1, level); lv < mesoTable.length; lv++) {
            total += mesoTable[lv];
        }
        return total;
    }

    // 아케인 지역별 메소표 선택 (소멸의 여로만 별도 비용)
    private static long[] resolveArcaneMesoTable(String regionName) {
        return regionName.contains("소멸의 여로") ? ARCANE_MESO_VANISHING_JOURNEY : ARCANE_MESO_OTHERS;
    }

    // 어센틱 지역별 메소표 선택 (비용표 미확인 지역은 null)
    private static long[] resolveAuthenticMesoTable(String regionName) {
        if (regionName.contains("에세테라")) return AUTHENTIC_MESO_ESETERA;
        if (regionName.contains("세르니움")) return AUTHENTIC_MESO_CERNIUM;
        if (regionName.contains("아르크스")) return AUTHENTIC_MESO_ARCUS;
        if (regionName.contains("아에르")) return AUTHENTIC_MESO_AER;
        if (regionName.contains("오디움")) return AUTHENTIC_MESO_ODIUM;
        if (regionName.contains("도원경")) return AUTHENTIC_MESO_DOWONGYEONG;
        return null;
    }
}
