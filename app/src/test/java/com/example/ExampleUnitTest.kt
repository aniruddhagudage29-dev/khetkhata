package com.example

import com.example.model.CategoryItem
import com.example.model.CropItem
import com.example.model.Language
import com.example.model.NumberToWords
import com.example.model.VoiceParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testVoiceParser_colloquialMarathi() {
        val parsed = VoiceParser.parse("कांदा बियाणे पंधराशे रुपये")
        assertEquals(CropItem.ONION, parsed.crop)
        assertEquals(CategoryItem.SEED, parsed.category)
        assertEquals(1500L, parsed.amount)
    }

    @Test
    fun testVoiceParser_colloquialHindiAndKNotation() {
        val parsed = VoiceParser.parse("सोयाबीन खाद 2.5k")
        assertEquals(CropItem.SOYBEAN, parsed.crop)
        assertEquals(CategoryItem.FERTILIZER, parsed.category)
        assertEquals(2500L, parsed.amount)
    }

    @Test
    fun testVoiceParser_devanagariNumerals() {
        val parsed = VoiceParser.parse("कापूस फवारणी २५००")
        assertEquals(CropItem.COTTON, parsed.crop)
        assertEquals(CategoryItem.PESTICIDE, parsed.category)
        assertEquals(2500L, parsed.amount)
    }

    @Test
    fun testNumberToWords_marathiSpellings() {
        assertEquals("शंभर रुपये", NumberToWords.convert(100L, Language.MR))
        assertEquals("एकेचाळीस रुपये", NumberToWords.convert(41L, Language.MR))
        assertEquals("एक हजार पाचशे रुपये", NumberToWords.convert(1500L, Language.MR))
        val devDigits = NumberToWords.toLocalDigits("1500", Language.MR)
        assertEquals("१५००", devDigits)
    }

    @Test
    fun testNumberToWords_indianCurrencyFormat() {
        assertEquals("₹ २५,०००", NumberToWords.formatCurrency(25000L, Language.MR))
        assertEquals("₹ १,५०,०००", NumberToWords.formatCurrency(150000L, Language.MR))
        assertEquals("₹ 1,50,000", NumberToWords.formatCurrency(150000L, Language.EN))
    }

    @Test
    fun testBreakEvenHeadline_allLanguages() {
        val mrHeadline = NumberToWords.buildBreakEvenHeadline(500L, Language.MR)
        assertTrue(mrHeadline.contains("₹ ५००"))
        val hiHeadline = NumberToWords.buildBreakEvenHeadline(500L, Language.HI)
        assertTrue(hiHeadline.contains("₹ ५००"))
        val enHeadline = NumberToWords.buildBreakEvenHeadline(500L, Language.EN)
        assertEquals("Sell above ₹ 500 per quintal to avoid loss", enHeadline)
    }

    @Test
    fun testOneWordCropLabels() {
        for (crop in CropItem.entries) {
            for (lang in Language.entries) {
                val label = crop.getDisplayName(lang).trim()
                assertFalse("Crop label '$label' should be one word", label.contains(" "))
            }
        }
    }
}
