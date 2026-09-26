package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.LabTestDetailScreen
import com.example.ui.screens.LabTestsScreen
import com.example.ui.screens.NotebookDetailScreen
import com.example.ui.screens.NotebooksScreen
import com.example.ui.screens.TimelineCompareScreen
import com.example.ui.theme.HealthOutline
import com.example.ui.theme.HealthPrimary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    var selectedNavTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    val notebooks by viewModel.notebooks.collectAsStateWithLifecycle()
    val labTests by viewModel.labTests.collectAsStateWithLifecycle()
    val allEntries by viewModel.allEntries.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()

    val selectedNotebook by viewModel.selectedNotebook.collectAsStateWithLifecycle()
    val entriesForNotebook by viewModel.entriesForSelectedNotebook.collectAsStateWithLifecycle()
    val selectedLabTest by viewModel.selectedLabTest.collectAsStateWithLifecycle()

    val isAnalyzingLabTest by viewModel.isAnalyzingLabTest.collectAsStateWithLifecycle()
    val isAssistantThinking by viewModel.isAssistantThinking.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val isSubScreenOpen = selectedNotebook != null || selectedLabTest != null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isSubScreenOpen) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 4.dp
                ) {
                    val navItems = listOf(
                        Triple(0, "Defterler", Icons.Default.MenuBook),
                        Triple(1, "Tahlil Analizi", Icons.Default.Biotech),
                        Triple(2, "Karşılaştırma", Icons.Default.Timeline),
                        Triple(3, "AI Asistan", Icons.Default.AutoAwesome)
                    )

                    navItems.forEach { (index, title, icon) ->
                        val isSelected = selectedNavTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedNavTab = index },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = HealthPrimary,
                                selectedTextColor = HealthPrimary,
                                indicatorColor = HealthPrimary.copy(alpha = 0.12f),
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            ),
                            modifier = Modifier.testTag("nav_item_$index")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                selectedNotebook != null -> {
                    NotebookDetailScreen(
                        notebook = selectedNotebook!!,
                        entries = entriesForNotebook,
                        onBack = { viewModel.selectNotebook(null) },
                        onDeleteNotebook = { nb -> viewModel.deleteNotebook(nb) },
                        onAddEntry = { id, num, sec, sev, notes, tags ->
                            viewModel.addNotebookEntry(id, num, sec, sev, notes, tags)
                        },
                        onDeleteEntry = { entry -> viewModel.deleteEntry(entry) }
                    )
                }

                selectedLabTest != null -> {
                    val params = remember(selectedLabTest!!.parametersJson) {
                        viewModel.parseParameters(selectedLabTest!!.parametersJson)
                    }
                    LabTestDetailScreen(
                        labTest = selectedLabTest!!,
                        parameters = params,
                        onBack = { viewModel.selectLabTest(null) },
                        onDeleteTest = { test -> viewModel.deleteLabTest(test) }
                    )
                }

                else -> {
                    AnimatedContent(
                        targetState = selectedNavTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "NavTransition"
                    ) { targetTab ->
                        when (targetTab) {
                            0 -> NotebooksScreen(
                                notebooks = notebooks,
                                allEntries = allEntries,
                                onSelectNotebook = { nb -> viewModel.selectNotebook(nb) },
                                onCreateNotebook = { title, cat, icon, unit, min, max, desc, color ->
                                    viewModel.createNotebook(title, cat, icon, unit, min, max, desc, color)
                                },
                                onAddEntry = { id, num, sec, sev, notes, tags ->
                                    viewModel.addNotebookEntry(id, num, sec, sev, notes, tags)
                                }
                            )

                            1 -> LabTestsScreen(
                                labTests = labTests,
                                isAnalyzing = isAnalyzingLabTest,
                                onSelectTest = { test -> viewModel.selectLabTest(test) },
                                onAnalyzeTest = { text, bitmap ->
                                    viewModel.analyzeAndSaveLabTest(text, bitmap)
                                },
                                parseParameters = { json -> viewModel.parseParameters(json) }
                            )

                            2 -> TimelineCompareScreen(
                                notebooks = notebooks,
                                entries = allEntries,
                                labTests = labTests,
                                parseParameters = { json -> viewModel.parseParameters(json) },
                                onSelectTest = { test -> viewModel.selectLabTest(test) },
                                onSelectNotebook = { nb -> viewModel.selectNotebook(nb) }
                            )

                            3 -> AssistantScreen(
                                messages = chatMessages,
                                isThinking = isAssistantThinking,
                                onSendMessage = { text -> viewModel.sendMessageToAssistant(text) },
                                onCreateFromSuggestion = { suggestion ->
                                    viewModel.createNotebookFromSuggestion(suggestion)
                                },
                                onClearChat = { viewModel.clearAssistantChat() },
                                parseSuggestion = { json -> viewModel.parseSuggestion(json) }
                            )
                        }
                    }
                }
            }
        }
    }
}
