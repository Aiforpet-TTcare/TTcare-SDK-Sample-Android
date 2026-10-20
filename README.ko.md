🌐 [English](README.md) · [한국어](README.ko.md) · [日本語](README.ja.md)

이 가이드를 참고하여 TTcare Scan SDK v2.2.3을 안드로이드 앱에 연동할 수 있습니다. 이 릴리즈는 온디바이스 분석과 TTAPI 서버 분석을 지원하며, 생성된 결과 이미지는 HTTPS URL로 반환합니다. API 28 이상이 필요합니다.

## v2.x의 새로운 기능
* v2.2.3에서 눈·피부·치아 사진을 최대 4장 처리하는 선택형 TTAPI 멀티 촬영 모드를 추가했습니다.
* v2.1.3에서는 SDK 호출 옵션에 `petName`이 추가되었고, 공개 결과 JSON이 최신 연동사 응답 포맷에 맞게 정리되었습니다.
* Target SDK가 API 36으로 상향되었습니다.
* ONNX Runtime이 이제 API 28+를 요구함에 따라 Minimum SDK가 API 28로 상향되었습니다.
* AI 진단 시 더 이상 서버와의 통신(round-trip)에 의존하지 않습니다.
* 결과 이미지 참조는 HTTPS URL로 반환되므로 호스트 앱에서 바로 로드할 수 있습니다.
* 생성된 미디어 캐시 정리를 위한 `LibraryClass.clearSdkScanCache()`가 추가되었습니다.
* 결과 화면 지원 언어는 영어, 한국어, 일본어, 이탈리아어, 스웨덴어, 태국어입니다.
* `enablePdfShare` 옵션으로 내장 결과 화면의 PDF 공유 버튼을 제어할 수 있습니다.

## 사전 요구 사항
* Android 9.0 이상 (minSdk 28)
* Target SDK 36 권장
* Java 17 권장
* 지원되는 ABI: `armeabi-v7a`, `arm64-v8a`

SDK는 CameraX, OkHttp, LiteRT, ONNX Runtime 등의 자체 런타임 의존성을 포함하고 있습니다. 별도의 추가적인 서드파티 의존성 설정은 필요하지 않습니다.

## Maven Central에서 설치
프로젝트의 리포지토리에 Maven Central을 추가하고 앱 모듈에 SDK를 추가합니다.

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.aiforpet-ttcare:scansdk-lib:2.2.3")
}
```
SDK가 CAMERA 및 INTERNET 권한을 선언하고 있으므로 자동으로 프로젝트의 Manifest에 병합됩니다.

## SDK key 설정

> [!IMPORTANT]
> **데모 앱 SDK key 안내**
> 본 샘플 프로젝트는 `Enter your issued SDK key` 플레이스홀더 값을 사용합니다.
> 샘플 앱을 실행하기 전에 프로젝트용으로 발급받은 SDK key로 교체하세요.

TTcare는 고객사 프로젝트별 SDK key를 발급합니다. 이 값은 secret으로 취급하고 앱 보안 정책에 따라 관리하세요.

**보안 규칙**
실제 운영용 SDK key를 공개 소스 코드에 노출하지 마십시오. 안전하게 저장하거나 런타임에 가져온 뒤, SDK Activity 실행 시 발급받은 SDK key 문자열을 `sdkKey`로 전달하세요.



## 카메라 실행하기
스캔 흐름은 다음 3단계로 진행됩니다:
1. ActivityResultLauncher 등록
2. 올바른 카메라 액티비티 선택
3. Bundle 데이터를 포함하여 실행(Launch)

### 1. 결과 콜백 등록
사용자가 내장된 결과 화면을 닫은 후 SDK는 최종 결과 JSON을 콜백을 통해 반환합니다.

```java
private final ActivityResultLauncher<Intent> scanLauncher =
    registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                String resultData = result.getData().getStringExtra("result");
                Log.d("ScanSDK", "Scan complete! Result: " + resultData);

                // 캐시를 비우기 전에 결과를 저장하거나 업로드하세요.
            }
        }
    );
