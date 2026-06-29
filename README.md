🌐 [English](README.md) · [한국어](README.ko.md) · [日本語](README.ja.md)

Use this guide to integrate TTcare Scan SDK v2.x into your Android app. This release line moves inference fully on-device, returns HTTPS URLs for generated result images, and requires API 28 or later.

## What's new in v2.x
* v2.1.2 includes bug fixes and stability improvements for the Android scan flow and built-in result experience
* Target SDK moved to API 36
* Minimum SDK moved to API 28 because ONNX Runtime now requires API 28+
* AI diagnosis no longer depends on a server round-trip
* Result image references are returned as HTTPS URLs, so host apps can load them directly
* Added `LibraryClass.clearSdkScanCache()` for generated media cleanup
* Supported result screen languages include English, Korean, Japanese, Italian, Swedish, and Thai
* `enablePdfShare` controls the PDF share button on the built-in result screen

## Prerequisites
* Android 9.0+ (minSdk 28)
* Target SDK 36 recommended
* Java 17 recommended
* Supported ABIs: `armeabi-v7a`, `arm64-v8a`

The SDK bundles its own runtime dependencies, including CameraX, OkHttp, LiteRT, and ONNX Runtime. No extra third-party dependency setup is required.

## Install from Maven Central
Add Maven Central to your project repositories and pull the SDK into your app module.

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.aiforpet-ttcare:scansdk-lib:2.1.2")
}
```
The SDK already declares CAMERA and INTERNET permissions, so they are merged into your manifest automatically.

## Set up the SDK key

> [!IMPORTANT]
> **Demo App SDK Key Notice**
> This sample project uses the placeholder value `Enter your issued SDK key`.
> Replace it with the SDK key issued for your project before running the sample app.

TTcare issues an SDK key for each partner project. Treat it as a secret and manage it according to your app security policy.

**Security rule**
Do not expose a real production SDK key in public source code. Store or fetch it securely, then pass the issued SDK key string as `sdkKey` when launching the SDK activity.



## Launch the camera
Launching the scan flow follows three steps:
1. Register an ActivityResultLauncher
2. Select the correct camera activity
3. Pass a Bundle and launch

### 1. Register the result callback
The SDK returns the final result JSON through the launcher callback after the user closes the built-in result screen.

```java
private final ActivityResultLauncher<Intent> scanLauncher =
    registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                String resultData = result.getData().getStringExtra("result");
                Log.d("ScanSDK", "Scan complete! Result: " + resultData);

                // Save or upload the result before clearing cache.
            }
        }
    );
```

### 2. Choose the camera activity
* Eye: `EyeCameraActivity`
* Skin (ear / belly / foot): `SkinCameraActivity`
* Teeth: `ToothCameraActivity`

### 3. Pass request parameters

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `userId` | String | Yes | User identifier from your service |
| `petType` | String | Yes | `DOG` or `CAT` |
| `sdkKey` | String | Yes | SDK key issued for your project |
| `partType` | String | Required for skin scans | `EAR`, `BELLY`, or `FOOT`. Required when launching `SkinCameraActivity` |
| `petId` | String | No | Pet identifier from your service. Returned in result data when provided |
| `petBirthday` | String | No | Pet birthday. `yyyy-MM-dd` recommended |
| `petBreedName` | String | No | Breed name. Use the partner breed list when your service maps breeds |
| `petGender` | String | No | `M`, `F`, `MC`, or `FC` |
| `petAdditionalInfo` | String | No | Additional metadata. JSON string recommended |
| `guideUrl` | String | No | Web guide URL shown inside the SDK camera guide view |
| `isAnalysisEnabled` | Boolean/String | No | Enables partner analytics wrapper mode when configured |
| `isFlashMode` | Boolean | No | Starts camera flash enabled. Default: `false` |
| `enablesQuestionnaire` | Boolean | No | Uses SDK questionnaire flow. Default: `true` |
| `enableResultView` | Boolean | No | Shows SDK built-in result screen. Default: `true` |
| `enablePdfShare` | Boolean | No | Shows PDF share button on the built-in result screen. Meaningful when `enableResultView=true` |

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
bundle.putString("guideUrl", "https://resource-core.aiforpetcdn.com/sdk/guide/en/dog/eye.html");
bundle.putString("petAdditionalInfo", "{\"info\":\"additional info\"}");
bundle.putBoolean("enablesQuestionnaire", true);
bundle.putBoolean("enableResultView", true);
bundle.putBoolean("enablePdfShare", true);

intent.putExtras(bundle);
scanLauncher.launch(intent);
```

