package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class KhetKhataRepository(private val dao: CropEntryDao) {

    val allEntries: Flow<List<CropEntryEntity>> = dao.getAllEntries()

    suspend fun insert(entry: CropEntryEntity): Long = withContext(Dispatchers.IO) {
        dao.insertEntry(entry)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteEntryById(id)
    }

    suspend fun deleteByCrop(cropKey: String) = withContext(Dispatchers.IO) {
        dao.deleteEntriesByCrop(cropKey)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        dao.deleteAll()
    }

    suspend fun exportJson(entries: List<CropEntryEntity>): String = withContext(Dispatchers.Default) {
        val jsonArray = JSONArray()
        for (item in entries) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("crop", item.crop)
                put("category", item.category)
                put("isIncome", item.isIncome)
                put("amount", item.amount)
                put("timestamp", item.timestamp)
            }
            jsonArray.put(obj)
        }
        jsonArray.toString(2)
    }

    suspend fun exportCsv(entries: List<CropEntryEntity>): String = withContext(Dispatchers.Default) {
        val sb = StringBuilder()
        sb.append("id,crop,category,type,amount,timestamp\n")
        for (item in entries) {
            val typeStr = if (item.isIncome) "income" else "expense"
            sb.append("${item.id},${item.crop},${item.category},$typeStr,${item.amount},${item.timestamp}\n")
        }
        sb.toString()
    }

    suspend fun importJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray(jsonString)
            val parsed = mutableListOf<CropEntryEntity>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                parsed.add(
                    CropEntryEntity(
                        id = 0, // Auto-generate clean primary key
                        crop = obj.optString("crop", "other"),
                        category = obj.optString("category", "other"),
                        isIncome = obj.optBoolean("isIncome", false),
                        amount = obj.optLong("amount", 0L),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            if (parsed.isNotEmpty()) {
                dao.insertAll(parsed)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun insertDemoEntries() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val oneDay = 86_400_000L
        val demoList = listOf(
            CropEntryEntity(crop = "onion", category = "seed", isIncome = false, amount = 2500, timestamp = now - 10 * oneDay),
            CropEntryEntity(crop = "onion", category = "fertilizer", isIncome = false, amount = 4000, timestamp = now - 8 * oneDay),
            CropEntryEntity(crop = "onion", category = "labour", isIncome = false, amount = 3500, timestamp = now - 5 * oneDay),
            CropEntryEntity(crop = "onion", category = "sale", isIncome = true, amount = 28000, timestamp = now - 1 * oneDay),
            CropEntryEntity(crop = "soybean", category = "seed", isIncome = false, amount = 3000, timestamp = now - 7 * oneDay),
            CropEntryEntity(crop = "soybean", category = "pesticide", isIncome = false, amount = 1500, timestamp = now - 3 * oneDay)
        )
        dao.insertAll(demoList)
    }
}
