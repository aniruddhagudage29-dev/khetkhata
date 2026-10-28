package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Looper
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.CropEntryEntity
import com.example.model.CategoryItem
import com.example.model.CropItem
import com.example.model.Language
import com.example.ui.KhetKhataViewModel
import com.example.ui.ScreenState
import com.example.ui.screens.FamilyHelperDialog
import com.example.ui.screens.ProfitScreen
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var app: Application

    @Before
    fun setUp() {
        runBlocking {
            app = ApplicationProvider.getApplicationContext()
            AppDatabase.getInstance(app).cropEntryDao().deleteAll()
            app.getSharedPreferences("khetkhata_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        }
    }

    private suspend fun waitUntilEntriesCount(vm: KhetKhataViewModel, expectedCount: Int) {
        var attempts = 0
        while (vm.entries.value.size != expectedCount && attempts < 60) {
            shadowOf(Looper.getMainLooper()).idle()
            delay(30)
            attempts++
        }
    }

    @Test
    fun test1_add3EntriesClearAndConfirmMyProfitIsEmpty() {
        runBlocking {
            val dao = AppDatabase.getInstance(app).cropEntryDao()
            val vm = KhetKhataViewModel(app)
            vm.selectLanguage(Language.EN)

            // Add 3 entries
            dao.insertEntry(CropEntryEntity(crop = CropItem.ONION.key, category = CategoryItem.SEED.key, isIncome = false, amount = 1200L))
            dao.insertEntry(CropEntryEntity(crop = CropItem.ONION.key, category = CategoryItem.LABOUR.key, isIncome = false, amount = 800L))
            dao.insertEntry(CropEntryEntity(crop = CropItem.ONION.key, category = CategoryItem.SALE.key, isIncome = true, amount = 5000L))
            waitUntilEntriesCount(vm, 3)
            assertEquals(3, vm.entries.value.size)

            // Run "Clear my entries"
            var completed = false
            vm.clearMyEntries { completed = true }
            var tries = 0
            while (!completed && tries < 60) {
                shadowOf(Looper.getMainLooper()).idle()
                delay(30)
                tries++
            }
            waitUntilEntriesCount(vm, 0)

            // Verify entries are empty, screen returned to HOME, and language preference was preserved
            assertEquals(0, vm.entries.value.size)
            assertEquals(ScreenState.HOME, vm.uiState.value.currentScreen)
            assertEquals(Language.EN, vm.uiState.value.language)
            val savedLang = app.getSharedPreferences("khetkhata_prefs", Context.MODE_PRIVATE).getString("selected_lang", null)
            assertEquals("en", savedLang)

            // Render My Profit screen and confirm friendly empty state with no NaN or errors
            composeTestRule.setContent {
                ProfitScreen(
                    entries = vm.entries.value,
                    profitFilterCrop = null,
                    lastSavedId = null,
                    language = Language.EN,
                    onUndoClick = {},
                    onDoneClick = {}
                )
            }
            composeTestRule.onNodeWithTag("profit_empty_state").assertIsDisplayed()
            composeTestRule.onNodeWithTag("profit_card").assertIsDisplayed()
        }
    }

    @Test
    fun test2_refreshAndConfirmEntriesStayGoneAndResetUrlParamStripped() {
        runBlocking {
            val dao = AppDatabase.getInstance(app).cropEntryDao()
            dao.insertEntry(CropEntryEntity(crop = CropItem.SUGARCANE.key, category = CategoryItem.FERTILIZER.key, isIncome = false, amount = 1500L))
            dao.insertEntry(CropEntryEntity(crop = CropItem.SOYBEAN.key, category = CategoryItem.LABOUR.key, isIncome = false, amount = 2500L))
            dao.insertEntry(CropEntryEntity(crop = CropItem.COTTON.key, category = CategoryItem.SALE.key, isIncome = true, amount = 9000L))
            assertEquals(3, dao.getAllEntries().first().size)

            // Launch MainActivity with ?reset=1 shortcut
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("khetkhata://app?reset=1&lang=mr"))
            val controller = Robolectric.buildActivity(MainActivity::class.java, intent).create()
            val activity = controller.get()

            // Wait for clearMyEntries coroutine to finish
            var tries = 0
            while (dao.getAllEntries().first().isNotEmpty() && tries < 60) {
                shadowOf(Looper.getMainLooper()).idle()
                delay(30)
                tries++
            }

            // Confirm entries were cleared and ?reset=1 parameter was removed from URL so refreshing won't clear again
            assertEquals(0, dao.getAllEntries().first().size)
            assertNull(activity.intent?.data?.getQueryParameter("reset"))
            assertEquals("mr", activity.intent?.data?.getQueryParameter("lang"))

            // Simulate a refresh / new ViewModel instance and confirm entries stay gone
            val refreshedVm = KhetKhataViewModel(app)
            waitUntilEntriesCount(refreshedVm, 0)
            assertEquals(0, refreshedVm.entries.value.size)
            controller.destroy()
        }
    }

    @Test
    fun test3_fullResetThenReloadAndConfirmAppStillLoads() {
        runBlocking {
            val dao = AppDatabase.getInstance(app).cropEntryDao()
            val vm = KhetKhataViewModel(app)
            vm.selectLanguage(Language.HI)
            dao.insertEntry(CropEntryEntity(crop = CropItem.TUR.key, category = CategoryItem.SEED.key, isIncome = false, amount = 700L))
            waitUntilEntriesCount(vm, 1)

            // Create a dummy cache file inside cacheDir
            val dummyCacheFile = File(app.cacheDir, "temp_test_cache.txt")
            dummyCacheFile.writeText("cached_data")
            assertTrue(dummyCacheFile.exists())

            // Execute Full reset
            var resetDone = false
            vm.fullReset { resetDone = true }
            var tries = 0
            while (!resetDone && tries < 60) {
                shadowOf(Looper.getMainLooper()).idle()
                delay(30)
                tries++
            }
            waitUntilEntriesCount(vm, 0)

            // Verify entries, preferences (including language), and cacheDir were cleared
            assertEquals(0, dao.getAllEntries().first().size)
            val prefs = app.getSharedPreferences("khetkhata_prefs", Context.MODE_PRIVATE)
            assertFalse(prefs.contains("selected_lang"))
            assertFalse(dummyCacheFile.exists())

            // Reload the app (fresh ViewModel) and confirm it loads cleanly on LANG screen
            val reloadedVm = KhetKhataViewModel(app)
            assertEquals(ScreenState.LANG, reloadedVm.uiState.value.currentScreen)
            assertEquals(0, reloadedVm.entries.value.size)
        }
    }

    @Test
    fun test4_cancelHoldEarlyAndConfirmNothingIsDeleted() {
        runBlocking {
            val dao = AppDatabase.getInstance(app).cropEntryDao()
            dao.insertEntry(CropEntryEntity(crop = CropItem.ONION.key, category = CategoryItem.SEED.key, isIncome = false, amount = 500L))
            dao.insertEntry(CropEntryEntity(crop = CropItem.ONION.key, category = CategoryItem.LABOUR.key, isIncome = false, amount = 600L))
            dao.insertEntry(CropEntryEntity(crop = CropItem.ONION.key, category = CategoryItem.SALE.key, isIncome = true, amount = 2000L))
            assertEquals(3, dao.getAllEntries().first().size)

            var clearCalled = false
            composeTestRule.mainClock.autoAdvance = false

            composeTestRule.setContent {
                FamilyHelperDialog(
                    language = Language.EN,
                    isResetting = false,
                    onExportCopy = {},
                    onImportJson = {},
                    onClearEntries = { clearCalled = true },
                    onFullReset = {},
                    onDismiss = {}
                )
            }
            composeTestRule.mainClock.advanceTimeBy(100)

            // Open confirm step for "Clear my entries"
            composeTestRule.onNodeWithTag("family_btn_clear_entries").performClick()
            composeTestRule.mainClock.advanceTimeBy(100)

            // Confirm safety elements are shown: Save a copy first, Green Back button, Red Hold 3s button
            composeTestRule.onNodeWithTag("confirm_save_copy_first_button").assertIsDisplayed()
            composeTestRule.onNodeWithTag("confirm_back_button").assertIsDisplayed()
            composeTestRule.onNodeWithTag("confirm_hold_reset_button").assertIsDisplayed()

            // Press and hold the red button for only 1.2 seconds (< 3 seconds), then release early
            composeTestRule.onNodeWithTag("confirm_hold_reset_button").performTouchInput {
                down(center)
            }
            composeTestRule.mainClock.advanceTimeBy(1200)
            composeTestRule.onNodeWithTag("confirm_hold_reset_button").performTouchInput {
                up()
            }
            composeTestRule.mainClock.advanceTimeBy(500)

            // Confirm nothing was deleted
            assertFalse(clearCalled)
            assertEquals(3, dao.getAllEntries().first().size)

            // Also test Green Back button returns to menu without deleting
            composeTestRule.onNodeWithTag("confirm_back_button").performClick()
            composeTestRule.mainClock.advanceTimeBy(100)
            assertFalse(clearCalled)
            assertEquals(3, dao.getAllEntries().first().size)
        }
    }
}
