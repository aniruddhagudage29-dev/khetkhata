package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Language
import com.example.ui.KhetKhataViewModel
import com.example.ui.ScreenState
import com.example.ui.components.KhetKhataTopBar
import com.example.ui.screens.AmountScreen
import com.example.ui.screens.CategoryScreen
import com.example.ui.screens.FamilyHelperDialog
import com.example.ui.screens.HighAmountDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HowToUseGuideDialog
import com.example.ui.screens.KisanMitraDialog
import com.example.ui.screens.LanguageScreen
import com.example.ui.screens.ProfitScreen
import com.example.ui.screens.QuintalInputDialog
import com.example.ui.theme.FarmCreamBg
import com.example.ui.theme.KhetKhataTheme

class MainActivity : ComponentActivity() {

    private val viewModel: KhetKhataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleUrlShortcuts()

        setContent {
            KhetKhataTheme {
                KhetKhataApp(viewModel = viewModel)
            }
        }
    }

    private fun handleUrlShortcuts() {
        val uri = intent?.data ?: return
        if (uri.getQueryParameter("reset") == "1") {
            viewModel.clearMyEntries()
            intent?.data = removeQueryParameter(uri, "reset")
            return
        }
        if (uri.getQueryParameter("demo") == "1") {
            viewModel.loadDemoData()
        }
    }

    private fun removeQueryParameter(uri: Uri, paramToRemove: String): Uri {
        val builder = uri.buildUpon().clearQuery()
        for (name in uri.queryParameterNames) {
            if (name != paramToRemove) {
                for (value in uri.getQueryParameters(name)) {
                    builder.appendQueryParameter(name, value)
                }
            }
        }
        return builder.build()
    }
}

