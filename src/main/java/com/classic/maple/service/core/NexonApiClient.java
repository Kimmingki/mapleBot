package com.classic.maple.service.core;

import com.classic.maple.dto.OcidDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class NexonApiClient {

    @Value("${nexon.api.key}")
    private String apiKey;

    @Value("${nexon.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getCharacterOcid(String characterName, String worldName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-nxopen-api-key", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/id")
                    .queryParam("character_name", characterName)
                    .queryParam("world_name", worldName)
                    .build().encode(StandardCharsets.UTF_8).toUri();

            OcidDTO response = restTemplate.exchange(uri, HttpMethod.GET, entity, OcidDTO.class).getBody();
            return response != null ? response.getOcid() : null;
        } catch (Exception e) {
            log.error("OCID 조회 실패 ({}): {}", characterName, e.getMessage());
            return null;
        }
    }

    public <T> T fetchApiData(String endpoint, String ocid, Class<T> responseType) {
        try {
            Thread.sleep(150); // 429 에러 방지
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-nxopen-api-key", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            URI uri = UriComponentsBuilder.fromUriString(baseUrl + endpoint)
                    .queryParam("ocid", ocid).build().encode(StandardCharsets.UTF_8).toUri();

            return restTemplate.exchange(uri, HttpMethod.GET, entity, responseType).getBody();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.warn("{} 호출 실패: {}", endpoint, e.getMessage());
            return null;
        }
    }

    // 🌟 길드 식별자(oguild_id) 조회 (맵으로 간단하게 파싱)
    public String getGuildId(String guildName, String worldName) {
        try {
            Thread.sleep(150);
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-nxopen-api-key", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/guild/id")
                    .queryParam("guild_name", guildName)
                    .queryParam("world_name", worldName)
                    .build().encode(StandardCharsets.UTF_8).toUri();

            Map response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class).getBody();
            if (response != null && response.containsKey("oguild_id")) {
                return String.valueOf(response.get("oguild_id"));
            }
            return null;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.warn("Guild ID 조회 실패: {}", e.getMessage());
            return null;
        }
    }

    // 🌟 길드 전용 데이터 호출 (oguild_id 사용)
    public <T> T fetchGuildData(String endpoint, String oguildId, Class<T> responseType) {
        try {
            Thread.sleep(150);
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-nxopen-api-key", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            URI uri = UriComponentsBuilder.fromUriString(baseUrl + endpoint)
                    .queryParam("oguild_id", oguildId).build().encode(StandardCharsets.UTF_8).toUri();

            return restTemplate.exchange(uri, HttpMethod.GET, entity, responseType).getBody();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.warn("{} 호출 실패: {}", endpoint, e.getMessage());
            return null;
        }
    }
}