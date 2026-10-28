package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crop_entries")
data class CropEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val crop: String,         // onion, soybean, cotton, sugarcane, tur, other
    val category: String,     // seed, fertilizer, pesticide, labour, diesel, water, transport, other, sale
    val isIncome: Boolean,    // true if sale, false if expense
    val amount: Long,         // in INR Rupees
    val timestamp: Long = System.currentTimeMillis()
)
