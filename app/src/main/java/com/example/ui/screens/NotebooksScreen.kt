package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.HealthNotebook
import com.example.data.model.NotebookEntry
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.getIconForName
import com.example.ui.components.parseColorSafe
import com.example.ui.theme.HealthBackground
import com.example.ui.theme.HealthOutline
import com.example.ui.theme.HealthPrimary
import com.example.ui.theme.HealthSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebooksScreen(
    notebooks: List<HealthNotebook>,
    allEntries: List<NotebookEntry>,
    onSelectNotebook: (HealthNotebook) -> Unit,
    onCreateNotebook: (String, String, String, String, Double?, Double?, String, String) -> Unit,
    onAddEntry: (Long, Double?, Double?, Int?, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("Tümü") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var quickEntryNotebook by remember { mutableStateOf<HealthNotebook?>(null) }

    val categories = listOf("Tümü", "Kronik Takip", "Ağrı & Belirti", "Döngü", "Genel")

    val filteredNotebooks = remember(notebooks, selectedCategory) {
        if (selectedCategory == "Tümü") notebooks
        else notebooks.filter { it.category == selectedCategory }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HealthBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Welcome Banner
            item {
                HealthOverviewBanner(
                    notebookCount = notebooks.size,
                    entryCount = allEntries.size
                )
            }

            // Medical Disclaimer reminder
            item {
                MedicalDisclaimerCard(isCompact = true)
            }

            // Category Filter Chips
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sağlık Defterlerim",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${notebooks.size} Aktif Takip",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = category == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HealthPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_chip_$category")
                            )
                        }
                    }
                }
            }

            // Notebook Cards
            if (filteredNotebooks.isEmpty()) {
                item {
                    EmptyNotebooksCard(onOpenCreate = { showCreateDialog = true })
                }
            } else {
                items(filteredNotebooks, key = { it.id }) { notebook ->
                    val entriesForThis = allEntries.filter { it.notebookId == notebook.id }
                    val latestEntry = entriesForThis.maxByOrNull { it.timestamp }

                    NotebookCard(
                        notebook = notebook,
                        entryCount = entriesForThis.size,
                        latestEntry = latestEntry,
                        onClick = { onSelectNotebook(notebook) },
                        onQuickAdd = { quickEntryNotebook = notebook }
                    )
                }
            }
        }

        // Floating Action Button
        ExtendedFloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("create_notebook_fab"),
            containerColor = HealthPrimary,
            contentColor = Color.White,
            icon = { Icon(Icons.Default.Add, contentDescription = "Yeni Defter Ekle") },
            text = { Text("Yeni Defter", fontWeight = FontWeight.SemiBold) }
        )
    }

    // Create Notebook Dialog
    if (showCreateDialog) {
        CreateNotebookDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, cat, icon, unit, min, max, desc, color ->
                onCreateNotebook(title, cat, icon, unit, min, max, desc, color)
                showCreateDialog = false
            }
        )
    }

    // Quick Add Entry Sheet
    quickEntryNotebook?.let { notebook ->
        AddEntryBottomSheet(
            notebook = notebook,
            onDismiss = { quickEntryNotebook = null },
            onSave = { numVal, secVal, severity, notes, tags ->
                onAddEntry(notebook.id, numVal, secVal, severity, notes, tags)
                quickEntryNotebook = null
            }
        )
    }
}

