🌐 [English](README.md) · [한국어](README.ko.md) · [日本語](README.ja.md)

이 가이드를 참고하여 TTcare Scan SDK v2.0.x를 안드로이드 앱에 연동할 수 있습니다. 이번 릴리즈는 모든 분석을 기기(On-device)에서 수행하며, 생성된 미디어의 로컬 파일 경로를 반환합니다. API 24 이상이 필요합니다.

## v2.0.x의 새로운 기능
* Target SDK가 API 36으로 상향되었습니다.
* ONNX Runtime이 이제 API 24+를 요구함에 따라 Minimum SDK가 API 24로 상향되었습니다.
* AI 진단 시 더 이상 서버와의 통신(round-trip)에 의존하지 않습니다.
* 결과 이미지 URL이 원격 `https://...` URL에서 로컬 `file://...` 경로로 변경되었습니다.
* 생성된 미디어 캐시 정리를 위한 `LibraryClass.clearSdkScanCache()`가 추가되었습니다.

## 사전 요구 사항
* Android 7.0 이상 (minSdk 24)
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
    implementation("io.github.aiforpet-ttcare:scansdk-lib:2.0.3")
}
```
SDK가 CAMERA 및 INTERNET 권한을 선언하고 있으므로 자동으로 프로젝트의 Manifest에 병합됩니다.

## 인증 키 설정
TTcare는 JSON 형태의 인증 키 파일을 발급합니다. 파일은 단 한 번만 제공되므로 기밀로 다루어야 하며, 분실 시 재발급 계획을 세워야 합니다.

**보안 규칙**
인증 키를 앱 패키지 내에 평문으로 저장하지 마십시오. 암호화하거나 런타임에 안전하게 가져온 후 복호화된 JSON 문자열을 `ttConf`로 전달하십시오.

권장하는 접근 방식:
* Android Keystore를 활용한 암호화
* 앱 내 안전한 저장소를 활용한 AES 암호화
* 런타임에 서버에서 안전하게 키 가져오기

```java
String ttConf = YourSecurityManager.decryptAuthKey(context);
bundle.putString("ttConf", ttConf);
```

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
**필수 필드:**
* `userId`
* `petType` (DOG 또는 CAT)
* `ttConf`
* `petBirthday` (yyyy-MM-dd)
* `petBreedName`
* `petGender` (M, F, MC, FC)

**선택 필드:**
* `petId`
* `partType` 피부 스캔 시 (EAR, BELLY, FOOT)
* `petAdditionalInfo`
* `guideUrl`
* `isAnalysisEnabled`
* `isFlashMode`

```java
Intent intent = new Intent(this, EyeCameraActivity.class);
Bundle bundle = new Bundle();

bundle.putString("petType", "DOG");
bundle.putString("userId", "your_user_id");
bundle.putString("ttConf", decryptedJsonKey);
bundle.putString("petBirthday", "2020-03-15");
bundle.putString("petBreedName", "Maltese");
bundle.putString("petGender", "MC");

bundle.putString("petId", "your_pet_id_123");
bundle.putString("guideUrl", "https://resource-core.aiforpetcdn.com/sdk/guide/ko/dog/eye.html");
bundle.putString("petAdditionalInfo", "{\"info\":\"additional info\"}");

intent.putExtras(bundle);
scanLauncher.launch(intent);
```

### 피부 스캔 참고 사항
피부 스캔은 다양한 세부 부위를 지원합니다. 캡처 대상을 미리 알고 있다면 `partType`을 전달하세요.

```java
Intent intent = new Intent(this, SkinCameraActivity.class);
Bundle bundle = new Bundle();

bundle.putString("partType", "EAR");   // 또는 BELLY, FOOT

intent.putExtras(bundle);
scanLauncher.launch(intent);
```

## 결과 파악하기
SDK는 `scanLauncher`를 통해 최종 진단 결과가 포함된 JSON 문자열을 반환합니다.

### 결과 상태(status)
* `NORMAL`: 이상 징후 없음
* `CAUTION`: 가벼운 이상 징후 감지됨, 주의 깊게 관찰 필요
* `WARNING`: 다수 또는 강한 이상 징후 감지됨, 후속 조치 권장

문진(Questionnaire)이 활성화된 경우, 최종 상태는 스캔 결과와 문진 답변을 모두 반영합니다.

### 로컬 이미지 경로 처리
v2.0.x 버전부터 히트맵과 크롭 이미지는 원격 URL이 아닌 로컬 `file://...` 경로로 반환됩니다. 접두사를 제거하고 디스크에서 파일을 로드하세요.

```java
String imageUrl = "file:///data/user/0/.../overlay.png";

Picasso.get()
    .load(new File(imageUrl.replace("file://", "")))
    .into(imageView);
```

### 결과 스키마 개요
최상위 필드는 다음과 같습니다:
* `status`
* `petType`
* `part`
* `createdAt`
* `subPart`
* `userId`
* `questions`
* `metadata`
* `response`

#### `response`
이 응답 블록은 UI 렌더링에 최적화되어 있으며 다음을 포함합니다:
* `status`
* `title`
* `analyzedDate`
* `description`
* `symptoms`

#### `response.symptoms[]`
감지된 각 증상(symptom)은 다음을 포함할 수 있습니다:
* `code`
* `name`
* `heatmapPath`
* `cropImageUrl`
* `score`
* `details`

#### `details[]`
증상별 세부 정보에는 다음 키들이 사용됩니다:
* `what_it_is`
* `related_clinical_conditions`
* `possible_causes`
* `what_you_can_do`

#### 부위(Position) 코드
* `EYER`: 오른쪽 눈
* `EYEL`: 왼쪽 눈
* `EAR`: 귀
* `BELLY`: 복부
* `FOOT`: 발
* `TCENTER`: 앞니
* `TRIGHT`: 오른쪽 치아
* `TLEFT`: 왼쪽 치아

## 캐시 정리
SDK는 진단 과정 중 기기 저장소에 크롭 이미지, 히트맵, 임시 파일을 생성합니다. 결과를 저장하거나 업로드한 후 생성된 캐시를 정리하세요.

```java
LibraryClass.clearSdkScanCache(context);
```
이 메서드는 생성된 결과 폴더와 이미지 파일들을 삭제합니다. 정리 후에는 반환된 `file://...` 경로가 무효화되므로 필요한 파일을 모두 복사하거나 업로드한 뒤에만 이 메서드를 호출하십시오.

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
5. 결과 저장 또는 업로드
6. 생성된 미디어 사용 완료 후 `LibraryClass.clearSdkScanCache()` 호출

## v1.7.x 마이그레이션 참고
* 진단이 기기 내(on-device)에서 실행됩니다.
* 결과 이미지 참조가 원격 URL에서 로컬 파일 경로로 변경되었습니다.
* 최소 지원 버전(Minimum SDK)이 24로 상향되었습니다.
* 캐시 정리를 호스트 앱에서 직접 명시적으로 처리해야 합니다.

## 고객 지원
품종 목록, 증상 사전 데이터, 기본 가이드 URL, 기타 연동 지원이 필요하시면 TTcare 팀에 문의해 주세요.
