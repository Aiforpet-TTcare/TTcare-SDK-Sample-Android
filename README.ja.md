🌐 [English](README.md) · [한국어](README.ko.md) · [日本語](README.ja.md)

本ガイドを利用して、TTcare Scan SDK v2.0.x を Android アプリに統合してください。このリリースでは、分析を完全にデバイス上(On-device)で実行し、生成されたメディアはローカルのファイルパスで返されます。API 24 以上が必要です。

## v2.0.x の新機能
* Target SDK が API 36 に移行されました
* ONNX Runtime が API 24+ を必要とするため、Minimum SDK が API 24 に移行されました
* AI 診断において、サーバー通信(round-trip)に依存しなくなりました
* 結果画像のURLがリモートの `https://...` からローカルの `file://...` パスに変更されました
* 生成されたメディアのキャッシュをクリアするための `LibraryClass.clearSdkScanCache()` が追加されました

## 動作要件
* Android 7.0+ (minSdk 24)
* Target SDK 36 推奨
* Java 17 推奨
* サポートされている ABI: `armeabi-v7a`, `arm64-v8a`

SDK には CameraX、OkHttp、LiteRT、ONNX Runtime などのランタイム依存関係が同梱されています。追加のサードパーティ依存関係の設定は必要ありません。

## Maven Central からのインストール
プロジェクトのリポジトリに Maven Central を追加し、アプリモジュールに SDK を追加します。

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.aiforpet-ttcare:scansdk-lib:2.0.4")
}
```
SDK はすでに CAMERA および INTERNET 権限を宣言しているため、マニフェストに自動的にマージされます。

## 認証キーの設定

> [!IMPORTANT]
> **デモアプリのライセンスキーに関するご案内**
> 本サンプルプロジェクトの `assets` フォルダには、SDK 実行用の実際のライセンスキー (`sdk` ファイル) は含まれていません。
> サンプルアプリを正常に実行するには、別途申請して発行されたライセンスキーファイルを `app/src/main/assets/sdk` パスに直接追加する必要があります。

TTcare は認証キーファイルを JSON として発行します。ファイルは一度しか提供されないため、機密情報として扱い、紛失時の再発行計画を立ててください。

**セキュリティルール**
認証キーをアプリのパッケージ内に平文で保存しないでください。暗号化するか実行時に安全に取得し、復号化された JSON 文字列を `ttConf` として渡してください。

推奨されるアプローチ:
* Android Keystore を利用した暗号化
* アプリ内の安全なストレージでの AES 暗号化
* サーバーからの実行時の安全な取得

```java
String ttConf = YourSecurityManager.decryptAuthKey(context);
bundle.putString("ttConf", ttConf);
```

## カメラの起動
スキャンのフローは次の 3 つのステップで構成されます：
1. ActivityResultLauncher の登録
2. 正しいカメラアクティビティの選択
3. Bundle データを渡して起動

### 1. 結果コールバックの登録
ユーザーが内蔵の結果画面を閉じた後、SDK はコールバックを通じて最終的な結果 JSON を返します。

```java
private final ActivityResultLauncher<Intent> scanLauncher =
    registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                String resultData = result.getData().getStringExtra("result");
                Log.d("ScanSDK", "Scan complete! Result: " + resultData);

                // キャッシュをクリアする前に結果を保存またはアップロードしてください。
            }
        }
    );
```

### 2. カメラアクティビティの選択
* 目(Eye): `EyeCameraActivity`
* 皮膚 (耳 / 腹部 / 足): `SkinCameraActivity`
* 歯(Teeth): `ToothCameraActivity`

### 3. リクエストパラメータの受け渡し
**必須フィールド:**
* `userId`
* `petType` (DOG または CAT)
* `ttConf`
* `petBirthday` (yyyy-MM-dd)
* `petBreedName`
* `petGender` (M, F, MC, FC)

**オプションのフィールド:**
* `petId`
* `partType` (皮膚スキャンの場合: EAR, BELLY, FOOT)
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
bundle.putString("guideUrl", "https://resource-core.aiforpetcdn.com/sdk/guide/ja/dog/eye.html");
bundle.putString("petAdditionalInfo", "{\"info\":\"additional info\"}");

intent.putExtras(bundle);
scanLauncher.launch(intent);
```

