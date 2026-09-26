package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LabParameter
import com.example.data.model.LabTest
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.ParameterStatusBadge
import com.example.ui.theme.HealthBackground
import com.example.ui.theme.HealthOutline
import com.example.ui.theme.HealthPrimary
import com.example.ui.theme.HealthSecondary
import com.example.ui.theme.HealthStatusAlert
import com.example.ui.theme.HealthStatusNormal
import com.example.ui.theme.HealthStatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabTestsScreen(
    labTests: List<LabTest>,
    isAnalyzing: Boolean,
    onSelectTest: (LabTest) -> Unit,
    onAnalyzeTest: (String, android.graphics.Bitmap?) -> Unit,
    parseParameters: (String) -> List<LabParameter>,
    modifier: Modifier = Modifier
) {
    var showNewAnalysisSheet by remember { mutableStateOf(false) }

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
            // Lab Analysis Hero Banner
            item {
                LabHeroBanner(onStartAnalysis = { showNewAnalysisSheet = true })
            }

            // Mandatory Medical Disclaimer
            item {
                MedicalDisclaimerCard(isCompact = false)
            }

            // Header for Past Lab Tests
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kayıtlı Tahlil Raporlarım",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${labTests.size} Rapor",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isAnalyzing) {
                item {
                    AnalyzingIndicatorCard()
                }
            }

            if (labTests.isEmpty() && !isAnalyzing) {
                item {
                    EmptyLabTestsCard(onStart = { showNewAnalysisSheet = true })
                }
            } else {
                items(labTests, key = { it.id }) { test ->
                    val parameters = remember(test.parametersJson) { parseParameters(test.parametersJson) }
                    LabTestSummaryCard(
                        labTest = test,
                        parameters = parameters,
                        onClick = { onSelectTest(test) }
                    )
                }
            }
        }

        // Floating Action Button
        ExtendedFloatingActionButton(
            onClick = { showNewAnalysisSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("analyze_new_test_fab"),
            containerColor = HealthSecondary,
            contentColor = Color.White,
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Tahlil Analizi") },
            text = { Text("Tahlil Analiz Et", fontWeight = FontWeight.Bold) }
        )
    }

    if (showNewAnalysisSheet) {
        NewLabAnalysisBottomSheet(
            onDismiss = { showNewAnalysisSheet = false },
            onAnalyze = { text, bitmap ->
                onAnalyzeTest(text, bitmap)
                showNewAnalysisSheet = false
            }
        )
    }
}

@Composable
fun LabHeroBanner(onStartAnalysis: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lab_hero_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = 0.25f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Gemini AI Destekli",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tahlil Sonuçlarınızı Anlayın",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Karmaşık tahlil kağıtlarını ve tıbbi terimleri sade bir Türkçe ile sağlık okuryazarlığına dönüştürün.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Biotech,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartAnalysis,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("hero_start_analysis_button"),
                colors = ButtonDefaults.buttonColors(containerColor = HealthSecondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tahlil Fotoğrafı / Metni Ekle", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AnalyzingIndicatorCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF86EFAC))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                color = HealthPrimary,
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Yapay Zeka Tahlili İnceliyor...",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF166534)
                )
                Text(
                    text = "Tıbbi terimler ve parametreler sadeleştiriliyor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF15803D)
                )
            }
        }
    }
}

@Composable
fun LabTestSummaryCard(
    labTest: LabTest,
    parameters: List<LabParameter>,
    onClick: () -> Unit
) {
    val lowCount = parameters.count { it.status == "LOW" }
    val highCount = parameters.count { it.status == "HIGH" }
    val normalCount = parameters.count { it.status == "NORMAL" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("lab_test_card_${labTest.id}"),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(HealthSecondary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = HealthSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = labTest.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${labTest.testDate} • ${labTest.laboratory}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI summary excerpt
            Text(
                text = labTest.aiSummary,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF334155),
                maxLines = 2,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Parameter status badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusCountChip(
                    label = "$normalCount Normal",
                    color = HealthStatusNormal,
                    bgColor = Color(0xFFDCFCE7)
                )
                if (lowCount > 0) {
                    StatusCountChip(
                        label = "$lowCount Düşük",
                        color = Color(0xFF0284C7),
                        bgColor = Color(0xFFE0F2FE)
                    )
                }
                if (highCount > 0) {
                    StatusCountChip(
                        label = "$highCount Yüksek",
                        color = HealthStatusAlert,
                        bgColor = Color(0xFFFEE2E2)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Detaylar →",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = HealthSecondary
                )
            }
        }
    }
}

