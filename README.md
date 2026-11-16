🌐 [English](README.md) · [한국어](README.ko.md) · [日本語](README.ja.md)

Use this sample to integrate TTcare Scan SDK 3.2.0 into an Android app. The SDK prepares images on the device and returns the server's analysis result. The SDK supports API 23+, while this sample app currently targets API 28+.

## 3.2.0 integration highlights
* Camera preview appears while the SDK finishes preparing; capture remains disabled until it is ready
* Invalid SDK keys cancel the SDK activity instead of crashing the host app; pass the issued key unchanged
* The default camera guide uses `https://guide.ai4pet.com/sdk/guide/{locale}/{pet}/{part}`; a supplied `guideUrl` takes priority
* Optional `enableMultiMode` collects up to four photos in server-analysis mode and returns a JSON array, even when only one photo is submitted
* Result image references are returned as HTTPS URLs, so host apps can load them directly
* `LibraryClass.clearSdkScanCache()` can clean up generated media
* Supported guide languages are Korean, English, Japanese, Portuguese (Brazil), Turkish, and Thai; unsupported locales fall back to English
* `enablePdfShare` controls the PDF share button on the built-in result screen

## Prerequisites
* SDK minimum: Android 6.0+ (API 23); this sample app currently has `minSdk 28`
* Java 17 bytecode support; verify your host app build toolchain with the SDK
* AGP 9 host apps must set `android.uniquePackageNames=false` in the root `gradle.properties`; AGP 8.2.2 is not supported
* Supported ABIs: `armeabi-v7a`, `arm64-v8a`

This sample is built with Gradle 9.1, AGP 8.13.2, Kotlin 2.3.20, and JDK 17. These are the sample's verified build settings, not a blanket minimum for every host app.

An internet connection and a project configured for server analysis are required. The SDK handles its own camera and image-preparation dependencies.

## Install from Maven Central
Add Maven Central to your project repositories and pull the SDK into your app module.

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.aiforpet-ttcare:scansdk-lib:3.2.0")
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
| `petName` | String | No | Pet name used in the built-in result screen and PDF report text |
| `petBirthday` | String | No | Pet birthday. `yyyy-MM-dd` recommended |
| `petBreedName` | String | No | Breed name. Use the partner breed list when your service maps breeds |
| `petGender` | String | No | `M`, `F`, `MC`, or `FC` |
| `guideUrl` | String | No | Web guide URL shown inside the SDK camera guide view |
| `isAnalysisEnabled` | Boolean/String | No | Enables partner analytics wrapper mode when configured |
| `isFlashMode` | Boolean | No | Starts camera flash enabled. Default: `false` |
| `enablesQuestionnaire` | Boolean | No | Uses SDK questionnaire flow. Default: `true` |
| `enableResultView` | Boolean | No | Shows SDK built-in result screen. Default: `true` |
| `enablePdfShare` | Boolean | No | Shows PDF share button on the built-in result screen. Meaningful when `enableResultView=true` |
| `enableMultiMode` | Boolean | No | Collects up to four photos and returns a JSON array. Default: `false` |

When `guideUrl` is omitted, SDK 3.2.0 chooses its own localized guide URL. This sample deliberately relies on that default.

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
The SDK returns the server's final result as a JSON string through `scanLauncher`. A single scan returns a JSON object. With `enableMultiMode=true`, the root is a capture-order JSON array even if only one photo was submitted; check each item's status separately.

### Result status
* `NORMAL`: no anomalies detected
* `CAUTION`: mild anomalies detected, observe closely
* `WARNING`: multiple or stronger anomalies detected, follow-up recommended

Use the status returned by the server. Questionnaire answers may be present, but the host app must not recalculate or override the final status from them.

### Display result images
Heatmaps and crop images are returned as HTTPS URLs. Host apps do not need to read SDK-generated local files; load the returned URL directly with your image loading library.

```java
String imageUrl = symptom.optString("heatmapUrl");

Picasso.get()
    .load(imageUrl)
    .into(imageView);
```

### Server-side payload notes
If your integration also receives SDK upload payloads on a partner server, use `symptom_images[]` as the source of symptom crop and heatmap image URLs. The payload root no longer includes representative `crop` or `heatmap` fields.

`inferences[]` keeps the public result label and score used by the backend pipeline and can include `pred_values` for model output values. This field is intended for server-side processing and is not included in the app-facing result JSON.

### PDF report sharing
When `enablePdfShare=true` and `enableResultView=true`, the built-in result screen shows a PDF share button. The PDF report uses localized resources for summary text and can use returned heatmap references such as `heatmapUrl`, `heatmapImageUrl`, or `heatmapPath` when preparing the report.

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
| `userId` | String | User identifier passed in the request |
| `petId` | String | Pet identifier passed in the request, when provided |
| `subPart` | String | Detailed position code, such as `EYEL`, `EAR`, or `TCENTER`, when available |

The public result JSON no longer includes a `metadata` object. Internal model fields such as `score`, `predValues`, `pred_values`, and `modelName` are not returned to partner apps.

#### `questions[]`

| Field | Type | Description |
| --- | --- | --- |
| `text` | String | Questionnaire question text |
| `select` | String | User answer. `Y` or `N` |

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
  "userId": "your_user_id",
  "petId": "your_pet_id_123",
  "subPart": "EYEL",
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
The SDK ships with consumer Proguard rules. Verify a release build and contact TTcare support if a problem occurs only in that build.

## Integration flow summary
1. Register `scanLauncher`
2. Build the request `Bundle`
3. Launch the matching camera activity
4. Receive result JSON after analysis
5. Render returned image URLs directly when needed
6. Call `LibraryClass.clearSdkScanCache()` after you finish handling the result

## Migrating from earlier Android SDK versions
* Image preparation remains on-device, while final analysis requires the server and a suitable SDK key
* Result image references are HTTPS URLs, not local file paths
* The SDK minimum is API 23; the sample app still sets `minSdk 28`
* SDK authentication is passed through the required `sdkKey` Bundle value
* Handle a JSON array when `enableMultiMode=true`; the Bundle fields and public result JSON keys are unchanged from 3.1.0

## Support
For breed lists, symptom dictionaries, default guide URLs, or integration support, contact the TTcare team.