### 皮膚スキャンの注意事項
皮膚のスキャンは複数のサブパーツをサポートしています。撮影対象がわかっている場合は `partType` を渡してください。

```java
Intent intent = new Intent(this, SkinCameraActivity.class);
Bundle bundle = new Bundle();

bundle.putString("partType", "EAR");   // または BELLY, FOOT

intent.putExtras(bundle);
scanLauncher.launch(intent);
```

## 結果の理解
SDK は `scanLauncher` を通じて、最終的な診断結果を含む JSON 文字列を返します。

### 結果ステータス(status)
* `NORMAL`: 異常は検出されませんでした
* `CAUTION`: 軽度の異常が検出されました、注意深く観察してください
* `WARNING`: 複数の、または強い異常が検出されました、経過観察を推奨します

問診(Questionnaire)が有効な場合、最終ステータスはスキャン結果と問診の回答の両方を反映します。

### ローカル画像パスの処理
v2.0.x から、ヒートマップとクロップ画像はリモート URL ではなく、ローカルの `file://...` パスとして返されます。プレフィックスを削除し、ディスクからファイルをロードしてください。

```java
String imageUrl = "file:///data/user/0/.../overlay.png";

Picasso.get()
    .load(new File(imageUrl.replace("file://", "")))
    .into(imageView);
```

### 結果スキーマの概要
トップレベルのフィールドは次のとおりです:
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
このレスポンスブロックは UI レンダリングに最適化されており、次を含みます:
* `status`
* `title`
* `analyzedDate`
* `description`
* `symptoms`

#### `response.symptoms[]`
検出された各症状(symptom)には次が含まれる場合があります:
* `code`
* `name`
* `heatmapPath`
* `cropImageUrl`
* `score`
* `details`

#### `details[]`
症状ごとの詳細には次のキーが使用されます:
* `what_it_is`
* `related_clinical_conditions`
* `possible_causes`
* `what_you_can_do`

#### ポジションコード
* `EYER`: 右目
* `EYEL`: 左目
* `EAR`: 耳
* `BELLY`: 腹部
* `FOOT`: 足
* `TCENTER`: 前歯
* `TRIGHT`: 右側の歯
* `TLEFT`: 左側の歯

## キャッシュのクリア
SDK は診断中にデバイスのストレージ内にクロップ画像、ヒートマップ、一時ファイルを生成します。結果を保存またはアップロードした後、生成されたキャッシュをクリアしてください。

```java
LibraryClass.clearSdkScanCache(context);
```
これにより、生成された結果フォルダと画像ファイルが削除されます。クリア後は返された `file://...` パスが無効になるため、必要なファイルをコピーまたはアップロードした後にのみ、このメソッドを呼び出してください。

## 任意の Mixpanel 統合
アプリで既に Mixpanel を使用している場合は、SDK の内部イベントをトラッカーに連携できます。

```java
bundle.putString("isAnalysisEnabled", "true");
```
次に、Application クラスにトラッカーを登録します:

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
SDK には利用者向けの Proguard ルールが付属していますが、リリースビルドでカメラスキャンがクラッシュする場合は、以下のように明示的に keep ルールを追加してください:

```proguard
-keep class com.aiforpet.pet.** { *; }
-keep class com.microsoft.onnxruntime.** { *; }
-keep class com.google.ai.edge.litert.** { *; }
```

## 統合フローの要約
1. `scanLauncher` の登録
2. リクエスト用の `Bundle` データの構築
3. 適切なカメラアクティビティの起動
4. 分析後、結果 JSON を受信
5. 結果の保存またはアップロード
6. 生成されたメディアの使用終了後、`LibraryClass.clearSdkScanCache()` を呼び出す

## v1.7.x からの移行に関する注意点
* 診断がデバイス上(on-device)で実行されるようになりました
* 結果画像の参照がリモート URL からローカルファイルパスに変更されました
* Minimum SDK が 24 になりました
* キャッシュのクリアはホストアプリ側で明示的に行う必要があります

## サポート
品種リスト、症状辞書、デフォルトのガイド URL、または統合に関するサポートについては、TTcare チームにお問い合わせください。