@Composable
private fun StatusCountChip(
    label: String,
    color: Color,
    bgColor: Color
) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun EmptyLabTestsCard(onStart: () -> Unit) {
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
                    .background(HealthSecondary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Biotech,
                    contentDescription = null,
                    tint = HealthSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Henüz Tahlil Eklenmedi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kan tahlili belgenizin fotoğrafını yükleyebilir veya e-Nabız metnini yapıştırarak yapay zekadan anlaşılır özet alabilirsiniz.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = HealthSecondary)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tahlil Analiz Et")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLabAnalysisBottomSheet(
    onDismiss: () -> Unit,
    onAnalyze: (String, android.graphics.Bitmap?) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var inputMode by remember { mutableStateOf<String>("photo") } // "photo" or "text" or "demo"
    var textInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                selectedBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
            } catch (_: Exception) {}
        }
    }

    val demoPresets = listOf(
        Pair(
            "Tam Kan Sayımı & B12 (Düşük B12 & Ferritin)",
            "B12 Vitamini: 185 pg/mL (Referans: 200 - 900)\nFerritin: 15 ng/mL (Referans: 20 - 200)\nHemoglobin: 13.5 g/dL (Referans: 12.0 - 16.0)\nAçlık Kan Şekeri: 92 mg/dL (Referans: 70 - 100)\nTSH: 2.1 uIU/mL (Referans: 0.4 - 4.2)"
        ),
        Pair(
            "Kapsamlı Biyokimya & Kolesterol & Karaciğer",
            "Total Kolesterol: 218 mg/dL (Referans: < 200)\nLDL Kolesterol: 138 mg/dL (Referans: < 100)\nALT (Karaciğer): 24 U/L (Referans: 7 - 35)\nAST (Karaciğer): 21 U/L (Referans: 10 - 35)\nKreatinin (Böbrek): 0.85 mg/dL (Referans: 0.6 - 1.2)"
        ),
        Pair(
            "Tiroid Paneli (TSH & Serbest T4)",
            "TSH: 4.8 uIU/mL (Referans: 0.4 - 4.2)\nSerbest T4 (FT4): 1.1 ng/dL (Referans: 0.8 - 1.8)\nAnti-TPO: 12 IU/mL (Referans: 0 - 35)"
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("new_lab_sheet"),
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
                        .background(HealthSecondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = HealthSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Yeni Tahlil Analizi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Görsel, e-Nabız metni veya örnek veri seçin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Mode Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabButton(
                    title = "Fotoğraf",
                    icon = Icons.Default.AddPhotoAlternate,
                    isSelected = inputMode == "photo",
                    onClick = { inputMode = "photo" },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "Metin Yapıştır",
                    icon = Icons.Default.ContentPaste,
                    isSelected = inputMode == "text",
                    onClick = { inputMode = "text" },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "Hazır Örnekler",
                    icon = Icons.Default.Description,
                    isSelected = inputMode == "demo",
                    onClick = { inputMode = "demo" },
                    modifier = Modifier.weight(1f)
                )
            }

            when (inputMode) {
                "photo" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, HealthOutline, RoundedCornerShape(14.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (selectedBitmap != null) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = HealthPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tahlil Görseli Seçildi",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = HealthPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            ) {
                                Text("Farklı Görsel Seç")
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = HealthSecondary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tahlil Belgesinin Fotoğrafını Yükleyin",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Yapay zeka tahlil parametrelerini otomatik olarak okuyacak ve sadeleştirecektir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HealthSecondary)
                            ) {
                                Text("Galeriden Seç")
                            }
                        }
                    }
                }

                "text" -> {
                    Column {
                        Text(
                            text = "Tahlil Metni veya e-Nabız Verisi:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Örn: B12: 190 pg/mL, Ferritin: 16 ng/mL, Hemoglobin: 13.8 g/dL, Açlık Şekeri: 95 mg/dL...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("lab_text_input"),
                            maxLines = 6
                        )
                    }
                }

                "demo" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Test etmek için hazır tahlil şablonu seçin:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        demoPresets.forEach { (title, content) ->
                            val isSelected = textInput == content
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { textInput = content },
                                color = if (isSelected) Color(0xFFE0F2FE) else Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) HealthSecondary else HealthOutline
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) HealthSecondary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = content.replace("\n", " • "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val promptText = if (inputMode == "photo" && selectedBitmap != null) {
                        "Lütfen bu tahlil görselindeki parametreleri oku ve sadeleştir."
                    } else {
                        textInput.ifEmpty { "B12: 195 pg/mL, Ferritin: 18 ng/mL, Hemoglobin: 13.8 g/dL, Açlık Şekeri: 92 mg/dL" }
                    }
                    onAnalyze(promptText, selectedBitmap)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_analyze_button"),
                colors = ButtonDefaults.buttonColors(containerColor = HealthSecondary)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yapay Zeka ile Analiz Et", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (isSelected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (isSelected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) HealthSecondary else Color(0xFF64748B),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) HealthSecondary else Color(0xFF64748B)
            )
        }
    }
}
