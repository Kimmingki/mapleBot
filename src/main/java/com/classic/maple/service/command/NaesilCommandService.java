package com.classic.maple.service.command;

import com.classic.maple.dto.NaesilDTO;
import com.classic.maple.service.core.NexonApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaesilCommandService {

    private final NexonApiClient apiClient;

    public String getCharacterNaesil(String characterName, String worldName) {
        String ocid = apiClient.getCharacterOcid(characterName, worldName);
        if (ocid == null) return "캐릭터 정보를 찾을 수 없습니다.";

        StringBuilder sb = new StringBuilder();
        String readMore = "\u200B".repeat(500);

        NaesilDTO.Basic basic = apiClient.fetchApiData("/character/basic", ocid, NaesilDTO.Basic.class);
        if (basic == null) return "캐릭터 기본 정보를 불러올 수 없습니다.";

        sb.append("🍁 【 내실 요약 】\n");
        sb.append(basic.getCharacterName()).append(" (").append(basic.getWorldName() != null ? basic.getWorldName() : worldName).append(")\n").append(readMore).append("\n");

        NaesilDTO.Union union = apiClient.fetchApiData("/user/union", ocid, NaesilDTO.Union.class);
        if (union != null) {
            sb.append("🍁 【 유니온 】 : ").append(union.getUnionGrade() != null ? union.getUnionGrade() : "정보 없음")
                    .append(" / 【 Lv.").append(union.getUnionLevel() != null ? union.getUnionLevel() : 0).append(" 】\n\n");
        } else {
            sb.append("🍁 【 유니온 】 : 정보 없음\n\n");
        }

        NaesilDTO.Equip equip = apiClient.fetchApiData("/character/item-equipment", ocid, NaesilDTO.Equip.class);
        if (equip != null) {
            int arcCount = 0, abCount = 0, pitCount = 0, arcSf = 0, abSf = 0;
            double totPot = 0.0, totAdd = 0.0;
            List<String> pitched = List.of("몽환의 벨트", "거대한 공포", "루즈 컨트롤 머신 마크", "마력이 깃든 안대", "고통의 근원", "창세의 뱃지", "저주받은 마도서", "저주받은 적의 마도서");

            List<NaesilDTO.Equip.Item> bossItems = findBossPreset(equip);

            if (bossItems != null && !bossItems.isEmpty()) {
                for (NaesilDTO.Equip.Item item : bossItems) {
                    String name = item.getItemName() != null ? item.getItemName() : "";
                    int baseSf = 0;
                    if (item.getStarforce() != null && !item.getStarforce().isEmpty()) {
                        try { baseSf = Integer.parseInt(item.getStarforce()); } catch (Exception ignored) {}
                    }

                    int calcSf = baseSf;
                    if (item.getItemSlotName() != null && item.getItemSlotName().contains("한벌옷")) {
                        calcSf *= 2;
                    }

                    if (name.contains("아케인셰이드")) { arcCount++; arcSf += calcSf; }
                    else if (name.contains("앱솔랩스")) { abCount++; abSf += calcSf; }
                    else { for (String p : pitched) if (name.contains(p)) { pitCount++; break; } }

                    totPot += sumStatPercentagesDto(item.getPotential());
                    totAdd += sumStatPercentagesDto(item.getAdditional());
                }
                sb.append("🍁 【 장비 (보스) 】\n - 세트: ").append(arcCount).append("앜").append(abCount).append("앱").append(pitCount).append("칠\n");
                sb.append(" - 스타포스: 아케인 ").append(arcSf).append("✰ / 앱솔 ").append(abSf).append("✰\n");
                sb.append(String.format(" - 잠재능력: %.1f%%\n - 에디셔널: %.1f%%\n\n", totPot, totAdd));
            } else {
                sb.append("🍁 【 장비 (보스) 】\n - 장착 중인 장비가 없습니다.\n\n");
            }
        } else {
            sb.append("🍁 【 장비 (보스) 】\n - 정보 없음\n\n");
        }

        NaesilDTO.Symbol symbol = apiClient.fetchApiData("/character/symbol", ocid, NaesilDTO.Symbol.class);
        if (symbol != null) {
            int arcForce = 0, autForce = 0;
            Pattern p = Pattern.compile("포스 증가 ([0-9]+)");

            if (symbol.getArcaneSymbol() != null) {
                for (NaesilDTO.Symbol.Data sym : symbol.getArcaneSymbol()) {
                    if (sym.getSymbolOption() != null) {
                        Matcher m = p.matcher(sym.getSymbolOption());
                        if (m.find()) arcForce += Integer.parseInt(m.group(1));
                    }
                }
            }
            if (symbol.getAuthenticSymbol() != null) {
                for (NaesilDTO.Symbol.Data sym : symbol.getAuthenticSymbol()) {
                    if (sym.getSymbolOption() != null) {
                        Matcher m = p.matcher(sym.getSymbolOption());
                        if (m.find()) autForce += Integer.parseInt(m.group(1));
                    }
                }
            }
            sb.append("🍁 【 심볼 】\n - 아케인 포스: ").append(arcForce).append("\n - 어센틱 포스: ").append(autForce).append("\n\n");
        } else {
            sb.append("🍁 【 심볼 】\n - 정보 없음\n\n");
        }

        NaesilDTO.HexaSkill hexaSkill = apiClient.fetchApiData("/character/hexamatrix-skill", ocid, NaesilDTO.HexaSkill.class);
        NaesilDTO.HexaStat hexaStat = apiClient.fetchApiData("/character/hexamatrix-stat", ocid, NaesilDTO.HexaStat.class);
        sb.append("🍁 【 헥사 】\n");
        if (hexaSkill != null && hexaSkill.getHexaCores() != null) {
            List<String> origin = new ArrayList<>(), mastery = new ArrayList<>(), enhance = new ArrayList<>();
            for (NaesilDTO.HexaSkill.Core core : hexaSkill.getHexaCores()) {
                String cType = core.getCoreType() != null ? core.getCoreType() : "";
                String lText = "【 Lv." + (core.getCoreLevel() != null ? core.getCoreLevel() : 0) + " 】";
                if (cType.contains("스킬 코어")) origin.add(lText);
                else if (cType.contains("마스터리 코어")) mastery.add(lText);
                else if (cType.contains("강화 코어")) enhance.add(lText);
            }
            sb.append(" - 오리진: ").append(origin.isEmpty() ? "정보 없음" : String.join(", ", origin)).append("\n");
            sb.append(" - 마스터리: ").append(mastery.isEmpty() ? "정보 없음" : String.join(", ", mastery)).append("\n");
            sb.append(" - 강화 코어: ").append(enhance.isEmpty() ? "정보 없음" : String.join(", ", enhance)).append("\n");
        } else {
            sb.append(" - 코어 정보: 정보 없음\n");
        }

        boolean statFound = false;
        if (hexaStat != null && hexaStat.getHexamatrixStat() != null) {
            for (NaesilDTO.HexaStat.StatCore core : hexaStat.getHexamatrixStat()) {
                if (core.getStatInfo() != null) {
                    for (NaesilDTO.HexaStat.StatInfo info : core.getStatInfo()) {
                        if ("1".equals(info.getActivateFlag())) {
                            sb.append(" - 헥사 스탯: ").append(info.getMainStat()).append(" 【 Lv.").append(info.getMainStatLevel()).append(" 】 / ");
                            sb.append("서브: 【 Lv.").append(info.getSub1StatLevel()).append(" 】, 【 Lv.").append(info.getSub2StatLevel()).append(" 】\n\n");
                            statFound = true; break;
                        }
                    }
                }
                if(statFound) break;
            }
        }
        if (!statFound) sb.append(" - 헥사 스탯: 정보 없음\n\n");

        sb.append("🍁 【 장착 중인 업적 뱃지 】\n장착 중인 뱃지가 없습니다.\n\n");

        NaesilDTO.Link link = apiClient.fetchApiData("/character/link-skill", ocid, NaesilDTO.Link.class);
        sb.append("🍁 【 장착 중인 링크 스킬 】\n");
        boolean linkFound = false;
        if (link != null && link.getLinkSkill() != null && link.getActivePresetNo() != null) {
            int activePresetNo = link.getActivePresetNo();
            for (NaesilDTO.Link.Preset preset : link.getLinkSkill()) {
                if (preset.getPresetNo() != null && preset.getPresetNo() == activePresetNo) {
                    if (preset.getLinkSkillInfo() != null) {
                        Set<String> uniqueSkills = new LinkedHashSet<>();
                        for (NaesilDTO.Link.Info info : preset.getLinkSkillInfo()) {
                            if (info.getSkillName() != null && !info.getSkillName().isEmpty()) uniqueSkills.add(info.getSkillName());
                        }
                        for (String sName : uniqueSkills) sb.append("▪ ").append(sName).append("\n");
                        linkFound = !uniqueSkills.isEmpty();
                    }
                    break;
                }
            }
        }
        if (!linkFound) sb.append("장착 중인 링크 스킬이 없습니다.\n");
        sb.append("\n");

        // 🌟 8. 길드 개인 스킬 (완벽 수정본)
        NaesilDTO.Guild guild = apiClient.fetchApiData("/character/guild", ocid, NaesilDTO.Guild.class);
        String gName = (guild != null && guild.getGuildName() != null) ? guild.getGuildName() : null;

        sb.append("🍁 【 길드 개인 스킬 (").append(gName != null ? gName : "없음").append(") 】\n");
        if (gName != null && !gName.isEmpty()) {
            String oguildId = apiClient.getGuildId(gName, basic.getWorldName() != null ? basic.getWorldName() : worldName);

            if (oguildId != null) {
                NaesilDTO.GuildBasic guildBasic = apiClient.fetchGuildData("/guild/basic", oguildId, NaesilDTO.GuildBasic.class);

                boolean skillFound = false;
                if (guildBasic != null && guildBasic.getGuildMember() != null) {
                    // 🌟 길드원 명단 중에서 명령어를 호출한 자기 자신(characterName) 찾기
                    for (NaesilDTO.GuildBasic.GuildMember member : guildBasic.getGuildMember()) {
                        if (characterName.equals(member.getCharacterName())) {
                            if (member.getGuildPersonalSkill() != null && !member.getGuildPersonalSkill().isEmpty()) {
                                for (NaesilDTO.GuildBasic.PersonalSkill skill : member.getGuildPersonalSkill()) {
                                    String sName = skill.getSkillName() != null ? skill.getSkillName() : "알 수 없는 스킬";
                                    String sOption = skill.getSkillOption() != null ? skill.getSkillOption() : "설명 없음";

                                    // 포맷에 맞게 줄바꿈 처리하여 출력
                                    sb.append("▪ ").append(sName).append("\n");
                                    sb.append("  └ ").append(sOption.replace("\n", " / ")).append("\n\n");
                                }
                                skillFound = true;
                            }
                            break; // 내 캐릭터를 찾았으면 더 이상 순회할 필요 없음
                        }
                    }
                }

                if (!skillFound) {
                    sb.append("적용 중인 길드 개인 스킬이 없습니다.\n");
                }
            } else {
                sb.append("길드 정보를 불러올 수 없습니다.\n");
            }
        } else {
            sb.append("가입된 길드가 없습니다.\n");
        }

        return sb.toString().trim();
    }

    private List<NaesilDTO.Equip.Item> findBossPreset(NaesilDTO.Equip equip) {
        if (equip.getEquipmentPreset() == null || equip.getEquipmentPreset().isEmpty()) {
            return equip.getItemEquipment();
        }

        List<NaesilDTO.Equip.Item> bestPreset = equip.getItemEquipment();
        double maxScore = -1.0;

        for (NaesilDTO.Equip.Preset preset : equip.getEquipmentPreset()) {
            if (preset.getItemEquipment() == null) continue;

            double score = 0;
            for (NaesilDTO.Equip.Item item : preset.getItemEquipment()) {
                score += sumStatPercentagesDto(item.getPotential());
                score += sumStatPercentagesDto(item.getAdditional());
            }

            if (score > maxScore) {
                maxScore = score;
                bestPreset = preset.getItemEquipment();
            }
        }
        return bestPreset;
    }

    private double sumStatPercentagesDto(List<NaesilDTO.Equip.Option> options) {
        if (options == null) return 0.0;
        double sum = 0;
        Pattern pattern = Pattern.compile("([0-9.]+)(?=%|$)");
        for (NaesilDTO.Equip.Option opt : options) {
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