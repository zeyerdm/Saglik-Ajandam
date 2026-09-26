package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HealthNotebook
import com.example.data.model.NotebookEntry
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.getIconForName
import com.example.ui.components.parseColorSafe
import com.example.ui.theme.HealthBackground
import com.example.ui.theme.HealthOutline
import com.example.ui.theme.HealthPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotebookDetailScreen(
    notebook: HealthNotebook,
    entries: List<NotebookEntry>,
    onBack: () -> Unit,
    onDeleteNotebook: (HealthNotebook) -> Unit,
    onAddEntry: (Long, Double?, Double?, Int?, String, String) -> Unit,
    onDeleteEntry: (NotebookEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var showAddEntrySheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val themeColor = parseColorSafe(notebook.colorHex)

    val numericEntries = remember(entries) {
        entries.filter { it.numericValue != null }.sortedBy { it.timestamp }
    }

    val latestEntry = entries.firstOrNull()
    val averageValue = if (numericEntries.isNotEmpty()) {
        numericEntries.mapNotNull { it.numericValue }.average()
    } else null
    val minValue = numericEntries.mapNotNull { it.numericValue }.minOrNull()
    val maxValue = numericEntries.mapNotNull { it.numericValue }.maxOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthBackground)
    ) {
        // App Bar
        Surface(
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("notebook_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri"
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(themeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForName(notebook.iconName),
                        contentDescription = null,
                        tint = themeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notebook.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${notebook.category} • ${notebook.unit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.testTag("delete_notebook_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Defteri Sil",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(HealthOutline)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ölçüm İstatistikleri",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatBox(
                                label = "Son Ölçüm",
                                value = if (latestEntry?.numericValue != null) {
                                    val v = latestEntry.numericValue
                                    val formatted = if (v % 1.0 == 0.0) v.toInt().toString() else "%.1f".format(v)
                                    if (latestEntry.secondaryValue != null) "$formatted/${latestEntry.secondaryValue.toInt()}" else formatted
                                } else if (latestEntry?.severity != null) {
                                    "${latestEntry.severity}/10"
                                } else "-",
                                unit = notebook.unit,
                                color = themeColor
                            )

                            StatBox(
                                label = "Ortalama",
                                value = averageValue?.let { "%.1f".format(it) } ?: "-",
                                unit = notebook.unit,
                                color = Color(0xFF0284C7)
                            )

                            StatBox(
                                label = "En Düşük",
                                value = minValue?.let { if (it % 1.0 == 0.0) it.toInt().toString() else "%.1f".format(it) } ?: "-",
                                unit = notebook.unit,
                                color = Color(0xFF16A34A)
                            )

                            StatBox(
                                label = "En Yüksek",
                                value = maxValue?.let { if (it % 1.0 == 0.0) it.toInt().toString() else "%.1f".format(it) } ?: "-",
                                unit = notebook.unit,
                                color = Color(0xFFE11D48)
                            )
                        }
                    }
                }
            }

            // Visual Trend Chart (if at least 2 numeric values)
            if (numericEntries.size >= 2) {
                item {
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ShowChart,
                                        contentDescription = null,
                                        tint = themeColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Zaman İçi Değişim Trendi",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${numericEntries.size} Ölçüm",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            TrendSparkline(
                                dataPoints = numericEntries.map { it.numericValue!!.toFloat() },
                                lineColor = themeColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )
                        }
                    }
                }
            }

            // Add Entry Action Button
            item {
                Button(
                    onClick = { showAddEntrySheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("notebook_add_entry_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yeni Kayıt / Ölçüm Ekle", fontWeight = FontWeight.Bold)
                }
            }

            // Entries List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Geçmiş Ölçümler (${entries.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (entries.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HealthOutline)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Henüz kayıt bulunamadı",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Yukarıdaki butonla ilk ölçümünüzü kaydedin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    EntryItemCard(
                        entry = entry,
                        unit = notebook.unit,
                        themeColor = themeColor,
                        onDelete = { onDeleteEntry(entry) }
                    )
                }
            }

            item {
                MedicalDisclaimerCard(isCompact = true)
            }
        }
    }

    if (showAddEntrySheet) {
        AddEntryBottomSheet(
            notebook = notebook,
            onDismiss = { showAddEntrySheet = false },
            onSave = { numVal, secVal, severity, notes, tags ->
                onAddEntry(notebook.id, numVal, secVal, severity, notes, tags)
                showAddEntrySheet = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Defteri Sil?") },
            text = { Text("'${notebook.title}' defterini ve içindeki tüm geçmiş kayıtları silmek istediğinizden emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteNotebook(notebook)
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Evet, Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    unit: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8),
            fontSize = 10.sp
        )
    }
}

@Composable
fun TrendSparkline(
    dataPoints: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (dataPoints.size < 2) return@Canvas

        val minVal = dataPoints.minOrNull() ?: 0f
        val maxVal = dataPoints.maxOrNull() ?: 1f
        val range = if (maxVal - minVal == 0f) 1f else (maxVal - minVal)

        val paddingBottom = 20.dp.toPx()
        val paddingTop = 10.dp.toPx()
        val availableHeight = size.height - paddingTop - paddingBottom
        val stepX = size.width / (dataPoints.size - 1)

        val points = dataPoints.mapIndexed { index, value ->
            val x = index * stepX
            val normalizedY = (value - minVal) / range
            val y = paddingTop + availableHeight * (1f - normalizedY)
            Offset(x, y)
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw circles on points
        points.forEach { pt ->
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = pt
            )
        }
    }
}

@Composable
fun EntryItemCard(
    entry: NotebookEntry,
    unit: String,
    themeColor: Color,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr")).format(Date(entry.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(HealthOutline)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Kaydı Sil",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (entry.numericValue != null) {
                    val formatted = if (entry.numericValue % 1.0 == 0.0) entry.numericValue.toInt().toString() else entry.numericValue.toString()
                    val fullVal = if (entry.secondaryValue != null) "$formatted/${entry.secondaryValue.toInt()} $unit" else "$formatted $unit"
                    Text(
                        text = fullVal,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }

                if (entry.severity != null) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${entry.severity}/10 Şiddet",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            if (entry.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    entry.tags.split(",").forEach { rawTag ->
                        val tag = rawTag.trim()
                        if (tag.isNotEmpty()) {
                            Surface(
                                color = themeColor.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = themeColor,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            if (entry.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = entry.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
