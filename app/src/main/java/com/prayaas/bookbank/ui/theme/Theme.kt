package com.prayaas.bookbank.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Brand Colors ─────────────────────────────────────────────────────────────
object PrayaasColors {
    val Orange = Color(0xFFE8621A)
    val OrangeDark = Color(0xFFB84D10)
    val OrangeLight = Color(0xFFFFF0E8)
    val OrangeSurface = Color(0xFFFFF8F4)

    val Black = Color(0xFF1A1A1A)
    val Gray = Color(0xFF6B6B6B)
    val LightGray = Color(0xFFF8F5F2)
    val Border = Color(0x1A000000)

    val Success = Color(0xFF2E7D32)
    val SuccessBg = Color(0xFFE8F5E9)
    val Warning = Color(0xFFF57F17)
    val WarningBg = Color(0xFFFFF8E1)
    val Info = Color(0xFF1565C0)
    val InfoBg = Color(0xFFE3F2FD)
    val Error = Color(0xFFC62828)
    val ErrorBg = Color(0xFFFFEBEE)

    // Book cover palette
    val Cover1 = Color(0xFF3949AB)
    val Cover2 = Color(0xFF00897B)
    val Cover3 = Color(0xFFC62828)
    val Cover4 = Color(0xFF6A1B9A)
    val Cover5 = Color(0xFFE65100)
    val Cover6 = Color(0xFF1565C0)
    val Cover7 = Color(0xFF2E7D32)
    val Cover8 = Color(0xFF37474F)
}

private val LightColorScheme = lightColorScheme(
    primary = PrayaasColors.Orange,
    onPrimary = Color.White,
    primaryContainer = PrayaasColors.OrangeLight,
    onPrimaryContainer = PrayaasColors.OrangeDark,
    secondary = PrayaasColors.Black,
    onSecondary = Color.White,
    background = PrayaasColors.LightGray,
    onBackground = PrayaasColors.Black,
    surface = Color.White,
    onSurface = PrayaasColors.Black,
    surfaceVariant = PrayaasColors.OrangeSurface,
    error = PrayaasColors.Error,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PrayaasColors.Orange,
    onPrimary = Color.White,
    primaryContainer = PrayaasColors.OrangeDark,
    onPrimaryContainer = PrayaasColors.OrangeLight,
    secondary = Color(0xFFE0E0E0),
    onSecondary = PrayaasColors.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A2A2A),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF1A0000)
)

@Composable
fun PrayaasBookBankTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PrayaasTypography,
        content = content
    )
}

val PrayaasTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)

// Shape tokens
val CardShape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
val ButtonShape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
val ChipShape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
val SmallCardShape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
