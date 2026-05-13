# AI Work Log
## 프로젝트: TTcare-SDK-Sample-Android
## 목적: AI와의 작업 내역 추적 및 관리

## [중요] 프로젝트 공통 규칙 및 인프라
- **배포 방식:** 홈페이지(com.aiforpet.sdk)는 `main` 브랜치에 Push되면 GitHub Actions(`.github/workflows/firebase-hosting-merge.yml`)를 통해 Firebase로 **자동 배포**됨. 수동 배포(`npm run deploy` 등) 시도하지 말 것. 그 외 작업 전 프로젝트 내 CI/CD 파이프라인 존재 여부를 확인할 것.
- **문서 동기화:** TTcareSDKSample의 README(다국어) 파일이 원본이며, 홈페이지 문서 수정 시 DevHomepage 쪽으로 항상 복사해서 맞출 것.


### [2026-05-13] 홈페이지(DevHomepage) SDK 버전 및 가이드 동기화
- **작업 내용:** 
  - DevHomepage(`com.aiforpet.sdk`)의 Android 버전을 2.0.3에서 2.0.5로 일괄 업데이트
  - DevHomepage의 Flutter 가이드 내 Android 의존성 버전 2.0.3 -> 2.0.5 변경
  - `TTcareSDKSample`의 최신 다국어 MD 파일(`README.md`, `ko`, `ja`)을 DevHomepage의 `v2.0.5` 폴더로 복사 및 적용
  - `public/pdfs/android/v2.0.5`의 영문/국문 PDF를 홈페이지에서 제공하도록 `docs.ts` 설정 수정
  - DevHomepage 저장소 Git Pull/Push 커밋 완료 및 Firebase 배포 진행 (배포 시 Firebase 권한 오류로 실패)
- **영향도 확인:** 기존 구조에 영향 없으며, 개발자 홈페이지 문서와 샘플 앱 문서를 최신 2.0.5 버전으로 통일함.
- **테스트 확인:** `npm run build` 결과 성공(Green) 확인.


### [2026-05-12] SDK 버전 업데이트 (2.0.5) 및 README 반영
- **작업 내용:** 
  - `gradle/libs.versions.toml` 내의 `scansdkLib` 버전을 2.0.5로 상향 업데이트
  - 다국어 README 파일(`README.md`, `README.ko.md`, `README.ja.md`)의 설치 스니펫 내 버전을 2.0.5로 수정
- **영향도 확인:** TTcare SDK 의존성 버전이 2.0.5로 변경됨. 문서 버전이 함께 동기화되었으며 기존 로직 및 데이터 구조에 변경사항 없음.
- **테스트 확인:** 단순 버전 펌핑이므로 별도 단위 테스트 생략.

### [2026-05-12] 다국어 README 파일 내 라이선스 키 복호화 스니펫 삭제
- **작업 내용:** 
  - `README.md`, `README.ko.md`, `README.ja.md`에서 `String ttConf = YourSecurityManager.decryptAuthKey(context);` 등 복호화 코드 스니펫 및 권장 접근 방식(Android Keystore 등) 안내 부분 삭제
- **영향도 확인:** 엄밀한 관점에서 메모리 상에 String이 노출될 수 있다는 점을 고려하여, 잠재적 보안 오해를 줄 수 있는 가이드를 문서에서 제거함. 코드 변경 없음.
- **테스트 확인:** 문서 일괄 변경 적용 후 마크다운 깨짐 없는지 확인 완료.

### [2026-05-12] 주석 영문화 및 라이선스 키 연동 안내 강화
- **작업 내용:** 
  - `StartActivity.kt` 파일 내부의 한글 주석을 모두 영문으로 번역하여 다국어 개발자 친화성 향상
  - 다국어 README 파일(`README.md`, `README.ko.md`, `README.ja.md`)에 데모 앱 내 라이선스 키(`sdk`) 파일 부재 안내 및 발급받은 키 적용 방법 명시
- **영향도 확인:** 기존 앱 로직이나 데이터 구조에는 변함이 없으며, 코드의 가독성 및 문서 명확성 개선
- **테스트 확인:** 스크립트를 통한 텍스트 일괄 교체 후 이상 없음 확인.

### [2026-05-12] 다국어 README 파일 내 SDK 버전 업데이트 (2.0.4)
- **작업 내용:** `README.md`, `README.ko.md`, `README.ja.md` 파일의 설치 스니펫 내 `scansdk-lib` 버전을 2.0.4로 수정
- **영향도 확인:** 문서상의 버전만 변경되었으며 소스코드 및 데이터 구조 변경 사항 없음.
- **테스트 확인:** Markdown 뷰어에서 버전 번호 정상 변경 확인.

### [2026-05-12] SDK 버전 업데이트 (2.0.4)
- **작업 내용:** `gradle/libs.versions.toml` 내의 `scansdkLib` 버전을 2.0.4로 상향 업데이트
- **영향도 확인:** TTcare SDK 의존성 버전이 2.0.4로 변경됨. 기존 로직 및 데이터 구조에 변경사항 없음.
- **테스트 확인:** 단순 버전 펌핑이므로 별도 단위 테스트 생략.

### [2026-04-30] README 다국어(한국어, 일본어) 지원 추가
- **작업 내용:**
  - `README.md` 파일 최상단에 지구본 아이콘이 포함된 언어 선택 내비게이션(`🌐 [English] · [한국어] · [日本語]`) 추가
  - 기존 영문 가이드를 번역하여 `README.ko.md`(한국어) 및 `README.ja.md`(일본어) 파일 신규 생성
  - 새로 생성된 다국어 파일들에도 동일한 언어 선택 내비게이션 적용
- **영향도 확인:** 다양한 언어권의 개발자가 SDK 연동 문서를 쉽게 확인할 수 있게 됨. 소스코드나 로직 변경은 없음.
- **테스트 확인:** Markdown 뷰어에서 언어 링크 정상 작동 및 번역 포맷 정상 렌더링 확인.

### [2026-04-30] SDK 설정 파일 암호화 원복 및 Git 추적 제외
- **작업 내용:**
  - `app/src/main/assets/sdk` 파일의 내용을 다시 평문 JSON으로 원복
  - `StartActivity.kt`에서 파일을 읽을 때 사용하던 `CryptoUtils.decrypt()` 제거 (평문 그대로 사용하도록 수정)
  - `app/src/main/assets/sdk` 파일을 `.gitignore`에 추가하고 Git 캐시에서 삭제하여 더 이상 커밋되지 않도록 조치
- **영향도 확인:** SDK 키값이 포함된 설정 파일이 Git에 더 이상 커밋되지 않아 보안사고를 예방함. 앱 실행 시에는 로컬 자산 파일에서 정상적으로 JSON 데이터를 가져옴.
- **테스트 확인:** 빌드 및 실행 확인 (코드상 `CryptoUtils` 제거 정상 적용).

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
