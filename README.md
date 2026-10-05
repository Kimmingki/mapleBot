# 🍁 mapleBot — 메이플스토리M 정보 안내 봇

넥슨 Open API(메이플스토리M)로 캐릭터 정보를 조회해 **카카오톡 채팅용 텍스트**로 돌려주는 Spring Boot 서버입니다.
카카오톡 봇 클라이언트가 `.정보 닉네임 서버` 같은 명령어를 받으면 이 서버의 API를 호출하고, 받은 문자열을 채팅방에 그대로 출력하는 방식입니다.

> ⚠️ 대상 게임은 PC 「메이플스토리」가 아니라 모바일 **「메이플스토리M」**입니다.
> 모든 API는 `https://open.api.nexon.com/maplestorym/v1` 경로를 사용합니다.

---

## 📌 목차

1. [기술 스택](#-기술-스택)
2. [빠른 시작](#-빠른-시작)
3. [채팅 명령어](#-채팅-명령어)
4. [REST API 명세](#-rest-api-명세)
5. [디버그 API](#-디버그-api)
6. [프로젝트 구조](#-프로젝트-구조)
7. [설정 파일](#-설정-파일)
8. [참고 사항](#-참고-사항)

---

## 🛠 기술 스택

| 구분 | 내용 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.13 (Web) |
| Build | Gradle 8.10 (Wrapper 포함) |
| 기타 | Lombok, RestTemplate |
| 외부 API | [넥슨 Open API - 메이플스토리M](https://openapi.nexon.com/ko/game/maplestorym/) |

---

## 🚀 빠른 시작

### 1. 넥슨 Open API 키 발급

[넥슨 Open API](https://openapi.nexon.com/)에서 애플리케이션을 등록하고 **메이플스토리M**용 API 키를 발급받습니다.

### 2. 시크릿 파일 생성

`src/main/resources/application-secret.properties` 파일을 만들고 키를 입력합니다.
이 파일은 `.gitignore`에 등록되어 있어 커밋되지 않습니다.

```properties
nexon.api.key=발급받은_API_키
```

### 3. 실행

기본 `application.properties`가 없으므로 **프로필(`local` 또는 `prod`)을 반드시 지정**해야 합니다.

```bash
# 로컬 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# Windows
gradlew.bat bootRun --args="--spring.profiles.active=local"
```

```bash
# 운영 실행
./gradlew bootJar
java -jar build/libs/maple-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

서버는 **8081** 포트로 실행됩니다.

### 4. 동작 확인

```bash
curl "http://localhost:8081/api/bot/info?name=캐릭터명&world=스카니아"
```

---

## 💬 채팅 명령어

모든 명령어는 앞에 `.`을 붙입니다. 서버(월드)를 생략하면 **스카니아**로 조회합니다.

```
.정보 귀요밍키 스카니아
.헥사 귀요밍키
```

### ✅ 사용 가능

| 명령어 | 설명 | 출력 내용 |
|---|---|---|
| `.명령어` | 도움말 | 전체 명령어 목록 (카카오톡 '전체보기' 적용) |
| `.정보` | 캐릭터 종합 정보 | 월드·레벨·직업·성별, 길드, 생성일, 유니온, 장착 펫, 세트 효과 |
| `.경험치` | 경험치 | 현재 레벨, 누적 경험치 |
| `.보스` | 보스 프리셋 장비 | 세트 구성(앜/앱/칠), 스타포스, 장착 소울, 부위별 잠재·추옵·가위 횟수 |
| `.내실` | 내실 요약 | 유니온, 보스 장비 세트, 아케인/어센틱 포스, 헥사, 링크 스킬, 길드 개인 스킬 |
| `.심볼` | 심볼 정보 | 아케인/어센틱 포스, 지역별 심볼 레벨·현재 성장치, MAX까지 남은 심볼 개수·필요 메소 (지역별·합계) |
| `.코강` | V매트릭스 | 장착 중인 강화 코어(레벨·포인트), 스킬 프리셋, 스킬 코어, 쓸만한 스킬, 특수 코어 |
| `.코디` | 코디 정보 | 헤어·얼굴·피부, 캐시 장비, 캐릭터 이미지 |
| `.길스` | 길드 스킬 | 소속 길드, 기여도, 길드 개인 스킬 |
| `.헥사` | 헥사 매트릭스 | 헥사 코어 레벨, 남은 솔 에르다·조각, 헥사 스탯 페이지 |

### 🚧 준비 중

아래 명령어는 도움말에만 있고 아직 서버 API가 구현되지 않았습니다.

- **캐릭터**: 링크
- **장비**: 사냥, 장비, 비교 / 헥사비교, 장착, 저장, 삭제, 스펙변동
- **게임 정보**: 쥬얼, 기여도, 도핑, 문장, 극옵, 레드메소, 유니온, 하이퍼스탯, 소울, 몬파, 계산기, 해방도움말
- **이미지**: 직업 이미지, 보스 패턴 이미지

---

## 📡 REST API 명세

- Base URL: `http://{host}:8081/api/bot`
- Method: 모두 `GET`
- 응답: `text/plain` (채팅방에 그대로 출력하는 문자열)

| 엔드포인트 | 채팅 명령어 | 파라미터 |
|---|---|---|
| `/help` | `.명령어` | 없음 |
| `/info` | `.정보` | `name`(필수), `world`(선택, 기본값 스카니아) |
| `/exp` | `.경험치` | 〃 |
| `/boss` | `.보스` | 〃 |
| `/naesil` | `.내실` | 〃 |
| `/symbol` | `.심볼` | 〃 |
| `/vmatrix` | `.코강` | 〃 |
| `/codi` | `.코디` | 〃 |
| `/guildskill` | `.길스` | 〃 |
| `/hexa` | `.헥사` | 〃 |

**예시**

```
GET /api/bot/hexa?name=귀요밍키&world=스카니아
```

```
🍁 【 메이플스토리M 헥사 매트릭스 】
귀요밍키 (스카니아)
...
🍁 【 필요 솔 에르다 】 : 1,234개
🍁 【 필요 솔 에르다 조각 】 : 56,789개
...
```

### 클라이언트 구현 시 참고

- **전체보기 처리**: 일부 응답에는 제목 뒤에 제로 위드 스페이스(`​`) 500개가 들어 있어 카카오톡에서 '전체보기'로 접힙니다.
- **코디 이미지**: `/codi` 응답 끝에는 `|||IMAGE|||{이미지URL}`이 붙습니다. 클라이언트에서 이 구분자를 기준으로 텍스트와 이미지 URL을 나눠 처리해야 합니다.
- **조회 실패**: 캐릭터를 찾을 수 없으면 `캐릭터 정보를 찾을 수 없습니다.` 같은 안내 문구를 반환합니다.

---

## 🐞 디버그 API

넥슨 API의 **원본 JSON**을 그대로 확인하는 개발용 엔드포인트입니다. DTO 필드를 매핑할 때 사용합니다.

- Base URL: `http://{host}:8081/api/debug`
- 파라미터: `name`(필수), `world`(선택, 기본값 스카니아)

| 엔드포인트 | 확인하는 넥슨 API |
|---|---|
| `/info` | `/character/basic`, `/character/guild`, `/user/union`, `/character/pet-equipment`, `/character/set-effect` |
| `/naesil` (.내실·.보스 공용) | `/character/basic`, `/character/item-equipment`, `/character/symbol`, `/character/hexamatrix-skill`, `/character/hexamatrix-stat`, `/character/link-skill`, `/character/guild` |
| `/vmatrix` | `/character/vmatrix`, `/character/skill-equipment` |
| `/codi` | `/character/basic`, `/character/beauty-equipment`, `/character/cashitem-equipment` |
| `/guildskill` | `/character/basic`, `/character/guild`, `/guild/basic` |
| `/hexa` | `/character/basic`, `/character/hexamatrix-skill`, `/character/hexamatrix-stat` |

> 운영 환경에서는 외부에 노출되지 않도록 주의하세요.

---

## 📂 프로젝트 구조

```
src/main/java/com/classic/maple
├── MapleApplication.java              # 애플리케이션 진입점
├── controller
│   ├── BotController.java             # 채팅 명령어용 API (/api/bot)
│   └── DebugController.java           # 원본 JSON 확인용 API (/api/debug)
├── service
│   ├── core
│   │   └── NexonApiClient.java        # 넥슨 Open API 공통 호출 (OCID, 길드 ID, 데이터 조회)
│   ├── command
│   │   ├── character                  # 캐릭터 정보 명령어
│   │   │   ├── InfoCommandService     # .정보 / .경험치
│   │   │   ├── NaesilCommandService   # .내실
│   │   │   ├── SymbolCommandService   # .심볼
│   │   │   ├── VMatrixCommandService  # .코강
│   │   │   ├── HexaCommandService     # .헥사
│   │   │   ├── CodiCommandService     # .코디
│   │   │   └── GuildSkillCommandService # .길스
│   │   └── equipment                  # 장비 정보 명령어
│   │       └── BossCommandService     # .보스
│   └── debug
│       └── DebugCommandService.java   # 원본 JSON 조회
└── dto                                # 넥슨 API 응답 매핑 객체
```

### 요청 처리 흐름

```
카카오톡 봇 클라이언트
   │  GET /api/bot/{command}?name=...&world=...
   ▼
BotController
   ▼
*CommandService ──▶ NexonApiClient
   │                  ├─ /id               → 캐릭터 OCID 조회
   │                  ├─ /character/...    → 캐릭터 데이터 조회
   │                  └─ /guild/id, /guild/basic → 길드 데이터 조회
   ▼
채팅용 텍스트 조립 후 반환
```

- 넥슨 API 요청 제한(429)을 피하려고 호출마다 **150ms 지연**을 둡니다.

---

## ⚙️ 설정 파일

| 파일 | 용도 |
|---|---|
| `application-local.properties` | 로컬 개발용 |
| `application-prod.properties` | 운영용 |
| `application-secret.properties` | API 키 보관 (직접 생성, Git 제외) |

---

## 📝 참고 사항

- **심볼 계산**: 넥슨 API의 `symbol_growth_value`(현재 레벨 구간 성장치)를 기준으로, MAX 레벨(아케인 20 / 어센틱 11)까지 필요한 양을 계산합니다.
    - 남은 심볼 = 현재 레벨~MAX 요구 성장치 합 − 현재 성장치 (0 미만이면 0)
    - 필요 메소 = 현재 레벨~MAX 강화 메소 합 (아케인은 소멸의 여로 / 츄츄 아일랜드 이후 지역 공통, 어센틱은 지역별 비용표 적용)
    - 비용표 출처: 메이플창고 정리표 (어센틱 2026-08-29, 아케인 2026-09-02 기준). 아에르·오디움·도원경 메소는 일부 추정치입니다.
    - 비용표에 없는 어센틱 지역은 `비용표 미확인`으로 표시되며, 메소 합계에서 제외됩니다.
    - MVP 등급 심볼 강화 비용 할인은 반영되지 않습니다.
- **데이터 출처**: 넥슨 Open API를 통해 제공받으며, 게임 내 실시간 정보와 차이가 있을 수 있습니다.