@Composable
fun HealthOverviewBanner(
    notebookCount: Int,
    entryCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("health_overview_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF059669), Color(0xFF0D9488), Color(0xFF0284C7))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kişisel Sağlık Ajandası",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Metriklerinizi ve tahlillerinizi sadeleştirin",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = "Sağlık",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatPill(
                        title = "Takip Defteri",
                        value = "$notebookCount Adet",
                        modifier = Modifier.weight(1f)
                    )
                    StatPill(
                        title = "Toplam Kayıt",
                        value = "$entryCount Ölçüm",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.18f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun NotebookCard(
    notebook: HealthNotebook,
    entryCount: Int,
    latestEntry: NotebookEntry?,
    onClick: () -> Unit,
    onQuickAdd: () -> Unit
) {
    val themeColor = parseColorSafe(notebook.colorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("notebook_card_${notebook.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(HealthOutline)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForName(notebook.iconName),
                        contentDescription = notebook.title,
                        tint = themeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = notebook.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${notebook.category} • ${notebook.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onQuickAdd,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(themeColor.copy(alpha = 0.1f))
                        .size(36.dp)
                        .testTag("quick_add_entry_${notebook.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Hızlı Kayıt Ekle",
                        tint = themeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (!notebook.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = notebook.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer info: Last value and total entries
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (latestEntry != null) {
                    val formattedVal = buildString {
                        if (latestEntry.numericValue != null) {
                            append(if (latestEntry.numericValue % 1.0 == 0.0) latestEntry.numericValue.toInt().toString() else latestEntry.numericValue.toString())
                            if (latestEntry.secondaryValue != null) {
                                append("/${latestEntry.secondaryValue.toInt()}")
                            }
                            append(" ${notebook.unit}")
                        } else if (latestEntry.severity != null) {
                            append("${latestEntry.severity}/10 Şiddet")
                        } else {
                            append("Kayıt mevcut")
                        }
                    }

                    val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale("tr")).format(Date(latestEntry.timestamp))

                    Column {
                        Text(
                            text = "Son Değer: $formattedVal",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                    }
                } else {
                    Text(
                        text = "Henüz kayıt girilmedi",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                Text(
                    text = "$entryCount Kayıt →",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EmptyNotebooksCard(onOpenCreate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(HealthOutline)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(HealthPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NoteAlt,
                    contentDescription = null,
                    tint = HealthPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Henüz Takip Defteri Yok",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tansiyon, migren, regl veya kan şekeri gibi takip etmek istediğiniz metrikler için bir defter oluşturun.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOpenCreate,
                colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("İlk Defteri Oluştur")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNotebookDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, Double?, Double?, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Genel") }
    var iconName by remember { mutableStateOf("heart") }
    var unit by remember { mutableStateOf("") }
    var minVal by remember { mutableStateOf("") }
    var maxVal by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#059669") }

    val quickTemplates = listOf(
        TemplatePreset("Tansiyon & Nabız", "Kronik Takip", "heart", "mmHg", "90", "120", "Büyük ve küçük tansiyon ölçümleri", "#E11D48"),
        TemplatePreset("Migren & Ağrı", "Ağrı & Belirti", "headache", "1-10 Şiddet", null, null, "Baş ağrısı atak ve şiddet takibi", "#7C3AED"),
        TemplatePreset("Regl & Döngü", "Döngü", "calendar", "Gün", null, null, "Döngü ve PMS belirtileri", "#EC4899"),
        TemplatePreset("Kan Şekeri", "Kronik Takip", "activity", "mg/dL", "70", "100", "Açlık ve tokluk şekeri", "#0284C7"),
        TemplatePreset("Kilo Takibi", "Genel", "scale", "kg", null, null, "Haftalık kilo tartımları", "#059669"),
        TemplatePreset("Su Tüketimi", "Genel", "droplet", "Bardak", "8", "12", "Günlük içilen su", "#0D9488"),
        TemplatePreset("Uyku Süresi", "Genel", "moon", "Saat", "7", "9", "Uyku kalitesi ve süresi", "#6366F1")
    )

    val colorOptions = listOf("#059669", "#0284C7", "#E11D48", "#7C3AED", "#EC4899", "#D97706", "#0D9488")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("create_notebook_dialog")
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Yeni Sağlık Defteri",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Hazır şablon seçin veya özel takip oluşturun",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick Templates
                item {
                    Text(
                        text = "Hızlı Şablonlar",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickTemplates) { t ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.clickable {
                                    title = t.title
                                    category = t.category
                                    iconName = t.iconName
                                    unit = t.unit
                                    minVal = t.min ?: ""
                                    maxVal = t.max ?: ""
                                    description = t.desc
                                    selectedColor = t.color
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getIconForName(t.iconName),
                                        contentDescription = null,
                                        tint = parseColorSafe(t.color),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = t.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Defter Adı (Örn: Tansiyon, Migren)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notebook_title_input"),
                        singleLine = true
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Kategori") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Birim (Örn: mmHg, kg)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = minVal,
                            onValueChange = { minVal = it },
                            label = { Text("Hedef Alt Sınır (İsteğe bağlı)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = maxVal,
                            onValueChange = { maxVal = it },
                            label = { Text("Hedef Üst Sınır (İsteğe bağlı)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Açıklama veya Takip Amacı") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }

                // Color picker
                item {
                    Text(
                        text = "Defter Rengi",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        colorOptions.forEach { hex ->
                            val isSelected = selectedColor == hex
                            val color = parseColorSafe(hex)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) Color(0xFF0F172A) else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("İptal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    val min = minVal.toDoubleOrNull()
                                    val max = maxVal.toDoubleOrNull()
                                    onCreate(title, category, iconName, unit, min, max, description, selectedColor)
                                }
                            },
                            enabled = title.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary),
                            modifier = Modifier.testTag("save_notebook_button")
                        ) {
                            Text("Oluştur")
                        }
                    }
                }
            }
        }
    }
}

private data class TemplatePreset(
    val title: String,
    val category: String,
    val iconName: String,
    val unit: String,
    val min: String?,
    val max: String?,
    val desc: String,
    val color: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryBottomSheet(
    notebook: HealthNotebook,
    onDismiss: () -> Unit,
    onSave: (Double?, Double?, Int?, String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var numValStr by remember { mutableStateOf("") }
    var secValStr by remember { mutableStateOf("") }
    var severity by remember { mutableFloatStateOf(0f) }
    var notes by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }

    val commonTags = when (notebook.category) {
        "Ağrı & Belirti" -> listOf("Stres", "Işık Hassasiyeti", "Uykusuzluk", "Yorgunluk", "Açlık")
        "Döngü" -> listOf("Kramp", "Şişkinlik", "Hassasiyet", "Ruh Hali", "Baş Ağrısı")
        "Kronik Takip" -> listOf("Aç Karnına", "Tok Karnına", "İlaç Öncesi", "İlaç Sonrası", "Dinlenme")
        else -> listOf("Sabah", "Öğle", "Akşam", "Egzersiz Sonrası", "Rutin")
    }

    val themeColor = parseColorSafe(notebook.colorHex)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("add_entry_bottom_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
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
                Column {
                    Text(
                        text = "${notebook.title} - Yeni Kayıt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ölçüm Birimi: ${notebook.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Numeric input (and secondary if Tansiyon)
            val isBloodPressure = notebook.title.contains("Tansiyon", ignoreCase = true) || notebook.unit.contains("mmHg", ignoreCase = true)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = numValStr,
                    onValueChange = { numValStr = it },
                    label = { Text(if (isBloodPressure) "Büyük (Sistolik)" else "Değer (${notebook.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("entry_numeric_input"),
                    singleLine = true
                )
                if (isBloodPressure) {
                    OutlinedTextField(
                        value = secValStr,
                        onValueChange = { secValStr = it },
                        label = { Text("Küçük (Diyastolik)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("entry_secondary_input"),
                        singleLine = true
                    )
                }
            }

            // Severity Slider (1-10)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Belirti / Ağrı Şiddeti:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (severity.toInt() == 0) "Belirtilmedi" else "${severity.toInt()}/10",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }
                Slider(
                    value = severity,
                    onValueChange = { severity = it },
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Quick Tag Chips
            Column {
                Text(
                    text = "Etiketler & Durum:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(commonTags) { tag ->
                        val isSelected = selectedTags.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTags = if (isSelected) selectedTags - tag else selectedTags + tag
                            },
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notlar ve Belirtiler (İsteğe bağlı)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_notes_input"),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    val num = numValStr.toDoubleOrNull()
                    val sec = secValStr.toDoubleOrNull()
                    val sev = if (severity > 0) severity.toInt() else null
                    val tagsStr = selectedTags.joinToString(", ")
                    onSave(num, sec, sev, notes, tagsStr)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_entry_button"),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor)
            ) {
                Text("Kaydı Ajandaya Ekle", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