```

### 2. 카메라 액티비티 선택
* 눈(Eye): `EyeCameraActivity`
* 피부 (귀 / 복부 / 발): `SkinCameraActivity`
* 치아(Teeth): `ToothCameraActivity`

### 3. 요청 파라미터 전달

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `userId` | String | 예 | 고객사 서비스의 사용자 식별자 |
| `petType` | String | 예 | `DOG` 또는 `CAT` |
| `sdkKey` | String | 예 | 프로젝트용으로 발급받은 SDK key |
| `partType` | String | 피부 검사 시 필수 | `EAR`, `BELLY`, `FOOT` 중 하나. `SkinCameraActivity` 실행 시 필수 |
| `petId` | String | 아니오 | 고객사 서비스의 반려동물 식별자. 전달 시 결과 데이터에 포함됩니다 |
| `petName` | String | 아니오 | 내장 결과 화면 및 PDF 리포트 문구에 사용할 반려동물 이름 |
| `petBirthday` | String | 아니오 | 반려동물 생일. `yyyy-MM-dd` 형식 권장 |
| `petBreedName` | String | 아니오 | 품종명. 서비스에서 품종을 매핑하는 경우 별도 Breed List 기준 사용 권장 |
| `petGender` | String | 아니오 | `M`, `F`, `MC`, `FC` |
| `guideUrl` | String | 아니오 | SDK 카메라 가이드 웹뷰에 표시할 URL |
| `isAnalysisEnabled` | Boolean/String | 아니오 | 설정된 경우 파트너 분석 래퍼 모드 활성화 |
| `isFlashMode` | Boolean | 아니오 | 카메라 플래시 기본 활성화 여부. 기본값: `false` |
| `enablesQuestionnaire` | Boolean | 아니오 | SDK 문진 사용 여부. 기본값: `true` |
| `enableResultView` | Boolean | 아니오 | SDK 내장 결과 화면 표시 여부. 기본값: `true` |
| `enablePdfShare` | Boolean | 아니오 | 내장 결과 화면의 PDF 공유 버튼 표시 여부. `enableResultView=true`일 때 의미가 있습니다 |
| `enableMultiMode` | Boolean | 아니오 | 프로젝트가 TTAPI 분석 모드일 때만 최대 4장 촬영을 활성화합니다. 기본값: `false` |

```java
Intent intent = new Intent(this, EyeCameraActivity.class);
Bundle bundle = new Bundle();
String sdkKey = "Enter your issued SDK key";

bundle.putString("petType", "DOG");
bundle.putString("userId", "your_user_id");
bundle.putString("sdkKey", sdkKey);
bundle.putString("petBirthday", "2020-03-15");
bundle.putString("petBreedName", "Maltese");
bundle.putString("petGender", "MC");

bundle.putString("petId", "your_pet_id_123");
bundle.putString("petName", "Coco");
bundle.putString("guideUrl", "https://resource-core.aiforpetcdn.com/sdk/guide/ko/dog/eye.html");
bundle.putBoolean("enablesQuestionnaire", true);
bundle.putBoolean("enableResultView", true);
bundle.putBoolean("enablePdfShare", true);
bundle.putBoolean("enableMultiMode", false); // 선택. TTAPI 멀티 촬영에서만 true

intent.putExtras(bundle);
scanLauncher.launch(intent);
```

### 피부 스캔 참고 사항
피부 스캔은 다양한 세부 부위를 지원합니다. `SkinCameraActivity`를 실행할 때는 `partType`을 필수로 전달하세요.

```java
Intent intent = new Intent(this, SkinCameraActivity.class);
Bundle bundle = new Bundle();

bundle.putString("partType", "EAR");   // 또는 BELLY, FOOT

intent.putExtras(bundle);
scanLauncher.launch(intent);
```

## 선택형 TTAPI 멀티 촬영

`enableMultiMode`는 프로젝트가 `analysis_mode=ttapi`로 설정된 경우에만 사용할 수 있습니다. `true`로 전달하면 눈·피부·치아 사진을 최대 4장 수집해 분석합니다. 옵션을 전달하지 않거나 `false`이면 기존 단건 촬영 흐름을 유지하며, 온디바이스와 하이브리드 분석도 단건 방식으로 동작합니다.

`result` Intent extra의 타입은 계속 JSON String입니다. 멀티 촬영에서는 한 장만 제출해도 문자열의 최상위 구조가 촬영 순서의 JSON 배열이며, 각 항목은 사진별 원본 결과입니다. 호출 앱은 각 항목의 상태를 개별 확인해야 합니다. 단건 모드는 기존처럼 JSON 객체 하나를 반환합니다.

## 결과 파악하기
SDK는 `scanLauncher`를 통해 최종 진단 결과가 포함된 JSON 문자열을 반환합니다. 단건 모드는 객체로, TTAPI 멀티 촬영은 배열로 파싱하세요.

### 결과 상태(status)
* `NORMAL`: 이상 징후 없음
* `CAUTION`: 가벼운 이상 징후 감지됨, 주의 깊게 관찰 필요
* `WARNING`: 다수 또는 강한 이상 징후 감지됨, 후속 조치 권장

문진(Questionnaire)이 활성화된 경우, 최종 상태는 스캔 결과와 문진 답변을 모두 반영합니다.

| 문진 응답 | AI 스캔 결과 | 최종 상태 |
| --- | --- | --- |
| 증상 있음 | 이상 징후 감지 | `WARNING` |
| 증상 없음 | 이상 징후 감지 | `CAUTION` |
| 증상 없음 | 이상 징후 없음 | `NORMAL` |
| 증상 있음 | 이상 징후 없음 | `CAUTION` |

### 결과 이미지 표시
히트맵과 크롭 이미지는 HTTPS URL로 반환됩니다. 호스트 앱은 SDK가 생성한 로컬 파일을 직접 읽을 필요 없이, 반환된 URL을 이미지 로딩 라이브러리로 바로 로드하면 됩니다.

```java
String imageUrl = symptom.optString("heatmapUrl");

