package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme =
  lightColorScheme(
    primary = HealthPrimary,
    onPrimary = Color.White,
    primaryContainer = HealthPrimaryContainer,
    onPrimaryContainer = HealthOnPrimaryContainer,
    secondary = HealthSecondary,
    onSecondary = Color.White,
    secondaryContainer = HealthSecondaryContainer,
    onSecondaryContainer = HealthOnSecondaryContainer,
    tertiary = HealthTertiary,
    onTertiary = Color.White,
    tertiaryContainer = HealthTertiaryContainer,
    background = HealthBackground,
    surface = HealthSurface,
    surfaceVariant = HealthSurfaceVariant,
    onBackground = HealthOnSurface,
    onSurface = HealthOnSurface,
    onSurfaceVariant = HealthOnSurfaceVariant,
    outline = HealthOutline
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = HealthPrimaryLight,
    onPrimary = HealthOnPrimaryContainer,
    primaryContainer = HealthPrimary,
    onPrimaryContainer = HealthPrimaryContainer,
    secondary = HealthSecondary,
    onSecondary = Color.White,
    secondaryContainer = HealthSecondaryContainer,
    onSecondaryContainer = HealthOnSecondaryContainer,
    tertiary = HealthTertiary,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
