🌐 [English](README.md) · [한국어](README.ko.md) · [日本語](README.ja.md)

이 샘플은 TTcare Scan SDK 3.2.0의 Android 연동 방법을 보여줍니다. SDK는 기기에서 이미지를 준비한 뒤 서버 분석 결과를 반환합니다. SDK 자체는 API 23 이상을 지원하며, 현재 샘플 앱 설정은 API 28 이상입니다.

## 3.2.0 연동 요점
* 인증·모델 준비 중에도 카메라 프리뷰를 먼저 보여주며 준비가 끝나기 전에는 촬영할 수 없습니다.
* SDK 키에 잘못된 공백·제어문자가 있으면 호스트 앱 충돌 대신 SDK 화면이 취소됩니다. 발급된 키를 그대로 전달하세요.
* 기본 촬영 가이드는 `https://guide.ai4pet.com/sdk/guide/{locale}/{pet}/{part}`를 사용하며, 앱에서 `guideUrl`을 전달하면 우선합니다.
* 선택형 `enableMultiMode`로 서버 분석 사진을 최대 4장 처리하며, 한 장만 제출해도 JSON 배열을 반환합니다.
* 결과 이미지 참조는 HTTPS URL로 반환되므로 호스트 앱에서 바로 로드할 수 있습니다.
* `LibraryClass.clearSdkScanCache()`로 생성된 미디어 캐시를 정리할 수 있습니다.
* 기본 촬영 가이드는 한국어, 영어, 일본어, 브라질 포르투갈어, 터키어, 태국어를 지원하며 미지원 언어는 영어를 사용합니다.
* `enablePdfShare` 옵션으로 내장 결과 화면의 PDF 공유 버튼을 제어할 수 있습니다.

## 사전 요구 사항
* SDK 최소 버전: Android 6.0 이상(API 23). 현재 샘플 앱은 `minSdk 28`입니다.
* SDK의 Java 17 바이트코드를 처리할 수 있는 앱 빌드 환경을 확인하세요.
* AGP 9 앱은 루트 `gradle.properties`에 `android.uniquePackageNames=false`가 필요하며, AGP 8.2.2는 지원하지 않습니다.
* 지원되는 ABI: `armeabi-v7a`, `arm64-v8a`

이 샘플은 Gradle 9.1, AGP 8.13.2, Kotlin 2.3.20, JDK 17에서 빌드를 확인했습니다. 이는 샘플의 검증 환경이며 모든 호스트 앱의 최소 버전을 뜻하지는 않습니다.

서버 분석이 허용된 프로젝트와 인터넷 연결이 필요합니다. 카메라·이미지 준비에 필요한 의존성은 SDK가 관리합니다.

## Maven Central에서 설치
프로젝트의 리포지토리에 Maven Central을 추가하고 앱 모듈에 SDK를 추가합니다.

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.aiforpet-ttcare:scansdk-lib:3.2.0")
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
| `enableMultiMode` | Boolean | 아니오 | 최대 4장을 수집하고 JSON 배열을 반환합니다. 기본값: `false` |

`guideUrl`을 전달하지 않으면 SDK 3.2.0이 언어와 부위에 맞는 기본 가이드를 선택합니다. 이 샘플은 기본 가이드를 사용합니다.

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
bundle.putBoolean("enablesQuestionnaire", true);
bundle.putBoolean("enableResultView", true);
bundle.putBoolean("enablePdfShare", true);
bundle.putBoolean("enableMultiMode", false);

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

## 결과 파악하기
SDK는 `scanLauncher`를 통해 서버의 최종 결과를 JSON 문자열로 반환합니다. 단건은 JSON 객체이고 `enableMultiMode=true`이면 한 장만 제출해도 최상위가 촬영 순서의 JSON 배열입니다. 각 항목의 상태를 개별 확인하세요.

### 결과 상태(status)
* `NORMAL`: 이상 징후 없음
* `CAUTION`: 가벼운 이상 징후 감지됨, 주의 깊게 관찰 필요
* `WARNING`: 다수 또는 강한 이상 징후 감지됨, 후속 조치 권장

최종 상태는 서버 응답을 따릅니다. 문진 답변이 결과에 포함될 수 있지만 호스트 앱에서 상태를 다시 계산하거나 덮어쓰지 마세요.

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
SDK에는 소비자용 Proguard 규칙이 포함되어 있습니다. 릴리즈 빌드를 검증하고 해당 빌드에서만 문제가 생기면 TTcare 팀에 문의해 주세요.

## 연동 흐름 요약
1. `scanLauncher` 등록
2. 요청용 `Bundle` 데이터 구성
3. 알맞은 카메라 액티비티 실행
4. 분석이 끝난 후 결과 JSON 수신
5. 필요한 경우 반환된 이미지 URL을 바로 렌더링
6. 결과 처리 완료 후 `LibraryClass.clearSdkScanCache()` 호출

## 이전 Android SDK 버전에서 이전하는 경우
* 이미지 준비는 기기에서 수행하지만 최종 분석에는 서버 연결과 적절한 SDK 키가 필요합니다.
* 결과 이미지 참조는 로컬 파일 경로가 아닌 HTTPS URL로 반환됩니다.
* SDK 최소 API는 23이며 샘플 앱 설정은 여전히 `minSdk 28`입니다.
* SDK 인증은 필수 `sdkKey` Bundle 값으로 전달합니다.
* `enableMultiMode=true`이면 JSON 배열을 처리하세요. Bundle 필드와 공개 결과 JSON 키는 3.1.0과 동일합니다.

## 고객 지원
품종 목록, 증상 사전 데이터, 기본 가이드 URL, 기타 연동 지원이 필요하시면 TTcare 팀에 문의해 주세요.
