🌐 [English](README.md) · [한국어](README.ko.md) · [日本語](README.ja.md)

本ガイドを利用して、TTcare Scan SDK v2.x を Android アプリに統合してください。このリリースでは、分析を完全にデバイス上(On-device)で実行し、生成された結果画像は HTTPS URL で返されます。API 28 以上が必要です。

## v2.x の新機能
* Target SDK が API 36 に移行されました
* ONNX Runtime が API 28+ を必要とするため、Minimum SDK が API 28 に移行されました
* AI 診断において、サーバー通信(round-trip)に依存しなくなりました
* 結果画像の参照は HTTPS URL として返されるため、ホストアプリで直接ロードできます
* 生成されたメディアのキャッシュをクリアするための `LibraryClass.clearSdkScanCache()` が追加されました
* 結果画面の対応言語は英語、韓国語、日本語、イタリア語、スウェーデン語、タイ語です
* `enablePdfShare` オプションで内蔵結果画面の PDF 共有ボタンを制御できます

## 動作要件
* Android 9.0+ (minSdk 28)
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
    implementation("io.github.aiforpet-ttcare:scansdk-lib:2.1.1")
}
```
SDK はすでに CAMERA および INTERNET 権限を宣言しているため、マニフェストに自動的にマージされます。

## SDK key の設定

> [!IMPORTANT]
> **デモアプリの SDK key に関するご案内**
> このサンプルプロジェクトでは `Enter your issued SDK key` というプレースホルダー値を使用しています。
> サンプルアプリを実行する前に、プロジェクト用に発行された SDK key に置き換えてください。

TTcare はパートナープロジェクトごとに SDK key を発行します。この値は secret として扱い、アプリのセキュリティポリシーに従って管理してください。

**セキュリティルール**
実際の本番用 SDK key を公開ソースコードに露出しないでください。安全に保存または実行時に取得し、SDK Activity 起動時に発行済み SDK key 文字列を `sdkKey` として渡してください。



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

| 名前 | 型 | 必須 | 説明 |
| --- | --- | --- | --- |
| `userId` | String | はい | パートナーサービスのユーザー識別子 |
| `petType` | String | はい | `DOG` または `CAT` |
| `sdkKey` | String | はい | プロジェクト用に発行された SDK key |
| `partType` | String | 皮膚スキャン時は必須 | `EAR`, `BELLY`, `FOOT` のいずれか。`SkinCameraActivity` 起動時に必須 |
| `petId` | String | いいえ | パートナーサービスのペット識別子。指定すると結果データに含まれます |
| `petBirthday` | String | いいえ | ペットの誕生日。`yyyy-MM-dd` 推奨 |
| `petBreedName` | String | いいえ | 品種名。サービス側で品種をマッピングする場合はパートナー用 Breed List の利用を推奨 |
| `petGender` | String | いいえ | `M`, `F`, `MC`, `FC` |
| `petAdditionalInfo` | String | いいえ | 追加メタデータ。JSON 文字列推奨 |
| `guideUrl` | String | いいえ | SDK カメラガイド WebView に表示する URL |
| `isAnalysisEnabled` | Boolean/String | いいえ | 設定済みの場合、パートナー分析ラッパーモードを有効化 |
| `isFlashMode` | Boolean | いいえ | カメラフラッシュを初期状態で有効にするか。デフォルト: `false` |
| `enablesQuestionnaire` | Boolean | いいえ | SDK 問診フローを使用するか。デフォルト: `true` |
| `enableResultView` | Boolean | いいえ | SDK 内蔵結果画面を表示するか。デフォルト: `true` |
| `enablePdfShare` | Boolean | いいえ | 内蔵結果画面に PDF 共有ボタンを表示するか。`enableResultView=true` の場合に意味があります |

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
bundle.putString("guideUrl", "https://resource-core.aiforpetcdn.com/sdk/guide/ja/dog/eye.html");
bundle.putString("petAdditionalInfo", "{\"info\":\"additional info\"}");
bundle.putBoolean("enablesQuestionnaire", true);
bundle.putBoolean("enableResultView", true);
bundle.putBoolean("enablePdfShare", true);

intent.putExtras(bundle);
scanLauncher.launch(intent);
```

### 皮膚スキャンの注意事項
皮膚のスキャンは複数のサブパーツをサポートしています。`SkinCameraActivity` を起動する場合は、`partType` を必須フィールドとして渡してください。

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

| 問診回答 | AI スキャン結果 | 最終ステータス |
| --- | --- | --- |
| 症状あり | 異常兆候あり | `WARNING` |
| 症状なし | 異常兆候あり | `CAUTION` |
| 症状なし | 異常兆候なし | `NORMAL` |
| 症状あり | 異常兆候なし | `CAUTION` |

### 結果画像の表示
ヒートマップとクロップ画像は HTTPS URL として返されます。ホストアプリは SDK が生成したローカルファイルを直接読む必要はなく、返された URL を画像読み込みライブラリでそのままロードできます。

```java
String imageUrl = symptom.optString("heatmapUrl");

Picasso.get()
    .load(imageUrl)
    .into(imageView);
```

### 結果スキーマの概要

#### トップレベルフィールド

| フィールド | 型 | 説明 |
| --- | --- | --- |
| `status` | String | SDK 処理ステータス。通常は `SUCCESS` |
| `petType` | String | `DOG` または `CAT` |
| `part` | String | スキャン部位。例: `EYE`, `SKIN`, `TOOTH` |
| `createdAt` | Long | UTC ミリ秒の分析生成時刻 |
| `questions` | Array | 問診使用時の回答 |
| `response` | Object | UI 表示用の結果サマリー |

