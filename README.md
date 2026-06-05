🌐 [English](README.md) · [한국어](README.ko.md) · [日本語](README.ja.md)

Use this guide to integrate TTcare Scan SDK v2.x into your Android app. This release line moves inference fully on-device, returns local file paths for generated media, and requires API 28 or later.

## What's new in v2.x
* Target SDK moved to API 36
* Minimum SDK moved to API 28 because ONNX Runtime now requires API 28+
* AI diagnosis no longer depends on a server round-trip
* Result image URLs changed from remote `https://...` URLs to local `file://...` paths
* Added `LibraryClass.clearSdkScanCache()` for generated media cleanup
* Supported result screen languages include English, Korean, Japanese, Italian, Swedish, and Thai
* Added `enablePdfShare` in v2.1.0 to control the PDF share button on the built-in result screen

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
    implementation("io.github.aiforpet-ttcare:scansdk-lib:2.1.0")
}
```
The SDK already declares CAMERA and INTERNET permissions, so they are merged into your manifest automatically.

## Set up the authentication key

> [!IMPORTANT]
> **Demo App License Key Notice**
> This sample project does NOT include an actual license key (`sdk` file) in the `assets` folder.
> To run the sample app properly, you must place your issued license key file in the `app/src/main/assets/sdk` directory.

TTcare issues an authentication key file as JSON. The file is provided only once, so treat it as a secret and plan for re-issuance if it is lost.

**Security rule**
Do not store the authentication key as plain text inside the app package. Encrypt it or fetch it securely at runtime, then pass the decrypted JSON string as `ttConf`.



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
**Required fields:**
* `userId`
* `petType` (DOG or CAT)
* `ttConf`

**Required for skin scans:**
* `partType` (EAR, BELLY, FOOT)

**Optional fields:**
* `petId`
* `petBirthday` (yyyy-MM-dd)
* `petBreedName`
* `petGender` (M, F, MC, FC)
* `petAdditionalInfo`
* `guideUrl`
* `isAnalysisEnabled`
* `isFlashMode`
* `enablesQuestionnaire`
* `enableResultView`
* `enablePdfShare`

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

### Handle local image paths
In v2.0.x, heatmaps and crops are returned as local `file://...` paths instead of remote URLs. Strip the prefix and load the file from disk.

```java
String imageUrl = "file:///data/user/0/.../overlay.png";

Picasso.get()
    .load(new File(imageUrl.replace("file://", "")))
    .into(imageView);
```

### Result schema overview
Top-level fields include:
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
The response block is optimized for UI rendering and includes:
* `status`
* `title`
* `analyzedDate`
* `description`
* `symptoms`

#### `response.symptoms[]`
Each detected symptom can include:
* `code`
* `name`
* `heatmapPath`
* `cropImageUrl`
* `score`
* `details`

#### `details[]`
Per-symptom details use the following keys:
* `what_it_is`
* `related_clinical_conditions`
* `possible_causes`
* `what_you_can_do`

#### Position codes
* `EYER`: right eye
* `EYEL`: left eye
* `EAR`: ear
* `BELLY`: belly
* `FOOT`: paw
* `TCENTER`: front teeth
* `TRIGHT`: right teeth
* `TLEFT`: left teeth

## Cache cleanup
The SDK creates cropped images, heatmaps, and intermediate files inside app storage during diagnosis. After you save or upload the result, clear the generated cache.

```java
LibraryClass.clearSdkScanCache(context);
```
This removes generated result folders and image files. Call it only after copying or uploading any file you still need, because returned `file://...` paths become invalid after cleanup.

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
5. Persist or upload the result
6. Call `LibraryClass.clearSdkScanCache()` after you finish using generated media

## Migration notes from v1.7.x
* Diagnosis now runs on-device
* Result image references are local files, not remote URLs
* Minimum SDK is now 28
* Cache cleanup is now an explicit host-app responsibility

## Support
For breed lists, symptom dictionaries, default guide URLs, or integration support, contact the TTcare team.