### Skin scan note
Skin scans support multiple sub-parts. When launching `SkinCameraActivity`, pass `partType` as a required field.

```java
Intent intent = new Intent(this, SkinCameraActivity.class);
Bundle bundle = new Bundle();

bundle.putString("partType", "EAR");   // or BELLY, FOOT

intent.putExtras(bundle);
scanLauncher.launch(intent);
```

## Understand the result
The SDK returns a JSON string with the final diagnosis result through `scanLauncher`.

### Result status
* `NORMAL`: no anomalies detected
* `CAUTION`: mild anomalies detected, observe closely
* `WARNING`: multiple or stronger anomalies detected, follow-up recommended

When the questionnaire is enabled, the final status reflects both scan output and questionnaire answers.

| Questionnaire answer | AI scan result | Final status |
| --- | --- | --- |
| Symptoms present | Abnormal signs detected | `WARNING` |
| No symptoms | Abnormal signs detected | `CAUTION` |
| No symptoms | No abnormal signs detected | `NORMAL` |
| Symptoms present | No abnormal signs detected | `CAUTION` |

### Display result images
Heatmaps and crop images are returned as HTTPS URLs. Host apps do not need to read SDK-generated local files; load the returned URL directly with your image loading library.

```java
String imageUrl = symptom.optString("heatmapUrl");

Picasso.get()
    .load(imageUrl)
    .into(imageView);
```

### Result schema overview

#### Top-level fields

| Field | Type | Description |
| --- | --- | --- |
| `status` | String | SDK processing status. Usually `SUCCESS` |
| `petType` | String | `DOG` or `CAT` |
| `part` | String | Scan part, such as `EYE`, `SKIN`, or `TOOTH` |
| `createdAt` | Long | Analysis creation time in UTC milliseconds |
| `questions` | Array | Questionnaire answers when questionnaire is enabled |
| `response` | Object | UI-ready result summary |

Depending on scan type and SDK configuration, the result may also include host app metadata such as `userId`, `petId`, `petBirthday`, `petBreedName`, `petGender`, `petAdditionalInfo`, or detailed position values.

#### `questions[]`

| Field | Type | Description |
| --- | --- | --- |
| `text` | String | Questionnaire question text |
| `select` | String | User answer. `Y` or `N` |

#### `metadata`

| Field | Type | Description |
| --- | --- | --- |
| `petBirthday` | String | Pet birthday passed in the request |
| `petBreedName` | String | Breed name passed in the request |
| `petGender` | String | Pet gender passed in the request |
| `petAdditionalInfo` | Object | Parsed additional information when a JSON string is provided |

#### `response`

| Field | Type | Description |
| --- | --- | --- |
| `status` | String | Final result status: `NORMAL`, `CAUTION`, or `WARNING` |
| `title` | String | User-facing result title |
| `analyzedDate` | String | Formatted analysis date |
| `description` | Object | Result-level guidance text |
| `symptoms` | Array | Detected symptom details |

#### `response.description`

| Field | Type | Description |
| --- | --- | --- |
| `title` | String | Guidance title |
| `contents` | Array | Guidance body lines |

#### `response.symptoms[]`

| Field | Type | Description |
| --- | --- | --- |
| `code` | String | Symptom code, such as `hyperemia` or `epiphora` |
| `name` | String | Localized symptom name |
| `abnormLevel` | Int | Abnormality level calculated by the SDK |
| `score` | Double | Model score for the symptom |
| `cropImageUrl` | String | HTTPS URL for the crop image |
| `heatmapUrl` | String | HTTPS URL for the heatmap image |
| `isAbnormal` | Boolean | Whether the symptom is judged abnormal |
| `resultLabel` | String | Result label, such as `normal` or `abnormal` |
| `details` | Array | Symptom explanation blocks |

