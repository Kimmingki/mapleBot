package com.classic.maple.service;

import com.classic.maple.dto.*;
import com.classic.maple.entity.ExpHistory;
import com.classic.maple.repository.ExpHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapleApiService {

    @Value("${nexon.api.key}")
    private String apiKey;

    @Value("${nexon.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ExpHistoryRepository expHistoryRepository;

    // 캐릭터의 고유 식별자(OCID)를 조회하는 메서드
    public String getCharacterOcid(String characterName, String worldName) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-nxopen-api-key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 🌟 수정된 부분: fromHttpUrl 대신 fromUriString을 사용합니다.
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/id")
                .queryParam("character_name", characterName)
                .queryParam("world_name", worldName)
                .build()
                .encode(StandardCharsets.UTF_8) // 한글 깨짐 방지
                .toUri();

        log.info("API 요청 URI: {}", uri);

        try {
            ResponseEntity<OcidResponseDTO> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    OcidResponseDTO.class
            );
            return response.getBody().getOcid();

        } catch (Exception e) {
            log.error("넥슨 API 호출 중 에러 발생: ", e);
            return "API 통신 실패: " + e.getMessage();
        }
    }

    public String getCharacterInfoDetail(String characterName, String worldName) {
        // 1. OCID 먼저 조회
        String ocid = getCharacterOcid(characterName, worldName);
        if (ocid == null) return "캐릭터 정보를 찾을 수 없습니다. (닉네임과 월드를 확인해 주세요)";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-nxopen-api-key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // 2. 기본 정보 조회
            URI basicUri = UriComponentsBuilder.fromUriString(baseUrl + "/character/basic")
                    .queryParam("ocid", ocid).build().toUri();
            CharacterBasicDTO basic = restTemplate.exchange(basicUri, HttpMethod.GET, entity, CharacterBasicDTO.class).getBody();

            // 3. 유니온 정보 조회 (API 엔드포인트는 넥슨 문서 기준 확인 필요, 예시로 작성)
            URI unionUri = UriComponentsBuilder.fromUriString(baseUrl + "/user/union")
                    .queryParam("ocid", ocid).build().toUri();
            CharacterUnionDTO union = restTemplate.exchange(unionUri, HttpMethod.GET, entity, CharacterUnionDTO.class).getBody();

            // 4. 펫 정보 조회
            URI petUri = UriComponentsBuilder.fromUriString(baseUrl + "/character/pet-equipment")
                    .queryParam("ocid", ocid).build().toUri();
            CharacterPetDTO pet = restTemplate.exchange(petUri, HttpMethod.GET, entity, CharacterPetDTO.class).getBody();

            // 5. 날짜 포맷팅 (2025-03-15T00:00+09:00 -> 2025-03-15)
            String createDate = basic.getCharacterDateCreate() != null
                    ? basic.getCharacterDateCreate().substring(0, 10) : "알 수 없음";

            // 🌟 추가: 성별 한글 변환 로직
            String rawGender = basic.getCharacterGender();
            String displayGender = (rawGender != null && rawGender.equalsIgnoreCase("Female")) ? "여" : "남";

            // 6. 데이터 조립
            StringBuilder sb = new StringBuilder();
            sb.append("🍁 【 메이플스토리M 정보 】\n\n");
            sb.append(basic.getCharacterName()).append("\n");

            // 🌟 수정: basic.getCharacterGender() 대신 위에서 변환한 displayGender 변수를 사용합니다.
            sb.append(basic.getWorldName()).append(" / 【 Lv.").append(basic.getCharacterLevel()).append(" 】 / ").append(basic.getCharacterJobName()).append(" (").append(displayGender).append(")\n");

            sb.append("길드 : ").append(basic.getCharacterGuildName() != null ? basic.getCharacterGuildName() : "없음").append("\n");
            sb.append("생성일 : ").append(createDate).append("\n\n");

            sb.append("🍁 【 유니온 】\n");
            sb.append(union.getUnionGrade() != null ? union.getUnionGrade() : "정보 없음").append(" / 【 Lv.").append(union.getUnionLevel() != null ? union.getUnionLevel() : 0).append(" 】\n\n");

            sb.append("🍁 【 장착 펫 】\n");
            String p1 = pet.getPet1Name() != null ? pet.getPet1Name() : "없음";
            String p2 = pet.getPet2Name() != null ? pet.getPet2Name() : "없음";
            String p3 = pet.getPet3Name() != null ? pet.getPet3Name() : "없음";
            sb.append(p1).append(" / ").append(p2).append(" / ").append(p3).append("\n");

            return sb.toString();
        } catch (Exception e) {
            log.error("정보 조회 중 에러 발생: ", e);
            return "상세 정보를 불러오는 중 서버 오류가 발생했습니다.";
        }
    }

    @Transactional // DB 저장이 포함되므로 트랜잭션 추가
    public String getCharacterExpHistory(String characterName, String worldName) {
        String ocid = getCharacterOcid(characterName, worldName);
        if (ocid == null) return "캐릭터 정보를 찾을 수 없습니다.";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-nxopen-api-key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        LocalDate today = LocalDate.now();

        // 1. 넥슨 API에서 [현재 최신 상태] 딱 1번만 조회!
        CharacterBasicDTO currentBasic = null;
        try {
            URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/character/basic")
                    .queryParam("ocid", ocid)
                    .build().encode(StandardCharsets.UTF_8).toUri();
            currentBasic = restTemplate.exchange(uri, HttpMethod.GET, entity, CharacterBasicDTO.class).getBody();
        } catch (Exception e) {
            log.error("API 조회 실패", e);
            return "현재 경험치 정보를 불러오는 중 에러가 발생했습니다.";
        }

        if (currentBasic == null || currentBasic.getCharacterExp() == null) {
            return "캐릭터 경험치 데이터가 존재하지 않습니다.";
        }

        // 2. DB에 오늘 기록이 없다면 새로 저장 (하루에 한 번만 갱신되도록)
        boolean isAlreadySaved = expHistoryRepository
                .findByCharacterNameAndWorldNameAndRecordDate(characterName, worldName, today)
                .isPresent();

        if (!isAlreadySaved) {
            ExpHistory newRecord = ExpHistory.builder()
                    .characterName(characterName)
                    .worldName(worldName)
                    .recordDate(today)
                    .level(currentBasic.getCharacterLevel())
                    .exp(currentBasic.getCharacterExp())
                    .build();
            expHistoryRepository.save(newRecord);
            log.info("{} 캐릭터의 {} 경험치 기록 DB 저장 완료", characterName, today);
        }

        // 3. DB에서 최근 6일 치 기록 불러오기 (최신순이므로 다시 오래된 순으로 뒤집기)
        List<ExpHistory> historyList = expHistoryRepository
                .findTop6ByCharacterNameAndWorldNameOrderByRecordDateDesc(characterName, worldName);
        Collections.reverse(historyList); // 보기 좋게 과거 -> 현재 순으로 정렬

        // 4. 문자열 조립 시작
        DateTimeFormatter displayDtf = DateTimeFormatter.ofPattern("yy.MM.dd");
        ExpHistory current = historyList.get(historyList.size() - 1);
        int dataCount = historyList.size();

        StringBuilder sb = new StringBuilder();
        sb.append("🍁 ").append(characterName).append(" 님 🍁\n");
        sb.append(String.format("LV. %d\n", current.getLevel()));
        sb.append(String.format("누적 EXP: %,d\n\n", current.getExp()));

        // 데이터가 1개뿐일 때의 안내 문구 (요청하신 디테일 반영)
        if (dataCount == 1) {
            sb.append("📜 히스토리 수집 시작!\n");
            sb.append("• ").append(current.getRecordDate().format(displayDtf))
                    .append(String.format(" | Lv.%d (%,d EXP)\n\n", current.getLevel(), current.getExp()));

            sb.append("🔮 성과 분석\n");
            sb.append("⚠️ 아직 데이터가 더 필요합니다.\n");
            sb.append("(성장량을 계산하려면 최소 내일까지 1번 더 조회해야 합니다.)");

            return sb.toString();
        }

        // 데이터가 2개 이상일 때 정상 계산 로직
        sb.append("📜 최근 ").append(dataCount).append("일 히스토리\n");
        for (ExpHistory data : historyList) {
            sb.append("• ").append(data.getRecordDate().format(displayDtf))
                    .append(String.format(" | Lv.%d (%,d EXP)\n", data.getLevel(), data.getExp()));
        }

        int avgDays = dataCount - 1;
        sb.append("\n🔮 성과 분석 (최근 ").append(avgDays).append("일 평균)\n");

        ExpHistory oldest = historyList.get(0);
        long gainedExp = current.getExp() - oldest.getExp();
        long dailyAvg = gainedExp / avgDays;

        if (dailyAvg > 0) {
            sb.append(String.format("📊 일평균 획득량: +%,d EXP\n", dailyAvg));
        } else {
            sb.append("📊 일평균 획득량: 변화 없음\n");
        }

        return sb.toString();
    }

    public String getBossEquipmentSetting(String characterName, String worldName) {
        String ocid = getCharacterOcid(characterName, worldName);
        if (ocid == null) return "캐릭터 정보를 찾을 수 없습니다.";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-nxopen-api-key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // 1. API 호출
            CharacterBasicDTO basic = restTemplate.exchange(
                    UriComponentsBuilder.fromUriString(baseUrl + "/character/basic").queryParam("ocid", ocid).build().encode(StandardCharsets.UTF_8).toUri(),
                    HttpMethod.GET, entity, CharacterBasicDTO.class).getBody();

            ItemEquipmentDTO equip = restTemplate.exchange(
                    UriComponentsBuilder.fromUriString(baseUrl + "/character/item-equipment").queryParam("ocid", ocid).build().encode(StandardCharsets.UTF_8).toUri(),
                    HttpMethod.GET, entity, ItemEquipmentDTO.class).getBody();

            // 쥬얼 데이터 호출 (만약 여기서 null이 나온다면 쥬얼 DTO의 이름표 확인이 필요합니다)
            JewelEquipmentDTO jewel = null;
            try {
                jewel = restTemplate.exchange(
                        UriComponentsBuilder.fromUriString(baseUrl + "/character/jewel").queryParam("ocid", ocid).build().encode(StandardCharsets.UTF_8).toUri(),
                        HttpMethod.GET, entity, JewelEquipmentDTO.class).getBody();
            } catch (Exception e) {
                log.warn("쥬얼 데이터를 불러오지 못했습니다.");
            }

            if (equip == null || equip.getItemEquipment() == null) return "장비 데이터를 불러올 수 없습니다.";

            // 2. 통계 변수들 초기화
            int arcaneCount = 0, absolCount = 0, pitchedCount = 0;
            int arcaneStarforce = 0, absolStarforce = 0;
            double totalPotential = 0.0, totalAdditional = 0.0;
            Map<String, Integer> soulMap = new HashMap<>();
            Map<String, Integer> jewelMap = new HashMap<>();

            // 칠흑 아이템 키워드 리스트 (마도서 2종 모두 추가)
            List<String> pitchedItems = List.of("몽환의 벨트", "거대한 공포", "루즈 컨트롤 머신 마크", "마력이 깃든 안대", "고통의 근원", "창세의 뱃지", "저주받은 마도서", "저주받은 적의 마도서");

            StringBuilder detailSb = new StringBuilder();
            detailSb.append("\n⭒ 착용 장비 상세 (보스 세팅)\n\n");

            // 3. 장비 리스트 순회
            for (ItemEquipmentDTO.ItemData item : equip.getItemEquipment()) {
                String name = item.getItemName() != null ? item.getItemName() : "알 수 없는 장비";

                // 세트 카운팅
                if (name.contains("아케인셰이드")) { arcaneCount++; }
                else if (name.contains("앱솔랩스")) { absolCount++; }
                else {
                    for (String pitched : pitchedItems) {
                        if (name.contains(pitched)) { pitchedCount++; break; }
                    }
                }

                // 🌟 핵심 수정 1: 스타포스가 "15" 같은 문자열로 오므로 숫자로 변환
                int sf = 0;
                if (item.getStarforce() != null && !item.getStarforce().isEmpty()) {
                    try { sf = Integer.parseInt(item.getStarforce()); } catch (NumberFormatException ignored) {}
                }
                if (name.contains("아케인셰이드")) { arcaneStarforce += sf; }
                else if (name.contains("앱솔랩스")) { absolStarforce += sf; }

                // 🌟 핵심 수정 2: 소울 정보 추출 로직 변경
                if (item.getSoulInfo() != null && item.getSoulInfo().getSoulName() != null) {
                    String sName = item.getSoulInfo().getSoulName();
                    soulMap.put(sName, soulMap.getOrDefault(sName, 0) + 1);
                }

                // 🌟 핵심 수정 3: 한글 등급(레어, 유니크 등)을 이니셜 마크로 변환
                String gradeStr = item.getItemGrade() != null ? item.getItemGrade() : "";
                String gradeInitial = "N";
                String gradeIcon = "Ⓝ";
                switch (gradeStr) {
                    case "레어": gradeInitial = "R"; gradeIcon = "Ⓡ"; break;
                    case "에픽": gradeInitial = "E"; gradeIcon = "Ⓔ"; break;
                    case "유니크": gradeInitial = "U"; gradeIcon = "Ⓤ"; break;
                    case "레전더리": gradeInitial = "L"; gradeIcon = "Ⓛ"; break;
                    case "미스틱": gradeInitial = "M"; gradeIcon = "Ⓜ"; break;
                    case "에인션트": gradeInitial = "A"; gradeIcon = "Ⓐ"; break;
                    case "카오스": gradeInitial = "C"; gradeIcon = "Ⓒ"; break;
                }

                // 상세 출력: 아이템 헤더
                String sfText = sf > 0 ? " (" + sf + "✰)" : "";
                detailSb.append(String.format("『 %s 』 %s%s\n", gradeInitial, name, sfText));
                if (item.getCuttableCount() != null) {
                    detailSb.append(" ⭒ 남은 가위 사용 횟수 : ").append(item.getCuttableCount()).append("회\n\n");
                } else {
                    detailSb.append("\n"); // 가위 횟수가 없는 장비(칭호 등) 여백 처리
                }

                // 옵션 출력 (수정된 이름표 배열 적용)
                appendOptions(detailSb, "기본", item.getBaseOption());
                appendOptions(detailSb, "추가옵션", item.getAddOption());

                // 잠재능력 통계 합산 및 출력
                totalPotential += sumStatPercentages(item.getPotentialOption());
                appendOptions(detailSb, gradeIcon + " 잠재능력", item.getPotentialOption());

                totalAdditional += sumStatPercentages(item.getAdditionalOption());
                appendOptions(detailSb, "Ⓡ 에디셔널", item.getAdditionalOption());

                // 소울 상세 옵션 출력
                if (item.getSoulInfo() != null && item.getSoulInfo().getSoulOption() != null) {
                    detailSb.append(" ⭒ 소울 : ").append(item.getSoulInfo().getSoulOption()).append("\n");
                }
                detailSb.append("\n");
            }

            // 쥬얼 통계
            if (jewel != null && jewel.getJewelEquipment() != null && jewel.getUseJewelPageNo() != null) {
                int activePageNo = jewel.getUseJewelPageNo(); // 현재 활성화된 페이지 번호

                for (JewelEquipmentDTO.JewelPage page : jewel.getJewelEquipment()) {
                    // 현재 활성화된 페이지를 찾았다면
                    if (page.getJewelPageNo() != null && page.getJewelPageNo() == activePageNo) {
                        if (page.getJewelInfo() != null) {
                            for (JewelEquipmentDTO.JewelData j : page.getJewelInfo()) {
                                String jName = j.getJewelName() != null ? j.getJewelName() : "알 수 없는 쥬얼";
                                jewelMap.put(jName, jewelMap.getOrDefault(jName, 0) + 1);
                            }
                        }
                        break; // 장착 중인 페이지를 확인했으니 더 이상 다른 페이지는 볼 필요 없음
                    }
                }
            }

            // 4. 최종 결과 문자열 조립
            StringBuilder sb = new StringBuilder();
            sb.append("🍁 【 메이플스토리M 보스 세팅 】\n");

            String rawGender = basic.getCharacterGender();
            String displayGender = (rawGender != null && rawGender.equalsIgnoreCase("Female")) ? "여" : (rawGender != null ? "남" : "");
            String genderText = displayGender.isEmpty() ? "" : " (" + displayGender + ")";

            sb.append(basic.getCharacterName()).append(" (").append(basic.getWorldName()).append(") / ").append(basic.getCharacterJobName()).append(genderText).append("\n\n");

            sb.append("🍁 【 세트 】 : ").append(arcaneCount).append("앜").append(absolCount).append("앱").append(pitchedCount).append("칠\n\n");

            sb.append("🍁 【 앱솔랩스 스타포스 】 : ").append(absolStarforce).append("✰\n");
            sb.append("🍁 【 아케인셰이드 스타포스 】 : ").append(arcaneStarforce).append("✰\n");
            sb.append(String.format("🍁 【 잠재능력 】 : %.1f%%\n", totalPotential));
            sb.append(String.format("🍁 【 에디셔널 】 : %.1f%%\n\n", totalAdditional));

            sb.append("🍁 【 장착 소울 】 : ");
            if (soulMap.isEmpty()) sb.append("없음\n");
            else {
                List<String> souls = soulMap.entrySet().stream().map(e -> e.getKey() + "(" + e.getValue() + ")").toList();
                sb.append(String.join(" / ", souls)).append("\n");
            }

            sb.append("🍁 【 장착 쥬얼 】 : ");
            if (jewelMap.isEmpty()) sb.append("없음\n");
            else {
                List<String> jewels = jewelMap.entrySet().stream().map(e -> e.getValue() + " " + e.getKey()).toList();
                sb.append(String.join(" / ", jewels)).append("\n");
            }

            // 상세 장비 내역 합치기
            sb.append(detailSb.toString());

            return sb.toString().trim();

        } catch (Exception e) {
            log.error("보스 세팅 조회 중 에러 발생: ", e);
            return "보스 세팅 정보를 불러오는 중 서버 오류가 발생했습니다.";
        }
    }



    // =============================== 헬퍼 =============================== //
    private void appendOptions(StringBuilder sb, String prefix, List<ItemEquipmentDTO.ItemOption> options) {
        if (options == null || options.isEmpty()) return;
        for (ItemEquipmentDTO.ItemOption opt : options) {
            sb.append(" ⭒ ").append(prefix).append(" : ").append(opt.getOptionName()).append(" ").append(opt.getOptionValue()).append("\n");
        }
        sb.append("\n");
    }


    // 잠재능력/에디셔널에서 주요 스탯(대미지, 보공 등)의 퍼센트 숫자만 추출해서 더하는 로직
    private double sumStatPercentages(List<ItemEquipmentDTO.ItemOption> options) {
        if (options == null) return 0.0;
        double sum = 0;
        Pattern pattern = Pattern.compile("([0-9.]+)(?=%|$)"); // 숫자 추출 정규식

        for (ItemEquipmentDTO.ItemOption opt : options) {
            String name = opt.getOptionName();
            // 합산할 스탯 필터링 (원하는 스탯명만 추가하세요)
            if (name.contains("물리 대미지") || name.contains("마법 대미지") || name.contains("보스 공격력") || name.contains("최종 대미지")) {
                Matcher matcher = pattern.matcher(opt.getOptionValue());
                if (matcher.find()) {
                    sum += Double.parseDouble(matcher.group(1));
                }
            }
        }
        return sum;
    }
}
