# AI Work Log
## 프로젝트: TTcare-SDK-Sample-Android
## 목적: AI와의 작업 내역 추적 및 관리

### [2026-04-27] DevHomepage - Flutter 가이드 연동 및 문서 추가
- **작업 내용:** 
  - `com.aiforpet.sdk` 홈페이지 리포지토리에 Flutter 네이티브 브릿지 연동 가이드 추가
  - 상단 내비게이션 바(TopBar)에 `Flutter 가이드` 신규 라우팅 연동
  - 안드로이드/iOS 가이드 스키마에 맞춘 Flutter용 Markdown 페이지 작성 (입출력 및 캐시 관리 상세 포함)
  - Flutter 가이드 한정으로 의미가 없는 PDF 다운로드 링크 제외 처리
- **영향도 확인:** 기존 안드로이드/iOS 문서 동작에는 영향을 주지 않으며, 신규 플랫폼(Flutter) 탭과 라우팅이 추가됨.
- **테스트 확인:** 로컬 브라우저 상에서 모든 페이지 접속 및 UI 정렬(세로 배치) 정상 렌더링 확인 완료.


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
