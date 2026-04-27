# AI Work Log
## 프로젝트: TTcare-SDK-Sample-Android
## 목적: AI와의 작업 내역 추적 및 관리

### [2026-04-27] SDK 버전 업데이트 (2.0.3)
- **작업 내용:** `gradle/libs.versions.toml` 내의 `scansdkLib` 버전을 2.0.3으로 상향 업데이트
- **영향도 확인:** TTcare SDK 의존성 버전이 2.0.3으로 변경됨. 기존 로직 및 데이터 구조에 변경사항 없음.
- **테스트 확인:** 단순 버전 펌핑이므로 별도 단위 테스트 생략.

### [2026-04-21] 레이아웃 문구 수정 (enablesQuestionnaire -> enableQuestionnaire)
- **작업 내용:** `activity_start.xml` 내의 오타 수정 (`enablesQuestionnaire`를 `enableQuestionnaire`로 변경)
- **영향도 확인:** UI 문구만 변경된 사항으로, 로직 및 기존 데이터 구조상 차이나 사이드 이펙트는 없음.
- **테스트 확인:** 단순 레이아웃 수정이므로 별도 단위 테스트 생략.

### [2026-04-20] Git 연동 및 초기 세팅
- **작업 내용:** 
  - 로컬 프로젝트(Android SDK Sample)를 GitHub Repository와 연동
  - Remote Repository 연결: `https://github.com/AIforpet-TTcare/TTcare-SDK-Sample-Android.git`
  - 초기 소스코드 커밋 (`first commit`) 및 `main` 브랜치에 Push 완료

---
*(이후 진행되는 모든 작업 및 변경 사항은 이 문서 상단에 누적하여 기록합니다.)*
