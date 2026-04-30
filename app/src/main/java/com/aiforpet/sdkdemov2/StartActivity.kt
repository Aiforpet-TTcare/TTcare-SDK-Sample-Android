package com.aiforpet.sdkdemov2

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

import com.aiforpet.pet.check.EyeCameraActivity
import com.aiforpet.pet.check.SkinCameraActivity
import com.aiforpet.pet.check.ToothCameraActivity
import com.aiforpet.sdkdemov2.utils.CryptoUtils
import com.google.android.material.card.MaterialCardView

import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale

class StartActivity : AppCompatActivity() {

    private lateinit var isenablesQuestionnaire: CheckBox
    private lateinit var isEnableResult: CheckBox

    private lateinit var mainView: LinearLayout

    private lateinit var dogEye: MaterialCardView
    private lateinit var dogEar: MaterialCardView
    private lateinit var dogBelly: MaterialCardView
    private lateinit var dogFoot: MaterialCardView
    private lateinit var dogTooth: MaterialCardView
    private lateinit var catEye: MaterialCardView
    private lateinit var catTooth: MaterialCardView

    private lateinit var resultView: RelativeLayout
    private lateinit var close: TextView
    private lateinit var resultTxt: TextView
    private var selectPart: String = ""
    private var petType: String = ""

    private val scanLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            // SDK(ResultActivty)에서 최종 반환된 결과 수신
            val resultData = result.data?.getStringExtra("result")
            val hasSymptoms = result.data?.getBooleanExtra("hasSymptoms", false) ?: false

            if (resultData != null) {
                try {
                    // JSON을 이쁘게 포맷팅해서 텍스트뷰에 출력
                    val json = JSONObject(resultData)
                    resultTxt.text = json.toString(4)
                } catch (e: Exception) {
                    resultTxt.text = resultData
                }
                resultView.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_start)


        val rootContainer = findViewById<View>(R.id.root_container)
        ViewCompat.setOnApplyWindowInsetsListener(rootContainer) { v, windowInsets ->
            val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // 가져온 inset 값을 뷰의 패딩으로 적용합니다.
            v.setPadding(systemBarInsets.left, 0, systemBarInsets.right, 0)

            // 리스너에게 모든 inset을 소비했다고 알립니다.
            WindowInsetsCompat.CONSUMED
        }

