package com.classic.maple.service.command;

import com.classic.maple.dto.NaesilDTO;
import com.classic.maple.service.core.NexonApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SymbolCommandService {

    private final NexonApiClient apiClient;

    public String getCharacterSymbol(String characterName, String worldName) {
        String ocid = apiClient.getCharacterOcid(characterName, worldName);
        if (ocid == null) return "캐릭터 정보를 찾을 수 없습니다.";

        NaesilDTO.Basic basic = apiClient.fetchApiData("/character/basic", ocid, NaesilDTO.Basic.class);
        NaesilDTO.Symbol symbol = apiClient.fetchApiData("/character/symbol", ocid, NaesilDTO.Symbol.class);

        if (basic == null || symbol == null) return "심볼 정보를 불러올 수 없습니다.";

        int arcForce = 0;
        int autForce = 0;
        Pattern p = Pattern.compile("포스 증가 ([0-9]+)");

        StringBuilder arcSb = new StringBuilder();
        StringBuilder autSb = new StringBuilder();

        // 🌟 아케인 심볼 파싱
        if (symbol.getArcaneSymbol() != null) {
            for (NaesilDTO.Symbol.Data sym : symbol.getArcaneSymbol()) {
                String name = sym.getSymbolName() != null ? sym.getSymbolName().replace("아케인심볼 : ", "") : "알 수 없음";
                int level = sym.getSymbolLevel() != null ? sym.getSymbolLevel() : 0;
                int growth = sym.getSymbolGrowthValue() != null ? sym.getSymbolGrowthValue() : 0; // 🌟 누적 심볼 개수

                if (sym.getSymbolOption() != null) {
                    Matcher m = p.matcher(sym.getSymbolOption());
                    if (m.find()) arcForce += Integer.parseInt(m.group(1));
                }

                arcSb.append(name).append(" 【 Lv.").append(level).append(" 】\n");
                arcSb.append("· 현재 누적 심볼: ").append(growth).append("개\n");
                // TODO: 레벨업 요구량 표가 있으면 "필요한 총 개수 - growth" 로 남은 개수를 구합니다!
                arcSb.append("· 남은 심볼: [계산식 필요]개\n");
                arcSb.append("· 필요 메소: [계산식 필요]메소\n\n");
            }
        }

        // 🌟 어센틱 심볼 파싱
        if (symbol.getAuthenticSymbol() != null) {
            for (NaesilDTO.Symbol.Data sym : symbol.getAuthenticSymbol()) {
                String name = sym.getSymbolName() != null ? sym.getSymbolName().replace("어센틱심볼 : ", "") : "알 수 없음";
                int level = sym.getSymbolLevel() != null ? sym.getSymbolLevel() : 0;
                int growth = sym.getSymbolGrowthValue() != null ? sym.getSymbolGrowthValue() : 0; // 🌟 누적 심볼 개수

                if (sym.getSymbolOption() != null) {
                    Matcher m = p.matcher(sym.getSymbolOption());
                    if (m.find()) autForce += Integer.parseInt(m.group(1));
                }

                autSb.append(name).append(" 【 Lv.").append(level).append(" 】\n");
                autSb.append("· 현재 누적 심볼: ").append(growth).append("개\n");
                // TODO: 레벨업 요구량 표가 있으면 "필요한 총 개수 - growth" 로 남은 개수를 구합니다!
                autSb.append("· 남은 심볼: [계산식 필요]개\n");
                autSb.append("· 필요 메소: [계산식 필요]메소\n\n");
            }
        }

        StringBuilder sb = new StringBuilder();
        String readMore = "\u200B".repeat(500);

        sb.append("🍁 【 메이플스토리M 심볼 】\n");
        sb.append(basic.getCharacterName()).append(" (").append(basic.getWorldName() != null ? basic.getWorldName() : worldName).append(")\n").append(readMore).append("\n");

        sb.append("🍁 【 아케인포스 】 : ").append(arcForce).append("\n");
        sb.append("· 필요 메소 합계 : [계산 불가] 메소\n\n");

        sb.append("🍁 【 어센틱포스 】 : ").append(autForce).append("\n");
        sb.append("· 필요 메소 합계 : [계산 불가] 메소\n\n");

        sb.append("🍁 【 아케인 심볼 (MAX 20) 】\n\n");
        sb.append(arcSb.length() > 0 ? arcSb.toString() : "장착 중인 아케인 심볼이 없습니다.\n\n");

        sb.append("🍁 【 어센틱 심볼 (MAX 11) 】\n\n");
        sb.append(autSb.length() > 0 ? autSb.toString() : "장착 중인 어센틱 심볼이 없습니다.\n");

        return sb.toString().trim();
    }
}