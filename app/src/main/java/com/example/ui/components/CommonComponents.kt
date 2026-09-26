package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HealthOutline
import com.example.ui.theme.HealthStatusAlert
import com.example.ui.theme.HealthStatusNormal
import com.example.ui.theme.HealthStatusWarning

@Composable
fun MedicalDisclaimerCard(
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("medical_disclaimer_card"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEF3C7) // Amber 100 soft
        ),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCD34D))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompact) 10.dp else 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF59E0B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = "Tıbbi Uyarı",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Tıbbi Teşhis Değildir",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Bu uygulama kesinlikle tıbbi teşhis koymaz, tedavi önermez veya ilaç tavsiyesi vermez. Sunulan bilgiler yalnızca sağlık okuryazarlığı amaçlıdır. Her zaman hekiminize danışınız.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF78350F),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ParameterStatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "NORMAL" -> Triple(Color(0xFFDCFCE7), HealthStatusNormal, "Normal")
        "LOW" -> Triple(Color(0xFFE0F2FE), Color(0xFF0284C7), "Düşük")
        "HIGH" -> Triple(Color(0xFFFEE2E2), HealthStatusAlert, "Yüksek")
        else -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

fun getIconForName(name: String): ImageVector {
    return when (name.lowercase()) {
        "heart" -> Icons.Default.Favorite
        "headache" -> Icons.Default.Psychology
        "calendar" -> Icons.Default.DateRange
        "activity" -> Icons.Default.ShowChart
        "droplet" -> Icons.Default.WaterDrop
        "scale" -> Icons.Default.Scale
        "moon" -> Icons.Default.Bedtime
        "fitness" -> Icons.Default.FitnessCenter
        "hospital" -> Icons.Default.LocalHospital
        else -> Icons.Default.MedicalServices
    }
}

fun parseColorSafe(hex: String, fallback: Color = Color(0xFF059669)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}