        init()
        eventSetting()
    }

    private fun init() {
        isenablesQuestionnaire = findViewById(R.id.isenablesQuestionnaire)
        isEnableResult = findViewById(R.id.isEnableResult)

        mainView = findViewById(R.id.mainView)
        resultView = findViewById(R.id.resultView)
        close = findViewById(R.id.close)
        resultTxt = findViewById(R.id.result_txt)

        dogEye = findViewById(R.id.dog_eye)
        dogEar = findViewById(R.id.dog_ear)
        dogBelly = findViewById(R.id.dog_belly)
        dogFoot = findViewById(R.id.dog_foot)
        dogTooth = findViewById(R.id.dog_tooth)

        catEye = findViewById(R.id.cat_eye)
        catTooth = findViewById(R.id.cat_tooth)
    }

    private fun eventSetting() {
        close.setOnClickListener { resultView.visibility = View.GONE }

        dogEye.setOnClickListener {
            selectPart = "EYE"
            petType = "DOG"
            val selectLang = getCurrentLanguageCategory()
            val guideBase = "https://resource-core.aiforpetcdn.com/sdk/guide/$selectLang/dog/"

            val intent = Intent(this@StartActivity, EyeCameraActivity::class.java)
            val bundle = Bundle().apply {
                putString("petType", "DOG") // 필수
                putString("userId", "userId") // 필수
                putString("petId", "petId") // 선택
                putString("petBirthday", "2025-01-01") // 선택
                putString("petBreedName", "MBSMIN") // 선택
                putString("petGender", "M") // 선택
                putBoolean("enablesQuestionnaire", isenablesQuestionnaire.isChecked) // 선택
                putBoolean("enableResultView", isEnableResult.isChecked) // 선택

                val petAdditionalInfo = JSONObject()
                try {
                    petAdditionalInfo.put("innerData", "innerData")
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                putString("petAdditionalInfo", petAdditionalInfo.toString()) // 선택

                putString("guideUrl", guideBase + "eye.html")
                putString("ttConf", CryptoUtils.decrypt(readAssetFile(this@StartActivity, "sdk").trim())) // 필수
            }
            intent.putExtras(bundle)
            scanLauncher.launch(intent)
        }

        dogEar.setOnClickListener {
            selectPart = "EAR"
            petType = "DOG"
            val selectLang = getCurrentLanguageCategory()
            val guideBase = "https://resource-core.aiforpetcdn.com/sdk/guide/$selectLang/dog/"

            val intent = Intent(this@StartActivity, SkinCameraActivity::class.java)
            val bundle = Bundle().apply {
                putString("petType", "DOG") // 필수
                putString("userId", "userId") // 필수
                putString("petId", "petId") // 선택
                putString("petBirthday", "2025-01-01") // 선택
                putString("petBreedName", "MBSMIN") // 선택
                putString("petGender", "M") // 선택
                putBoolean("enablesQuestionnaire", isenablesQuestionnaire.isChecked) // 선택
                putBoolean("enableResultView", isEnableResult.isChecked) // 선택

                val petAdditionalInfo = JSONObject()
                try {
                    petAdditionalInfo.put("innerData", "innerData")
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                putString("petAdditionalInfo", petAdditionalInfo.toString())
                putString("partType", "EAR") // 선택
                putString("guideUrl", guideBase + "skin.html")
                putString("ttConf", CryptoUtils.decrypt(readAssetFile(this@StartActivity, "sdk").trim())) // 필수
            }
            intent.putExtras(bundle)
            scanLauncher.launch(intent)
        }

        dogBelly.setOnClickListener {
            selectPart = "BODY"
            petType = "DOG"
            val selectLang = getCurrentLanguageCategory()
            val guideBase = "https://resource-core.aiforpetcdn.com/sdk/guide/$selectLang/dog/"

            val intent = Intent(this@StartActivity, SkinCameraActivity::class.java)
            val bundle = Bundle().apply {
                putString("petType", "DOG") // 필수
                putString("userId", "userId") // 필수
                putString("petId", "petId") // 선택
                putString("petBirthday", "2025-01-01") // 선택
                putString("petBreedName", "MBSMIN") // 선택
                putString("petGender", "M") // 선택
                putBoolean("enablesQuestionnaire", isenablesQuestionnaire.isChecked) // 선택
                putBoolean("enableResultView", isEnableResult.isChecked) // 선택

                val petAdditionalInfo = JSONObject()
                try {
                    petAdditionalInfo.put("innerData", "innerData")
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                putString("petAdditionalInfo", petAdditionalInfo.toString())
                putString("partType", "BELLY") // 선택
                putString("guideUrl", guideBase + "skin.html")
                putString("ttConf", CryptoUtils.decrypt(readAssetFile(this@StartActivity, "sdk").trim())) // 필수
            }
            intent.putExtras(bundle)
            scanLauncher.launch(intent)
        }

        dogFoot.setOnClickListener {
            selectPart = "FOOT"
            petType = "DOG"
            val selectLang = getCurrentLanguageCategory()
            val guideBase = "https://resource-core.aiforpetcdn.com/sdk/guide/$selectLang/dog/"

            val intent = Intent(this@StartActivity, SkinCameraActivity::class.java)
            val bundle = Bundle().apply {
                putString("petType", "DOG") // 필수
                putString("userId", "userId") // 필수
                putString("petId", "petId") // 선택
                putString("petBirthday", "2025-01-01") // 선택
                putString("petBreedName", "MBSMIN") // 선택
                putString("petGender", "M") // 선택

                val petAdditionalInfo = JSONObject()
                try {
                    petAdditionalInfo.put("innerData", "innerData")
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                putString("petAdditionalInfo", petAdditionalInfo.toString())
                putString("partType", "FOOT") // 선택
                putString("guideUrl", guideBase + "skin.html")
                putBoolean("enablesQuestionnaire", isenablesQuestionnaire.isChecked) // 선택
                putBoolean("enableResultView", isEnableResult.isChecked) // 선택
                putString("ttConf", CryptoUtils.decrypt(readAssetFile(this@StartActivity, "sdk").trim())) // 필수
            }
            intent.putExtras(bundle)
            scanLauncher.launch(intent)
        }

        dogTooth.setOnClickListener {
            selectPart = "TEETH"
            petType = "DOG"
            val selectLang = getCurrentLanguageCategory()
            val guideBase = "https://resource-core.aiforpetcdn.com/sdk/guide/$selectLang/dog/"

            val intent = Intent(this@StartActivity, ToothCameraActivity::class.java)
            val bundle = Bundle().apply {
                putString("petType", "DOG") // 필수
                putString("userId", "userId") // 필수
                putString("petId", "petId") // 선택
                putString("petBirthday", "2025-01-01") // 선택
                putString("petBreedName", "MBSMIN") // 선택
                putString("petGender", "M") // 선택
                putBoolean("enablesQuestionnaire", isenablesQuestionnaire.isChecked) // 선택
                putBoolean("enableResultView", isEnableResult.isChecked) // 선택

                val petAdditionalInfo = JSONObject()
                try {
                    petAdditionalInfo.put("innerData", "innerData")
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                putString("petAdditionalInfo", petAdditionalInfo.toString())
                putString("guideUrl", guideBase + "tooth.html")
                putString("ttConf", CryptoUtils.decrypt(readAssetFile(this@StartActivity, "sdk").trim())) // 필수
            }
            intent.putExtras(bundle)
            scanLauncher.launch(intent)
        }

        catEye.setOnClickListener {
            selectPart = "EYE"
            petType = "CAT"
            val selectLang = getCurrentLanguageCategory()
            val guideBase = "https://resource-core.aiforpetcdn.com/sdk/guide/$selectLang/cat/"

            val intent = Intent(this@StartActivity, EyeCameraActivity::class.java)
            val bundle = Bundle().apply {
                putString("petType", "CAT") // 필수
                putString("userId", "userId") // 필수
                putString("petId", "petId") // 선택
                putString("petBirthday", "2025-01-01") // 선택
                putString("petBreedName", "MBSMIN") // 선택
                putString("petGender", "M") // 선택
                putBoolean("enablesQuestionnaire", isenablesQuestionnaire.isChecked) // 선택
                putBoolean("enableResultView", isEnableResult.isChecked) // 선택

                val petAdditionalInfo = JSONObject()
                try {
                    petAdditionalInfo.put("innerData", "innerData")
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                putString("petAdditionalInfo", petAdditionalInfo.toString())
                putString("guideUrl", guideBase + "eye.html")
                putString("ttConf", CryptoUtils.decrypt(readAssetFile(this@StartActivity, "sdk").trim())) // 필수
            }
            intent.putExtras(bundle)
            scanLauncher.launch(intent)
        }

        catTooth.setOnClickListener {
            selectPart = "TEETH"
            petType = "CAT"
            val selectLang = getCurrentLanguageCategory()
            val guideBase = "https://resource-core.aiforpetcdn.com/sdk/guide/$selectLang/cat/"

            val intent = Intent(this@StartActivity, ToothCameraActivity::class.java)
            val bundle = Bundle().apply {
                putString("petType", "CAT") // 필수
                putString("userId", "userId") // 필수
                putBoolean("enablesQuestionnaire", isenablesQuestionnaire.isChecked) // 선택
                putBoolean("enableResultView", isEnableResult.isChecked) // 선택

                putString("petId", "petId") // 선택
                putString("petBirthday", "2025-01-01") // 선택
                putString("petBreedName", "MBSMIN") // 선택
                putString("petGender", "M") // 선택

                val petAdditionalInfo = JSONObject()
                try {
                    petAdditionalInfo.put("innerData", "innerData")
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                putString("petAdditionalInfo", petAdditionalInfo.toString())

                putString("guideUrl", guideBase + "tooth.html")
                putString("ttConf", CryptoUtils.decrypt(readAssetFile(this@StartActivity, "sdk").trim())) // 필수
            }
            intent.putExtras(bundle)
            scanLauncher.launch(intent)
        }
    }

    companion object {
        /**
         * Reads an asset file and returns its contents as a String.
         */
        fun readAssetFile(context: Context, filename: String): String {
            val builder = StringBuilder()
            try {
                context.assets.open(filename).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            builder.append(line).append('\n')
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return builder.toString()
        }

        fun getCurrentLanguageCategory(): String {
            val currentLocale = Locale.getDefault()
            val languageCode = currentLocale.language // 예: "ko", "ja", "en"

            return when (languageCode) {
                "ko" -> "ko"
                "ja" -> "ja"
                else -> "en" // 한국어나 일본어가 아니면 '글로벌'로 분류
            }
        }
    }
}
