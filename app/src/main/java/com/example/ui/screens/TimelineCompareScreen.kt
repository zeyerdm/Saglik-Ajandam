package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HealthNotebook
import com.example.data.model.LabParameter
import com.example.data.model.LabTest
import com.example.data.model.NotebookEntry
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.ParameterStatusBadge
import com.example.ui.components.getIconForName
import com.example.ui.components.parseColorSafe
import com.example.ui.theme.HealthBackground
import com.example.ui.theme.HealthOutline
import com.example.ui.theme.HealthPrimary
import com.example.ui.theme.HealthSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimelineCompareScreen(
    notebooks: List<HealthNotebook>,
    entries: List<NotebookEntry>,
    labTests: List<LabTest>,
    parseParameters: (String) -> List<LabParameter>,
    onSelectTest: (LabTest) -> Unit,
    onSelectNotebook: (HealthNotebook) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Karşılaştırma & Trendler, 1: Kronolojik Çizelge

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthBackground)
    ) {
        Surface(
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Zaman Çizelgesi & Karşılaştırma",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tahliller arasındaki artış/azalış trendleri ve sağlık günlüğü",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = HealthPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tahlil Karşılaştırma", fontWeight = FontWeight.SemiBold)
                            }
                        },
                        modifier = Modifier.testTag("tab_compare")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Zaman Çizelgesi", fontWeight = FontWeight.SemiBold)
                            }
                        },
                        modifier = Modifier.testTag("tab_timeline")
                    )
                }
            }
        }

        if (selectedTab == 0) {
            CompareTabContent(
                labTests = labTests,
                parseParameters = parseParameters
            )
        } else {
            TimelineTabContent(
                notebooks = notebooks,
                entries = entries,
                labTests = labTests,
                onSelectTest = onSelectTest,
                onSelectNotebook = onSelectNotebook
            )
        }
    }
}

