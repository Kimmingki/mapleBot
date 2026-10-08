package com.classic.maple.service.core;

import com.classic.maple.dto.OcidDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class NexonApiClient {

    @Value("${nexon.api.key}")
    private String apiKey;

    @Value("${nexon.api.base-url}")
    private String baseUrl;

    // 초당 허용 호출 수 (API 키 등급 기준)
    @Value("${nexon.api.rate-per-second:5}")
    private int ratePerSecond;

    // 식별자 캐시 유지 시간 (ocid, oguild_id)
    private static final long ID_CACHE_TTL_MILLIS = Duration.ofHours(6).toMillis();

    private final RestTemplate restTemplate = createRestTemplate();

    // 전역 호출 슬롯 (다음 호출 가능 시각, 나노초)
    private final AtomicLong nextSlotNanos = new AtomicLong(System.nanoTime());

    // 월드:캐릭터명 → ocid
    private final Map<String, CachedId> ocidCache = new ConcurrentHashMap<>();

    // 월드:길드명 → oguild_id
    private final Map<String, CachedId> guildIdCache = new ConcurrentHashMap<>();

    // 연결/응답 타임아웃 적용 RestTemplate
    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }

    public String getCharacterOcid(String characterName, String worldName) {
        // 캐시 우선 조회
        String cacheKey = worldName + ":" + characterName;
        CachedId cached = ocidCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) return cached.value();

        try {
            acquireSlot();
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-nxopen-api-key", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/id")
                    .queryParam("character_name", characterName)
                    .queryParam("world_name", worldName)
                    .build().encode(StandardCharsets.UTF_8).toUri();

            OcidDTO response = restTemplate.exchange(uri, HttpMethod.GET, entity, OcidDTO.class).getBody();
            String ocid = response != null ? response.getOcid() : null;
            if (ocid != null) ocidCache.put(cacheKey, CachedId.of(ocid));
            return ocid;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null;
        } catch (RestClientException e) {
            log.error("OCID 조회 실패 ({}): {}", characterName, e.getMessage());
            return null;
        }
    }

    public <T> T fetchApiData(String endpoint, String ocid, Class<T> responseType) {
        try {
            acquireSlot();
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-nxopen-api-key", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            URI uri = UriComponentsBuilder.fromUriString(baseUrl + endpoint)
                    .queryParam("ocid", ocid).build().encode(StandardCharsets.UTF_8).toUri();

            return restTemplate.exchange(uri, HttpMethod.GET, entity, responseType).getBody();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null;
        } catch (RestClientException e) {
            log.warn("{} 호출 실패: {}", endpoint, e.getMessage());
            return null;
        }
    }

    // 🌟 길드 식별자(oguild_id) 조회 (맵으로 간단하게 파싱)
    public String getGuildId(String guildName, String worldName) {
        // 캐시 우선 조회
        String cacheKey = worldName + ":" + guildName;
        CachedId cached = guildIdCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) return cached.value();

        try {
            acquireSlot();
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-nxopen-api-key", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/guild/id")
                    .queryParam("guild_name", guildName)
                    .queryParam("world_name", worldName)
                    .build().encode(StandardCharsets.UTF_8).toUri();

            Map response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class).getBody();
            if (response != null && response.containsKey("oguild_id")) {
                String oguildId = String.valueOf(response.get("oguild_id"));
                guildIdCache.put(cacheKey, CachedId.of(oguildId));
                return oguildId;
            }
            return null;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null;
        } catch (RestClientException e) {
            log.warn("Guild ID 조회 실패: {}", e.getMessage());
            return null;
        }
    }

    // 🌟 길드 전용 데이터 호출 (oguild_id 사용)
    public <T> T fetchGuildData(String endpoint, String oguildId, Class<T> responseType) {
        try {
            acquireSlot();
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-nxopen-api-key", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            URI uri = UriComponentsBuilder.fromUriString(baseUrl + endpoint)
                    .queryParam("oguild_id", oguildId).build().encode(StandardCharsets.UTF_8).toUri();

            return restTemplate.exchange(uri, HttpMethod.GET, entity, responseType).getBody();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null;
        } catch (RestClientException e) {
            log.warn("{} 호출 실패: {}", endpoint, e.getMessage());
            return null;
        }
    }

    // 전역 호출 간격 확보 (동시 요청 포함 초당 한도 유지, 429 방지)
    private void acquireSlot() throws InterruptedException {
        long intervalNanos = TimeUnit.SECONDS.toNanos(1) / ratePerSecond;
        long now = System.nanoTime();
        long reserved = nextSlotNanos.getAndUpdate(prev -> Math.max(prev, now) + intervalNanos);
        long waitNanos = reserved - now;
        if (waitNanos > 0) TimeUnit.NANOSECONDS.sleep(waitNanos);
    }

    // 캐시 항목 (식별자 + 만료 시각)
    private record CachedId(String value, long expiresAtMillis) {
        static CachedId of(String value) {
            return new CachedId(value, System.currentTimeMillis() + ID_CACHE_TTL_MILLIS);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAtMillis;
        }
    }
}