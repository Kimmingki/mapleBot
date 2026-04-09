package com.classic.maple.service.command.equipment;

import com.classic.maple.dto.CharacterBasicDTO;
import com.classic.maple.dto.ItemEquipmentDTO;
import com.classic.maple.service.core.NexonApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BossCommandService {

    private final NexonApiClient apiClient;

    public String getBossEquipmentSetting(String characterName, String worldName) {
        String ocid = apiClient.getCharacterOcid(characterName, worldName);
        if (ocid == null) return "캐릭터 정보를 찾을 수 없습니다.";

        CharacterBasicDTO basic = apiClient.fetchApiData("/character/basic", ocid, CharacterBasicDTO.class);
        ItemEquipmentDTO equip = apiClient.fetchApiData("/character/item-equipment", ocid, ItemEquipmentDTO.class);

        if (basic == null || equip == null) return "장비 데이터를 불러올 수 없습니다.";

        List<ItemEquipmentDTO.ItemData> bossItems = findBossPreset(equip);
        if (bossItems == null || bossItems.isEmpty()) return "장착 중인 장비가 없습니다.";

        int arcaneCount = 0, absolCount = 0, pitchedCount = 0;
        int arcaneStarforce = 0, absolStarforce = 0;
        double totalPotential = 0.0, totalAdditional = 0.0;
        Map<String, Integer> soulMap = new HashMap<>();
        List<String> pitchedItems = List.of("몽환의 벨트", "거대한 공포", "루즈 컨트롤 머신 마크", "마력이 깃든 안대", "고통의 근원", "창세의 뱃지", "저주받은 마도서", "저주받은 적의 마도서");

        StringBuilder detailSb = new StringBuilder();
        detailSb.append("\n⭒ 착용 장비 상세 (보스 세팅)\n\n");

        for (ItemEquipmentDTO.ItemData item : bossItems) {
            String name = item.getItemName() != null ? item.getItemName() : "알 수 없는 장비";

            if (name.contains("아케인셰이드")) arcaneCount++;
            else if (name.contains("앱솔랩스")) absolCount++;
            else {
                for (String pitched : pitchedItems) {
                    if (name.contains(pitched)) { pitchedCount++; break; }
                }
            }

            // 🌟 기본 스타포스 파싱
            int baseSf = 0;
            if (item.getStarforce() != null && !item.getStarforce().isEmpty()) {
                try { baseSf = Integer.parseInt(item.getStarforce()); } catch (Exception ignored) {}
            }

            // 🌟 합산용 스타포스 (한벌옷이면 2배 적용)
            int calcSf = baseSf;
            if (item.getItemSlotName() != null && item.getItemSlotName().contains("한벌옷")) {
                calcSf *= 2;
            }

            if (name.contains("아케인셰이드")) arcaneStarforce += calcSf;
            else if (name.contains("앱솔랩스")) absolStarforce += calcSf;

            if (item.getSoulInfo() != null && item.getSoulInfo().getSoulName() != null) {
                String sName = item.getSoulInfo().getSoulName();
                soulMap.put(sName, soulMap.getOrDefault(sName, 0) + 1);
            }

            String gradeStr = item.getItemGrade() != null ? item.getItemGrade() : "";
            String gradeInitial = "N", gradeIcon = "Ⓝ";
            switch (gradeStr) {
                case "레어": gradeInitial = "R"; gradeIcon = "Ⓡ"; break;
                case "에픽": gradeInitial = "E"; gradeIcon = "Ⓔ"; break;
                case "유니크": gradeInitial = "U"; gradeIcon = "Ⓤ"; break;
                case "레전더리": gradeInitial = "L"; gradeIcon = "Ⓛ"; break;
                case "미스틱": gradeInitial = "M"; gradeIcon = "Ⓜ"; break;
                case "에인션트": gradeInitial = "A"; gradeIcon = "Ⓐ"; break;
                case "카오스": gradeInitial = "C"; gradeIcon = "Ⓒ"; break;
            }

            // 개별 아이템 표기에는 원본 스타포스(baseSf)를 사용
            String sfText = baseSf > 0 ? " (" + baseSf + "✰)" : "";
            detailSb.append(String.format("『 %s 』 %s%s\n", gradeInitial, name, sfText));
            if (item.getCuttableCount() != null) detailSb.append(" ⭒ 남은 가위 사용 횟수 : ").append(item.getCuttableCount()).append("회\n\n");
            else detailSb.append("\n");

            appendOptions(detailSb, "기본", item.getBaseOption());
            appendOptions(detailSb, "추가옵션", item.getAddOption());

            totalPotential += sumStatPercentages(item.getPotentialOption());
            appendOptions(detailSb, gradeIcon + " 잠재능력", item.getPotentialOption());

            totalAdditional += sumStatPercentages(item.getAdditionalOption());
            appendOptions(detailSb, "Ⓡ 에디셔널", item.getAdditionalOption());

            if (item.getSoulInfo() != null && item.getSoulInfo().getSoulOption() != null) {
                detailSb.append(" ⭒ 소울 : ").append(item.getSoulInfo().getSoulOption()).append("\n");
            }
            detailSb.append("\n");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🍁 【 메이플스토리M 보스 세팅 】\n");
        String gender = basic.getCharacterGender() != null && basic.getCharacterGender().equalsIgnoreCase("Female") ? "여" : (basic.getCharacterGender() != null ? "남" : "");
        String genderText = gender.isEmpty() ? "" : " (" + gender + ")";
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
        sb.append(detailSb.toString());

        return sb.toString().trim();
    }

    private List<ItemEquipmentDTO.ItemData> findBossPreset(ItemEquipmentDTO equip) {
        if (equip.getEquipmentPreset() == null || equip.getEquipmentPreset().isEmpty()) {
            return equip.getItemEquipment();
        }

        List<ItemEquipmentDTO.ItemData> bestPreset = equip.getItemEquipment();
        double maxScore = -1.0;

        for (ItemEquipmentDTO.Preset preset : equip.getEquipmentPreset()) {
            if (preset.getItemEquipment() == null) continue;

            double score = 0;
            for (ItemEquipmentDTO.ItemData item : preset.getItemEquipment()) {
                score += sumStatPercentages(item.getPotentialOption());
                score += sumStatPercentages(item.getAdditionalOption());
            }

            if (score > maxScore) {
                maxScore = score;
                bestPreset = preset.getItemEquipment();
            }
        }
        return bestPreset;
    }

    private void appendOptions(StringBuilder sb, String prefix, List<ItemEquipmentDTO.ItemOption> options) {
        if (options == null || options.isEmpty()) return;
        boolean hasOption = false;
        for (ItemEquipmentDTO.ItemOption opt : options) {
            if (opt.getOptionName() != null && opt.getOptionValue() != null) {
                sb.append(" ⭒ ").append(prefix).append(" : ").append(opt.getOptionName()).append(" ").append(opt.getOptionValue()).append("\n");
                hasOption = true;
            }
        }
        if (hasOption) sb.append("\n");
    }

    private double sumStatPercentages(List<ItemEquipmentDTO.ItemOption> options) {
        if (options == null) return 0.0;
        double sum = 0;
        Pattern pattern = Pattern.compile("([0-9.]+)(?=%|$)");
        for (ItemEquipmentDTO.ItemOption opt : options) {
            String name = opt.getOptionName();
            if (name == null || opt.getOptionValue() == null) continue;
            if (name.contains("물리 대미지") || name.contains("마법 대미지") || name.contains("보스 공격력") || name.contains("최종 대미지")) {
                Matcher matcher = pattern.matcher(opt.getOptionValue());
                if (matcher.find()) {
                    try { sum += Double.parseDouble(matcher.group(1)); } catch (Exception ignored) {}
                }
            }
        }
        return sum;
    }
}