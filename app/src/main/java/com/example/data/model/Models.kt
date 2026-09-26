package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "health_notebooks")
data class HealthNotebook(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Genel", "Ağrı & Belirti", "Döngü", "Kronik", "Metrik"
    val iconName: String, // "heart", "headache", "calendar", "activity", "droplet", "scale", "moon"
    val unit: String,     // "mmHg", "1-10 Şiddet", "Gün", "mg/dL", "kg", "Bardak"
    val targetMin: Double? = null,
    val targetMax: Double? = null,
    val description: String = "",
    val colorHex: String = "#059669",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notebook_entries",
    foreignKeys = [
        ForeignKey(
            entity = HealthNotebook::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("notebookId")]
)
data class NotebookEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val notebookId: Long,
    val numericValue: Double? = null,
    val secondaryValue: Double? = null, // e.g. diastolic for blood pressure
    val severity: Int? = null,          // 1-10
    val notes: String = "",
    val tags: String = "",              // Comma-separated tags
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "lab_tests")
data class LabTest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val testDate: String,
    val laboratory: String = "Merkez Laboratuvarı",
    val rawText: String = "",
    val aiSummary: String,
    val doctorQuestions: String,
    val parametersJson: String, // JSON serialized List<LabParameter>
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class LabParameter(
    val name: String,
    val value: Double,
    val unit: String,
    val referenceRange: String,
    val refMin: Double,
    val refMax: Double,
    val status: String, // "NORMAL", "LOW", "HIGH"
    val explanation: String // Plain Turkish health literacy note
)

@Entity(tableName = "assistant_messages")
data class AssistantMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val isUser: Boolean,
    val message: String,
    val suggestedNotebookJson: String? = null, // JSON of NotebookTemplateSuggestion
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class NotebookTemplateSuggestion(
    val title: String,
    val category: String,
    val iconName: String,
    val unit: String,
    val description: String,
    val colorHex: String,
    val targetMin: Double? = null,
    val targetMax: Double? = null
)
