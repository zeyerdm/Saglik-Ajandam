package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.local.AssistantMessageDao
import com.example.data.local.HealthNotebookDao
import com.example.data.local.LabTestDao
import com.example.data.local.NotebookEntryDao
import com.example.data.model.AssistantMessage
import com.example.data.model.HealthNotebook
import com.example.data.model.LabParameter
import com.example.data.model.LabTest
import com.example.data.model.NotebookEntry
import com.example.data.model.NotebookTemplateSuggestion
import com.example.data.remote.AssistantReply
import com.example.data.remote.GeminiHealthService
import com.example.data.remote.LabAnalysisResult
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class HealthRepository(
    private val notebookDao: HealthNotebookDao,
    private val entryDao: NotebookEntryDao,
    private val labTestDao: LabTestDao,
    private val assistantMessageDao: AssistantMessageDao,
    private val geminiService: GeminiHealthService
) {
    val allNotebooks: Flow<List<HealthNotebook>> = notebookDao.getAllNotebooks()
    val allLabTests: Flow<List<LabTest>> = labTestDao.getAllLabTests()
    val allEntries: Flow<List<NotebookEntry>> = entryDao.getAllEntries()
    val chatMessages: Flow<List<AssistantMessage>> = assistantMessageDao.getAllMessages()

    fun getEntriesForNotebook(notebookId: Long): Flow<List<NotebookEntry>> =
        entryDao.getEntriesForNotebook(notebookId)

    suspend fun createNotebook(notebook: HealthNotebook): Long =
        notebookDao.insertNotebook(notebook)

    suspend fun updateNotebook(notebook: HealthNotebook) =
        notebookDao.updateNotebook(notebook)

    suspend fun deleteNotebook(notebook: HealthNotebook) {
        entryDao.deleteEntriesForNotebook(notebook.id)
        notebookDao.deleteNotebook(notebook)
    }

    suspend fun addEntry(entry: NotebookEntry): Long =
        entryDao.insertEntry(entry)

    suspend fun deleteEntry(entry: NotebookEntry) =
        entryDao.deleteEntry(entry)

    suspend fun deleteLabTest(labTest: LabTest) =
        labTestDao.deleteLabTest(labTest)

    suspend fun analyzeAndSaveLabTest(
        inputText: String,
        bitmap: Bitmap? = null
    ): LabTest {
        val analysis: LabAnalysisResult = geminiService.analyzeLabTest(inputText, bitmap)

        val paramsJsonArray = JSONArray()
        for (p in analysis.parameters) {
            val obj = JSONObject()
            obj.put("name", p.name)
            obj.put("value", p.value)
            obj.put("unit", p.unit)
            obj.put("referenceRange", p.referenceRange)
            obj.put("refMin", p.refMin)
            obj.put("refMax", p.refMax)
            obj.put("status", p.status)
            obj.put("explanation", p.explanation)
            paramsJsonArray.put(obj)
        }

        val labTest = LabTest(
            title = analysis.title,
            testDate = analysis.testDate,
            laboratory = analysis.laboratory,
            rawText = inputText,
            aiSummary = analysis.aiSummary,
            doctorQuestions = analysis.doctorQuestions,
            parametersJson = paramsJsonArray.toString()
        )
        val id = labTestDao.insertLabTest(labTest)
        return labTest.copy(id = id)
    }

    suspend fun sendAssistantMessage(
        userText: String,
        healthContext: String
    ): AssistantReply {
        // Save user message
        assistantMessageDao.insertMessage(
            AssistantMessage(
                isUser = true,
                message = userText,
                suggestedNotebookJson = null
            )
        )

        val reply = geminiService.chatAssistant(userText, healthContext)

        var suggestionJson: String? = null
        reply.suggestedTemplate?.let { template ->
            val obj = JSONObject()
            obj.put("title", template.title)
            obj.put("category", template.category)
            obj.put("iconName", template.iconName)
            obj.put("unit", template.unit)
            obj.put("description", template.description)
            obj.put("colorHex", template.colorHex)
            template.targetMin?.let { obj.put("targetMin", it) }
            template.targetMax?.let { obj.put("targetMax", it) }
            suggestionJson = obj.toString()
        }

        // Save assistant message
        assistantMessageDao.insertMessage(
            AssistantMessage(
                isUser = false,
                message = reply.replyText,
                suggestedNotebookJson = suggestionJson
            )
        )

        return reply
    }

    suspend fun createNotebookFromSuggestion(suggestion: NotebookTemplateSuggestion): Long {
        val notebook = HealthNotebook(
            title = suggestion.title,
            category = suggestion.category,
            iconName = suggestion.iconName,
            unit = suggestion.unit,
            targetMin = suggestion.targetMin,
            targetMax = suggestion.targetMax,
            description = suggestion.description,
            colorHex = suggestion.colorHex
        )
        return createNotebook(notebook)
    }

    suspend fun clearChat() {
        assistantMessageDao.clearChat()
    }
}
