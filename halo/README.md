# Halo

"Halo" 하고 부르면 대답하는 안드로이드 AI 비서. 설계는 [docs/halo-design.md](../docs/halo-design.md)에 있어요.

## 구조

역할별로 칸을 나눴어요. 두뇌 부분(`core`)은 안드로이드 없이 빌드하고 테스트할 수 있고, `app`은 그 위에 화면과 음성을 얹어요.

```
halo/
├── core/                         Halo의 두뇌 (순수 Kotlin)
│   └── src/main/
│       ├── kotlin/halo/
│       │   ├── config/           설정: 모델, 생각 깊이, 기억할 대화 수
│       │   ├── prompts/          지시문을 읽어 시스템 프롬프트로 조립
│       │   ├── agent/            HaloAgent: 말을 받아 대답을 돌려주는 본체
│       │   ├── brain/            Brain 인터페이스와 Claude API 연결(ClaudeBrain)
│       │   ├── tools/            도구 틀과 기본 도구(현재 시각, 수첩에 적기)
│       │   ├── memory/           짧은 기억(최근 대화)과 Halo 수첩
│       │   └── evaluation/       대답 규칙 검사, 시험 문장, 실제 평가 실행기
│       └── resources/prompts/    Halo 성격(persona.md)과 대화 규칙(conversation.md)
├── core/src/test/                각 부분 테스트
└── app/                          안드로이드 앱 (지금은 뼈대 화면)
```

대화 흐름: 사용자 말 → `HaloAgent` → `Brain`(Claude) ⇄ `tools` → 대답 → `memory`에 저장

## 실행

```bash
cd halo
./gradlew :core:test                               # 테스트 (API 키 필요 없음)
ANTHROPIC_API_KEY=... ./gradlew :core:eval         # 실제 Claude로 대답 품질 평가 (API 요금 발생)
./gradlew :app:assembleDebug                       # 앱 빌드 (안드로이드 SDK 필요)
```

GitHub Actions가 테스트를 돌리고 앱 설치 파일(APK)을 만들어요. Actions 실행 결과의 `halo-debug-apk`에서 받을 수 있어요.

API 키는 저장소에 넣지 마세요. 이 저장소는 공개예요.