@Composable
fun CompareTabContent(
    labTests: List<LabTest>,
    parseParameters: (String) -> List<LabParameter>
) {
    val sortedTests = remember(labTests) { labTests.sortedBy { it.timestamp } }

    // Collect all unique parameter names across tests
    val testParamsMap = remember(sortedTests) {
        sortedTests.associateWith { parseParameters(it.parametersJson) }
    }

    val allParamNames = remember(testParamsMap) {
        testParamsMap.values.flatten().map { it.name }.distinct()
    }

    var selectedParamName by remember(allParamNames) {
        mutableStateOf(allParamNames.firstOrNull { it.contains("B12") || it.contains("Ferritin") } ?: allParamNames.firstOrNull() ?: "")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MedicalDisclaimerCard(isCompact = true)
        }

        if (sortedTests.size < 2) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(HealthOutline)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = HealthSecondary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Karşılaştırma İçin En Az 2 Tahlil Gerekir",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Farklı tarihlerdeki tahlillerinizi sisteme yüklediğinizde, değerlerdeki artış ve azalışlar burada otomatik olarak kıyaslanır.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Parameter Selection Chips
            item {
                Column {
                    Text(
                        text = "Karşılaştırılacak Tahlil Parametresi:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allParamNames) { name ->
                            val isSelected = name == selectedParamName
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedParamName = name },
                                label = { Text(name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HealthSecondary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Comparison Results Card
            item {
                val matches = sortedTests.mapNotNull { test ->
                    val param = testParamsMap[test]?.find { it.name.equals(selectedParamName, ignoreCase = true) }
                    if (param != null) Pair(test, param) else null
                }

                if (matches.size >= 2) {
                    val first = matches.first()
                    val latest = matches.last()
                    val diff = latest.second.value - first.second.value
                    val percentChange = if (first.second.value > 0) (diff / first.second.value) * 100 else 0.0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(HealthOutline)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = selectedParamName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Referans Aralığı: ${latest.second.referenceRange} ${latest.second.unit}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Trend badge
                                val isPositive = diff > 0
                                Surface(
                                    color = if (isPositive) Color(0xFFDCFCE7) else Color(0xFFE0F2FE),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = if (isPositive) Color(0xFF16A34A) else Color(0xFF0284C7),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${if (diff > 0) "+" else ""}${String.format(Locale.US, "%.1f", percentChange)}%",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPositive) Color(0xFF16A34A) else Color(0xFF0284C7)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Comparison Side by Side
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Previous Test
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Önceki Tahlil",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = first.first.testDate,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${first.second.value} ${first.second.unit}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ParameterStatusBadge(status = first.second.status)
                                }

                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                // Latest Test
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF0FDF4))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Güncel Tahlil",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF166534)
                                    )
                                    Text(
                                        text = latest.first.testDate,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${latest.second.value} ${latest.second.unit}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF15803D)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ParameterStatusBadge(status = latest.second.status)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Health literacy note on this change
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "💡 Sağlık Notu: ${latest.second.explanation} Önceki tahlile göre fark: ${if (diff > 0) "+" else ""}${String.format(Locale.US, "%.1f", diff)} ${latest.second.unit}.",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF334155),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "'$selectedParamName' parametresi birden fazla tahlilde ortak bulunamadı.",
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

private sealed class TimelineItem(val timestamp: Long) {
    class EntryItem(val entry: NotebookEntry, val notebook: HealthNotebook?) : TimelineItem(entry.timestamp)
    class TestItem(val labTest: LabTest) : TimelineItem(labTest.timestamp)
}

@Composable
fun TimelineTabContent(
    notebooks: List<HealthNotebook>,
    entries: List<NotebookEntry>,
    labTests: List<LabTest>,
    onSelectTest: (LabTest) -> Unit,
    onSelectNotebook: (HealthNotebook) -> Unit
) {
    val notebookMap = remember(notebooks) { notebooks.associateBy { it.id } }

    val mergedItems = remember(entries, labTests, notebookMap) {
        val list = mutableListOf<TimelineItem>()
        entries.forEach { e -> list.add(TimelineItem.EntryItem(e, notebookMap[e.notebookId])) }
        labTests.forEach { t -> list.add(TimelineItem.TestItem(t)) }
        list.sortedByDescending { it.timestamp }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (mergedItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Henüz Zaman Çizelgesi Kaydı Yok",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Defter kayıtlarınız ve tahlilleriniz burada birleşik bir sağlık günlüğü olarak listelenir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(mergedItems) { item ->
                when (item) {
                    is TimelineItem.TestItem -> {
                        val test = item.labTest
                        val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("tr")).format(Date(test.timestamp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTest(test) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(HealthSecondary.copy(alpha = 0.4f))
                            )
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(HealthSecondary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Science,
                                        contentDescription = null,
                                        tint = HealthSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = test.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tahlil Raporu • $dateStr",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Text(
                                    text = "İncele →",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthSecondary
                                )
                            }
                        }
                    }

                    is TimelineItem.EntryItem -> {
                        val entry = item.entry
                        val notebook = item.notebook
                        val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale("tr")).format(Date(entry.timestamp))
                        val themeColor = notebook?.let { parseColorSafe(it.colorHex) } ?: HealthPrimary

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { notebook?.let { onSelectNotebook(it) } },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(HealthOutline)
                            )
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(themeColor.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getIconForName(notebook?.iconName ?: "activity"),
                                        contentDescription = null,
                                        tint = themeColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = notebook?.title ?: "Sağlık Kaydı",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val valText = if (entry.numericValue != null) {
                                        val f = if (entry.numericValue % 1.0 == 0.0) entry.numericValue.toInt().toString() else entry.numericValue.toString()
                                        "$f ${notebook?.unit ?: ""}"
                                    } else if (entry.severity != null) {
                                        "${entry.severity}/10 Şiddet"
                                    } else entry.notes.take(30)
                                    Text(
                                        text = "$valText • $dateStr",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