@Composable
fun KhetKhataApp(viewModel: KhetKhataViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()

    // Microphone Permission Launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceInput()
        }
    }

    // Android Hardware Back Button Handling
    BackHandler(
        enabled = uiState.currentScreen != ScreenState.HOME &&
            uiState.currentScreen != ScreenState.LANG ||
            uiState.isQuintalModalOpen ||
            uiState.isHowToUseModalOpen
    ) {
        viewModel.handleHardwareBack()
    }

    val instructionText = when (uiState.currentScreen) {
        ScreenState.LANG -> "🌱 भाषा निवडा"
        ScreenState.HOME -> when (uiState.language) {
            Language.MR -> "🌱 खालील पीक निवडा"
            Language.HI -> "🌱 नीचे से फसल चुनें"
            Language.EN -> "🌱 Tap Your Crop Below"
        }
        ScreenState.CATEGORY -> when (uiState.language) {
            Language.MR -> "🛠️ खर्च किंवा विक्री निवडा"
            Language.HI -> "🛠️ खर्च या बिक्री चुनें"
            Language.EN -> "🛠️ Tap Expense or Sale"
        }
        ScreenState.AMOUNT -> when (uiState.language) {
            Language.MR -> "₹ रक्कम टाका व हिरवे ✔ दाबा"
            Language.HI -> "₹ रकम डालें और हरा ✔ दबाएं"
            Language.EN -> "₹ Enter Amount & Tap Green ✔"
        }
        ScreenState.PROFIT -> when (uiState.language) {
            Language.MR -> "📊 नफा-तोटा व किमान भाव (हमीभाव)"
            Language.HI -> "📊 मुनाफा-नुकसान व न्यूनतम भाव"
            Language.EN -> "📊 Profit / Loss & Break-Even Price"
        }
    }

    Scaffold(
        containerColor = FarmCreamBg,
        modifier = Modifier
            .fillMaxSize()
            .background(FarmCreamBg)
            .safeDrawingPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FarmCreamBg)
                .padding(innerPadding)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // Top Bar (Shown on all screens except initial Language selection)
            if (uiState.currentScreen != ScreenState.LANG) {
                KhetKhataTopBar(
                    screen = uiState.currentScreen,
                    language = uiState.language,
                    instructionText = instructionText,
                    isOnline = uiState.isOnline,
                    isListening = uiState.isListening,
                    onBackClick = { viewModel.handleHardwareBack() },
                    onMicClick = {
                        val hasMicPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasMicPermission) {
                            if (uiState.isListening) {
                                viewModel.stopVoiceInput()
                            } else {
                                viewModel.startVoiceInput()
                            }
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onSpeakerClick = { viewModel.speakCurrentInstruction() },
                    onOpenFamilyHelper = { viewModel.openFamilyModal() }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Main Screen Container
            Box(modifier = Modifier.weight(1f)) {
                when (uiState.currentScreen) {
                    ScreenState.LANG -> {
                        LanguageScreen(
                            hasTaught = uiState.hasTaught,
                            onLanguageSelected = { lang ->
                                viewModel.selectLanguage(lang)
                            }
                        )
                    }
                    ScreenState.HOME -> {
                        HomeScreen(
                            language = uiState.language,
                            hasTaught = uiState.hasTaught,
                            onCropSelected = { crop -> viewModel.selectCrop(crop) },
                            onMyProfitClick = { viewModel.navigateTo(ScreenState.PROFIT, null) },
                            onLanguageClick = { viewModel.navigateTo(ScreenState.LANG) },
                            onFamilyClick = { viewModel.openFamilyModal() },
                            onAdvisorClick = { viewModel.openAdvisorModal() },
                            onHowToUseClick = { viewModel.openHowToUseModal() }
                        )
                    }
                    ScreenState.CATEGORY -> {
                        CategoryScreen(
                            crop = uiState.selectedCrop,
                            language = uiState.language,
                            hasTaught = uiState.hasTaught,
                            onCategorySelected = { category -> viewModel.selectCategory(category) }
                        )
                    }
                    ScreenState.AMOUNT -> {
                        AmountScreen(
                            crop = uiState.selectedCrop,
                            category = uiState.selectedCategory,
                            amountStr = uiState.amountString,
                            language = uiState.language,
                            isSaving = uiState.isSaving,
                            showSaveCelebration = uiState.showSaveCelebration,
                            onNumpadPress = { key -> viewModel.onNumpadPress(key) },
                            onChipPress = { chipVal -> viewModel.addQuickChip(chipVal) },
                            onSaveClick = { viewModel.attemptSaveEntry() }
                        )
                    }
                    ScreenState.PROFIT -> {
                        ProfitScreen(
                            entries = entries,
                            profitFilterCrop = uiState.profitFilterCrop,
                            lastSavedId = uiState.lastSavedId,
                            language = uiState.language,
                            cropQuintalsMap = uiState.cropQuintalsMap,
                            onOpenQuintalModal = { crop -> viewModel.openQuintalModal(crop) },
                            onSpeakBreakEven = { crop, q -> viewModel.speakBreakEvenForCrop(crop, q) },
                            onDeleteEntry = { entryId -> viewModel.deleteEntryById(entryId) },
                            onDeleteCropCalculations = { crop -> viewModel.deleteEntriesForCrop(crop) },
                            onClearQuintals = { crop -> viewModel.clearQuintalsForCrop(crop) },
                            onClearAllEntries = { viewModel.clearMyEntries() },
                            onUndoClick = { viewModel.undoLastEntry() },
                            onDoneClick = { viewModel.navigateTo(ScreenState.HOME) }
                        )
                    }
                }
            }
        }
    }

    // High Amount Guard Modal (> ₹1,00,000)
    if (uiState.isHighAmountModalOpen) {
        HighAmountDialog(
            amountStr = uiState.amountString,
            language = uiState.language,
            onConfirm = { viewModel.confirmHighAmount() },
            onDismiss = { viewModel.dismissHighAmountModal() }
        )
    }

    // Break-Even Expected Harvest (Quintals) Numpad Modal
    if (uiState.isQuintalModalOpen) {
        QuintalInputDialog(
            crop = uiState.quintalTargetCrop,
            quintalStr = uiState.quintalInputString,
            language = uiState.language,
            onNumpadPress = { key -> viewModel.onQuintalNumpadPress(key) },
            onChipPress = { chip -> viewModel.addQuintalQuickChip(chip) },
            onSave = { viewModel.saveQuintalsForCrop() },
            onDismiss = { viewModel.closeQuintalModal() }
        )
    }

    // How-To-Use & Math Formula Guide Modal
    if (uiState.isHowToUseModalOpen) {
        HowToUseGuideDialog(
            language = uiState.language,
            onSpeakGuide = { viewModel.openHowToUseModal() },
            onDismiss = { viewModel.closeHowToUseModal() }
        )
    }

    // Family Helper Modal
    if (uiState.isFamilyModalOpen) {
        FamilyHelperDialog(
            language = uiState.language,
            isResetting = uiState.isResetting,
            onExportCopy = { onReady ->
                viewModel.exportDataJson { json ->
                    viewModel.exportDataCsv { csv ->
                        onReady(json, csv)
                    }
                }
            },
            onImportJson = { jsonStr ->
                viewModel.importDataJson(jsonStr) { success ->
                    if (success) viewModel.closeFamilyModal()
                }
            },
            onClearEntries = { viewModel.clearMyEntries() },
            onFullReset = {
                viewModel.fullReset {
                    (context as? ComponentActivity)?.recreate()
                }
            },
            onDismiss = { viewModel.closeFamilyModal() }
        )
    }

    // Kisan Mitra Advisor Modal
    if (uiState.isAdvisorModalOpen) {
        KisanMitraDialog(
            language = uiState.language,
            chatMessages = uiState.advisorChat,
            onAskQuestion = { query -> viewModel.askAdvisor(query) },
            onSpeakMessage = { text ->
                viewModel.ttsManager.speak(text.replace(Regex("[*#•]"), ""), uiState.language)
            },
            onDismiss = { viewModel.closeAdvisorModal() }
        )
    }
}
