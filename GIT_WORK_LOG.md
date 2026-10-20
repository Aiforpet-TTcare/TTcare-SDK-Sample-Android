# Git Work Log

### [2026-10-20 09:45 KST] Android SDK 2.2.3 및 TTAPI 멀티 촬영 예제
- **브랜치:** `codex/public-android-2.2.3-sample-20261020`을 `origin/main`에서 생성하고 원격 브랜치 등록.
- **변경:** 공개 SDK 의존성을 2.2.3으로 갱신하고, `enableMultiMode (TTAPI)` 체크박스를 Eye/Skin/Teeth 호출 Bundle에 연결함. 결과 화면은 기존 JSON 객체와 멀티 촬영 JSON 배열을 모두 표시함.
- **외부 계약:** `enableMultiMode` 기본값은 false이며 `analysis_mode=ttapi`에서만 최대 4장에 적용됨. true일 때 한 장을 포함해 배열을 반환하고, 단건/on-device/hybrid의 객체 결과는 유지함.
- **검증:** `:app:testDebugUnitTest`, `:app:assembleDebug`, `git diff --check` 통과. 실제 라이선스 키 패턴 없음.
- **상태:** 이 변경 커밋을 작업 브랜치에 푸시함. main 병합 및 배포 없음.