#### `response.symptoms[].details[]`

| Key | Description |
| --- | --- |
| `what_it_is` | General explanation of the detected sign |
| `related_clinical_conditions` | Partner-facing related clinical conditions and factors |
| `possible_causes` | Guardian-friendly possible causes |
| `what_you_can_do` | Home-care guidance and observation tips |

#### Position codes

| Code | Scan type | Description |
| --- | --- | --- |
| `EYER` | Eye | Right eye |
| `EYEL` | Eye | Left eye |
| `EAR` | Skin | Ear |
| `BELLY` | Skin | Belly |
| `FOOT` | Skin | Paw |
| `TCENTER` | Teeth | Front teeth |
| `TRIGHT` | Teeth | Right teeth |
| `TLEFT` | Teeth | Left teeth |

### Sample result JSON

```json
{
  "status": "SUCCESS",
  "petType": "DOG",
  "part": "EYE",
  "createdAt": 1781485248231,
  "questions": [
    { "text": "Are the pupil sizes different?", "select": "N" }
  ],
  "response": {
    "status": "WARNING",
    "title": "A veterinary visit is recommended as soon as possible.",
    "analyzedDate": "2026. 06. 15 10:00",
    "description": {
      "title": "When to visit a veterinary clinic",
      "contents": [
        "If squinting or blinking does not improve",
        "If redness, swelling, or discharge continues or worsens"
      ]
    },
    "symptoms": [
      {
        "code": "opacity",
        "abnormLevel": 1,
        "score": 0.53,
        "cropImageUrl": "https://cdn-results.ai4pet.com/.../diagnosis_crop",
        "name": "Opacity",
        "isAbnormal": true,
        "resultLabel": "abnormal",
        "heatmapUrl": "https://cdn-results.ai4pet.com/.../diagnosis_heatmap",
        "details": [
          {
            "key": "what_it_is",
            "title": "What is this sign?",
            "contents": ["The eye surface appears cloudy or blurred."]
          }
        ]
      }
    ]
  }
}
```

## Cache cleanup
The SDK may create temporary files during diagnosis. Result image references are returned as HTTPS URLs, so host apps do not need to copy local crop or heatmap files before cleanup.

```java
LibraryClass.clearSdkScanCache(context);
```
Call this after your app finishes handling the returned result JSON.

| Deleted target | Description |
| --- | --- |
| `ttSdk_*` folders | Per-scan temporary SDK working folders |
| `*.png` files | Temporary images generated during analysis |
| `dataResult`, `result` files | Temporary debug/result files generated by the SDK |

## Optional Mixpanel integration
If your app already uses Mixpanel, you can wire SDK internal events to your tracker.

```java
bundle.putString("isAnalysisEnabled", "true");
```
Then register your tracker in the Application class:

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

Register your `Application` class in `AndroidManifest.xml` when you use this integration.

## Proguard
The SDK ships with consumer Proguard rules, but if camera scanning crashes in release builds, add explicit keep rules:

```proguard
-keep class com.aiforpet.pet.** { *; }
-keep class com.microsoft.onnxruntime.** { *; }
-keep class com.google.ai.edge.litert.** { *; }
```

## Integration flow summary
1. Register `scanLauncher`
2. Build the request `Bundle`
3. Launch the matching camera activity
4. Receive result JSON after analysis
5. Render returned image URLs directly when needed
6. Call `LibraryClass.clearSdkScanCache()` after you finish handling the result

## Migration notes from v1.7.x
* Diagnosis now runs on-device
* Result image references are HTTPS URLs, not local file paths
* Minimum SDK is now 28
* SDK authentication is passed through the required `sdkKey` Bundle value

## Support
For breed lists, symptom dictionaries, default guide URLs, or integration support, contact the TTcare team.
