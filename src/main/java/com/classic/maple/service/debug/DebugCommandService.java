package com.classic.maple.service.debug;

import com.classic.maple.service.core.NexonApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebugCommandService {

    private final NexonApiClient apiClient;

    private String getOcidSafe(String characterName, String worldName) {
        String ocid = apiClient.getCharacterOcid(characterName, worldName);
        if (ocid == null) {
            throw new IllegalArgumentException("OCID를 조회할 수 없습니다. 닉네임과 월드를 확인해주세요.");
        }
        return ocid;
    }

    private void appendRawData(StringBuilder sb, String title, String endpoint, String idParamKey, String idParamValue) {
        sb.append("💎 [").append(title).append("] 원본 JSON:\n");
        try {
            // NexonApiClient를 직접 수정하지 않고 간단히 호출하기 위해,
            // 원본 URL 구조를 고려하여 하드코딩된 조회 로직을 사용할 수도 있지만
            // 기존 fetchApiData 구조를 최대한 활용합니다.
            String rawJson;
            if ("oguild_id".equals(idParamKey)) {
                rawJson = apiClient.fetchGuildData(endpoint, idParamValue, String.class);
            } else {
                rawJson = apiClient.fetchApiData(endpoint, idParamValue, String.class);
            }
            sb.append(rawJson != null ? rawJson : "데이터 없음 (null)").append("\n\n");
        } catch (Exception e) {
            sb.append("호출 실패: ").append(e.getMessage()).append("\n\n");
        }
    }

    public String debugInfo(String characterName, String worldName) {
        StringBuilder sb = new StringBuilder("🍁 [.정보] 디버그 결과\n\n");
        try {
            String ocid = getOcidSafe(characterName, worldName);
            appendRawData(sb, "기본 정보", "/character/basic", "ocid", ocid);
            appendRawData(sb, "길드", "/character/guild", "ocid", ocid);
            appendRawData(sb, "유니온", "/user/union", "ocid", ocid);
            appendRawData(sb, "펫 장비", "/character/pet-equipment", "ocid", ocid);
            appendRawData(sb, "세트 효과", "/character/set-effect", "ocid", ocid);
        } catch (Exception e) {
            sb.append(e.getMessage());
        }
        return sb.toString();
    }

    public String debugBossAndNaesil(String characterName, String worldName) {
        StringBuilder sb = new StringBuilder("🍁 [.보스 / .내실] 디버그 결과\n\n");
        try {
            String ocid = getOcidSafe(characterName, worldName);
            appendRawData(sb, "기본 정보", "/character/basic", "ocid", ocid);
            appendRawData(sb, "장비 및 프리셋", "/character/item-equipment", "ocid", ocid);
            appendRawData(sb, "심볼", "/character/symbol", "ocid", ocid);
            appendRawData(sb, "헥사 스킬", "/character/hexamatrix-skill", "ocid", ocid);
            appendRawData(sb, "헥사 스탯", "/character/hexamatrix-stat", "ocid", ocid);
            appendRawData(sb, "링크 스킬", "/character/link-skill", "ocid", ocid);

            // 길드 스킬 조회 로직 포함
            String guildRaw = apiClient.fetchApiData("/character/guild", ocid, String.class);
            sb.append("💎 [길드명 조회] 원본 JSON:\n").append(guildRaw).append("\n\n");

        } catch (Exception e) {
            sb.append(e.getMessage());
        }
        return sb.toString();
    }

    public String debugVMatrix(String characterName, String worldName) {
        StringBuilder sb = new StringBuilder("🍁 [.코강] 디버그 결과\n\n");
        try {
            String ocid = getOcidSafe(characterName, worldName);
            appendRawData(sb, "V매트릭스", "/character/vmatrix", "ocid", ocid);
            appendRawData(sb, "장착 스킬(프리셋)", "/character/skill-equipment", "ocid", ocid);
        } catch (Exception e) {
            sb.append(e.getMessage());
        }
        return sb.toString();
    }

    public String debugCodi(String characterName, String worldName) {
        StringBuilder sb = new StringBuilder("🍁 [.코디] 디버그 결과\n\n");
        try {
            String ocid = getOcidSafe(characterName, worldName);
            appendRawData(sb, "기본 정보(이미지)", "/character/basic", "ocid", ocid);
            appendRawData(sb, "뷰티 정보", "/character/beauty-equipment", "ocid", ocid);
            appendRawData(sb, "캐시 장비", "/character/cashitem-equipment", "ocid", ocid);
        } catch (Exception e) {
            sb.append(e.getMessage());
        }
        return sb.toString();
    }

    public String debugGuildSkill(String characterName, String worldName) {
        StringBuilder sb = new StringBuilder("🍁 [.길스] 디버그 결과\n\n");
        try {
            String ocid = getOcidSafe(characterName, worldName);
            appendRawData(sb, "기본 정보", "/character/basic", "ocid", ocid);

            // 1. 길드명 조회 원본 데이터
            String guildRaw = apiClient.fetchApiData("/character/guild", ocid, String.class);
            sb.append("💎 [길드명 조회] 원본 JSON:\n").append(guildRaw != null ? guildRaw : "데이터 없음").append("\n\n");

            // 2. 길드 상세 정보 조회를 위해 기존 DTO를 잠시 빌려 길드명 추출
            com.classic.maple.dto.NaesilDTO.Guild guildDto = apiClient.fetchApiData("/character/guild", ocid, com.classic.maple.dto.NaesilDTO.Guild.class);

            if (guildDto != null && guildDto.getGuildName() != null) {
                String guildName = guildDto.getGuildName();
                String oguildId = apiClient.getGuildId(guildName, worldName);

                if (oguildId != null) {
                    // 3. 최종 목적인 길드 베이직(스킬 및 기여도 포함) 원본 데이터 추출
                    appendRawData(sb, "길드 상세 정보 (기여도 및 스킬)", "/guild/basic", "oguild_id", oguildId);
                } else {
                    sb.append("길드 ID(oguild_id)를 발급받을 수 없습니다.\n\n");
                }
            } else {
                sb.append("가입된 길드가 없거나 길드명을 조회할 수 없습니다.\n\n");
            }

        } catch (Exception e) {
            sb.append("에러 발생: ").append(e.getMessage());
        }
        return sb.toString();
    }

    public String debugHexa(String characterName, String worldName) {
        StringBuilder sb = new StringBuilder("🍁 [.헥사] 디버그 결과\n\n");
        try {
            String ocid = getOcidSafe(characterName, worldName);
            appendRawData(sb, "기본 정보", "/character/basic", "ocid", ocid);

            // 1. 헥사 스킬 코어 원본 데이터 덤프
            appendRawData(sb, "헥사 스킬 코어", "/character/hexamatrix-skill", "ocid", ocid);

            // 2. 헥사 스탯 원본 데이터 덤프
            appendRawData(sb, "헥사 스탯", "/character/hexamatrix-stat", "ocid", ocid);

        } catch (Exception e) {
            sb.append("에러 발생: ").append(e.getMessage());
        }
        return sb.toString();
    }
}