スキャン種別や SDK 設定によって、`userId`, `petId`, `petBirthday`, `petBreedName`, `petGender`, `petAdditionalInfo`、詳細位置などが追加で含まれる場合があります。

#### `questions[]`

| フィールド | 型 | 説明 |
| --- | --- | --- |
| `text` | String | 問診質問テキスト |
| `select` | String | ユーザー回答。`Y` または `N` |

#### `metadata`

| フィールド | 型 | 説明 |
| --- | --- | --- |
| `petBirthday` | String | リクエストで渡したペットの誕生日 |
| `petBreedName` | String | リクエストで渡した品種名 |
| `petGender` | String | リクエストで渡した性別 |
| `petAdditionalInfo` | Object | JSON 文字列を渡した場合にパースされた追加情報 |

#### `response`

| フィールド | 型 | 説明 |
| --- | --- | --- |
| `status` | String | 最終判定ステータス: `NORMAL`, `CAUTION`, `WARNING` |
| `title` | String | ユーザー表示用の結果タイトル |
| `analyzedDate` | String | フォーマット済み分析日時 |
| `description` | Object | 結果レベルの案内文 |
| `symptoms` | Array | 検出された症状詳細 |

#### `response.description`

| フィールド | 型 | 説明 |
| --- | --- | --- |
| `title` | String | 案内タイトル |
| `contents` | Array | 案内本文のリスト |

#### `response.symptoms[]`

| フィールド | 型 | 説明 |
| --- | --- | --- |
| `code` | String | 異常兆候コード。例: `hyperemia`, `epiphora` |
| `name` | String | ローカライズされた異常兆候名 |
| `abnormLevel` | Int | SDK が算出した異常レベル |
| `score` | Double | 症状に対するモデルスコア |
| `cropImageUrl` | String | クロップ画像の HTTPS URL |
| `heatmapUrl` | String | ヒートマップ画像の HTTPS URL |
| `isAbnormal` | Boolean | この症状が異常と判定されたか |
| `resultLabel` | String | `normal`, `abnormal` などの結果ラベル |
| `details` | Array | 症状説明ブロック |

#### `response.symptoms[].details[]`

| Key | 説明 |
| --- | --- |
| `what_it_is` | 検出された症状の一般説明 |
| `related_clinical_conditions` | パートナー向けの関連疾患および要因 |
| `possible_causes` | 飼い主向けの考えられる原因 |
| `what_you_can_do` | ホームケアおよび観察ガイド |

#### ポジションコード

| コード | スキャン種別 | 説明 |
| --- | --- | --- |
| `EYER` | 目 | 右目 |
| `EYEL` | 目 | 左目 |
| `EAR` | 皮膚 | 耳 |
| `BELLY` | 皮膚 | 腹部 |
| `FOOT` | 皮膚 | 足 |
| `TCENTER` | 歯 | 前歯 |
| `TRIGHT` | 歯 | 右側の歯 |
| `TLEFT` | 歯 | 左側の歯 |

### サンプル結果 JSON

```json
{
  "status": "SUCCESS",
  "petType": "DOG",
  "part": "EYE",
  "createdAt": 1781485248231,
  "questions": [
    { "text": "瞳孔の大きさが左右で違いますか？", "select": "N" }
  ],
  "response": {
    "status": "WARNING",
    "title": "できるだけ早く動物病院への相談をおすすめします。",
    "analyzedDate": "2026. 06. 15 10:00",
    "description": {
      "title": "動物病院への相談が必要な場合",
      "contents": [
        "目を細める、まばたきが改善しない場合",
        "充血、腫れ、分泌物が続く、または悪化する場合"
      ]
    },
    "symptoms": [
      {
        "code": "opacity",
        "abnormLevel": 1,
        "score": 0.53,
        "cropImageUrl": "https://cdn-results.ai4pet.com/.../diagnosis_crop",
        "name": "混濁",
        "isAbnormal": true,
        "resultLabel": "abnormal",
        "heatmapUrl": "https://cdn-results.ai4pet.com/.../diagnosis_heatmap",
        "details": [
          {
            "key": "what_it_is",
            "title": "この症状は何ですか",
            "contents": ["目の表面が白く濁ったり、ぼやけて見える状態です。"]
          }
        ]
      }
    ]
  }
}
```

## キャッシュのクリア
SDK は診断中に一時ファイルを生成する場合があります。結果画像の参照は HTTPS URL として返されるため、ホストアプリがクロップ画像やヒートマップのローカルファイルをコピーする必要はありません。

```java
LibraryClass.clearSdkScanCache(context);
```
返された結果 JSON の処理が完了した後に、このメソッドを呼び出してください。

| 削除対象 | 説明 |
| --- | --- |
| `ttSdk_*` フォルダ | スキャンごとの SDK 一時作業フォルダ |
| `*.png` ファイル | 分析中に生成される場合がある一時画像 |
| `dataResult`, `result` ファイル | SDK が生成した一時デバッグ/結果ファイル |

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

この連携を使用する場合は、`AndroidManifest.xml` に作成した `Application` クラスを登録してください。

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
5. 必要に応じて返された画像 URL を直接レンダリング
6. 結果処理完了後、`LibraryClass.clearSdkScanCache()` を呼び出す

## v1.7.x からの移行に関する注意点
* 診断がデバイス上(on-device)で実行されるようになりました
* 結果画像の参照はローカルファイルパスではなく HTTPS URL として返されます
* Minimum SDK が 28 になりました
* SDK 認証は必須の `sdkKey` Bundle 値で渡します

## サポート
品種リスト、症状辞書、デフォルトのガイド URL、または統合に関するサポートについては、TTcare チームにお問い合わせください。