Picasso.get()
    .load(imageUrl)
    .into(imageView);
```

### 서버 전송 payload 참고
파트너 서버가 SDK 업로드 payload를 함께 수신하는 연동 구조라면, 증상별 crop 및 heatmap 이미지 URL은 `symptom_images[]`를 기준으로 사용하세요. payload 루트에는 더 이상 대표 `crop` 또는 `heatmap` 필드가 포함되지 않습니다.

`inferences[]`에는 서버 처리에 필요한 결과 라벨과 점수가 유지되며, 모델 출력값으로 `pred_values`가 포함될 수 있습니다. 이 필드는 서버 처리용이며 앱으로 반환되는 공개 결과 JSON에는 포함되지 않습니다.

### PDF 리포트 공유
`enablePdfShare=true`이고 `enableResultView=true`이면 내장 결과 화면에 PDF 공유 버튼이 표시됩니다. PDF 리포트는 언어별 리소스 기반의 요약 문구를 사용하며, 리포트 생성 시 `heatmapUrl`, `heatmapImageUrl`, `heatmapPath` 같은 히트맵 참조 값을 사용할 수 있습니다.

### 결과 스키마 개요

#### 최상위 필드

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `status` | String | SDK 처리 상태. 일반적으로 `SUCCESS` |
| `petType` | String | `DOG` 또는 `CAT` |
| `part` | String | 검사 부위. 예: `EYE`, `SKIN`, `TOOTH` |
| `createdAt` | Long | UTC 밀리초 기준 분석 생성 시각 |
| `questions` | Array | 문진 사용 시 문진 응답 |
| `response` | Object | UI 표시용 결과 요약 |
| `userId` | String | 요청 시 전달한 사용자 식별자 |
| `petId` | String | 요청 시 전달한 반려동물 식별자 |
| `subPart` | String | 가능한 경우 `EYEL`, `EAR`, `TCENTER` 같은 상세 위치 코드 |

공개 결과 JSON에는 더 이상 `metadata` 객체가 포함되지 않습니다. `score`, `predValues`, `pred_values`, `modelName` 같은 내부 모델 필드는 연동사 앱으로 반환되지 않습니다.

#### `questions[]`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `text` | String | 문진 질문 텍스트 |
| `select` | String | 사용자 응답. `Y` 또는 `N` |

#### `response`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `status` | String | 최종 판정 상태: `NORMAL`, `CAUTION`, `WARNING` |
| `title` | String | 사용자 표시용 결과 제목 |
| `analyzedDate` | String | 포맷된 분석 일시 |
| `description` | Object | 결과 수준의 안내 문구 |
| `symptoms` | Array | 감지된 증상 상세 |

#### `response.description`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `title` | String | 안내 제목 |
| `contents` | Array | 안내 본문 목록 |

#### `response.symptoms[]`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `code` | String | 이상 징후 코드. 예: `hyperemia`, `epiphora` |
| `name` | String | 현지화된 이상 징후 이름 |
| `abnormLevel` | Int | SDK가 계산한 이상 수준 |
| `cropImageUrl` | String | 크롭 이미지 HTTPS URL |
| `heatmapUrl` | String | 히트맵 이미지 HTTPS URL |
| `isAbnormal` | Boolean | 해당 증상이 이상으로 판단되었는지 여부 |
| `resultLabel` | String | `normal`, `abnormal` 등의 결과 라벨 |
| `details` | Array | 증상 설명 블록 |

#### `response.symptoms[].details[]`

| Key | 설명 |
| --- | --- |
| `what_it_is` | 감지된 증상에 대한 일반 설명 |
| `related_clinical_conditions` | 파트너용 관련 질환 및 요인 |
| `possible_causes` | 보호자용 가능한 원인 |
| `what_you_can_do` | 홈케어 및 관찰 가이드 |

#### 부위(Position) 코드

| 코드 | 검사 종류 | 설명 |
| --- | --- | --- |
| `EYER` | 눈 | 오른쪽 눈 |
| `EYEL` | 눈 | 왼쪽 눈 |
| `EAR` | 피부 | 귀 |
| `BELLY` | 피부 | 복부 |
| `FOOT` | 피부 | 발 |
| `TCENTER` | 치아 | 앞니 |
| `TRIGHT` | 치아 | 오른쪽 치아 |
| `TLEFT` | 치아 | 왼쪽 치아 |

### 샘플 결과 JSON

```json
{
  "status": "SUCCESS",
  "petType": "DOG",
  "part": "EYE",
  "createdAt": 1781485248231,
  "userId": "your_user_id",
  "petId": "your_pet_id_123",
  "subPart": "EYEL",
  "questions": [
    { "text": "동공 크기가 서로 다른가요?", "select": "N" }
  ],
  "response": {
    "status": "WARNING",
    "title": "가능한 한 빠른 동물병원 진료를 권장해요.",
    "analyzedDate": "2026. 06. 15 10:00",
    "description": {
      "title": "동물병원 내원이 필요한 경우",
      "contents": [
        "눈을 찡그리거나 깜빡이는 증상이 호전되지 않는 경우",
        "충혈, 부기, 분비물이 지속되거나 악화되는 경우"
      ]
    },
    "symptoms": [
      {
        "code": "opacity",
        "abnormLevel": 1,
        "cropImageUrl": "https://cdn-results.ai4pet.com/.../diagnosis_crop",
        "name": "혼탁",
        "isAbnormal": true,
        "resultLabel": "abnormal",
        "heatmapUrl": "https://cdn-results.ai4pet.com/.../diagnosis_heatmap",
        "details": [
          {
            "key": "what_it_is",
            "title": "이 증상은 무엇인가요",
            "contents": ["눈 표면이 뿌옇거나 흐릿하게 보이는 상태예요."]
          }
        ]
      }
    ]
  }
}
```

## 캐시 정리
SDK는 진단 과정 중 임시 파일을 생성할 수 있습니다. 결과 이미지 참조는 HTTPS URL로 반환되므로, 호스트 앱이 크롭 이미지나 히트맵 로컬 파일을 직접 복사할 필요는 없습니다.

```java
LibraryClass.clearSdkScanCache(context);
```
반환된 결과 JSON 처리를 마친 뒤 이 메서드를 호출하세요.

| 삭제 대상 | 설명 |
| --- | --- |
| `ttSdk_*` 폴더 | 스캔별 SDK 임시 작업 폴더 |
| `*.png` 파일 | 분석 과정에서 생성될 수 있는 임시 이미지 |
| `dataResult`, `result` 파일 | SDK가 생성한 임시 디버그/결과 파일 |

## 선택적 Mixpanel 연동
앱에서 이미 Mixpanel을 사용하고 있다면 SDK 내부 이벤트를 추적기에 연결할 수 있습니다.

```java
bundle.putString("isAnalysisEnabled", "true");
```
그런 다음 Application 클래스에 트래커를 등록합니다:

```kotlin
class MyMixpanelTracker(context: Context, token: String) : MixpanelTracker {
    private val mixpanel = MixpanelAPI.getInstance(context, token, true)

