package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HealthDatabase
import com.example.data.model.AssistantMessage
import com.example.data.model.HealthNotebook
import com.example.data.model.LabParameter
import com.example.data.model.LabTest
import com.example.data.model.NotebookEntry
import com.example.data.model.NotebookTemplateSuggestion
import com.example.data.remote.GeminiHealthService
import com.example.data.repository.HealthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = HealthDatabase.getDatabase(application)
    private val repository = HealthRepository(
        notebookDao = database.notebookDao(),
        entryDao = database.entryDao(),
        labTestDao = database.labTestDao(),
        assistantMessageDao = database.assistantMessageDao(),
        geminiService = GeminiHealthService()
    )

    val notebooks: StateFlow<List<HealthNotebook>> = repository.allNotebooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val labTests: StateFlow<List<LabTest>> = repository.allLabTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEntries: StateFlow<List<NotebookEntry>> = repository.allEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<AssistantMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedNotebook = MutableStateFlow<HealthNotebook?>(null)
    val selectedNotebook: StateFlow<HealthNotebook?> = _selectedNotebook.asStateFlow()

    val entriesForSelectedNotebook: StateFlow<List<NotebookEntry>> = _selectedNotebook
        .flatMapLatest { nb ->
            if (nb == null) flowOf(emptyList()) else repository.getEntriesForNotebook(nb.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedLabTest = MutableStateFlow<LabTest?>(null)
    val selectedLabTest: StateFlow<LabTest?> = _selectedLabTest.asStateFlow()

    private val _isAnalyzingLabTest = MutableStateFlow(false)
    val isAnalyzingLabTest: StateFlow<Boolean> = _isAnalyzingLabTest.asStateFlow()

    private val _isAssistantThinking = MutableStateFlow(false)
    val isAssistantThinking: StateFlow<Boolean> = _isAssistantThinking.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun selectNotebook(notebook: HealthNotebook?) {
        _selectedNotebook.value = notebook
    }

    fun selectLabTest(labTest: LabTest?) {
        _selectedLabTest.value = labTest
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun createNotebook(
        title: String,
        category: String,
        iconName: String,
        unit: String,
        targetMin: Double?,
        targetMax: Double?,
        description: String,
        colorHex: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val notebook = HealthNotebook(
                title = title.trim(),
                category = category,
                iconName = iconName,
                unit = unit.trim(),
                targetMin = targetMin,
                targetMax = targetMax,
                description = description.trim(),
                colorHex = colorHex
            )
            repository.createNotebook(notebook)
            _snackbarMessage.value = "'$title' defteri başarıyla oluşturuldu."
        }
    }

    fun deleteNotebook(notebook: HealthNotebook) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNotebook(notebook)
            if (_selectedNotebook.value?.id == notebook.id) {
                _selectedNotebook.value = null
            }
            _snackbarMessage.value = "'${notebook.title}' defteri silindi."
        }
    }

    fun addNotebookEntry(
        notebookId: Long,
        numericValue: Double?,
        secondaryValue: Double?,
        severity: Int?,
        notes: String,
        tags: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val entry = NotebookEntry(
                notebookId = notebookId,
                numericValue = numericValue,
                secondaryValue = secondaryValue,
                severity = severity,
                notes = notes.trim(),
                tags = tags.trim(),
                timestamp = System.currentTimeMillis()
            )
            repository.addEntry(entry)
            _snackbarMessage.value = "Yeni kayıt deftere eklendi."
        }
    }

    fun deleteEntry(entry: NotebookEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEntry(entry)
            _snackbarMessage.value = "Kayıt silindi."
        }
    }

    fun analyzeAndSaveLabTest(text: String, bitmap: Bitmap? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAnalyzingLabTest.value = true
            try {
                val savedTest = repository.analyzeAndSaveLabTest(text, bitmap)
                _selectedLabTest.value = savedTest
                _snackbarMessage.value = "Tahlil başarıyla analiz edildi ve kaydedildi."
            } catch (e: Exception) {
                _snackbarMessage.value = "Tahlil analizi sırasında hata oluştu: ${e.message}"
            } finally {
                _isAnalyzingLabTest.value = false
            }
        }
    }

    fun deleteLabTest(labTest: LabTest) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLabTest(labTest)
            if (_selectedLabTest.value?.id == labTest.id) {
                _selectedLabTest.value = null
            }
            _snackbarMessage.value = "'${labTest.title}' tahlili silindi."
        }
    }

    fun sendMessageToAssistant(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            _isAssistantThinking.value = true
            try {
                // Build concise summary context
                val activeNotebooks = notebooks.value.joinToString(", ") { "${it.title} (${it.unit})" }
                val recentTests = labTests.value.take(2).joinToString("; ") { test ->
                    "${test.title} (${test.testDate})"
                }
                val context = "Kullanıcının Defterleri: $activeNotebooks. Son Tahliller: $recentTests."

                repository.sendAssistantMessage(trimmed, context)
            } catch (e: Exception) {
                _snackbarMessage.value = "Asistan yanıt veremedi: ${e.message}"
            } finally {
                _isAssistantThinking.value = false
            }
        }
    }

    fun createNotebookFromSuggestion(suggestion: NotebookTemplateSuggestion) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createNotebookFromSuggestion(suggestion)
            _snackbarMessage.value = "'${suggestion.title}' defteri oluşturuldu!"
        }
    }

    fun clearAssistantChat() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearChat()
            _snackbarMessage.value = "Sohbet geçmişi temizlendi."
        }
    }

    // Helper to parse parameters JSON of a lab test
    fun parseParameters(json: String): List<LabParameter> {
        val list = mutableListOf<LabParameter>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    LabParameter(
                        name = o.optString("name", ""),
                        value = o.optDouble("value", 0.0),
                        unit = o.optString("unit", ""),
                        referenceRange = o.optString("referenceRange", ""),
                        refMin = o.optDouble("refMin", 0.0),
                        refMax = o.optDouble("refMax", 100.0),
                        status = o.optString("status", "NORMAL"),
                        explanation = o.optString("explanation", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    // Helper to parse suggestion JSON
    fun parseSuggestion(json: String?): NotebookTemplateSuggestion? {
        if (json.isNullOrEmpty()) return null
        return try {
            val o = JSONObject(json)
            NotebookTemplateSuggestion(
                title = o.optString("title", ""),
                category = o.optString("category", "Genel"),
                iconName = o.optString("iconName", "activity"),
                unit = o.optString("unit", ""),
                description = o.optString("description", ""),
                colorHex = o.optString("colorHex", "#059669"),
                targetMin = if (o.has("targetMin") && !o.isNull("targetMin")) o.getDouble("targetMin") else null,
                targetMax = if (o.has("targetMax") && !o.isNull("targetMax")) o.getDouble("targetMax") else null
            )
        } catch (_: Exception) {
            null
        }
    }
}
