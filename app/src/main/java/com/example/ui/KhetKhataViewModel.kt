package com.example.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.advisor.AdvisorMessage
import com.example.advisor.KisanMitraAdvisor
import com.example.audio.SpeechInputManager
import com.example.audio.TtsManager
import com.example.data.AppDatabase
import com.example.data.CropEntryEntity
import com.example.data.KhetKhataRepository
import com.example.model.CategoryItem
import com.example.model.CropItem
import com.example.model.Language
import com.example.model.NumberToWords
import com.example.model.VoiceParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenState {
    LANG,
    HOME,
    CATEGORY,
    AMOUNT,
    PROFIT
}

data class UiState(
    val currentScreen: ScreenState = ScreenState.LANG,
    val language: Language = Language.MR,
    val hasTaught: Boolean = false,
    val selectedCrop: CropItem = CropItem.ONION,
    val selectedCategory: CategoryItem = CategoryItem.SEED,
    val amountString: String = "0",
    val lastSavedId: Long? = null,
    val profitFilterCrop: CropItem? = null,
    val isHighAmountModalOpen: Boolean = false,
    val isFamilyModalOpen: Boolean = false,
    val isAdvisorModalOpen: Boolean = false,
    val isHowToUseModalOpen: Boolean = false,
    val isQuintalModalOpen: Boolean = false,
    val quintalTargetCrop: CropItem = CropItem.ONION,
    val quintalInputString: String = "0",
    val cropQuintalsMap: Map<String, Long> = emptyMap(),
    val showSaveCelebration: Boolean = false,
    val isListening: Boolean = false,
    val isOnline: Boolean = true,
    val isSaving: Boolean = false,
    val isResetting: Boolean = false,
    val userHasInteracted: Boolean = false,
    val advisorChat: List<AdvisorMessage> = emptyList(),
    val statusMessage: String? = null
)

class KhetKhataViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("khetkhata_prefs", Context.MODE_PRIVATE)
    private val repository: KhetKhataRepository

    val ttsManager = TtsManager(application)
    var speechInputManager: SpeechInputManager? = null

    private val _uiState = MutableStateFlow(
        UiState(
            language = Language.fromCode(prefs.getString("selected_lang", null)),
            hasTaught = prefs.getBoolean("has_taught", false),
            currentScreen = if (prefs.contains("selected_lang")) ScreenState.HOME else ScreenState.LANG,
            cropQuintalsMap = loadSavedQuintalsMap()
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val entries: StateFlow<List<CropEntryEntity>>

    init {
        val db = AppDatabase.getInstance(application)
        repository = KhetKhataRepository(db.cropEntryDao())
        entries = repository.allEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

        speechInputManager = SpeechInputManager(
            context = application,
            onResult = { transcript -> onVoiceTranscriptReceived(transcript) },
            onStateChange = { listening ->
                _uiState.value = _uiState.value.copy(isListening = listening)
            }
        )

        monitorConnectivity(application)
    }

    private fun loadSavedQuintalsMap(): Map<String, Long> {
        val map = mutableMapOf<String, Long>()
        for (crop in CropItem.entries) {
            val q = prefs.getLong("quintals_${crop.key}", 0L)
            if (q > 0L) {
                map[crop.key] = q
            }
        }
        return map
    }

    private fun monitorConnectivity(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val isCurrentlyOnline = cm?.activeNetworkInfo?.isConnected == true
        _uiState.value = _uiState.value.copy(isOnline = isCurrentlyOnline)

        cm?.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _uiState.value = _uiState.value.copy(isOnline = true)
                }
                override fun onLost(network: Network) {
                    _uiState.value = _uiState.value.copy(isOnline = false)
                }
            }
        )
    }

    fun onUserGesture() {
        if (!_uiState.value.userHasInteracted) {
            _uiState.value = _uiState.value.copy(userHasInteracted = true)
        }
    }

    fun selectLanguage(lang: Language) {
        onUserGesture()
        prefs.edit().putString("selected_lang", lang.code).apply()
        _uiState.value = _uiState.value.copy(
            language = lang,
            currentScreen = ScreenState.HOME
        )
        speakCurrentInstruction()
    }

    fun navigateTo(screen: ScreenState, profitFilter: CropItem? = null) {
        onUserGesture()
        _uiState.value = _uiState.value.copy(
            currentScreen = screen,
            profitFilterCrop = profitFilter
        )
        speakCurrentInstruction()
    }

    fun selectCrop(crop: CropItem) {
        onUserGesture()
        _uiState.value = _uiState.value.copy(
            selectedCrop = crop,
            currentScreen = ScreenState.CATEGORY
        )
        speakCurrentInstruction()
    }

    fun selectCategory(category: CategoryItem) {
        onUserGesture()
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            amountString = "0",
            currentScreen = ScreenState.AMOUNT
        )
        speakCurrentInstruction()
    }

    fun onNumpadPress(key: String) {
        onUserGesture()
        val current = _uiState.value.amountString
            .filter { it.isDigit() }
            .trimStart('0')
            .ifEmpty { "0" }

        val next = when (key) {
            "C" -> "0"
            "DEL" -> {
                if (current.length > 1) current.dropLast(1) else "0"
            }
            else -> {
                val digit = key.filter { it.isDigit() }
                if (digit.isEmpty()) {
                    current
                } else if (current == "0") {
                    if (digit == "0") "0" else digit.take(7)
                } else if (current.length >= 7) {
                    current
                } else {
                    (current + digit).take(7)
                }
            }
        }
        _uiState.value = _uiState.value.copy(amountString = next)
        val logMsg = "console.log: key=$key -> amount=$next"
        println(logMsg)
        Log.d("KhetKhata", logMsg)
    }

    fun addQuickChip(chipVal: Long) {
        onUserGesture()
        val current = _uiState.value.amountString
            .filter { it.isDigit() }
            .toLongOrNull() ?: 0L
        val next = (current + chipVal).coerceAtMost(9_999_999L)
        val nextStr = next.toString()
        _uiState.value = _uiState.value.copy(amountString = nextStr)
        val logMsg = "console.log: chip=+$chipVal -> amount=$nextStr"
        println(logMsg)
        Log.d("KhetKhata", logMsg)
    }

    fun attemptSaveEntry() {
        onUserGesture()
        if (_uiState.value.isSaving) {
            val ignoreMsg = "console.log: save ignored (double-tap while saving)"
            println(ignoreMsg)
            Log.d("KhetKhata", ignoreMsg)
            return
        }

        val amt = _uiState.value.amountString
            .filter { it.isDigit() }
            .toLongOrNull() ?: 0L
        if (amt <= 0L) {
            val zeroMsg = "console.log: save ignored (amount is 0)"
            println(zeroMsg)
            Log.d("KhetKhata", zeroMsg)
            speakCurrentInstruction()
            return
        }

        if (amt > 100_000L) {
            val highMsg = "console.log: save amount=$amt > 100000 -> asking high-amount confirmation"
            println(highMsg)
            Log.d("KhetKhata", highMsg)
            _uiState.value = _uiState.value.copy(isHighAmountModalOpen = true)
            return
        }

        commitSave(amt)
    }

    fun confirmHighAmount() {
        _uiState.value = _uiState.value.copy(isHighAmountModalOpen = false)
        val amt = _uiState.value.amountString
            .filter { it.isDigit() }
            .toLongOrNull() ?: 0L
        if (amt > 0L) commitSave(amt)
    }

    fun dismissHighAmountModal() {
        _uiState.value = _uiState.value.copy(isHighAmountModalOpen = false)
    }

    private fun commitSave(amount: Long) {
        if (_uiState.value.isSaving) return
        _uiState.value = _uiState.value.copy(isSaving = true, showSaveCelebration = true)

        val crop = _uiState.value.selectedCrop
        val category = _uiState.value.selectedCategory

        val saveLog = "console.log: save crop=${crop.key} category=${category.key} isIncome=${category.isSale} amount=$amount"
        println(saveLog)
        Log.d("KhetKhata", saveLog)

        viewModelScope.launch {
            val entity = CropEntryEntity(
                crop = crop.key,
                category = category.key,
                isIncome = category.isSale,
                amount = amount
            )
            val newId = repository.insert(entity)

            if (!_uiState.value.hasTaught) {
                prefs.edit().putBoolean("has_taught", true).apply()
            }

            // Brief <=150ms checkmark celebration before showing profit card
            delay(120L)

            val existingQuintals = _uiState.value.cropQuintalsMap[crop.key] ?: 0L

            _uiState.value = _uiState.value.copy(
                isSaving = false,
                showSaveCelebration = false,
                hasTaught = true,
                lastSavedId = newId,
                profitFilterCrop = crop,
                currentScreen = ScreenState.PROFIT,
                isQuintalModalOpen = false,
                quintalTargetCrop = crop,
                quintalInputString = if (existingQuintals > 0L) existingQuintals.toString() else "0"
            )

            speakProfitOrBreakEven(crop)
        }
    }

    // Break-Even Quintal Calculator Actions
    fun openQuintalModal(crop: CropItem) {
        onUserGesture()
        val existing = _uiState.value.cropQuintalsMap[crop.key] ?: 0L
        prefs.edit().putBoolean("quintal_prompted_${crop.key}", true).apply()
        _uiState.value = _uiState.value.copy(
            isQuintalModalOpen = true,
            quintalTargetCrop = crop,
            quintalInputString = if (existing > 0L) existing.toString() else "0"
        )
        speakQuintalPrompt(crop)
    }

    fun closeQuintalModal() {
        _uiState.value = _uiState.value.copy(isQuintalModalOpen = false)
    }

    fun onQuintalNumpadPress(key: String) {
        onUserGesture()
        val current = _uiState.value.quintalInputString
            .filter { it.isDigit() }
            .trimStart('0')
            .ifEmpty { "0" }

        val next = when (key) {
            "C" -> "0"
            "DEL" -> if (current.length > 1) current.dropLast(1) else "0"
            else -> {
                val digit = key.filter { it.isDigit() }
                if (digit.isEmpty()) current
                else if (current == "0") {
                    if (digit == "0") "0" else digit.take(5)
                } else if (current.length >= 5) {
                    current
                } else {
                    (current + digit).take(5)
                }
            }
        }
        _uiState.value = _uiState.value.copy(quintalInputString = next)
        val logMsg = "console.log: quintal_key=$key -> quintals=$next"
        println(logMsg)
        Log.d("KhetKhata", logMsg)
    }

    fun addQuintalQuickChip(chipVal: Long) {
        onUserGesture()
        val current = _uiState.value.quintalInputString
            .filter { it.isDigit() }
            .toLongOrNull() ?: 0L
        val next = (current + chipVal).coerceAtMost(99_999L)
        _uiState.value = _uiState.value.copy(quintalInputString = next.toString())
        val logMsg = "console.log: quintal_chip=+$chipVal -> quintals=$next"
        println(logMsg)
        Log.d("KhetKhata", logMsg)
    }

    fun saveQuintalsForCrop() {
        onUserGesture()
        val crop = _uiState.value.quintalTargetCrop
        val qVal = _uiState.value.quintalInputString
            .filter { it.isDigit() }
            .toLongOrNull() ?: 0L
        val updatedMap = _uiState.value.cropQuintalsMap.toMutableMap()

        val logMsg = "console.log: save_quintals crop=${crop.key} quintals=$qVal"
        println(logMsg)
        Log.d("KhetKhata", logMsg)

        if (qVal > 0L) {
            prefs.edit().putLong("quintals_${crop.key}", qVal).apply()
            updatedMap[crop.key] = qVal
        } else {
            prefs.edit().remove("quintals_${crop.key}").apply()
            updatedMap.remove(crop.key)
        }

        _uiState.value = _uiState.value.copy(
            cropQuintalsMap = updatedMap,
            isQuintalModalOpen = false
        )

        if (qVal > 0L) {
            speakBreakEvenForCrop(crop, qVal)
        }
    }

    fun speakBreakEvenForCrop(crop: CropItem, quintals: Long) {
        onUserGesture()
        if (quintals <= 0L) return
        val totalCost = entries.value
            .filter { it.crop.equals(crop.key, ignoreCase = true) && !it.isIncome && it.category != "sale" }
            .sumOf { it.amount }

        val breakEvenPerQuintal = if (totalCost > 0L) (totalCost + quintals - 1) / quintals else 0L
        val speech = NumberToWords.buildBreakEvenSpeech(
            cropName = crop.getDisplayName(_uiState.value.language),
            totalCost = totalCost,
            quintals = quintals,
            pricePerQuintal = breakEvenPerQuintal,
            language = _uiState.value.language
        )
        ttsManager.speak(speech, _uiState.value.language)
    }

    private fun speakQuintalPrompt(crop: CropItem) {
        if (!_uiState.value.userHasInteracted) return
        val lang = _uiState.value.language
        val cropName = crop.getDisplayName(lang)
        val prompt = when (lang) {
            Language.MR -> "$cropName पिकाचे अंदाजे उत्पादन किती क्विंटल होईल ते टाका"
            Language.HI -> "$cropName की अनुमानित उपज कितने क्विंटल होगी वह डालें"
            Language.EN -> "Enter expected harvest in quintals for $cropName"
        }
        ttsManager.speak(prompt, lang)
    }

    private fun speakProfitOrBreakEven(crop: CropItem) {
        if (!_uiState.value.userHasInteracted) return
        val qVal = _uiState.value.cropQuintalsMap[crop.key] ?: 0L
        val totalCost = entries.value
            .filter { it.crop.equals(crop.key, ignoreCase = true) && !it.isIncome && it.category != "sale" }
            .sumOf { it.amount }

        if (qVal > 0L && totalCost > 0L) {
            speakBreakEvenForCrop(crop, qVal)
        } else {
            speakCurrentInstruction()
        }
    }

    fun undoLastEntry() {
        val idToUndo = _uiState.value.lastSavedId ?: return
        viewModelScope.launch {
            repository.deleteById(idToUndo)
            _uiState.value = _uiState.value.copy(
                lastSavedId = null,
                currentScreen = ScreenState.HOME
            )
            speakCurrentInstruction()
        }
    }

    fun deleteEntryById(entryId: Long) {
        onUserGesture()
        viewModelScope.launch {
            try {
                repository.deleteById(entryId)
                if (_uiState.value.lastSavedId == entryId) {
                    _uiState.value = _uiState.value.copy(lastSavedId = null)
                }
            } catch (e: Exception) {
                Log.e("KhetKhata", "Error deleting entry id=$entryId", e)
            }
        }
    }

    fun deleteEntriesForCrop(crop: CropItem) {
        onUserGesture()
        viewModelScope.launch {
            try {
                repository.deleteByCrop(crop.key)
                prefs.edit()
                    .remove("quintals_${crop.key}")
                    .remove("quintal_prompted_${crop.key}")
                    .commit()
                val updatedMap = _uiState.value.cropQuintalsMap.toMutableMap()
                updatedMap.remove(crop.key)
                _uiState.value = _uiState.value.copy(
                    cropQuintalsMap = updatedMap,
                    lastSavedId = null
                )
            } catch (e: Exception) {
                Log.e("KhetKhata", "Error deleting entries for crop=${crop.key}", e)
            }
        }
    }

    fun clearQuintalsForCrop(crop: CropItem) {
        onUserGesture()
        prefs.edit()
            .remove("quintals_${crop.key}")
            .remove("quintal_prompted_${crop.key}")
            .commit()
        val updatedMap = _uiState.value.cropQuintalsMap.toMutableMap()
        updatedMap.remove(crop.key)
        _uiState.value = _uiState.value.copy(cropQuintalsMap = updatedMap)
    }

    fun handleHardwareBack(): Boolean {
        if (_uiState.value.isQuintalModalOpen) {
            closeQuintalModal()
            return true
        }
        if (_uiState.value.isHowToUseModalOpen) {
            closeHowToUseModal()
            return true
        }
        return when (_uiState.value.currentScreen) {
            ScreenState.AMOUNT -> {
                _uiState.value = _uiState.value.copy(currentScreen = ScreenState.CATEGORY)
                speakCurrentInstruction()
                true
            }
            ScreenState.CATEGORY -> {
                _uiState.value = _uiState.value.copy(currentScreen = ScreenState.HOME)
                speakCurrentInstruction()
                true
            }
            ScreenState.PROFIT -> {
                _uiState.value = _uiState.value.copy(currentScreen = ScreenState.HOME, lastSavedId = null)
                speakCurrentInstruction()
                true
            }
            else -> false
        }
    }

    fun startVoiceInput() {
        onUserGesture()
        speechInputManager?.startListening(_uiState.value.language)
    }

    fun stopVoiceInput() {
        speechInputManager?.stopListening()
    }

    private fun onVoiceTranscriptReceived(transcript: String) {
        val parsed = VoiceParser.parse(transcript)

        var nextCrop = _uiState.value.selectedCrop
        var nextCat = _uiState.value.selectedCategory
        var nextAmt = _uiState.value.amountString

        if (parsed.crop != null) nextCrop = parsed.crop
        if (parsed.category != null) nextCat = parsed.category
        if (parsed.amount != null && parsed.amount > 0) nextAmt = parsed.amount.toString()

        _uiState.value = _uiState.value.copy(
            selectedCrop = nextCrop,
            selectedCategory = nextCat,
            amountString = nextAmt,
            currentScreen = ScreenState.AMOUNT
        )
        speakCurrentInstruction()
    }

    fun speakCurrentInstruction() {
        if (!_uiState.value.userHasInteracted) return

        val text = when (_uiState.value.currentScreen) {
            ScreenState.LANG -> "भाषा निवडा"
            ScreenState.HOME -> when (_uiState.value.language) {
                Language.MR -> "पीक निवडा"
                Language.HI -> "फसल चुनें"
                Language.EN -> "Tap a crop"
            }
            ScreenState.CATEGORY -> when (_uiState.value.language) {
                Language.MR -> "खर्च किंवा विक्री निवडा"
                Language.HI -> "खर्च या बिक्री चुनें"
                Language.EN -> "Tap expense or sale"
            }
            ScreenState.AMOUNT -> when (_uiState.value.language) {
                Language.MR -> "रक्कम टाका आणि हिरवी खूण दाबा"
                Language.HI -> "रकम डालें और हरा निशान दबाएं"
                Language.EN -> "Enter amount and tap green tick"
            }
            ScreenState.PROFIT -> when (_uiState.value.language) {
                Language.MR -> "तुमचा नफा किंवा तोटा आणि हमीभाव पहा"
                Language.HI -> "आपका मुनाफा या नुकसान और न्यूनतम भाव देखें"
                Language.EN -> "Your profit or loss and break-even price"
            }
        }
        ttsManager.speak(text, _uiState.value.language)
    }

    // How-To-Use Guide Modal
    fun openHowToUseModal() {
        onUserGesture()
        _uiState.value = _uiState.value.copy(isHowToUseModalOpen = true)
        val guideSpeech = when (_uiState.value.language) {
            Language.MR -> "तीन सोप्या पायऱ्या: पहिली पायरी पीक निवडा, दुसरी पायरी खर्च किंवा विक्री निवडा, तिसरी पायरी रक्कम टाकून हिरवी खूण दाबा. विक्री वजा खर्च म्हणजे तुमचा नफा, आणि एकूण खर्च भागिले क्विंटल म्हणजे किमान विक्री भाव."
            Language.HI -> "तीन आसान कदम: पहले फसल चुनें, फिर खर्च या बिक्री चुनें, और रकम डालकर हरा निशान दबाएं। कुल खर्च को क्विंटल से भाग देने पर न्यूनतम बिक्री भाव मिलता है।"
            Language.EN -> "Three simple steps: First tap a crop, second tap expense or sale, third enter the amount and tap the green tick. Profit equals sales minus expenses, and break-even price equals total cost divided by quintals."
        }
        ttsManager.speak(guideSpeech, _uiState.value.language)
    }

    fun closeHowToUseModal() {
        _uiState.value = _uiState.value.copy(isHowToUseModalOpen = false)
    }

    // Family Helper
    fun openFamilyModal() {
        _uiState.value = _uiState.value.copy(isFamilyModalOpen = true)
    }
    fun closeFamilyModal() {
        _uiState.value = _uiState.value.copy(isFamilyModalOpen = false)
    }

    fun exportDataJson(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportJson(entries.value)
            onExportReady(json)
        }
    }

    fun exportDataCsv(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            val csv = repository.exportCsv(entries.value)
            onExportReady(csv)
        }
    }

    fun importDataJson(jsonString: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importJson(jsonString)
            onComplete(success)
        }
    }

    fun clearMyEntries(onComplete: () -> Unit = {}) {
        if (_uiState.value.isResetting) return
        _uiState.value = _uiState.value.copy(isResetting = true)
        viewModelScope.launch {
            try {
                repository.deleteAll()
                val editor = prefs.edit()
                for (crop in CropItem.entries) {
                    editor.remove("quintals_${crop.key}")
                    editor.remove("quintal_prompted_${crop.key}")
                }
                editor.commit()
            } catch (e: Exception) {
                Log.e("KhetKhata", "Error clearing entries", e)
            } finally {
                _uiState.value = _uiState.value.copy(
                    isResetting = false,
                    isFamilyModalOpen = false,
                    currentScreen = ScreenState.HOME,
                    amountString = "0",
                    lastSavedId = null,
                    profitFilterCrop = null,
                    cropQuintalsMap = emptyMap()
                )
                onComplete()
            }
        }
    }

    fun fullReset(onComplete: () -> Unit = {}) {
        if (_uiState.value.isResetting) return
        _uiState.value = _uiState.value.copy(isResetting = true)
        viewModelScope.launch {
            try {
                repository.deleteAll()
                prefs.edit().clear().commit()
                clearAppCaches()
            } catch (e: Exception) {
                Log.e("KhetKhata", "Error performing full reset", e)
            } finally {
                _uiState.value = UiState(
                    currentScreen = ScreenState.LANG,
                    language = Language.MR,
                    hasTaught = false,
                    isResetting = false,
                    isFamilyModalOpen = false,
                    amountString = "0",
                    lastSavedId = null,
                    profitFilterCrop = null,
                    cropQuintalsMap = emptyMap()
                )
                onComplete()
            }
        }
    }

    private fun clearAppCaches() {
        try {
            val app = getApplication<Application>()
            app.cacheDir?.listFiles()?.forEach { it.deleteRecursively() }
            app.codeCacheDir?.listFiles()?.forEach { it.deleteRecursively() }
        } catch (e: Exception) {
            Log.e("KhetKhata", "Error clearing cache directories", e)
        }
    }

    fun deleteAllEntries() {
        clearMyEntries()
    }

    fun loadDemoData() {
        viewModelScope.launch {
            repository.insertDemoEntries()
            // Also seed realistic demo quintals for onion (20 quintals) & soybean (10 quintals)
            prefs.edit()
                .putLong("quintals_onion", 20L)
                .putBoolean("quintal_prompted_onion", true)
                .putLong("quintals_soybean", 10L)
                .putBoolean("quintal_prompted_soybean", true)
                .commit()
            _uiState.value = _uiState.value.copy(cropQuintalsMap = loadSavedQuintalsMap())
        }
    }

    // Advisor Modal
    fun openAdvisorModal() {
        val analysis = KisanMitraAdvisor.generateCropFinancialAnalysis(
            _uiState.value.selectedCrop,
            entries.value,
            _uiState.value.language
        )
        val initialMessages = listOf(
            AdvisorMessage("advisor", analysis)
        )
        _uiState.value = _uiState.value.copy(
            isAdvisorModalOpen = true,
            advisorChat = initialMessages
        )
    }

    fun closeAdvisorModal() {
        _uiState.value = _uiState.value.copy(isAdvisorModalOpen = false)
    }

    fun askAdvisor(query: String) {
        if (query.isBlank()) return
        val currentChat = _uiState.value.advisorChat.toMutableList()
        currentChat.add(AdvisorMessage("user", query))

        val answer = KisanMitraAdvisor.answerFarmerQuery(query, _uiState.value.language)
        currentChat.add(AdvisorMessage("advisor", answer))

        _uiState.value = _uiState.value.copy(advisorChat = currentChat)
        ttsManager.speak(answer.replace(Regex("[*#•]"), ""), _uiState.value.language)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        speechInputManager?.stopListening()
    }
}