    override fun trackEvent(eventName: String, properties: Map<String, String>) {
        mixpanel.track(eventName, JSONObject(properties))
    }

    override fun trackScreen(screenName: String) {
        mixpanel.track(screenName)
    }
}
```

```kotlin
class AiForPetApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val tracker = MyMixpanelTracker.getInstance(
            this,
            "your_mixpanel_token"
        )
        MySDK.setMixpanelTracker(tracker)
    }
}
```

이 연동을 사용할 경우 `AndroidManifest.xml`에 직접 만든 `Application` 클래스를 등록하세요.

## Proguard
SDK에는 라이브러리 사용자용 Proguard 규칙이 포함되어 있지만, 릴리즈 빌드에서 카메라 스캔이 충돌하는 경우 아래와 같이 직접 keep 규칙을 추가하세요:

```proguard
-keep class com.aiforpet.pet.** { *; }
-keep class com.microsoft.onnxruntime.** { *; }
-keep class com.google.ai.edge.litert.** { *; }
```

## 연동 흐름 요약
1. `scanLauncher` 등록
2. 요청용 `Bundle` 데이터 구성
3. 알맞은 카메라 액티비티 실행
4. 분석이 끝난 후 결과 JSON 수신
5. 필요한 경우 반환된 이미지 URL을 바로 렌더링
6. 결과 처리 완료 후 `LibraryClass.clearSdkScanCache()` 호출

## v1.7.x 마이그레이션 참고
* 진단이 기기 내(on-device)에서 실행됩니다.
* 결과 이미지 참조는 로컬 파일 경로가 아닌 HTTPS URL로 반환됩니다.
* 최소 지원 버전(Minimum SDK)이 28로 상향되었습니다.
* SDK 인증은 필수 `sdkKey` Bundle 값으로 전달합니다.

## 고객 지원
품종 목록, 증상 사전 데이터, 기본 가이드 URL, 기타 연동 지원이 필요하시면 TTcare 팀에 문의해 주세요.
